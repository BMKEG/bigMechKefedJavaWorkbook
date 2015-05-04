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


import edu.isi.bmkeg.digitalLibrary.cleartk.annotators.SaveAsBioC;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S10_01_BuildBioCFromNxml2Txt {

	public static class Options {

		@Option(name = "-inDir", usage = "Input Directory", required = true, metaVar = "IN-DIRECTORY")
		public File inDir;

		@Option(name = "-corpus", usage = "Name of the collection", required = true, metaVar = "CORPUS")
		public String corpus;

		@Option(name = "-outDir", usage = "Output Directory", required = true, metaVar = "OUT-DIRECTORY")
		public File outDir;

		@Option(name = "-outFormat", usage = "Output Format", required = true, metaVar = "XML/JSON")
		public String outFormat;

	}

	private static Logger logger = Logger
			.getLogger(S10_01_BuildBioCFromNxml2Txt.class);

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

		if (!options.outDir.getParentFile().exists())
			options.outDir.getParentFile().mkdirs();

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription("bioc.TypeSystem");

		CollectionReader cr = CollectionReaderFactory.createCollectionReader(
				Nxml2TxtFilesCollectionReader.class, typeSystem,
				Nxml2TxtFilesCollectionReader.INPUT_DIRECTORY, options.inDir);

		AggregateBuilder builder = new AggregateBuilder();

		builder.add(SentenceAnnotator.getDescription()); // Sentence
															// segmentation
		builder.add(TokenAnnotator.getDescription()); // Tokenization

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

		// The simple document classification annotator
		String corpusName = options.corpus;

		corpusName = corpusName.replaceAll("\\s+", "_");
		corpusName = corpusName.replaceAll("\\/", "_");

		String outFormat = SaveAsBioCDocuments.JSON;
		if( options.outFormat.equals("XML") ) 
			outFormat = SaveAsBioCDocuments.XML;

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddBioCPassagesAndAnnotationsToDocuments.class));

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				SaveAsBioCDocuments.class, 
				SaveAsBioCDocuments.PARAM_FILE_PATH,
				options.outDir.getPath(),
				SaveAsBioCDocuments.PARAM_FORMAT,
				outFormat));

		SimplePipeline.runPipeline(cr, builder.createAggregateDescription());

	}

}
