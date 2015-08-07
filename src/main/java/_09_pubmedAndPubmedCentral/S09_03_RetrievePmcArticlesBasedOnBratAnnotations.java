package _09_pubmedAndPubmedCentral;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
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
public class S09_03_RetrievePmcArticlesBasedOnBratAnnotations {

	public static class Options {

		@Option(name = "-bratDir", usage = "Brat annotation directory", required = true, metaVar = "INPUT")
		public File bratDir;

		@Option(name = "-pmcDir", usage = "OA Pubmed Central Dump Location", required = true, metaVar = "DATA")
		public File pmcDir;

		@Option(name = "-outDir", usage = "Directory where data should be put", required = true, metaVar = "OUTPUT DIR")
		public File outDir;

	}

	private static Logger logger = Logger
			.getLogger(S09_03_RetrievePmcArticlesBasedOnBratAnnotations.class);

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

		Set<String> pmidSet = new HashSet<String>();
		String[] fileTypes = {"ann"};		
		Collection<File> l = (Collection<File>) FileUtils.listFiles(options.bratDir, fileTypes, true);
		Pattern patt = Pattern.compile("^(\\d+)_");
		
		for(File annFile : l) {
			Matcher m = patt.matcher(annFile.getName());
			if(m.find()) 
				pmidSet.add(m.group(1));
		}
		
		for(String pmid : pmidSet) {
			
			try {
				
				if( pmES.hasEntry("pmid", pmid, "nxml") ) {
							
					Map<String,Object> nxmlMap = pmES.getMapFromTerm("pmid", pmid, "nxml");			
					String nxmlLoc = (String) nxmlMap.get("nxml_location");
					File xml = new File(options.pmcDir.getPath() + "/" + nxmlLoc);
					if(xml.exists()) {
						// Copy the file over the local file subsystem.
						File target = new File(
								options.outDir.getPath() + "/" + pmid + ".nxml");
						target.getParentFile().mkdirs();
						Files.copy(xml.toPath(), 
								target.toPath(), 
								StandardCopyOption.REPLACE_EXISTING);
					}
					
					Map<String,Object> pdfMap = null;
					pdfMap = pmES.getMapFromTerm("pmid", pmid, "pdf");
					
					URL pdfUrl = new URL( (String) pdfMap.get("pdf_location") );
					InputStream input = pdfUrl.openStream();
					byte[] buffer = new byte[4096];
					int n = - 1;
					
					File outPdf = new File(options.outDir.getPath() + 
							"/" + pmid + ".pdf");
					if( outPdf.exists() ) 
						outPdf.delete();
					
					if( !outPdf.getParentFile().exists() )
						outPdf.getParentFile().mkdirs();
					OutputStream output = new FileOutputStream( outPdf );
					while ( (n = input.read(buffer)) != -1) {
						output.write(buffer, 0, n);
					}
					output.close();
					input.close();
					
				} else {
					logger.info(pmid + " not in index");
				}

			} catch (NumberFormatException e) {
				// just ignore these, skip to the next.
			}
			
		}

	}

}
