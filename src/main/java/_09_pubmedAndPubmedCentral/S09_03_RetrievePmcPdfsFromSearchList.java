package _09_pubmedAndPubmedCentral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class S09_03_RetrievePmcPdfsFromSearchList {

	public static class Options {

		@Option(name = "-pmidFile", usage = "File containing pmid values", required = true, metaVar = "INPUT")
		public File pmidFile;

		@Option(name = "-outDir", usage = "Directory where data should be put", required = true, metaVar = "OUTPUT DIR")
		public File outDir;

	}

	private static Logger logger = Logger
			.getLogger(S09_03_RetrievePmcPdfsFromSearchList.class);
	
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

		PubMedIdMap h1 = new PubMedIdMap();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(options.pmidFile)));
		File outList = new File(options.outDir.getPath() + "/file_list.txt");
		if (outList.exists())
			outList.delete();

		Pattern pmidPatt = Pattern.compile("^(\\d+)[\\s,]");
		Set<Integer> prevPmids = new HashSet<Integer>();

		if( !options.outDir.exists() )
			options.outDir.mkdirs();
		
		PrintWriter out = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outList, true)));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {

			try {

				Matcher m = pmidPatt.matcher(inputLine);

				if( !m.find() )
					continue; 
				
				Integer pmid = new Integer(m.group(1));
				
				if( prevPmids.contains(pmid) )
					continue;

				File outPdf = new File( options.outDir.getPath() + "/" + pmid + ".pdf");
				if( outPdf.exists() )
					continue;
				
				if (h1.pm_to_pdf.containsKey(pmid)) {

					prevPmids.add(pmid);

					URL pdfUrl = new URL(h1.pm_to_pdf.get(pmid));
					InputStream input = pdfUrl.openStream();
					byte[] buffer = new byte[4096];
					int n = - 1;
					
					if( !outPdf.getParentFile().exists() )
						outPdf.getParentFile().mkdirs();
					OutputStream output = new FileOutputStream( outPdf );
					while ( (n = input.read(buffer)) != -1) {
						output.write(buffer, 0, n);
					}
					output.close();
					input.close();
					
					out.write(pmid + ".pdf\n");
						
				}
				Thread.sleep(500);

			} catch (NumberFormatException e) {
				// just ignore these, skip to the next.
			}
		}
		in.close();
		out.close();

	}

}
