package _05_kefedExtraction;

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
		
		@Option(name = "-pmid", usage = "Pmid", required = false, metaVar = "PMID")
		public int pmid = -1;

		@Option(name = "-frgType", usage = "FrgType", required = true, metaVar = "FRG_TYPE")
		public String frgType = "";

		@Option(name = "-frgCode", usage = "FrgCode", required = true, metaVar = "FRG_CODE")
		public String frgCode = "";
		
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
		Pattern p2 = Pattern.compile("^(\\D?)(\\d+)(\\D*)$");
				
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
					"frg.frgType = '"+ options.frgType + "' ";
			
			if(options.pmid != -1) {
				fromWhereSql += " AND a.pmid = '"+ options.pmid + "' ";
			}
			
			fromWhereSql += " ORDER BY l.vpdmfId, frg.vpdmfId, frg.frgOrder, blk.vpdmfOrder;";
				
			de.getDigLibDao().getCoreDao().getCe().connectToDB();
			
			ResultSet countRs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
					countSql + fromWhereSql);
			
			countRs.next();
			int count = countRs.getInt(1);
						
			countRs.close();
			
			ResultSet rs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
					selectSql + fromWhereSql);

			Map<Long,Map<String,String>> lookup = new HashMap<Long,Map<String,String>>();

			Map<String,Map<String,Map<String,String>>> pmidHash = 
					new HashMap<String,Map<String,Map<String,String>>>();
			
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
				
				Map<String,Map<String,String>> figHash = null;
				
				String pmidStr = pmid + "";
				if( !pmidHash.containsKey( pmidStr  ) ) {
					figHash = new HashMap<String,Map<String,String>>();
					pmidHash.put(pmidStr, figHash );
				} else {
					figHash = pmidHash.get( pmidStr);
				}
				
				// Parse the frgOrder code.
				// First split any '+' codes
				// then enumerate numbers for figures and ignore Supplemental data. 
				String[] splitCodes = frgOrder.split("\\+");
				for( String s : splitCodes) {
					
					Matcher m = p2.matcher(s);
					if( m.find() ) {
						
						String suppCode = m.group(1);
						String number = m.group(2);
						String letters = m.group(3);
						
						if( suppCode.equals("S") )
							continue;

						for(int i=0; i<letters.length(); i++) {
							String l = letters.substring(i, i+1);
							
							if(  !figHash.containsKey(number+l) ) {
								frgText = "";
								annText = "";
							} else {
								Map<String, String> map = figHash.get(number+l);
								frgText = map.get("txt");
								annText = map.get("ann");
							}
							
							frgText += blkText;
							
							int start = frgText.indexOf(blkText);
							int end = start + blkText.length() - 1;
							
							annText += "T"+ (j++) + "\t" + 
									blkCode.replaceAll(": ", "_") + " " + 
									start + " " +
									end + "\t" + 
									blkText +"\n";

							Map<String, String> map = new HashMap<String, String>();
							figHash.put( number+l , map);
							map.put("txt", frgText);
							map.put("ann", annText);
							
							pos += pos + blkText.length();
							
						}
				
					} else {
						
						logger.info("skipping fragment coded: " + pmid + "_" + s);
											
					}
				
				}
								
			}
			rs.close();

			//
			// now write these to files. 
			//
			List<String> pmids = new ArrayList<String>(pmidHash.keySet());
			Collections.sort(pmids);
			
			for(String pmid : pmids) {

				Map<String,Map<String,String>> codeHash = pmidHash.get(pmid);
				List<String> codes = new ArrayList<String>(codeHash.keySet());
				Collections.sort(codes);
					
				for( String code: codes ) {
										
					Map<String,String> h = codeHash.get(code);
					frgText = h.get("txt");
					annText = h.get("ann");
					
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
