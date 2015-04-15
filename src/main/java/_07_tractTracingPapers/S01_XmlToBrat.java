package _07_tractTracingPapers;

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
public class S01_XmlToBrat {

	public static class Options {

		@Option(name = "-inXml", usage = "Input directory", required = true, metaVar = "INPUT")
		public File inFile;

		@Option(name = "-outBrat", usage = "Output directory", required = true, metaVar = "INPUT")
		public File outFile;

		@Option(name = "-tagFile", usage = "Path to file with tag names", required = true, metaVar = "INPUT")
		public File tagFile;
		
		@Option(name = "-newlineTagFile", usage = "Path to file with tags to replace with \\n", required = true, metaVar = "INPUT")
		public File newlineTagFile;

	}

	private static Logger logger = Logger.getLogger(S01_XmlToBrat.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
			
		try {

			parser.parseArgument(args);
			
			if( !options.outFile.exists() )
				options.outFile.mkdirs();
			
			String tagString = TextUtils.readFileToString(options.tagFile);
			List<String> tags = new ArrayList<String>();
			for(String s : tagString.split("\\s+")) { tags.add(s); }
			
			String newlineTagString = TextUtils.readFileToString(options.newlineTagFile);
			List<String> newlineTags = new ArrayList<String>();
			for(String s : newlineTagString.split("\\s+")) { newlineTags.add(s); }
			
			SimpleXmlHandler handler = new SimpleXmlHandler(tags, newlineTags);
			
			Pattern p = Pattern.compile("\\.xml");
			Map<String, File> inFiles = Converters.recursivelyListFiles(options.inFile,p);
			for(File f : inFiles.values() ) {

				handler.setXmlFile(f);
				FileInputStream is = new FileInputStream(f);			
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				saxFactory.setValidating(false);
				SAXParser saxParser = saxFactory.newSAXParser();
				saxParser.parse(is, handler);
				
				String fp = options.outFile.getPath() + "/" + f.getName();
				fp = fp.substring(0,fp.length()-4);
				File outFile = new File(fp + ".txt");
				PrintWriter out = new PrintWriter(new BufferedWriter(
							new FileWriter(outFile, true)));
				out.println(handler.getContent());
				out.close();

				File annFile = new File(fp + ".ann");
				out = new PrintWriter(new BufferedWriter(new FileWriter(
							annFile, true)));
					
				int i = 0;
				for (BioCAnnotation a : handler.getAnnotations()) {
					BioCLocation l = a.getLocations().get(0);
					String s = "T" + i  + "\t" + a.getInfon("type") + " " + 
								l.getOffset() + " " +
								(l.getOffset() + l.getLength()) + "\t" + 
								a.getText();
						out.println(s);
						i++;
				}
				out.close();
				
			}
			
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
