package _10_pipelineElements;

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
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S10_05_ProcessFigureLegends {

	public static class Options {

		@Option(name = "-inDir", usage = "Input Directory", required = true, metaVar = "IN-DIRECTORY")
		public File inDir;
		
		@Option(name = "-outFile", usage = "Output Directory", required = true, metaVar = "OUT-DIRECTORY")
		public File outFile;

		@Option(name = "-figLabelFile", usage = "Figure Label File", required = true, metaVar = "FIGURE-LABEL-FILE")
		public File figLabelFile;

	}

	private static Logger logger = Logger
			.getLogger(S10_05_ProcessFigureLegends.class);

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

		CollectionReader cr = CollectionReaderFactory.createCollectionReader(
				BioCCollectionReader.class, typeSystem,
				BioCCollectionReader.INPUT_DIRECTORY, options.inDir,
				BioCCollectionReader.PARAM_FORMAT, BioCCollectionReader.JSON);
		
		AggregateBuilder builder = new AggregateBuilder();

		builder.add(SentenceAnnotator.getDescription()); // Sentence
														// segmentation
		builder.add(TokenAnnotator.getDescription()); // Tokenization
	
		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				AddBioCPassagesAndAnnotationsToDocuments.class));

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				FigureCodeAnnotator.class));

		builder.add(AnalysisEngineFactory.createPrimitiveDescription(
				FigureCodeSummarizer.class,
				FigureCodeSummarizer.PARAM_OUT_FILE, options.outFile,
				FigureCodeSummarizer.PARAM_FIG_LABEL_FILE, options.figLabelFile));
				
		SimplePipeline.runPipeline(cr, builder.createAggregateDescription());

	}

}
