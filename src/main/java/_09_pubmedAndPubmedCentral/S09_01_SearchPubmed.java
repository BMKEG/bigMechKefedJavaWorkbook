package _09_pubmedAndPubmedCentral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
public class S09_01_SearchPubmed {

	public static class Options {

		@Option(name = "-query", usage = "Query String", required = true, metaVar = "QUERY")
		public String query;

		@Option(name = "-outFile", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outFile;

		@Option(name = "-email", usage = "Email", required = true, metaVar = "EMAIL")
		public String email;
		
		@Option(name = "-pgSize", usage = "Page Size", required = false, metaVar = "PAGE SIZE")
		public int pgSize = 20;

	}

	static String PUBMED_SEARCH_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=";
	static String PMC_SEARCH_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pmc&term=";

	private static Logger logger = Logger.getLogger(S09_01_SearchPubmed.class);

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

		ESearchPubmedHandler h1 = new ESearchPubmedHandler();

		String urlString = PUBMED_SEARCH_URL + options.query
				+ "&retmax=" + options.pgSize;

		URL url = new URL(urlString);

		List<Map<String, String>> entries = new ArrayList<Map<String, String>>();

		if (options.outFile.exists())
			options.outFile.delete();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		 /*String inputLine;
		 while ((inputLine = in.readLine()) != null)
		 System.out.println(inputLine);
		 in.close();*/

		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		InputSource is = new InputSource(in);
		saxParser.parse(is, h1);

		int total = h1.getTotal();
		int pos = h1.getOffset() + h1.getEntries().size();
		entries.addAll(h1.getEntries());
		
		logger.info(urlString);

		logger.info(" total number of docs = " + total);

		logger.info(" number of entries downloaded = " + h1.getEntries().size());

		while (pos < total) {

			Thread.sleep(3000);
			
			URL url2 = new URL(url.toString() + "&retstart=" + pos);
			logger.info(url2.toString());

			try {

				in = new BufferedReader(
						new InputStreamReader(url2.openStream()));

				h1 = new ESearchPubmedHandler();
				is = new InputSource(in);
				saxParser.parse(is, h1);

				pos = h1.getOffset() + h1.getEntries().size();
				entries.addAll(h1.getEntries());

				logger.info("number of entries downloaded = " + pos);
				logger.info("Total number of entries downloaded = "
						+ entries.size());

			} catch (Exception e2) {

				e2.printStackTrace();

				throw e2;

			}

		}

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				options.outFile, true)));
		out.println(urlString);
		String s = "id";
		out.println(s);

		for (Map<String, String> a : entries) {
			s = a.get("id");
			out.println(s);
		}
		out.close();

		logger.info("Total number of references loaded: " + entries.size());
		logger.info("Data saved to " + options.outFile.getPath());

	}

}
