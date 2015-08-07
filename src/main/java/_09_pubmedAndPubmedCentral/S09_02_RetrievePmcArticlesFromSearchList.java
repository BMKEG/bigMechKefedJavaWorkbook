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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.isi.bmkeg.uimaBioC.PubMedESIndex;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S09_02_RetrievePmcArticlesFromSearchList {

	public static class Options {

		@Option(name = "-pmidFile", usage = "File containing pmid values", required = false, metaVar = "INPUT")
		public File pmidFile;

		@Option(name = "-pmcidFile", usage = "File containing pmcid values", required = false, metaVar = "INPUT")
		public File pmcidFile;

		@Option(name = "-pmcDir", usage = "OA Pubmed Central Dump Location", required = true, metaVar = "DATA")
		public File pmcDir;

		@Option(name = "-outDir", usage = "Directory where data should be put", required = true, metaVar = "OUTPUT DIR")
		public File outDir;

		@Option(name = "-getPdfs", usage = "Flag to determine if PDFs are downloaded too", required = false, metaVar = "PDFS?")
		public boolean getPdfs = false;

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

		PubMedESIndex pmES = new PubMedESIndex(options.pmcDir);

		boolean isPmid = true;
		File inputFile = options.pmidFile;
		if( inputFile == null ) {
			inputFile = options.pmcidFile;
			isPmid = false;
		}
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputFile)));
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
				
				if( (isPmid && pmES.hasEntry("pmid", inputLine, "nxml")) ||
						(!isPmid && pmES.hasEntry("pmcId", inputLine, "nxml")) ) {
							
					Map<String,Object> nxmlMap = null;
					if( isPmid )
						nxmlMap = pmES.getMapFromTerm("pmid", inputLine, "nxml");
					else 
						nxmlMap = pmES.getMapFromTerm("pmcId", inputLine, "nxml");
						
					if( nxmlMap == null )
						continue;
					
					String nxmlLoc = (String) nxmlMap.get("nxml_location");
					String pmid = (String) nxmlMap.get("pmid");
					File xml = new File(options.pmcDir.getPath() + "/" + nxmlLoc);
					File target = new File(options.outDir.getPath() + "/" + pmid + ".nxml");
					if(!target.exists() ) {
						if(xml.exists()) {
							// Copy the file over the local file subsystem.
							target.getParentFile().mkdirs();
							Files.copy(xml.toPath(), 
									target.toPath(), 
									StandardCopyOption.REPLACE_EXISTING);
						}
					}
					
					File outPdf = new File(options.outDir.getPath() + 
							"/" + pmid + ".pdf");
					
					if( options.getPdfs && !outPdf.exists() ) {

						Map<String,Object> pdfMap = null;
						if( isPmid )
							pdfMap = pmES.getMapFromTerm("pmid", inputLine, "pdf");
						else 
							pdfMap = pmES.getMapFromTerm("pmcId", inputLine, "pdf");
						
						if( pdfMap == null )
							continue;
						
						URL pdfUrl = new URL( (String) pdfMap.get("pdf_location") );
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
					}
					
				} else {
					logger.info(inputLine + " not in open access index");
				}

			} catch (NumberFormatException e) {
				// just ignore these, skip to the next.
			}
		}
		in.close();
		out.close();

	}

}
