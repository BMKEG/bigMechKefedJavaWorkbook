package _12_elasticSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.isi.bmkeg.kefed.utils.json.JsonKefedExperiment;
import edu.isi.bmkeg.kefed.utils.json.JsonKefedModel;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S12_01_UploadJsonIntoES {

	public static class Options {

		@Option(name = "-jsonFile", usage = "Output File", required = true, metaVar = "JSON-FILE")
		public File jsonFile;

		@Option(name = "-isExpt", usage = "Is this an experiment?", required = true, metaVar = "IS-THIS-EXPT?")
		public Boolean isExpt;

	}

	private static Logger logger = Logger
			.getLogger(S12_01_UploadJsonIntoES.class);

	static String LOCAL_URL = "http://localhost:9200/";
	
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

		String urlString = LOCAL_URL + "kefed/"
				+ (options.isExpt?"data":"model");
		URL url = new URL(urlString);

		Set<String> names = new HashSet<String>();

		try {

			BufferedReader in = new BufferedReader(new FileReader(
					options.jsonFile));
			StringBuilder sb = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine + "\n");
			}
			in.close();

			String kefedJson = sb.toString();

			Settings settings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", "pubmed").build();
			Client esClient = new TransportClient(settings)
					.addTransportAddress(new InetSocketTransportAddress(
							"127.0.0.1", 9300));

			Gson gson = new Gson();
			JsonParser jParser = new JsonParser();
			JsonArray array = jParser.parse(kefedJson).getAsJsonArray();
			Iterator<JsonElement> it = array.iterator();
			while (it.hasNext()) {
				JsonElement je = it.next();
				
				JsonKefedModel model = gson.fromJson(je, JsonKefedModel.class);
							
				if(!options.isExpt && !model.get_type().equals("KefedModel"))
					continue;

				if(options.isExpt && !model.get_type().equals("KefedExperiment"))
					continue;

				model.setId( UUID.randomUUID().toString() );
				model.setUid( model.getId() );
				IndexResponse response = esClient
						.prepareIndex("kefed", (options.isExpt?"data":"model"), model.getUid())
						.setSource(je.toString()).execute().actionGet();
				
			}
			
		} catch (IOException e2) {

			if( !e2.getMessage().contains( "Server returned HTTP response code: 460 for URL" ) ) {				
				e2.printStackTrace();
				throw e2;
			}

		} catch (Exception e3) {

			e3.printStackTrace();
			throw e3;

		}

	}
}
