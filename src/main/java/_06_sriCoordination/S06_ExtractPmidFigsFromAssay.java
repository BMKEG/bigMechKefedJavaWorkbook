package _06_sriCoordination;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * This script queries a locally running instance of the PL database in 
 * MongoDB and returns the pmids and figures of all entries of data in question.
 * We also permit the system to only return data that is in the OA list of papers.  
 * 
 * @author Gully
 *
 */
public class S06_ExtractPmidFigsFromAssay {

	public static class Options {

		@Option(name = "-assay", 
				usage = "Assay Code: http://pl.csl.sri.com/CurationNotebook/pages/_Assays.html", 
				required = false, metaVar = "ASSAY")
		public String assay;

		@Option(name = "-file_list", 
				usage = "Local address of file list from NLM: ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/file_list.txt", 
				required = false, metaVar = "OA_FILE_LIST")
		public File fileList;

		@Option(name = "-outFile", 
				usage = "Output file", 
				required = true, metaVar = "OUTPUT")
		public File outFile;
		
	}

	private static Logger logger = Logger.getLogger(S06_ExtractPmidFigsFromAssay.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
		Pattern patt = Pattern.compile("PMID:(\\d+)$");
		
		Map<String,String> types = new HashMap<String, String>();
		types.put("acetylation", "Modifies");
		types.put("boundby", "Binds");
		types.put("boundto", "Binds");
		types.put("cleavage", "Binds");
		types.put("colocwith", "Translocates");
		types.put("copptby", "Binds");
		types.put("dimerization", "Binds");
		types.put("Gal4-reporter", "Increases Activity");
		types.put("GDP-dissociation", "Increases Activity");
		types.put("GTP-association", "Increases Activity");
		types.put("GTP-bdpd", "Increases Activity");
		types.put("GTP-hydrolysis", "Increases Activity");
		types.put("GTP-percent", "Increases Activity");
		types.put("infraction", "Translocates");
		types.put("internalization", "Translocates");
		types.put("IVGefA", "Increases Activity");
		types.put("IVKA", "Modifies");
		types.put("IVLKA", "Modifies");
		types.put("LexA-reporter", "Increases Activity");
		types.put("locatedin", "Translocates");
		types.put("mRNA", "Increases");
		types.put("nuc-export", "Translocates");
		types.put("nuc-import", "Translocates");
		types.put("oligo-binding", "Binds");

		types.put("oligomerization", "Binds");
		types.put("phos", "Modifies");
		types.put("polymerization", "Binds");
		types.put("promo-reporter", "Increases Activity");
		types.put("prot-exp", "Increases");
		types.put("prot-stability", "Increases");
		types.put("secretion", "Translocates");
		types.put("snaggedby", "Translocates");
		types.put("Sphos", "Modifies");
		types.put("STphos", "Modifies");
		types.put("sumo", "Modifies");
		types.put("surface-exp", "Translocates");
		types.put("Tphos", "Modifies");
		types.put("ubiq", "Modifies");
		types.put("upshift", "Modifies");
		types.put("Yphos", "Modifies");		
			
		try {

			parser.parseArgument(args);
			
			Set<Integer> pmids = null;
			if(options.fileList != null ){
				pmids = new HashSet<Integer>();
				try (BufferedReader br = new BufferedReader(new FileReader(options.fileList))) {
				    String line;
				    while ((line = br.readLine()) != null) {
				    	Matcher m = patt.matcher(line);
				    	if( m.find() ) {
					    	pmids.add(new Integer(m.group(1)));			    		
				    	}
				    }
				}		
			}
			
			MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
			MongoDatabase db = mongoClient.getDatabase( "pathwaylogic" );
			MongoCollection<Document> datums = db.getCollection("datums");
			
			//{"assay.assay" : "copptby"}, {"_id":0, "assay.assay":1, "source.pmid":1, "source.figures":1}
			BasicDBObject query = new BasicDBObject();
			if( options.assay != null  )
				query = new BasicDBObject("assay.assay", options.assay);
			
			/*BasicDBObject select = new BasicDBObject("_id", 0)
					.append("assay.assay", 1)
					.append("source.pmid", 1)
					.append("source.figures", 1);*/
			
			options.outFile.delete();
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
					options.outFile, true)));
			
			MongoCursor<Document> cursor = datums.find(query).iterator();
			while( cursor.hasNext() ) {
				Document doc = cursor.next();
				Integer pmid = new Integer((String) ((Document) doc.get("source")).get("pmid"));
				if( options.fileList != null && !pmids.contains(pmid)) {
					continue;
				}
				Object figures = ((Document) doc.get("source")).get("figures");
				String assay = (String) ((Document) doc.get("assay")).get("assay");
				String type = types.get(assay);
				out.println ( type + "\t" + assay + "\t" + pmid + "\t" + figures );
			}
			
			out.close();
						
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
