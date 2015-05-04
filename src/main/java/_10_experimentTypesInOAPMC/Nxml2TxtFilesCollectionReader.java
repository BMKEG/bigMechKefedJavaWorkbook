package _10_experimentTypesInOAPMC;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;

import bioc.type.UimaBioCAnnotation;
import bioc.type.UimaBioCDocument;
import bioc.type.UimaBioCLocation;
import bioc.type.UimaBioCPassage;
import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;
import edu.isi.bmkeg.utils.Converters;

/**
 * We want to optimize this interaction for speed, so we run a
 * manual query over the underlying database involving a minimal subset of
 * tables.
 * 
 * @author burns
 * 
 */
public class Nxml2TxtFilesCollectionReader extends JCasCollectionReader_ImplBase {
	
	private Map<String, File> txtFiles;
	private Iterator<File> txtFileIt; 
	
	private int pos = 0;
	private int count = 0;
	
	private static Logger logger = Logger.getLogger(Nxml2TxtFilesCollectionReader.class);

	public static final String INPUT_DIRECTORY = ConfigurationParameterFactory
			.createConfigurationParameterName(Nxml2TxtFilesCollectionReader.class,
					"inputDirectory");
	@ConfigurationParameter(mandatory = true, description = "Input Directory for Nxml2Txt Files")
	protected String inputDirectory;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {

		try {

			 Map<String, File> txtFiles = Converters.recursivelyListFiles(
					new File(inputDirectory),
					Pattern.compile("\\.txt"));

			 count = txtFiles.size();
			 txtFileIt = txtFiles.values().iterator();
			
		} catch (Exception e) {

			throw new ResourceInitializationException(e);

		}

	}

	/**
	 * @see com.ibm.uima.collection.CollectionReader#getNext(com.ibm.uima.cas.CAS)
	 */
	public void getNext(JCas jcas) throws IOException, CollectionException {

		try {
			
			if(!txtFileIt.hasNext()) 
				return;
			
			File txtFile = txtFileIt.next();
			File soFile = new File(txtFile.getPath().replaceAll("\\.txt$", ".so"));

			while( !txtFile.exists() || !soFile.exists() ) {

				if(!txtFileIt.hasNext()) 
					return;
				txtFile = txtFileIt.next();
				soFile = new File(txtFile.getPath().replaceAll("\\.txt$", ".so"));
			
			}

			String txt = FileUtils.readFileToString(txtFile);
			jcas.setDocumentText( txt );

			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			UimaBioCDocument uiDoc = new UimaBioCDocument(jcas);
			
			Map<String,String> infons = new HashMap<String,String>();
			infons.put("relative-source-path", txtFile.getPath().replaceAll(inputDirectory + "/", ""));
			uiDoc.setInfons(BioCUtils.convertInfons(infons, jcas));
			
			uiDoc.setBegin(0);
			uiDoc.setEnd(txt.length());
						
			uiDoc.addToIndexes();
			int passageCount = 0;
			int nSkip = 0;
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(soFile)));
			String line;
			while ((line = in.readLine()) != null) {
			
				String[] fields = line.split("\t");
				
				if( fields.length < 3 ) {
					continue;		
				}
				
				String id = fields[0];

				String typeOffsetStr = fields[1];
				String[] typeOffsetArray = typeOffsetStr.split(" ");
				String type = typeOffsetArray[0];
				int begin = new Integer(typeOffsetArray[1]);
				int end = new Integer(typeOffsetArray[2]);
								
				String str = "";
				if( fields.length > 2 ) 
					str = fields[2];
				
				String codes = "";
				if( fields.length > 3 ) 
					codes = fields[3];
				
				// Just run through the data and assert the pieces to the jcas

				// High level sections, none of these have types of extra data.
				if( type.equals("front") || 
						type.equals("abstract") ||  
						type.equals("body") ||  
						type.equals("ref-list")){					
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					UimaBioCPassage uiP = new UimaBioCPassage(jcas);
					int annotationCount = 0;
					uiP.setBegin(begin);
					uiP.setEnd(end);
					uiP.setOffset(begin);
					passageCount++;
					
					infons = new HashMap<String, String>();
					infons.put("type", type);
					uiP.setInfons(BioCUtils.convertInfons(infons, jcas));
					uiP.addToIndexes();
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				}
				
				// Sections, Paragraphs, Titles, Subtitles Figure Captions.
				if( type.equals("title") || 
						type.equals("subtitle") ||  
						type.equals("sec") ||  
						type.equals("p") ||  
						type.equals("caption") ||  
						type.equals("fig")){					
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					UimaBioCPassage uiP = new UimaBioCPassage(jcas);
					int annotationCount = 0;
					uiP.setBegin(begin);
					uiP.setEnd(end);
					uiP.setOffset(begin);
					passageCount++;

					infons = new HashMap<String, String>();
					infons.put("type", type);
					uiP.setInfons(BioCUtils.convertInfons(infons, jcas));
					uiP.addToIndexes();
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				}
				
				// Formatting annotations.
				if( type.equals("bold") || 
						type.equals("italic") ||  
						type.equals("sub") ||  
						type.equals("sup") ){					
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					UimaBioCAnnotation uiA = new UimaBioCAnnotation(jcas);
					uiA.setBegin(begin);
					uiA.setEnd(end);
					Map<String,String> infons2 = new HashMap<String, String>();
					infons2.put("type", type);
					uiA.setInfons(BioCUtils.convertInfons(infons2, jcas));
					uiA.addToIndexes();
					
					FSArray locations = new FSArray(jcas, 1);
					uiA.setLocations(locations);
					UimaBioCLocation uiL = new UimaBioCLocation(jcas);
					locations.set(0, uiL);
					uiL.setOffset(begin);
					uiL.setLength(end - begin);
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				}
				
				// Id values for the BioCDocument.
				if( type.equals("article-id") ){					
					
					infons = BioCUtils.convertInfons(uiDoc.getInfons());
					String[] keyValue = codes.split("=");
					infons.put(keyValue[1], str);
					uiDoc.setInfons(
							BioCUtils.convertInfons(infons, jcas)
							);
					if( keyValue[1].contains("pmid")){
						uiDoc.setId(str);
					}
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				
				}
				
			}

		    pos++;
		    if( (pos % 1000) == 0) {
		    	System.out.println("Processing " + pos + "th document.");
		    }
		    
		} catch (Exception e) {
			
			System.err.print(this.count);
			throw new CollectionException(e);

		}

	}
		
	protected void error(String message) {
		logger.error(message);
	}

	@SuppressWarnings("unused")
	protected void warn(String message) {
		logger.warn(message);
	}

	@SuppressWarnings("unused")
	protected void debug(String message) {
		logger.error(message);
	}

	public Progress[] getProgress() {		
		Progress progress = new ProgressImpl(
				this.pos, 
				this.count, 
				Progress.ENTITIES);
		
        return new Progress[] { progress };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return txtFileIt.hasNext();
	}

}
