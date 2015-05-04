package _09_pubmedAndPubmedCentral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S09_02_RetrievePmcArticlesFromSearchList {

	public static class Options {

		@Option(name = "-pmidFile", usage = "File containing pmid values", required = true, metaVar = "INPUT")
		public File pmidFile;

		@Option(name = "-pmcDir", usage = "OA Pubmed Central Dump Location", required = true, metaVar = "DATA")
		public File pmcDir;

		@Option(name = "-outDir", usage = "Directory where data should be put", required = true, metaVar = "OUTPUT DIR")
		public File outDir;

	}

	private static Logger logger = Logger
			.getLogger(S09_02_RetrievePmcArticlesFromSearchList.class);

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

		PubMedIdMap h1 = new PubMedIdMap(options.pmcDir);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(options.pmidFile)));
		File outList = new File(options.outDir.getPath() + "/file_list.txt");
		if(outList.exists())
			outList.delete();
		
		if( !options.outDir.exists() )
			options.outDir.mkdirs();
		
		PrintWriter out = new PrintWriter(
				new BufferedWriter(
						new FileWriter(
				outList, true)));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {

			try {

				Integer pmid = new Integer(inputLine);
				if (h1.pm_to_xml.containsKey(pmid)) {

					File xml = new File(h1.pm_to_xml.get(pmid));
					if(xml.exists()) {
						out.println(pmid + 
								"\t" + h1.pm_to_xml.get(pmid).replaceAll(
										options.pmcDir.getPath(), "") + 
								"\t" + h1.pm_to_pdf.get(pmid));
					
						// Copy the file over the local file subsystem.
						File target = new File(
								xml.getPath().replaceAll(
										options.pmcDir.getPath(), 
										options.outDir.getPath()));
						target.getParentFile().mkdirs();
						Files.copy(xml.toPath(), 
								target.toPath(), 
								StandardCopyOption.REPLACE_EXISTING);
					}
				}

			} catch (NumberFormatException e) {
				// just ignore these, skip to the next.
			}
		}
		in.close();
		out.close();

	}

}
