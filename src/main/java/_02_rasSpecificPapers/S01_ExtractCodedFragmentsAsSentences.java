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

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.isi.bmkeg.digitalLibrary.controller.DigitalLibraryEngine;

public class S01_ExtractCodedFragmentsAsSentences {

	public static class Options {

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outdir;
		
		@Option(name = "-corpus", usage = "Corpus", required = true, metaVar = "CORPUS")
		public String corpus = "";

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

	private static Logger logger = Logger.getLogger(S01_ExtractCodedFragmentsAsSentences.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();
		Pattern patt = Pattern.compile("(\\S+)-fig(\\S+)");
		
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

			String selectSql = "SELECT l.vpdmfId, a.pmid, f.name, frg.vpdmfId, frg.frgOrder, blk.vpdmfOrder, blk.text ";
			
			String fromWhereSql = "FROM LiteratureCitation AS l," +
					" ArticleCitation as a, FTD as f, " + 
					" FTDFragment as frg, FTDFragmentBlock as blk, " +
					" Corpus_corpora__resources_LiteratureCitation AS link, " + 
					" Corpus AS c " +
					" WHERE " +
					"blk.fragment_id = frg.vpdmfId AND " +
					"l.fullText_id = f.vpdmfId AND " +
					"l.vpdmfId = a.vpdmfId AND " +
					"frg.ftd_id = f.vpdmfId AND " +
					"link.resources_id=l.vpdmfId AND " +
					"link.corpora_id=c.vpdmfId AND " +
					"c.name = '"+ options.corpus+ "' AND " + 
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

			Map<String,String> hash = new HashMap<String,String>();
			
			while( rs.next() ) {

				Long citationId = rs.getLong("l.vpdmfId");
				String fileStem = rs.getString("f.name");
				int pmid = rs.getInt("a.pmid");
				String frgOrder = rs.getString("frg.frgOrder");
				String blkId = rs.getString("blk.vpdmfOrder");
				String text = rs.getString("blk.text");

				if( hash.containsKey(pmid + "-fig" + frgOrder) ) {
					String s = hash.get(pmid + "-fig" + frgOrder);
					s += text;
					hash.put(pmid + "-fig" + frgOrder, s);						
				} else {
					hash.put(pmid + "-fig" + frgOrder, text);						
				}
				
			}
			rs.close();

			//
			// now write these to files. 
			//
			List<String> fragments = new ArrayList<String>(hash.keySet());
			Collections.sort(fragments);

			String cPmid = "";
			String cCode = "";
			BufferedWriter writer = null;
			
			for(String s : fragments) {
			
				Matcher m = patt.matcher(s);
				if( m.find() ) {
					String pmid = m.group(1);
					String code = m.group(2);
					
					File output = new File(options.outdir.getPath() + "/" + pmid + "_ann.txt");
					if(!cPmid.equals(pmid) ) {
						if( writer != null )
							writer.close();
						writer = new BufferedWriter(new FileWriter(output));
						cPmid = pmid;
					}
					writer.write(code + "\t" + hash.get(s) + "\n" );	
				}
				
			}
			writer.close();

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
