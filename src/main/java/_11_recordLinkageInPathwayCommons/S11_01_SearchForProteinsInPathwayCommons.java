package _11_recordLinkageInPathwayCommons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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
public class S11_01_SearchForProteinsInPathwayCommons {

	public static class Options {

		@Option(name = "-searchString", usage = "Search String", required = true, metaVar = "SEARCH")
		public String searchString;

		@Option(name = "-outFile", usage = "Output File", required = true, metaVar = "OUT-FILE")
		public File outFile;

		@Option(name = "-entityType", usage = "Entity Type", required = true, metaVar = "ENTITY-TYPE")
		public String entityType;

		@Option(name = "-database", usage = "Database", required = false, metaVar = "SEARCH")
		public String database;

	}

	private static Logger logger = Logger
			.getLogger(S11_01_SearchForProteinsInPathwayCommons.class);

	static String SEARCH_URL = "http://www.pathwaycommons.org/pc2/search?organism=9606&q=";

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

		PathwayCommonsSearchHandler h1 = new PathwayCommonsSearchHandler();

		String urlString = SEARCH_URL + options.searchString + "&type="
				+ options.entityType;
		
		if( options.database != null )
			urlString += "&datasource=" + options.database;

		URL url = new URL(urlString);

		Set<String> names = new HashSet<String>();

		if (options.outFile.exists())
			options.outFile.delete();

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));

			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			InputSource is = new InputSource(in);
			saxParser.parse(is, h1);

			int total = h1.getTotal();
			int pos = (h1.getHitsPerPage() * h1.getPageNo())
					+ h1.getNames().size();
			names.addAll(h1.getNames());
			int page = 0;

			logger.info(urlString);

			logger.info(" total number of entries = " + total);

			logger.info(" number of entries downloaded = "
					+ h1.getNames().size());

			while (pos < total) {

				Thread.sleep(3000);

				page++;
				URL url2 = new URL(url.toString() + "&page=" + page);
				logger.info(url2.toString());

				in = new BufferedReader(
						new InputStreamReader(url2.openStream()));

				h1 = new PathwayCommonsSearchHandler();
				is = new InputSource(in);
				saxParser.parse(is, h1);

				pos = (h1.getHitsPerPage() * h1.getPageNo())
						+ h1.getNames().size();
				names.addAll(h1.getNames());

				logger.info("position = " + pos);
				logger.info("Total number of unique names downloaded = "
						+ names.size());
			}
			
		} catch (IOException e2) {

			if( !e2.getMessage().contains( "Server returned HTTP response code: 460 for URL" ) ) {				
				e2.printStackTrace();
				throw e2;
			}

		} catch (Exception e3) {

			e3.printStackTrace();
			throw e3;

		}

		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
				options.outFile, true)));
		/*out.println(urlString);
		String s1 = "name";
		out.println(s1);*/

		for (String s2 : names) {
			out.println(s2);
		}
		out.close();

		logger.info("Total number of names loaded: " + names.size());
		logger.info("Data saved to " + options.outFile.getPath());

	}
}
