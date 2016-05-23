package _15_friesIntegration;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bigmech.fries.FRIES_EntityMention;
import org.bigmech.fries.FRIES_EventMention;
import org.bigmech.fries.FRIES_Frame;
import org.bigmech.fries.FRIES_FrameCollection;
import org.bigmech.fries.FRIES_Passage;
import org.bigmech.fries.FRIES_Sentence;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class S15_01_readJsonFiles {

	public static class Options {

		@Option(name = "-inDir", usage = "Input Directory", required = true, metaVar = "IN-DIRECTORY")
		public File inDir;

		@Option(name = "-outFile", usage = "Output Directory", required = false, metaVar = "OUT-DIRECTORY")
		public File outFile;

		@Option(name = "-figLabelFile", usage = "Figure Label File", required = false, metaVar = "FIGURE-LABEL-FILE")
		public File figLabelFile;

	}

	private static Logger logger = Logger.getLogger(S15_01_readJsonFiles.class);

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

        final RuntimeTypeAdapterFactory<FRIES_Frame> typeFactory = RuntimeTypeAdapterFactory
                .of(FRIES_Frame.class, "frame-type")
                .registerSubtype(FRIES_EntityMention.class, "entity-mention")
                .registerSubtype(FRIES_Sentence.class, "sentence")
                .registerSubtype(FRIES_Passage.class, "passage")
                .registerSubtype(FRIES_EventMention.class, "event-mention");
		
		Gson gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.registerTypeAdapterFactory(typeFactory)
				.create();

		// First list the sentences
		String[] extensions = { "json" };
		Collection<File> frameFiles = FileUtils.listFiles(options.inDir, extensions, true);
		for (File f : frameFiles) {
			FRIES_FrameCollection fc = gson.fromJson(new FileReader(f), 
					FRIES_FrameCollection.class);	 
			
			
			
		}

	}

}
