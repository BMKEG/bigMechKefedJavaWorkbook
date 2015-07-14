package _13_connectToPdfs;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;

import edu.isi.bmkeg.uimaBioC.uima.readers.BioCCollectionReader;

/**
 * This script takes a card file and adds a single fragment to the database.  
 * 
 * @author Gully
 * 
 */
public class S13_01_AddBigMechCardFragments {

	public static class Options {

		@Option(name = "-cardDir", usage = "Directory of Big Mech Index Cards", 
				required = true, 
				metaVar = "CARD-DIR")
		public File cardDir;

		@Option(name = "-bioCFile", 
				usage = "Spatially Indexed BioC File", 
				required = true, 
				metaVar = "BIOC-DIR")
		public File bioCDir;
		
		@Option(name = "-l", usage = "Database login", required = true, metaVar = "LOGIN")
		public String login = "";

		@Option(name = "-p", usage = "Database password", required = true, metaVar = "PASSWD")
		public String password = "";

		@Option(name = "-db", usage = "Database name", required = true, metaVar = "DBNAME")
		public String dbName = "";

		@Option(name = "-wd", usage = "Working directory", required = true, metaVar = "WDIR")
		public String workingDirectory = "";
	}

	private static Logger logger = Logger
			.getLogger(S13_01_AddBigMechCardFragments.class);

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

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription("bioc.TypeSystem");

		CollectionReader cr = CollectionReaderFactory.createCollectionReader(
				BioCCollectionReader.class, typeSystem,
				BioCCollectionReader.INPUT_DIRECTORY, options.bioCDir.getPath(),
				BioCCollectionReader.PARAM_FORMAT, BioCCollectionReader.JSON);

		AggregateBuilder builder = new AggregateBuilder();

		builder.add(SentenceAnnotator.getDescription()); // Sentence
		builder.add(TokenAnnotator.getDescription());   // Tokenization
		
		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				BuildFragmentsFromBigMechIndexCards.class, 
				BuildFragmentsFromBigMechIndexCards.LOGIN, options.login,
				BuildFragmentsFromBigMechIndexCards.PASSWORD, options.password,
				BuildFragmentsFromBigMechIndexCards.DB_URL, options.dbName,
				BuildFragmentsFromBigMechIndexCards.WORKING_DIRECTORY, options.workingDirectory,
				BuildFragmentsFromBigMechIndexCards.CARD_DIRECTORY, options.cardDir.getPath()
				));

		SimplePipeline.runPipeline(cr, builder.createAggregateDescription());

		/*
		// 1. Parse the Index Card. 		
		JSONParser p = new JSONParser();
		Map json = (Map) p.parse(new FileReader(options.cardDir));
		String pmc_id = (String) json.get("pmc_id");
		String evidence = (String) json.get("evidence");

		// 2. Lookup the pmid from our data. 
		PubMedESIndex pmES = new PubMedESIndex();
		Map<String,Object> nxmlMap =  pmES.getMapFromTerm("pmcId", pmc_id, "nxml");
		String pmid = (String) nxmlMap.get("pmid");
		
		// 4. Set up BioScholar Database
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
				" ArticleCitation as a, FTD as f " + 
				" WHERE " +
				"l.fullText_id = f.vpdmfId AND " +
				"l.vpdmfId = a.vpdmfId AND " +
				"a.pmid = '"+ pmc_id + "' " + 
				" ORDER BY l.vpdmfId;";
			
		de.getDigLibDao().getCoreDao().getCe().connectToDB();
		
		ResultSet countRs = de.getDigLibDao().getCoreDao().getCe().executeRawSqlQuery(
				countSql + fromWhereSql);
		
		// 4. Insert fragment.
		 * 
		 */


	}

}
