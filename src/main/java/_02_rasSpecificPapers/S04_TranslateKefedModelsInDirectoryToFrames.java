package _02_rasSpecificPapers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import _02_rasSpecificPapers.S03_TranslateKefedModelsToFrames.Options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.isi.bmkeg.kefed.utils.json.JsonKefedModel;
import edu.isi.bmkeg.kefed.utils.json.KefedFieldValueTemplate;
import edu.isi.bmkeg.kefed.utils.json.KefedObject;
import edu.isi.bmkeg.utils.Converters;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S04_TranslateKefedModelsInDirectoryToFrames {

	public static class Options {

		@Option(name = "-jsonDir", usage = "KEfED Directory", required = true, metaVar = "KEFED_MODEL")
		public File kefedDir;

	}

	private static Logger logger = Logger
			.getLogger(S03_TranslateKefedModelsToDumpFiles.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);

		try {

			parser.parseArgument(args);

			Pattern patt = Pattern.compile("\\.json");
			Map<String, File> files = Converters.recursivelyListFiles(options.kefedDir, patt);
			for(File f: files.values()) {
				if(f.getName().endsWith( "_frame.json") ) {
					f.delete();
				}
			}

			files = Converters.recursivelyListFiles(options.kefedDir, patt);
			for(File f: files.values()) {
				
				String newPath = f.getPath().replace(".json", "_frame.json");
				
				String[] args2 = new String[] { 
						"-jsonKefedFile", f.getPath(),
						"-frameFile", newPath				
						};						
				S03_TranslateKefedModelsToFrames.main(args2);
	
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

	private JsonKefedModel readJsonKefedModel(File kefedFile)
			throws Exception {

		BufferedReader in = new BufferedReader(new FileReader(kefedFile));

		StringBuilder sb = new StringBuilder();

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine + "\n");
			// System.out.println(inputLine);
		}
		in.close();

		String kefedJson = sb.toString();

		List<JsonKefedModel> list = new ArrayList<JsonKefedModel>();

		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(kefedJson).getAsJsonObject();
		
		JsonKefedModel model = gson.fromJson(json, JsonKefedModel.class);
		
		return model;

	}

	private Set<Entry<String, JsonElement>> readJsonKefedData(File kefedFile)
			throws Exception {

		BufferedReader in = new BufferedReader(new FileReader(kefedFile));

		StringBuilder sb = new StringBuilder();

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine + "\n");
			// System.out.println(inputLine);
		}
		in.close();

		String kefedJson = sb.toString();

		List<JsonKefedModel> list = new ArrayList<JsonKefedModel>();

		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(kefedJson).getAsJsonObject();
				
		JsonObject data = json.get("experimentData").getAsJsonObject();
		
		return data.entrySet();
	
	}
	
	private String clean(String s) {

		s = s.replaceAll("\\n", " ");
		s = s.replaceAll("\\r", " ");
		s = s.replaceAll("\\s+$", "");
		s = s.replaceAll("\\s*\\(.*\\)\\s*$", "");
		s = s.replaceAll("-\\s*\\d+\\s*$", "");
		s = s.replaceAll("\\s+\\d+\\s*$", "");

		return s;

	}

}
