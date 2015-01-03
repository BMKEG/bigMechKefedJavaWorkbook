package _02_rasSpecificPapers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.isi.bmkeg.digitalLibrary.controller.DigitalLibraryEngine;
import edu.isi.bmkeg.ftd.model.FTDFragment;
import edu.isi.bmkeg.ftd.model.FTDFragmentBlock;

/**
 * This script runs through the digital library and extracts 
 * all fragemnts for a given corpus
 * 
 * @author Gully
 *
 */
public class S01_ExtractCodedFragmentsAsAnnotations {

	public static class Options {

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outdir;
		
		@Option(name = "-pmid", usage = "Pmid", required = true, metaVar = "PMID")
		public int pmid = -1;

		@Option(name = "-frgType", usage = "FrgType", required = true, metaVar = "FRG_TYPE")
		public String frgType = "";
		
		@Option(name = "-l", usage = "Database login", required = true, metaVar = "LOGIN")
		public String login = "";

		@Option(name = "-p", usage = "Database password", required = true, metaVar = "PASSWD")
		public String password = "";

		@Option(name = "-db", usage = "Database name", required = true, metaVar = "DBNAME")
		public String dbName = "";

		@Option(name = "-wd", usage = "Working directory", required = true, metaVar = "WDIR")
		public String workingDirectory = "";

	}

	private static Logger logger = Logger.getLogger(S01_ExtractCodedFragmentsAsAnnotations.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();
		Pattern patt = Pattern.compile("(\\S+)___(\\S+)");
		
		Map<Integer,Set<String>> pmids = new HashMap<Integer,Set<String>>();
		Map<Integer,String> pmcids = new HashMap<Integer,String>();
		Map<String,String> pdfLocs = new HashMap<String,String>();
		
		CmdLineParser parser = new CmdLineParser(options);
	
		int nLapdfErrors = 0, nSwfErrors = 0, total = 0;
		
		try {

			parser.parseArgument(args);
						
			DigitalLibraryEngine de = new DigitalLibraryEngine();
			de.initializeVpdmfDao(
					options.login, 
					options.password, 
					options.dbName, 
					options.workingDirectory);	
			
			// Query based on a query constructed with SqlQueryBuilder based on the TriagedArticle view.
			String countSql = "SELECT COUNT(*) ";

			String selectSql = "SELECT l.vpdmfId, a.pmid, f.name, frg.vpdmfId, frg.frgOrder, blk.vpdmfOrder, blk.text, blk.code ";
			
			String fromWhereSql = "FROM LiteratureCitation AS l," +
					" ArticleCitation as a, FTD as f, " + 
					" FTDFragment as frg, FTDFragmentBlock as blk " +
					" WHERE " +
					"blk.fragment_id = frg.vpdmfId AND " +
					"l.fullText_id = f.vpdmfId AND " +
					"l.vpdmfId = a.vpdmfId AND " +
					"frg.ftd_id = f.vpdmfId AND " +
					"a.pmid = '"+ options.pmid + "' AND " + 
					"frg.frgType = '"+ options.frgType + "' " +
					" ORDER BY l.vpdmfId, frg.vpdmfId, frg.frgOrder, blk.vpdmfOrder;";

			de.getDigLibDao().getCoreDao().getCe().connectToDB();
			
			ResultSet countRs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
					countSql + fromWhereSql);
			countRs.next();
			int count = countRs.getInt(1);
						
			countRs.close();
			
			ResultSet rs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
					selectSql + fromWhereSql);

			Map<Long,Map<String,String>> lookup = new HashMap<Long,Map<String,String>>();

			Map<String,String> txtHash = new HashMap<String,String>();
			Map<String,String> annHash = new HashMap<String,String>();
			
			String frgText = "";
			String annText = "";
			int j = 0, pos = 0;
			
			while( rs.next() ) {

				Long citationId = rs.getLong("l.vpdmfId");
				String fileStem = rs.getString("f.name");
				int pmid = rs.getInt("a.pmid");
				String frgOrder = rs.getString("frg.frgOrder");
				String blkId = rs.getString("blk.vpdmfOrder");
				
				String blkCode = rs.getString("blk.code");
				if( blkCode == null || blkCode.equals("-") ) 
					continue;
				
				String blkText = rs.getString("blk.text");
				blkText = blkText.replaceAll("\\s+", " ");
				blkText = blkText.replaceAll("\\-\\s+", "");

				frgText += blkText;
						
				int start = frgText.indexOf(blkText);
				int end = start + blkText.length() - 1;
				
				annText += "T"+ (j++) + "\t" + 
						blkCode.replaceAll(": ", "_") + " " + 
						start + " " +
						end + "\t" + 
						blkText +"\n";

				txtHash.put(pmid + "___" + frgOrder, frgText);
				annHash.put(pmid + "___" + frgOrder, annText);
				
				pos += pos + blkText.length();
				
			}
			rs.close();

			//
			// now write these to files. 
			//
			List<String> fragments = new ArrayList<String>(txtHash.keySet());
			Collections.sort(fragments);
			
			for(String s : fragments) {
			
				Matcher m = patt.matcher(s);
				if( m.find() ) {
					String pmid = m.group(1);
					String code = m.group(2);

					frgText = txtHash.get(s);
					annText = annHash.get(s);
					
					String pathStem = options.outdir.getPath() + "/" + options.frgType +
							"/" + pmid + "/" + pmid + "_" + code;
					File frgFile = new File( pathStem + ".txt" );
					File annFile = new File( pathStem + ".ann" );
					
					FileUtils.writeStringToFile(frgFile, frgText);
					FileUtils.writeStringToFile(annFile, annText);
					
				}
				
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
