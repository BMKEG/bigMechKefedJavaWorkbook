package _08_semanticWeb;

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

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.isi.bmkeg.kefed.utils.json.JsonKefedModel;
import edu.isi.bmkeg.kefed.utils.json.KefedFieldValueTemplate;
import edu.isi.bmkeg.kefed.utils.json.KefedObject;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S08_TranslateKefedDataToHDF5 {

	public static class Options {

		@Option(name = "-jsonKefedFile", usage = "KEfED File", required = true, metaVar = "KEFED_MODEL")
		public File kefedFile;

		@Option(name = "-hdf5File", usage = "Frame File", required = true, metaVar = "FRAME")
		public File frameFile;

	}

	private static Logger logger = Logger
			.getLogger(S08_TranslateKefedDataToHDF5.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();
		Pattern patt = Pattern.compile("(\\S+)___(\\S+)");

		Map<Integer, Set<String>> pmids = new HashMap<Integer, Set<String>>();
		Map<Integer, String> pmcids = new HashMap<Integer, String>();
		Map<String, String> pdfLocs = new HashMap<String, String>();

		CmdLineParser parser = new CmdLineParser(options);

		int nLapdfErrors = 0, nSwfErrors = 0, total = 0;

		try {

			parser.parseArgument(args);

			S08_TranslateKefedDataToHDF5 jsonToHDF5 = new S08_TranslateKefedDataToHDF5();
			JsonKefedModel jsonKefed = jsonToHDF5.readJsonKefedModel(options.kefedFile);			
			
			/* Port this to java. */
			Map<String, List<KefedObject>> vbLookup = new HashMap<String, List<KefedObject>>();
			Map<String, KefedObject> mLookup = new HashMap<String, KefedObject>();
			Map<String, KefedFieldValueTemplate> subTypeLookup = new HashMap<String, KefedFieldValueTemplate>();
			for (KefedObject ko : jsonKefed.getNodes()) {
				if (ko.getSpriteid().equals("Measurement Specification")) {
					vbLookup.put(ko.getUid(),
							jsonKefed.getDependOnsForMeasurement(ko));
					mLookup.put(ko.getUid(), ko);
				}
				if( ko.getValueType() != null ) {
					for( KefedFieldValueTemplate kfvt : 
							ko.getValueType().getMultipleSlotFields() ) {
						subTypeLookup.put(kfvt.getUid(), kfvt);						
					}
				}
				
			}

			Map<String, Object> df = new HashMap<String, Object>();
			df.put("name", jsonKefed.getModelName());
			df.put("type", jsonKefed.getType());

			Set<Entry<String, JsonElement>> jsonData = 
					jsonToHDF5.readJsonKefedData(options.kefedFile);
			if( jsonData == null ) 
				return;
			
			List<Map<String, Object>> mm = new ArrayList<Map<String, Object>>();
			df.put("measurements", mm);
			
			for( Map.Entry<String, JsonElement> hash: jsonData ) { 
				String uuid = hash.getKey();
				JsonArray table = hash.getValue().getAsJsonArray();

				Iterator<JsonElement> mObjIt = table.iterator();				
				while( mObjIt.hasNext() ) {
					JsonElement mObjEl = mObjIt.next();
					JsonObject mObj = mObjEl.getAsJsonObject();
					
					Map<String, Object> m = new HashMap<String, Object>();
					Map<String, Object> v = new HashMap<String, Object>();
					Map<String, Object> c = new HashMap<String, Object>();
					mm.add(m);
					m.put("uid", UUID.randomUUID().toString());
					m.put("context", c);

					List<KefedObject> params = vbLookup.get(uuid);
					for(KefedObject param : params) {
						
						JsonElement hash2 = mObj.get(param.getUid());
						if( hash2.isJsonPrimitive() ) {
						
							c.put(param.getNameValue(), hash2.getAsString() );							
						
						} else {
							
							Map<String, String> table2 = new HashMap<String, String>(); 
							c.put(param.getNameValue(), table2 );							
							
							JsonObject jHash2Obj = hash2.getAsJsonObject();
							for( Map.Entry<String, JsonElement> hash3: jHash2Obj.entrySet() ) {

								KefedFieldValueTemplate kfvt = subTypeLookup.get(hash3.getKey());
								String dataValue = hash3.getValue().getAsString();
								table2.put(kfvt.getNameValue(), dataValue);							
								
							}
							
						}
					
					}
					
					m.put("value", v);
					
					if( mObj.get(uuid).isJsonPrimitive() ) {
						
						v.put(mLookup.get(uuid).getNameValue(), mObj.get(uuid).getAsString());
				
					} else {
						
						Map<String, String> table2 = new HashMap<String, String>(); 
						v.put(mLookup.get(uuid).getNameValue(), table2 );							
						
						JsonObject jHash2Obj = mObj.get(uuid).getAsJsonObject();
						for( Map.Entry<String, JsonElement> hash3: jHash2Obj.entrySet() ) {

							KefedFieldValueTemplate kfvt = subTypeLookup.get(hash3.getKey());
							String dataValue = hash3.getValue().getAsString();
							table2.put(kfvt.getNameValue(), dataValue);							
							
						}
						
					}
					
					
					
				}
				
			}
			
			int file_id = -1;
			// Create a new file using default properties.
			try {
//				file_id = H5.H5Fcreate(FILENAME, HDF5Constants.H5F_ACC_TRUNC,
//						HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			// Close the file.
			try {
				if (file_id >= 0)
					H5.H5Fclose(file_id);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String json = gson.toJson(df);
			
			FileUtils.writeStringToFile(options.frameFile, json);		
			
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
				
		if( json.get("experimentData") != null ) {
			JsonObject data = json.get("experimentData").getAsJsonObject();
			return data.entrySet();
		} else {
			System.err.println(kefedFile.getPath() + " has no data.");
			return null;
		}
		
	
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
