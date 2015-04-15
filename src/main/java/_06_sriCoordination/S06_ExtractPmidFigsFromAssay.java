package _06_sriCoordination;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
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
				required = true, metaVar = "ASSAY")
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
			BasicDBObject query = new BasicDBObject("assay.assay", options.assay);
			BasicDBObject select = new BasicDBObject("_id", 0)
					.append("assay.assay", 1)
					.append("source.pmid", 1)
					.append("source.figures", 1);
			
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
				out.println ( pmid + "\t" + figures );
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
