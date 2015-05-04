package _10_experimentTypesInOAPMC;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.snowball.DefaultSnowballStemmer;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.uimafit.factory.AggregateBuilder;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.SimplePipeline;


/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S10_02_SearchForWordsInPassages {

	public static class Options {

		@Option(name = "-inDir", usage = "Input Directory", required = true, metaVar = "IN-DIRECTORY")
		public File inDir;

		@Option(name = "-outFile", usage = "Output File", required = true, metaVar = "IN-DIRECTORY")
		public File outFile;

		@Option(name = "-words", usage = "Words to search for", required = true, metaVar = "SEARCH")
		public String wordList;

		@Option(name = "-passage", usage = "Passages to search in", required = true, metaVar = "SECTION")
		public String passage;

	}

	private static Logger logger = Logger
			.getLogger(S10_02_SearchForWordsInPassages.class);

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
				Nxml2TxtFilesCollectionReader.class, typeSystem,
				Nxml2TxtFilesCollectionReader.INPUT_DIRECTORY, options.inDir);

		AggregateBuilder builder = new AggregateBuilder();

		builder.add(SentenceAnnotator.getDescription()); // Sentence
		builder.add(TokenAnnotator.getDescription()); // Tokenization
	    builder.add(DefaultSnowballStemmer.getDescription("English"));

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddBioCPassagesAndAnnotationsToDocuments.class));

		
		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddBioCPassagesAndAnnotationsToDocuments.class));
		
		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				ExperimentTypeClassifier.class,
				ExperimentTypeClassifier.PARAM_OUTPUT_FILE, options.outFile));

		/*
		 * builder.add(AnalysisEngineFactory.createPrimitiveDescription(
		 * AddFragmentsAndCodes.class, AddFragmentsAndCodes.LOGIN,
		 * options.login, AddFragmentsAndCodes.PASSWORD, options.password,
		 * AddFragmentsAndCodes.DB_URL, options.dbName,
		 * AddFragmentsAndCodes.WORKING_DIRECTORY, options.workingDirectory,
		 * AddFragmentsAndCodes.FRAGMENT_TYPE, options.frgType ));
		 */

		/*
		 * builder.add(AnalysisEngineFactory.createPrimitiveDescription(
		 * AddBratAnnotations.class, AddBratAnnotations.BRAT_DATA_DIRECTORY,
		 * options.inBrat ));
		 */
	
		/*builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				SaveAsBioCDocuments.class, 
				SaveAsBioCDocuments.PARAM_FILE_PATH,
				options.outDir.getPath(),
				SaveAsBioCDocuments.PARAM_FORMAT,
				outFormat));*/

		SimplePipeline.runPipeline(cr, builder.createAggregateDescription());

	}

}
