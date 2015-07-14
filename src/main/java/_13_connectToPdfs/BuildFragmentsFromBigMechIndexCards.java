package _13_connectToPdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import edu.isi.bmkeg.digitalLibrary.controller.DigitalLibraryEngine;
import edu.isi.bmkeg.ftd.model.FTD;
import edu.isi.bmkeg.ftd.model.FTDFragment;
import edu.isi.bmkeg.ftd.model.FTDFragmentBlock;
import edu.isi.bmkeg.ftd.model.qo.FTDFragment_qo;
import edu.isi.bmkeg.ftd.model.qo.FTD_qo;
import edu.isi.bmkeg.uimaBioC.PubMedESIndex;
import edu.isi.bmkeg.uimaBioC.UimaBioCUtils;
import edu.isi.bmkeg.vpdmf.model.instances.LightViewInstance;

public class BuildFragmentsFromBigMechIndexCards extends JCasAnnotator_ImplBase {

	public static final String LOGIN = ConfigurationParameterFactory
			.createConfigurationParameterName(
					BuildFragmentsFromBigMechIndexCards.class, "login");
	@ConfigurationParameter(mandatory = true, description = "Login for the Digital Library")
	protected String login;

	public static final String PASSWORD = ConfigurationParameterFactory
			.createConfigurationParameterName(
					BuildFragmentsFromBigMechIndexCards.class, "password");
	@ConfigurationParameter(mandatory = true, description = "Password for the Digital Library")
	protected String password;

	public static final String WORKING_DIRECTORY = ConfigurationParameterFactory
			.createConfigurationParameterName(
					BuildFragmentsFromBigMechIndexCards.class,
					"workingDirectory");
	@ConfigurationParameter(mandatory = true, description = "Working Directory for the Digital Library")
	protected String workingDirectory;

	public static final String DB_URL = ConfigurationParameterFactory
			.createConfigurationParameterName(
					BuildFragmentsFromBigMechIndexCards.class, "dbUrl");
	@ConfigurationParameter(mandatory = true, description = "The Digital Library URL")
	protected String dbUrl;

	public static final String CARD_DIRECTORY = ConfigurationParameterFactory
			.createConfigurationParameterName(
					BuildFragmentsFromBigMechIndexCards.class, "cardDirectory");
	@ConfigurationParameter(mandatory = true, description = "Directory holding index cards")
	protected String cardDirectory;

	private DigitalLibraryEngine de;

	private Collection<File> cards;
	private PubMedESIndex pmES;

	private Pattern patt = Pattern.compile("PMC\\d+\\-(\\d+)\\.json");

	private StringMetric cosineSimilarityMetric;
	private StringMetric levenshteinSimilarityMetric;

	private CleartkExtractor<DocumentAnnotation, Token> extractor;

	private static Logger logger = Logger
			.getLogger(BuildFragmentsFromBigMechIndexCards.class);

	public void initialize(UimaContext context)
			throws ResourceInitializationException {

		super.initialize(context);

		try {

			de = new DigitalLibraryEngine();
			de.initializeVpdmfDao(login, password, dbUrl, workingDirectory);

			this.pmES = new PubMedESIndex();

			File cardDir = new File(this.cardDirectory);
			if (!cardDir.exists())
				throw new FileNotFoundException(this.cardDirectory);

			String[] fileTypes = { "json" };
			cards = (Collection<File>) FileUtils.listFiles(cardDir, fileTypes,
					true);

		} catch (Exception e) {

			throw new ResourceInitializationException(e);

		}

		cosineSimilarityMetric = new StringMetricBuilder()
				.with(new CosineSimilarity<String>())
				.simplify(new CaseSimplifier.Lower())
				.simplify(new NonDiacriticSimplifier())
				.tokenize(new WhitespaceTokenizer())
				.tokenize(new QGramTokenizer(2)).build();

		levenshteinSimilarityMetric = new StringMetricBuilder()
				.with(new Levenshtein()).simplify(new NonDiacriticSimplifier())
				.build();

	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas,
				UimaBioCDocument.class);

		// Read PMC_ID from store...
		Map<String, Object> nxmlMap = pmES.getMapFromTerm("pmid", uiD.getId(),
				"nxml");
		String pmcId = (String) nxmlMap.get("pmcId");

		// Run through matching index cards and insert fragments as required...
		int idCount = 0;
		CARD_LOOP: for (File card : cards) {

			try {

				if (!card.getName().toLowerCase()
						.startsWith(pmcId.toLowerCase())) {
					continue;
				}

				Matcher m = patt.matcher(card.getName());
				if (m.find()) {
					idCount = new Integer(m.group(1));
				} else {
					idCount++;
				}

				// Is there an article in the database corresponding to this
				// file?
				de.getExtDigLibDao().getCoreDao().connectToDb();
				FTD ftd = de.getExtDigLibDao()
						.findArticleDocumentByPmidInTrans(
								new Integer(uiD.getId()));
				de.getExtDigLibDao().getCoreDao().closeDbConnection();

				if (ftd == null) {
					logger.error("Can't find PMID:" + uiD.getId());
					continue;
				}

				String txt = jCas.getDocumentText();
				List<Sentence> sentences = new ArrayList<Sentence>();
				for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
					sentences.add(sentence);
				}

				Collection<UimaBioCDocument> uiDs = JCasUtil.select(jCas,
						UimaBioCDocument.class);
				if (uiDs.size() != 1) {
					throw new Exception("Number of BioCDocuments linked to "
							+ "this document = " + uiDs.size()
							+ ", this should be 1.");
				}

				// Text from the index card to match to the PDF.
				JSONParser p = new JSONParser();
				Map json = (Map) p.parse(new FileReader(card));
				String pmc_id = (String) json.get("pmc_id");

				List<String> evidenceList = new ArrayList<String>();
				if (json.get("evidence") instanceof String) {
					String evidence = (String) json.get("evidence");
					evidenceList.add(evidence);
				} else {
					JSONArray evidenceArray = (JSONArray) json.get("evidence");
					for (Object o : evidenceArray) {
						evidenceList.add((String) o);
					}
				}

				String interactionType = "";
				if (json.get("extracted_information") instanceof JSONObject) {
					JSONObject o = (JSONObject) json
							.get("extracted_information");
					interactionType = (String) o.get("interaction_type");
				}

				// Need to search for Fragments with the same evidence strings.
				FTDFragment_qo frgQo = new FTDFragment_qo();
				FTD_qo ftdQo = new FTD_qo();
				frgQo.setFtd(ftdQo);
				ftdQo.setVpdmfId(ftd.getVpdmfId() + "");
				frgQo.setFrgType("bigMechInt");

				//
				// Inefficient, list all the fragments for each document to
				// check every time.
				//
				Map<String, Long> existingFrgs = new HashMap<String, Long>();
				for (LightViewInstance lvi : de.getDigLibDao().getCoreDao()
						.list(frgQo, "FTDFragment")) {

					String[] fields = lvi.getIndexTupleFields().split(
							"\\{\\|\\}");
					String[] tuple = lvi.getIndexTuple().split("\\{\\|\\}");
					Map<String, String> o = new HashMap<String, String>();
					for (int i = 0; i < fields.length; i++) {

						String f = fields[i];
						String v = tuple[i];
						if (v == null)
							v = "";
						v = v.replaceAll("\\<\\|\\>", " ");
						v = v.replaceAll("\\s+", " ");
						o.put(f, v);
					}
					existingFrgs.put(o.get("FTDFragment_3"), lvi.getVpdmfId());
				}

				Long frgVpdmfId = -1L;
				EVIDENCE_LOOP: for (String evidence : evidenceList) {

					for (String key : existingFrgs.keySet()) {
						final float test = levenshteinSimilarityMetric.compare(
								evidence, key);
						if (test > 0.95) {
							frgVpdmfId = existingFrgs.get(key);
							break;
						}
					}

					float best = 0;
					int sCount = 0;
					int bestCount = 0;
					int winL = 50;
					int l = evidence.length();
					Sentence startS = null;
					for (Sentence sentence : sentences) {
						int s = sentence.getBegin();
						int e = (l > winL) ? (s + winL) : (s + l);
						if (e > txt.length())
							e = txt.length();
						String sText = txt.substring(s, e);
						final float result = cosineSimilarityMetric.compare(
								evidence.substring(0, winL), sText);
						if (result > best) {
							best = result;
							startS = sentence;
							bestCount = sCount;
						}
						sCount++;
					}

					//
					// If we can't find a good match, skip this whole fragment.
					//
					if (best < 0.70) {
						int w = (l > winL) ? (winL) : (l);
						System.err.println("ERROR("
								+ uiD.getId()
								+ "), score:"
								+ best
								+ ",\n   guess: "
								+ txt.substring(startS.getBegin(),
										startS.getBegin() + w) + "\n   frg: "
								+ evidence.substring(0, w) + "\n");

						continue;
					}

					int nextCount = 0;
					Sentence lastS = sentences.get(bestCount + nextCount);
					int delta1 = evidence.length()
							- (lastS.getEnd() - startS.getBegin());
					Sentence nextS = sentences.get(bestCount + nextCount + 1);
					int delta2 = evidence.length()
							- (nextS.getEnd() - startS.getBegin());
					while (Math.abs(delta1) > Math.abs(delta2)) {
						nextCount++;
						lastS = sentences.get(bestCount + nextCount);
						delta1 = evidence.length()
								- (lastS.getEnd() - startS.getBegin());

						if (bestCount + nextCount + 1 >= sentences.size()) {
							continue CARD_LOOP;
						}

						nextS = sentences.get(bestCount + nextCount + 1);
						delta2 = evidence.length()
								- (nextS.getEnd() - startS.getBegin());

					}

					// Find all the chunks that cover this sentence.
					List<UimaBioCAnnotation> chunkList = this
							.readChunkListFromCas(jCas, startS.getBegin(),
									startS.getEnd());

					List<UimaBioCAnnotation> coveredList = JCasUtil
							.selectCovered(jCas, UimaBioCAnnotation.class,
									startS.getBegin(), startS.getEnd());
					List<UimaBioCAnnotation> wordBlockList = new ArrayList<UimaBioCAnnotation>();
					for (UimaBioCAnnotation ann : coveredList) {
						if (UimaBioCUtils.readInfons(ann.getInfons(), "type")
								.equals("lapdf-word-block")) {
							wordBlockList.add(ann);
						}
					}

					if (wordBlockList.size() == 0) {
						logger.error("INDEXING ERROR: Cannot find evidence : "
								+ evidence);
						continue EVIDENCE_LOOP;
					}

					//
					// Need to construct fragments from these word blocks.
					// NOT QUITE FINISHED. NEED TO DETECT TRANSITIONS
					// TO OTHER CHUNKS.
					//
					FTDFragment frg = new FTDFragment();
					FTDFragmentBlock blk = null;

					int maxX = -1, minX = 10000;
					int lastX = 0, lastY = 0, lastW = 0, lastH = 0;
					int beginPos = 0, endPos = 0;

					String s = "";
					UimaBioCAnnotation lastChunk = null;
					for (UimaBioCAnnotation wordBlock : wordBlockList) {

						// If this block
						List<UimaBioCAnnotation> chunks = this
								.readChunkListFromCas(jCas,
										wordBlock.getBegin(),
										wordBlock.getEnd());
						UimaBioCAnnotation chunk = chunks.get(0);
						if (!chunk.equals(lastChunk)) {
							if (blk != null) {
								blk.setX3(lastX + lastW);
								blk.setY3(lastY + lastH);

								blk.setX4(minX);
								blk.setY4(lastY);
								blk.setText(s);
								frg.getAnnotations().add(blk);
							}

							blk = new FTDFragmentBlock();
							maxX = -1;
							minX = 10000;
							lastX = 0;
							lastY = 0;
							lastW = 0;
							lastH = 0;
						}

						Map<String, String> infons = UimaBioCUtils
								.convertInfons(wordBlock.getInfons());
						int x = new Integer(infons.get("x"));
						int y = new Integer(infons.get("y"));
						int w = new Integer(infons.get("w"));
						int h = new Integer(infons.get("h"));
						int pg = new Integer(infons.get("p"));

						if (s.length() > 0)
							s += " ";
						s += UimaBioCUtils.readInfons(wordBlock.getInfons(),
								"t");

						// first block in the fragment...
						if (blk.getX1() == 0) {
							blk.setX1(x);
							blk.setY1(y + h);
							blk.setY2(y);
							blk.setP(pg);
							blk.setCode(interactionType);
						}

						// first block in a new line...
						if (blk.getY1() != y) {
							blk.setX2(maxX);
						}

						if (x + w > maxX)
							maxX = x + w;

						if (x < minX)
							minX = x;

						lastX = x;
						lastY = y;
						lastW = w;
						lastH = h;
						lastChunk = chunk;

					}

					blk.setX3(lastX + lastW);
					blk.setY3(lastY);

					blk.setX4(minX);
					blk.setY4(lastY + lastH);
					blk.setText(s);

					frg.getAnnotations().add(blk);
					frg.setFtd(ftd);
					frg.setFrgType("bigMechInt");
					frg.setFrgOrder(idCount + "");
					frgVpdmfId = de.getFtdDao().getCoreDao()
							.insert(frg, "FTDFragment");

				}

			} catch (Exception e) {

				logger.error("Error in card: " + card.getPath());
				e.printStackTrace();

			}

		}

	}

	private List<UimaBioCAnnotation> readChunkListFromCas(JCas jCas, int begin,
			int end) {

		List<UimaBioCAnnotation> chunkList = new ArrayList<UimaBioCAnnotation>();
		List<UimaBioCAnnotation> coveringList = JCasUtil.selectCovering(jCas,
				UimaBioCAnnotation.class, begin, end);
		for (UimaBioCAnnotation ann : coveringList) {
			Map<String, String> infons = UimaBioCUtils.convertInfons(ann
					.getInfons());
			if (infons.get("type").equals("lapdf-chunk-block")) {
				chunkList.add(ann);
			}
		}

		return chunkList;

	}

}
