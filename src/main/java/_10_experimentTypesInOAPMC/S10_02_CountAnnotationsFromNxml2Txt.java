package _10_experimentTypesInOAPMC;

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

import edu.isi.bmkeg.digitalLibrary.cleartk.annotators.AddBratAnnotations;
import edu.isi.bmkeg.digitalLibrary.cleartk.annotators.AddFragmentsAndCodes;
import edu.isi.bmkeg.digitalLibrary.cleartk.cr.DigitalLibraryCollectionReader;
import edu.isi.bmkeg.uimaBioC.uima.out.TabulateNestedBioCAnnotations;
import edu.isi.bmkeg.uimaBioC.uima.readers.Nxml2TxtFilesCollectionReader;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S10_02_CountAnnotationsFromNxml2Txt {

	public static class Options {

		@Option(name = "-inDir", usage = "Input Directory", required = true, metaVar = "IN-DIRECTORY")
		public File inDir;

		//~~~~~~~~~~~~~~~~~~~~~~~~~
		
		@Option(name = "-bratDir", usage = "Brat directory", required = true, metaVar = "LOGIN")
		public File bratDir;

		@Option(name = "-bratType", usage = "Brat directory", required = true, metaVar = "LOGIN")
		public File bratType;
		//~~~~~~~~~~~~~~~~~~~~~~~~~

		@Option(name = "-corpus", usage = "The target corpus to be evaluated", required = true)
		public String corpus = "";
		
		@Option(name = "-l", usage = "Database login", required = true, metaVar = "LOGIN")
		public String login = "";

		@Option(name = "-p", usage = "Database password", required = true, metaVar = "PASSWD")
		public String password = "";

		@Option(name = "-db", usage = "Database name", required = true, metaVar = "DBNAME")
		public String dbName = "";

		@Option(name = "-wd", usage = "Working directory", required = true, metaVar = "WDIR")
		public String workingDirectory = "";

		//~~~~~~~~~~~~~~~~~~~~~~~~~
		
		@Option(name = "-outFile", usage = "Output Directory", required = true, metaVar = "OUT-DIRECTORY")
		public File outFile;

		@Option(name = "-annType1", usage = "Output Format", required = true, metaVar = "XML/JSON")
		public String annType1;

		@Option(name = "-annType2", usage = "Output Format", required = true, metaVar = "XML/JSON")
		public String annType2;

	}

	private static Logger logger = Logger
			.getLogger(S10_02_CountAnnotationsFromNxml2Txt.class);

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
		
		if (!options.outFile.getParentFile().exists())
			options.outFile.getParentFile().mkdirs();

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription("bioc.TypeSystem");

		/*CollectionReader cr = CollectionReaderFactory.createCollectionReader(
				Nxml2TxtFilesCollectionReader.class, typeSystem,
				Nxml2TxtFilesCollectionReader.PARAM_INPUT_DIRECTORY, options.inDir);*/
		
		CollectionReader cr = CollectionReaderFactory
				.createCollectionReader(
						DigitalLibraryCollectionReader.class,
						typeSystem, DigitalLibraryCollectionReader.LOGIN,
						options.login,
						DigitalLibraryCollectionReader.PASSWORD,
						options.password,
						DigitalLibraryCollectionReader.DB_URL,
						options.dbName,
						DigitalLibraryCollectionReader.WORKING_DIRECTORY,
						options.workingDirectory,
						DigitalLibraryCollectionReader.CORPUS_NAME,
						options.corpus);

		AggregateBuilder builder = new AggregateBuilder();

		builder.add(SentenceAnnotator.getDescription()); // Sentence
														// segmentation
		builder.add(TokenAnnotator.getDescription()); // Tokenization

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddFragmentsAndCodes.class, 
				AddFragmentsAndCodes.LOGIN, options.login, 
				AddFragmentsAndCodes.PASSWORD, options.password,
				AddFragmentsAndCodes.DB_URL, options.dbName,
				AddFragmentsAndCodes.WORKING_DIRECTORY, options.workingDirectory,
				AddFragmentsAndCodes.FRAGMENT_TYPE, "epistSeg"));
		 
		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddBratAnnotations.class, 
				AddBratAnnotations.BRAT_DATA_DIRECTORY,
				options.bratDir,
				AddBratAnnotations.BRAT_TYPE,
				options.bratType ));
		 
		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddBioCPassagesAndAnnotationsToDocuments.class));

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				TabulateNestedBioCAnnotations.class, 
				TabulateNestedBioCAnnotations.PARAM_OUTER_ANNOTATION_TYPE,
				options.annType1,
				TabulateNestedBioCAnnotations.PARAM_FILE_PATH,
				options.outFile.getPath(),
				TabulateNestedBioCAnnotations.PARAM_INNER_ANNOTATION_TYPE,
				options.annType2));

		SimplePipeline.runPipeline(cr, builder.createAggregateDescription());

	}

}
