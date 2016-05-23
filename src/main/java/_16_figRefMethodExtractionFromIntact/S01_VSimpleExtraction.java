package _16_figRefMethodExtractionFromIntact;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import bioc.BioCAnnotation;
import bioc.BioCLocation;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.TextUtils;

/**
 * This script runs through the digital library and extracts 
 * all fragments for a given corpus
 * 
 * @author Gully
 *
 */
public class S01_VSimpleExtraction {

	public static class Options {

		@Option(name = "-inXml", usage = "Input directory", required = true, metaVar = "INPUT")
		public File inFile;

		@Option(name = "-outFile", usage = "Output File", required = true, metaVar = "OUTPUT")
		public File outFile;

	}

	private static Logger logger = Logger.getLogger(S01_VSimpleExtraction.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
			
		try {

			List<SimpleIntactExperimentCode> intactData = new ArrayList<SimpleIntactExperimentCode>();
			
			parser.parseArgument(args);
			
			if( !options.outFile.getParentFile().exists() )
				options.outFile.getParentFile().mkdirs();
			
			SimpleXmlHandler handler = new SimpleXmlHandler();
			
			Pattern p = Pattern.compile("\\.xml");
			Map<String, File> inFiles = Converters.recursivelyListFiles(options.inFile,p);
			for(File f : inFiles.values() ) {

				FileInputStream is = new FileInputStream(f);			
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				saxFactory.setValidating(false);
				SAXParser saxParser = saxFactory.newSAXParser();
				saxParser.parse(is, handler);
				intactData.addAll(handler.getIntactData());
				
			}
			
			PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter(options.outFile, true)));
			
			for( SimpleIntactExperimentCode record : intactData) {
				out.println(
						record.pmid + "\t" + record.exptId + "\t" + record.figCode + 
						"\t" + record.interactionCode + "\t" + record.interactionLabel +
						"\t" + record.partipantCode + "\t" + record.participantLabel
				);
			}
			out.close();			
			
		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.print("Arguments: ");
			parser.printSingleLineUsage(System.err);
			System.err.println("\n\n Options: \n");
			parser.printUsage(System.err);
			System.exit(-1);

		} catch (Exception e2) {

			e2.printStackTrace();

		}
				
	}
	
}
