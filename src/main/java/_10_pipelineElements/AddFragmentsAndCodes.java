
package _10_pipelineElements;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.simmetrics.StringMetric;
import org.simmetrics.StringMetricBuilder;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.simplifiers.CaseSimplifier;
import org.simmetrics.simplifiers.NonDiacriticSimplifier;
import org.simmetrics.tokenizers.QGramTokenizer;
import org.simmetrics.tokenizers.WhitespaceTokenizer;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import bioc.type.UimaBioCAnnotation;
import bioc.type.UimaBioCDocument;
import bioc.type.UimaBioCLocation;
import bioc.type.UimaBioCPassage;
import edu.isi.bmkeg.digitalLibrary.controller.DigitalLibraryEngine;
import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;
import edu.isi.bmkeg.uimaBioC.UimaBioCUtils;

public class AddFragmentsAndCodes extends JCasAnnotator_ImplBase {
	
	public static final String LOGIN = ConfigurationParameterFactory
			.createConfigurationParameterName(AddFragmentsAndCodes.class,
					"login");
	@ConfigurationParameter(mandatory = true, description = "Login for the Digital Library")
	protected String login;

	public static final String PASSWORD = ConfigurationParameterFactory
			.createConfigurationParameterName(AddFragmentsAndCodes.class,
					"password");
	@ConfigurationParameter(mandatory = true, description = "Password for the Digital Library")
	protected String password;

	public static final String WORKING_DIRECTORY = ConfigurationParameterFactory
			.createConfigurationParameterName(AddFragmentsAndCodes.class,
					"workingDirectory");
	@ConfigurationParameter(mandatory = true, description = "Working Directory for the Digital Library")
	protected String workingDirectory;
	
	public static final String DB_URL = ConfigurationParameterFactory
			.createConfigurationParameterName(AddFragmentsAndCodes.class,
					"dbUrl");
	@ConfigurationParameter(mandatory = true, description = "The Digital Library URL")
	protected String dbUrl;
	
	public static final String FRAGMENT_TYPE = ConfigurationParameterFactory
			.createConfigurationParameterName(AddFragmentsAndCodes.class,
					"fragmentType");
	@ConfigurationParameter(mandatory = true, description = "Fragment Type")
	protected String fragmentType;

	private DigitalLibraryEngine de;
	private String countSql;
	private String selectSql;
	private String fromWhereSql;
	private String orderBySql;
	
	private StringMetric cosineSimilarityMetric;
	private StringMetric levenshteinSimilarityMetric;
	
	private CleartkExtractor<DocumentAnnotation, Token> extractor;
	
	private static Logger logger = Logger
			.getLogger(AddFragmentsAndCodes.class);
	
	private Pattern unicodeStripper;
	private Map<String, String> frgTextHash;
	private Map<String, List<UimaBioCAnnotation>> frgBlockListHash;
	private List<UimaBioCAnnotation> newBlkAnnotations;
	private List<UimaBioCAnnotation> newFrgAnnotations;
	
	public void initialize(UimaContext context)
			throws ResourceInitializationException {

		super.initialize(context);
		
		try {
			
			de = new DigitalLibraryEngine();
			de.initializeVpdmfDao(
					login, 
					password, 
					dbUrl, 
					workingDirectory);
			
			unicodeStripper = Pattern.compile("(alpha|beta|gamma|delta)");
			
		} catch (Exception e) {

			throw new ResourceInitializationException(e);
		
		}	
		
		// Query based on a query constructed with SqlQueryBuilder based on the TriagedArticle view.
		countSql = "SELECT COUNT(*) ";

		selectSql = "SELECT l.vpdmfId, a.pmid, f.name, frg.vpdmfId, frg.frgOrder, blk.vpdmfOrder, blk.text, blk.code ";
		
		fromWhereSql = "FROM LiteratureCitation AS l," +
				" ArticleCitation as a, FTD as f, " + 
				" FTDFragment as frg, FTDFragmentBlock as blk " +
				" WHERE " +
				"blk.fragment_id = frg.vpdmfId AND " +
				"l.fullText_id = f.vpdmfId AND " +
				"l.vpdmfId = a.vpdmfId AND " +
				"frg.ftd_id = f.vpdmfId AND " +
				"frg.frgType = '"+ fragmentType + "' ";
		
		orderBySql = " ORDER BY l.vpdmfId, frg.vpdmfId, frg.frgOrder, blk.vpdmfOrder;";
		
		cosineSimilarityMetric = new StringMetricBuilder()
			.with(new CosineSimilarity<String>())
			.simplify(new CaseSimplifier.Lower())
			.simplify(new NonDiacriticSimplifier())
			.tokenize(new WhitespaceTokenizer())
			.tokenize(new QGramTokenizer(2))
			.build();

		levenshteinSimilarityMetric = new StringMetricBuilder()
			.with(new Levenshtein())
			.simplify(new NonDiacriticSimplifier())
			.build();
		
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		
		try {
			
			String txt = jCas.getDocumentText();
			List<Sentence> sentences = new ArrayList<Sentence>();
			for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
				sentences.add(sentence);
			}
			
			readFragmentsFromRs( jCas );
			
			List<String> codes = new ArrayList<String>(frgTextHash.keySet());
			Collections.sort(codes);
			
			newBlkAnnotations = new ArrayList<UimaBioCAnnotation>();
			newFrgAnnotations = new ArrayList<UimaBioCAnnotation>();
						
			FRAGMENT_LOOP: for( String code: codes ) {
									
				UimaBioCAnnotation frg = locateFragmentInDoc(jCas, code);
				if( frg == null )
					continue;
				
				locateBlocksInDocForFragment(jCas, frg); 
								
			}
			
			UimaBioCPassage uiP = UimaBioCUtils.readDocument(jCas);
			FSArray annotations = uiP.getAnnotations();
			int totalAnnotationCount = annotations.size() 
					+ newBlkAnnotations.size()
					+ newFrgAnnotations.size();
			
			FSArray withAddedAnnotations = new FSArray(jCas, totalAnnotationCount);
			for(int i=0; i<annotations.size(); i++) {
				withAddedAnnotations.set(i, annotations.get(i));
			}
			int jj = annotations.size();
			for( UimaBioCAnnotation blk : newBlkAnnotations ) {
				withAddedAnnotations.set(jj, blk);			
				jj++;
			}
			for( UimaBioCAnnotation frg : newFrgAnnotations ) {
				withAddedAnnotations.set(jj, frg);			
				jj++;
			}
							
			uiP.setAnnotations(withAddedAnnotations);			

		} catch (Exception e) {
		
			throw new AnalysisEngineProcessException(e);
		
		} 
		

	}
	
	private void readFragmentsFromRs( JCas jCas ) throws Exception {

		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		
		String pmidWhereSql = " AND a.pmid = '" + uiD.getId() + "' ";

		de.getDigLibDao().getCoreDao().getCe().connectToDB();
		
		ResultSet countRs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
				countSql + fromWhereSql + pmidWhereSql + orderBySql);
		
		countRs.next();
		int count = countRs.getInt(1);
		countRs.close();
		
		ResultSet rs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
				selectSql + fromWhereSql + pmidWhereSql + orderBySql);
		
		String frgText = "";
		int j = 0, pos = 0;
		List<UimaBioCAnnotation> frgBlockList = null;
		
		frgTextHash = new HashMap<String,String>();
		frgBlockListHash = new HashMap<String,List<UimaBioCAnnotation>>();
		
		while( rs.next() ) {

			String frgOrder = rs.getString("frg.frgOrder");
			String blkId = rs.getString("blk.vpdmfOrder");
			
			String blkCode = rs.getString("blk.code");
			if( blkCode == null || blkCode.equals("-") ) 
				continue;
			
			String blkText = rs.getString("blk.text");
			blkText = blkText.replaceAll("\\s+", " ");
			blkText = blkText.replaceAll("\\-\\s+", "");
							
			// Parse the frgOrder code.
			// First split any '+' codes
			// then enumerate numbers for figures and ignore Supplemental data. 
			if(  !frgTextHash.containsKey(frgOrder) ) {
				frgText = "";
				frgBlockList = new ArrayList<UimaBioCAnnotation>();
			} else {
				frgText = frgTextHash.get(frgOrder);
				frgBlockList = frgBlockListHash.get(frgOrder);
			}
						
			if( frgText.length() != 0 )
				frgText += " ";
			frgText += blkText;
						
			int start = frgText.indexOf(blkText);
			int end = start + blkText.length() - 1;
						
			frgTextHash.put( frgOrder , frgText);

			UimaBioCAnnotation frgBlock = new UimaBioCAnnotation(jCas);
			frgBlock.setBegin(start);
			frgBlock.setEnd(end);
			Map<String,String> infons2 = new HashMap<String, String>();
			infons2.put("type", "epistSeg");
			infons2.put("value", blkCode);
			frgBlock.setInfons(BioCUtils.convertInfons(infons2, jCas));
			frgBlock.setText(blkText);
			frgBlockList.add(frgBlock);
			
			frgBlockListHash.put( frgOrder, frgBlockList);
			
			pos += pos + blkText.length();
							
		}
		
		rs.close();
		de.getDigLibDao().getCoreDao().getCe().closeDbConnection();
		
	}
	
	private UimaBioCAnnotation locateFragmentInDoc(JCas jCas, String code ) {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);

		List<Sentence> sentences = new ArrayList<Sentence>();
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			sentences.add(sentence);
		}
		
		String txt = jCas.getDocumentText();
		String frgText = frgTextHash.get(code);
		
		float best = 0;
		int sCount = 0;
		int bestCount = 0;
		int winL = 80;
		int l = frgText.length();
		Sentence bestS = null;	
		for (Sentence sentence : sentences) {
			
			int s = sentence.getBegin();
			int e = (l>winL)?(s+winL):(s+l);
			if( e > txt.length() )
				e = txt.length();
			String sText = txt.substring(s, e);
			
			//
			// Minor hack to remove unicode characters from the comparison. 
			//
			Matcher m = unicodeStripper.matcher(sText);
			if( m.find() ) {
				String newText = sText.replaceAll(unicodeStripper.pattern(), " ");
				int extraText = sText.length() - newText.length();
				sText = txt.substring(s,e + extraText);
				sText = sText.replaceAll(unicodeStripper.pattern(), " ");
			}
			
			final float result = levenshteinSimilarityMetric.compare(
					frgText.substring(0,(l>winL?winL:l)), 
					sText); 
			
			if( result > best ) {
				best = result;
				bestS = sentence;
				bestCount = sCount;
			}
			sCount++;
		}
		
		//
		// If we can't find a good match, skip this whole fragment. 
		//
		if( best < 0.80 ) {
			int w = (l>winL)?(winL):(l);
			logger.error("ERROR(" + uiD.getId() + "_" + code + 
					"), score:" + best + 
					",\n   guess: " + txt.substring(bestS.getBegin(), bestS.getBegin()+w) 
					+ "\n   frg: " + 
					frgText.substring(0, w) + "\n");
			
			return null;
		}
		
		int nextCount = 0;
		Sentence thisS = sentences.get(bestCount + nextCount);
		
		logger.debug("(" + uiD.getId() + "_" + code + 
				"), score:" + best + 
				",\n   guess: " + bestS.getCoveredText()
				+ "\n   frg: " + 
				frgText.substring(0, 50) + "\n");
		
		int delta1 = frgText.length() - (thisS.getEnd() - bestS.getBegin());
		Sentence nextS = sentences.get(bestCount + nextCount + 1);
		int delta2 = frgText.length() - (nextS.getEnd() - bestS.getBegin());
		while( Math.abs(delta1) > Math.abs(delta2) ) {
			nextCount++;
			thisS = sentences.get(bestCount + nextCount);
			delta1 = frgText.length() - (thisS.getEnd() - bestS.getBegin());					
			
			if( bestCount + nextCount + 1 >= sentences.size() ) {
				return null;	
			}
			
			nextS = sentences.get(bestCount + nextCount + 1);
			delta2 = frgText.length() - (nextS.getEnd() - bestS.getBegin());					
		
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		UimaBioCAnnotation frg = new UimaBioCAnnotation(jCas);
		newFrgAnnotations.add(frg);
		frg.setBegin(bestS.getBegin());
		frg.setEnd(thisS.getEnd());
		
		Map<String,String> infons = new HashMap<String, String>();
		infons.put("type", "Fragment");
		infons.put("fragmentCode", code);
		frg.setInfons(BioCUtils.convertInfons(infons, jCas));
		frg.addToIndexes();
		
		FSArray locations = new FSArray(jCas, 1);
		frg.setLocations(locations);
		UimaBioCLocation uiL = new UimaBioCLocation(jCas);
		locations.set(0, uiL);
		uiL.setOffset(frg.getBegin());
		uiL.setLength(frg.getEnd() - frg.getBegin());
		
		return frg;
		
	}
	
	public void locateBlocksInDocForFragment(JCas jCas, UimaBioCAnnotation frg) {
		
		String code = BioCUtils.convertInfons(frg.getInfons()).get("fragmentCode");
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);

		List<Sentence> sentences = new ArrayList<Sentence>();
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			sentences.add(sentence);
		}
		
		String txt = jCas.getDocumentText();
		String frgText = frgTextHash.get(code);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Now locate each of the blocks 
		// within the text of the fragment
		List<UimaBioCAnnotation> frgBlockList = frgBlockListHash.get(code);
		for( UimaBioCAnnotation blk : frgBlockList ) {
			int ss = blk.getBegin() + frg.getBegin();
			int ee = blk.getEnd() + frg.getBegin();
			String tt = blk.getText();
			String ttNoWhite = tt.replaceAll("\\s+", "");
			if(ttNoWhite.endsWith("-"))
				ttNoWhite = ttNoWhite.substring(0,ttNoWhite.length()-1);
			String lastCharacter = ttNoWhite.substring(ttNoWhite.length()-1, ttNoWhite.length());
			
			int whiteCount = tt.length() - ttNoWhite.length();
			
			float best_s_score = 0;
			float best_e_score = 0;
			int win1 = 20;
			int win2 = (ttNoWhite.length() < win1) ? ttNoWhite.length() : win1;  
			int best_s = 0;	
			int best_e = 0;	
			
			for(int i = 0; i<win1; i++) {
				
				int s = ss + i;
				String baseText = txt.substring(s, s+tt.length()).replaceAll("\\s+", "");
				String sText = (baseText.length()<win2)?baseText:baseText.substring(0, win2);
				
				float s_result = levenshteinSimilarityMetric.compare(ttNoWhite.substring(0,win2), sText); 
				
				// Only permit this position to be included as a candidate if 
				// (A) it leads to a better match
				// (B) it's the same as the starting non-whitespace character of the original text
				// (B) it's not itself whitespace in the original text
				if( s_result > best_s_score 
						&& !txt.substring(s,s+1).equals(" ") 
						&& ttNoWhite.startsWith(sText.substring(0,1))) {
					best_s_score = s_result;
					best_s = s;
				}
				if( i > 0 ){
					s = ss - i;
					baseText = txt.substring(s, s+tt.length()).replaceAll("\\s+", "");
					sText = (baseText.length()<win2)?baseText:baseText.substring(0, win2);
					s_result = levenshteinSimilarityMetric.compare(ttNoWhite.substring(0,win2), sText); 
					if( s_result > best_s_score 
							&& !txt.substring(s,s+1).equals(" ") 
							&& ttNoWhite.startsWith(sText.substring(0,1))) {
						best_s_score = s_result;
						best_s = s;
					}
				}
				
				int e = ee + i;
				baseText = txt.substring(e-tt.length(), e).replaceAll("\\s+", "");
				String eText = (baseText.length()<win2)?
						baseText
						:baseText.substring(baseText.length()-win2, baseText.length());
				float e_result = levenshteinSimilarityMetric.compare(
						ttNoWhite.substring(ttNoWhite.length()-win2,ttNoWhite.length()), 
						eText); 
				
				// Interesting bug: Since some blocks end with '-' at the end of the line,
				// we have to relax the condition for the last character to match perfectly.
				if( e_result > best_e_score 
						&& !txt.substring(e-1,e).equals(" ") 
						&& ttNoWhite.endsWith(lastCharacter) ) {
					best_e_score = e_result;
					best_e = e;
				}
				if( i > 0 ){
					e = ee - i;
					baseText = txt.substring(e-tt.length(), e).replaceAll("\\s+", "");
					eText = (baseText.length()<win2)?
							baseText
							:baseText.substring(baseText.length()-win2, baseText.length());
					e_result = levenshteinSimilarityMetric.compare(
							ttNoWhite.substring(ttNoWhite.length()-win2,ttNoWhite.length()), 
							eText); 
					if( e_result > best_e_score 
							&& !txt.substring(e-1,e).equals(" ") 
							&& ttNoWhite.endsWith(lastCharacter) ) {
						best_e_score = e_result;
						best_e = e;
					}
				}
					
				if( best_s_score == 1.0 && best_e_score == 1.0 )
					break;
			}
			
			// Debug Checks
			if( best_s_score != 1.0 || best_e_score != 1.0 ) {
				logger.debug("\n\t" + blk.getText()  
						+ "\n\t\t" + txt.substring(best_s, best_e) 
						+ "\n");	
			}
			
			
			// BUG. We get this error when fragments are not continuous in the text
			// We can't find the next block in the text. 
			if(best_s == 0) {
				logger.error("Can't identify block: " 
						+ uiD.getId() + "_" + code + "('" + tt + "')");
				continue;
			}
			
			blk.setBegin(best_s);
			blk.setEnd(best_e);
			blk.setText(blk.getCoveredText());
			blk.addToIndexes();
			
			newBlkAnnotations.add(blk);
			
			FSArray locations = new FSArray(jCas, 1);
			blk.setLocations(locations);
			UimaBioCLocation uiL = new UimaBioCLocation(jCas);
			locations.set(0, uiL);
			uiL.setOffset(best_s);
			uiL.setLength(best_e - best_s);
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
		}
		
	}

}
