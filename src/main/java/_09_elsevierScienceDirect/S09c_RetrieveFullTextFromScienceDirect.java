package _09_elsevierScienceDirect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.xml.sax.InputSource;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S09c_RetrieveFullTextFromScienceDirect {

	public static class Options {

		@Option(name = "-searchFile", usage = "Input File", required = true, metaVar = "INPUT")
		public File input;

		@Option(name = "-apiKey", usage = "API String", required = true, metaVar = "APIKEY")
		public String apiKey;

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outDir;
		
	}

	private static Logger logger = Logger
			.getLogger(S09c_RetrieveFullTextFromScienceDirect.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
		
		try {

			parser.parseArgument(args);

		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.print("Arguments: ");
			parser.printSingleLineUsage(System.err);
			System.err.println("\n\n Options: \n");
			parser.printUsage(System.err);
			System.exit(-1);

		}

		S09c_RetrieveFullTextFromScienceDirect readScienceDirect = new S09c_RetrieveFullTextFromScienceDirect();

		if( !options.outDir.exists() )
			options.outDir.mkdirs();
		
		//http://api.elsevier.com/content/article/pii/S0001457515000780?apiKey=97f607fc347bd27badcddcab8decb51f&httpAccept=application/json
		
		BufferedReader in = new BufferedReader(new FileReader(options.input));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			String[] fields = inputLine.split("\\t");
			if( fields.length<6 || !fields[5].startsWith("http") )
				continue;
			
			URL url = new URL(fields[5]);
			String doi = fields[4];
			doi = doi.replaceAll("\\/", "_slash_");
			doi = doi.replaceAll("\\:", "_colon_");

			File f = new File(options.outDir.getPath() + "/" + doi + ".json"); 
			if( f.exists() )
				continue;
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
			
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String inputLine2;
			while ((inputLine2 = in2.readLine()) != null)
				out.println(inputLine2);
			in2.close();
			out.close();

			logger.info("File Downloaded: " 
					+ ((fields[0].length() > 20)?fields[0].substring(0,20):fields[0])
					+ " (" + fields[2] + "), " + fields[4]);
			
		}
		in.close();
	
	}

}
