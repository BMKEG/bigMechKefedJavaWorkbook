package _02_rasSpecificPapers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.isi.bmkeg.kefed.utils.json.JsonKefedModel;
import edu.isi.bmkeg.kefed.utils.json.KefedFullValueTemplate;
import edu.isi.bmkeg.kefed.utils.json.KefedObject;
import edu.isi.bmkeg.ooevv.model.ExperimentalVariable;
import edu.isi.bmkeg.ooevv.model.OoevvElement;
import edu.isi.bmkeg.ooevv.model.OoevvEntity;
import edu.isi.bmkeg.ooevv.model.OoevvProcess;
import edu.isi.bmkeg.ooevv.model.scale.BinaryScale;
import edu.isi.bmkeg.ooevv.model.scale.BinaryScaleWithNamedValues;
import edu.isi.bmkeg.ooevv.model.scale.CompositeScale;
import edu.isi.bmkeg.ooevv.model.scale.DecimalScale;
import edu.isi.bmkeg.ooevv.model.scale.FileScale;
import edu.isi.bmkeg.ooevv.model.scale.HierarchicalScale;
import edu.isi.bmkeg.ooevv.model.scale.IntegerScale;
import edu.isi.bmkeg.ooevv.model.scale.MeasurementScale;
import edu.isi.bmkeg.ooevv.model.scale.NaturalLanguageScale;
import edu.isi.bmkeg.ooevv.model.scale.NominalScale;
import edu.isi.bmkeg.ooevv.model.scale.NominalScaleWithAllowedTerms;
import edu.isi.bmkeg.ooevv.model.scale.OrdinalScale;
import edu.isi.bmkeg.ooevv.model.scale.OrdinalScaleWithMaxRank;
import edu.isi.bmkeg.ooevv.model.scale.OrdinalScaleWithNamedRanks;
import edu.isi.bmkeg.ooevv.model.scale.RelativeTermScale;
import edu.isi.bmkeg.ooevv.model.scale.TimestampScale;
import edu.isi.bmkeg.ooevv.model.value.BinaryValue;
import edu.isi.bmkeg.ooevv.model.value.HierarchicalValue;
import edu.isi.bmkeg.ooevv.model.value.MeasurementValue;
import edu.isi.bmkeg.ooevv.model.value.NominalValue;
import edu.isi.bmkeg.ooevv.model.value.OrdinalValue;
import edu.isi.bmkeg.ooevv.model.value.RelativeValue;

/**
 * This script runs through serialized JSON files from the model and 
 * converts them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S03_TranslateKefedModelsToDumpFiles {

	public static class Options {

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outdir;

		@Option(name = "-jsonKefedFile", usage = "KEfED File", required = true, metaVar = "FRG_TYPE")
		public File kefedFile;

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
			.getLogger(S03_TranslateKefedModelsToDumpFiles.class);

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

			S03_TranslateKefedModelsToDumpFiles jsonToVpdmf = new S03_TranslateKefedModelsToDumpFiles();

			List<JsonKefedModel> jsonKefed = jsonToVpdmf
					.readJsonArrayFromFile(options.kefedFile);
			
			Map<String, OoevvElement> map = jsonToVpdmf.extractOoevvElementsFromJsonArray(jsonKefed);
			
			System.out.println("Entities");
			for(String key : map.keySet() ) {
				OoevvElement el = map.get(key);
				if( el instanceof OoevvEntity ) {
					System.out.println("\t" + el.getTermValue());					
				}
			}
			
			System.out.println("Processes");
			for(String key : map.keySet() ) {
				OoevvElement el = map.get(key);
				if( el instanceof OoevvProcess ) {
					System.out.println("\t" + el.getTermValue());					
				}
			}
			
			System.out.println("Variables");
			for(String key : map.keySet() ) {
				OoevvElement el = map.get(key);
				if( el instanceof ExperimentalVariable ) {
					System.out.println("\t" + el.getTermValue());					
				}
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

	private List<JsonKefedModel> readJsonArrayFromFile(File kefedFile)
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
		JsonArray array = parser.parse(kefedJson).getAsJsonArray();
		Iterator<JsonElement> it = array.iterator();
		while (it.hasNext()) {
			JsonElement je = it.next();
			JsonKefedModel model = gson.fromJson(je, JsonKefedModel.class);
			list.add(model);
		}

		return list;

	}

	private Map<String, OoevvElement> extractOoevvElementsFromJsonArray(List<JsonKefedModel> mList)
			throws Exception {

		//
		// Preliminary things that we may need to track. 
		//
		Map<String, OoevvElement> elMap = new HashMap<String, OoevvElement>();		
		Map<CompositeScale, List<String>> compositeScales = new HashMap<CompositeScale, List<String>>();
		Set<String> subVariables = new HashSet<String>();
		Map<String, ExperimentalVariable> exVbLookup = new HashMap<String, ExperimentalVariable>();
		Map<MeasurementValue, String> valueScaleLookup = new HashMap<MeasurementValue, String>();
		
		Iterator<JsonKefedModel> mIt = mList.iterator();
		while (mIt.hasNext()) {
			JsonKefedModel m = mIt.next();

			Set<String> paramValueSet = new HashSet<String>();

			Iterator<KefedObject> nIt = m.getNodes().iterator();
			while (nIt.hasNext()) {
				KefedObject n = nIt.next();

				String type = n.getSpriteid();
				String eName = clean(n.getNameValue());
				String shortTermId = eName.toLowerCase().replaceAll("\\s+", "_");

				if (type.equals("Experimental Object")) {
									
					OoevvEntity e = new OoevvEntity();
					e.setElementType("OoevvEntity");
					e.setDefinition(n.getNotes());
					e.setTermValue(eName);
					e.setShortTermId(shortTermId);
					elMap.put(eName, e);
					
				} else if (type.equals("Activity")) {

					OoevvProcess p = new OoevvProcess();
					p.setElementType("OoevvProcess");
					p.setDefinition(n.getNotes());
					p.setTermValue(eName);
					p.setShortTermId(shortTermId);
					
					elMap.put(eName, p);

				} else if (type.equals("Independent Variable Data") ||
						type.equals("Parameter Specification") ||
						type.equals("Dependent Variable Data") || 
						type.equals("Measurement Specification")) {

					ExperimentalVariable ev = new ExperimentalVariable();
					ev.setElementType("ExperimentalVariable");
					ev.setDefinition(n.getNotes());
					ev.setTermValue(eName);
					ev.setShortTermId(shortTermId);
					elMap.put(eName, ev);
							
					//
					// Build MeasurementScale 
					// from KefedFullValueTemplate.
					//
					if (n.getValueType() != null) {
						
						KefedFullValueTemplate kfvt = n.getValueType();
						String vtName = kfvt.getValueTypeName();
						
						//
						// TYPES FROM THE OLD INTERFACE
						//
						//   "True/False", 
						//   "Integer", "Decimal", "Decimal with units", 
						//   "Term", 
						//   "Text", "Text List", "Long Text", 
						//   "Region", 
						//   "Date", "Time", "DateTime", 
						//   "File", "Image"
						//
						MeasurementScale ms = null;
						if (vtName.equals("True/False")) {

							ms = new BinaryScale();

						} else if (vtName.equals("Integer")) {

							ms = new IntegerScale();

						} else if (vtName.equals("Decimal With Units") 
								|| vtName.equals("Decimal")) {

							ms = new DecimalScale();

						} else if (vtName.equals("Date")
								|| vtName.equals("Time")
								|| vtName.equals("DateTime")) {

							ms = new TimestampScale();
							TimestampScale tss = (TimestampScale) ms;
							//tss.setFormat(sValue);

						} else if (vtName.equals("Term") || 
								vtName.equals("Text")) {

							ms = new NominalScale();
						
						} else if (vtName.equals("Text List")) {

							ms = new NominalScaleWithAllowedTerms();

							NominalScaleWithAllowedTerms bswnv = (NominalScaleWithAllowedTerms) ms;

							/*Iterator<String> idIt = sValues.iterator();
							while (idIt.hasNext()) {
								String id = idIt.next();
								NominalValue av = (NominalValue) values.get(id);
								if (av != null)
									bswnv.getNVal().add(av);
							}*/

						/*} else if (sType.equals("OrdinalScale")) {

							ms = new OrdinalScale();

						} else if (sType.equals("OrdinalScaleWithMaxRank")) {

							ms = new OrdinalScaleWithMaxRank();
							OrdinalScaleWithMaxRank oswr = (OrdinalScaleWithMaxRank) ms;
							Iterator<String> idIt = sValues.iterator();
							while (idIt.hasNext()) {
								String id = idIt.next();
								Float temp = new Float(id);
								Long max = temp.longValue();
								oswr.setMaximumRank(max.intValue());
							}

						} else if (sType.equals("OrdinalScaleWithNamedRanks")) {

							ms = new OrdinalScaleWithNamedRanks();

							OrdinalScaleWithNamedRanks oswnr = (OrdinalScaleWithNamedRanks) ms;

							int r = 0;
							Iterator<String> idIt = sValues.iterator();
							while (idIt.hasNext()) {
								String id = idIt.next();
								OrdinalValue av = (OrdinalValue) values.get(id);
								if (av != null) {
									oswnr.getOVal().add(av);
									av.setRank(r);
									r++;
								}
							}

						}*/
							
						} else if (vtName.equals("Table")) {

							ms = new CompositeScale();

							CompositeScale mvcs = (CompositeScale) ms;

							//compositeScales.put(mvcs, sValues);
							//subVariables.addAll(sValues);

						} else if (vtName.equals("File") ||
								vtName.equals("Image") ) {

							ms = new FileScale();

							FileScale fs = (FileScale) ms;

							// Set the file extension in the first column
							/*if (sValues.size() > 0)
								fs.setSuffix(sValues.get(0));

							if (sValues.size() > 1)
								fs.setMimeType(sValues.get(1));*/

						} else {

							throw new Exception("Don't recognize " + vtName
									+ ", as a type of scale");

						}
						
						ms.setTermValue(vtName + "__scale");
						ev.setScale(ms);
						
					}
					
					/*if (n.getValueType() != null) {
						Iterator<String> vIt = n.getValueType()
								.getAllowedValues().iterator();
						while (vIt.hasNext()) {
							String v = vIt.next();
							paramValueSet.add(eName + "=" + v);
						}

						if (n.getValueType() != null
								&& n.getValueType().getMultipleSlotFields()
										.size() > 0) {
							Iterator<KefedFieldValueTemplate> fIt = n
									.getValueType().getMultipleSlotFields()
									.iterator();
							while (fIt.hasNext()) {
								KefedFieldValueTemplate f = fIt.next();
								paramSet.add(eName + "." + clean(f.getNameValue()));

								Iterator<String> v2It = f.getValueType()
										.getAllowedValues().iterator();
								while (v2It.hasNext()) {
									String v2 = v2It.next();
									paramValueSet.add(eName + "."
											+ clean(f.getNameValue()) + "="
											+ v2);
								}

							}

						}
					}*/

				} else {

					int pause = 0;
					pause++;

				}

			}
			
			int i=0;
			i++;

		}
		
		return elMap;

	}
	
	
	

/*	// ____________________________________________

	String vSheet = "OoEVV Values";
	Dimension vDim = this.getMatrixDimensions(vSheet);
	Map<String, Integer> ch = getColumnHeadings(vSheet);
	int nValues = vDim.height - 1;

	Map<String, MeasurementValue> values = new HashMap<String, MeasurementValue>();

	for (int i = 0; i < nValues; i++) {

		Integer idCol = ch.get("shortId");
		Integer scaleCol = ch.get("scale");
		Integer valCol = ch.get("value");
		Integer defCol = ch.get("definition");
		Integer typeCol = ch.get("type");
		Integer ontIdCol = ch.get("ontologyId");
		Integer curCol = ch.get("curator");

		if (idCol == null || valCol == null || defCol == null
				|| typeCol == null || curCol == null)
			throw new MalformedOoevvFileException(
					"Misnamed Value Definition column headings");

		String scale = this.getData(i + 1, scaleCol, vSheet);
		String vid = this.getData(i + 1, idCol, vSheet);
		String val = this.getData(i + 1, valCol, vSheet);
		String def = this.getData(i + 1, defCol, vSheet);
		String type = this.getData(i + 1, typeCol, vSheet);

		String ontIdStr = this.getData(i + 1, ontIdCol, vSheet);
		if (ontIdStr.lastIndexOf(".") != -1)
			ontIdStr = ontIdStr.substring(0, ontIdStr.lastIndexOf("."));

		String cur = this.getData(i + 1, curCol, vSheet);

		if (vid == null || vid.isEmpty())
			continue;

		// ____________________________________________
		// Read the spreadsheet and instantiate
		// OoEVV MeasurementValue objects appropriately
		//
		MeasurementValue mv = null;
		if (type.equals("BinaryValue")) {

			mv = new BinaryValue();

		} else if (type.equals("IntegerValue")) {

			mv = new IntegerValue();

			// how to deal with units?

		} else if (type.equals("DecimalValue")) {

			mv = new DecimalValue();

			// how to deal with units?

		} else if (type.equals("NominalValue")) {

			mv = new NominalValue();

		} else if (type.equals("OrdinalValue")) {

			mv = new OrdinalValue();

		} else if (type.equals("RelativeTermValue")) {

			mv = new RelativeValue();
			RelativeValue rts = (RelativeValue) mv;

		} else if (type.equals("HierarchicalValue")) {

			mv = new HierarchicalValue();
			HierarchicalValue hts = (HierarchicalValue) mv;

		} else {

			throw new Exception("Don't recognize " + type
					+ ", as a type of value");

		}

		mv.setTermValue(val);
		mv.setShortTermId(vid);
		mv.setDefinition(def);

		if (ontIdStr.length() > 0 && includeLookup) {

			Integer ontId = new Integer(ontIdStr);
			List<Ontology> ontHits = new ArrayList<Ontology>();
			List<Term> termHits = new ArrayList<Term>();
			Term vTerm = null;
			Ontology o = null;

			termHits = bps.termSearch(ontId, vid);
			ontHits = bps.ontologySearch(ontId);

			if (ontHits.size() == 1) {

				o = ontHits.get(0);

			} else {

				throw new Exception(ontId + " returns " + termHits.size()
						+ " results, should be a unique ontology.");

			}

			if (termHits.size() == 1) {

				vTerm = termHits.get(0);
				vTerm.setOntology(o);

			} else {

				throw new Exception(o.getDisplayName() + ":" + vid
						+ " returns " + termHits.size()
						+ " results, should be unique.");

			}

		}

		valueScaleLookup.put(mv, scale);

		if (vid == null || vid.length() == 0) {
			continue;
		}

		values.put(vid, mv);

	}

	// ____________________________________________

	sSheet = "OoEVV Scales";
	Dimension sDim = this.getMatrixDimensions(sSheet);
	int nScales = sDim.height - 1;

	Map<String, MeasurementScale> scales = new HashMap<String, MeasurementScale>();

	// detect column headings
	ch = getColumnHeadings(sSheet);

	for (int i = 0; i < nScales; i++) {

		// ____________________________________________
		// Read the spreadsheet.
		//
		String sid = this.getData(i + 1, ch.get("shortId"), sSheet);
		String sName = this.getData(i + 1, ch.get("name"), sSheet);
		String sDef = this.getData(i + 1, ch.get("definition"), sSheet);
		String sType = this.getData(i + 1, ch.get("type"), sSheet);
		String sUnits = this.getData(i + 1, ch.get("units"), sSheet);
		String sCurator = this.getData(i + 1, ch.get("curator"), sSheet);

		if (sName.length() == 0)
			continue;

		List<String> sValues = new ArrayList<String>();
		int valCol = ch.get("values");
		String sValue = this.getData(i + 1, valCol, sSheet);
		while (sValue.length() > 0 && valCol < sDim.width) {
			sValues.add(sValue);
			valCol++;
			if (valCol < sDim.width)
				sValue = this.getData(i + 1, valCol, sSheet);
		}

		// ____________________________________________
		// Read the spreadsheet and instantiate
		// an OoEVV scale appropriately
		//
		MeasurementScale ms = null;
		if (sType.equals("BinaryScale")) {

			ms = new BinaryScale();

		} else if (sType.equals("BinaryScaleWithNamedValues")) {

			ms = new BinaryScaleWithNamedValues();
			BinaryScaleWithNamedValues bswnv = (BinaryScaleWithNamedValues) ms;

			if (sValues.size() != 2)
				throw new Exception(
						"Please specify named values for BinaryScaleWithNamedValues:"
								+ sName);

			BinaryValue trueValue = (BinaryValue) values
					.get(sValues.get(0));
			trueValue.setBinaryValue(true);

			BinaryValue falseValue = (BinaryValue) values.get(sValues
					.get(1));
			falseValue.setBinaryValue(false);

			if (trueValue == null || falseValue == null)
				throw new Exception(
						"Nulls in true / false values for BinaryScaleWithNamedValues:"
								+ sName);

			bswnv.setTrueValue(trueValue);
			bswnv.setFalseValue(falseValue);

		} else if (sType.equals("IntegerScale")) {

			ms = new IntegerScale();

			// how to deal with units?

		} else if (sType.equals("DecimalScale")) {

			ms = new DecimalScale();

			// how to deal with units?

		} else if (sType.equals("TimestampScale")) {

			ms = new TimestampScale();
			TimestampScale tss = (TimestampScale) ms;
			tss.setFormat(sValue);

		} else if (sType.equals("NaturalLanguageScale")) {

			ms = new NaturalLanguageScale();

		} else if (sType.equals("NominalScale")) {

			ms = new NominalScale();

		} else if (sType.equals("NominalScaleWithAllowedTerms")) {

			ms = new NominalScaleWithAllowedTerms();

			NominalScaleWithAllowedTerms bswnv = (NominalScaleWithAllowedTerms) ms;

			Iterator<String> idIt = sValues.iterator();
			while (idIt.hasNext()) {
				String id = idIt.next();
				NominalValue av = (NominalValue) values.get(id);
				if (av != null)
					bswnv.getNVal().add(av);
			}

		} else if (sType.equals("OrdinalScale")) {

			ms = new OrdinalScale();

		} else if (sType.equals("OrdinalScaleWithMaxRank")) {

			ms = new OrdinalScaleWithMaxRank();
			OrdinalScaleWithMaxRank oswr = (OrdinalScaleWithMaxRank) ms;
			Iterator<String> idIt = sValues.iterator();
			while (idIt.hasNext()) {
				String id = idIt.next();
				Float temp = new Float(id);
				Long max = temp.longValue();
				oswr.setMaximumRank(max.intValue());
			}

		} else if (sType.equals("OrdinalScaleWithNamedRanks")) {

			ms = new OrdinalScaleWithNamedRanks();

			OrdinalScaleWithNamedRanks oswnr = (OrdinalScaleWithNamedRanks) ms;

			int r = 0;
			Iterator<String> idIt = sValues.iterator();
			while (idIt.hasNext()) {
				String id = idIt.next();
				OrdinalValue av = (OrdinalValue) values.get(id);
				if (av != null) {
					oswnr.getOVal().add(av);
					av.setRank(r);
					r++;
				}
			}

		} else if (sType.equals("RelativeTermScale")) {

			ms = new RelativeTermScale();
			RelativeTermScale rts = (RelativeTermScale) ms;

			Iterator<String> idIt = sValues.iterator();
			while (idIt.hasNext()) {
				String id = idIt.next();
				RelativeValue ar = (RelativeValue) values.get(id);
				// NEED TO DO THIS A DIFFERENT WAY.
				// if (ar != null)
				// rts.getAllowedRelations().add(ar);
			}

		} else if (sType.equals("HierarchicalScale")) {

			ms = new HierarchicalScale();

			HierarchicalScale hts = (HierarchicalScale) ms;

			Iterator<String> idIt = sValues.iterator();
			while (idIt.hasNext()) {
				String id = idIt.next();
				HierarchicalValue av = (HierarchicalValue) values.get(id);
				if (av != null)
					hts.getHValues().add(av);
			}

		} else if (sType.equals("CompositeScale")) {

			ms = new CompositeScale();

			CompositeScale mvcs = (CompositeScale) ms;

			compositeScales.put(mvcs, sValues);
			subVariables.addAll(sValues);

		} else if (sType.equals("FileScale")) {

			ms = new FileScale();

			FileScale fs = (FileScale) ms;

			// Set the file extension in the first column
			if (sValues.size() > 0)
				fs.setSuffix(sValues.get(0));

			if (sValues.size() > 1)
				fs.setMimeType(sValues.get(1));

		} else {

			throw new Exception("Don't recognize " + sType
					+ ", as a type of scale");

		}

		ms.setClassType(sType);

		ms.setTermValue(sName);
		ms.setShortTermId(sid);
		ms.setDefinition(sDef);

		scales.put(sid, ms);

	}

	vSheet = "OoEVV Variables";
	vDim = this.getMatrixDimensions(vSheet);
	ch = getColumnHeadings(vSheet);
	int nVariables = vDim.height - 1;

	Pattern whitespacePattern = Pattern.compile("\\s+");
	Pattern slashPattern = Pattern.compile("\\/");
	Pattern percentPattern = Pattern.compile("\\%");

	for (int i = 0; i < nVariables; i++) {

		Integer idCol = ch.get("shortId");
		Integer nameCol = ch.get("name");
		Integer defCol = ch.get("definition");
		Integer termCol = ch.get("measures");
		Integer scaleCol = ch.get("scale");
		Integer comCol = ch.get("comments");
		Integer curCol = ch.get("curator");
		Integer ontIdCol = ch.get("ontologyId");

		if (idCol == null || nameCol == null || defCol == null
				|| termCol == null || scaleCol == null || comCol == null
				|| curCol == null)
			throw new Exception(
					"Misnamed Variable Definition column headings");

		String vid = this.getData(i + 1, idCol, vSheet);

		// trim leading and trailing whitespace
		vid = vid.replaceAll("(\\s+)$", "");
		vid = vid.replaceAll("^(\\s+)", "");

		String vName = this.getData(i + 1, nameCol, vSheet);
		String vDef = this.getData(i + 1, defCol, vSheet);
		String vMeasures = this.getData(i + 1, termCol, vSheet);
		String vScaleName = this.getData(i + 1, scaleCol, vSheet);
		String vComments = this.getData(i + 1, comCol, vSheet);
		String vCurator = this.getData(i + 1, curCol, vSheet);

		String ontIdStr = this.getData(i + 1, ontIdCol, vSheet);
		if (ontIdStr.lastIndexOf(".") != -1)
			ontIdStr = ontIdStr.substring(0, ontIdStr.lastIndexOf("."));

		if (vName.length() == 0)
			continue;

		Matcher m1 = whitespacePattern.matcher(vid);
		/*
		 * if( m1.find() ) { throw new BadlyFormedTermNameException(
		 * "White space is not allowed in term names"); }
		 * 
		 * Matcher m2 = percentPattern.matcher(vid); if( m2.find() ) { throw
		 * new BadlyFormedTermNameException(
		 * "Percentage signs are not allowed in term names"); }
		 * 
		 * Matcher m3 = slashPattern.matcher(vid); if( m2.find() ) { throw
		 * new BadlyFormedTermNameException(
		 * "Slash signs are not allowed in term names"); }
		 *

		Term measures = null;
		if (ontIdStr.length() > 0 && includeLookup) {

			Integer ontId = new Integer(ontIdStr);
			List<Ontology> ontHits = new ArrayList<Ontology>();
			List<Term> measureHits = new ArrayList<Term>();
			try {
				measureHits = bps.termSearch(ontId, vMeasures);
				ontHits = bps.ontologySearch(ontId);
			} catch (IOException e) {
				log.debug("Bioportal seems to be down");
			}

			if (measureHits.size() == 1) {

				measures = measureHits.get(0);
				Ontology o = ontHits.get(0);
				measures.setOntology(o);

			} else {

				throw new Exception(vMeasures + " returns "
						+ measureHits.size()
						+ " results, should be unique.");

			}

		}

		ExperimentalVariable v = new ExperimentalVariable();
		v.setElementType("ExperimentalVariable");
		if (subVariables.contains(vid)) {
			SubVariable subV = new SubVariable();
			v = subV;
			v.setElementType("SubVariable");
		}
		v.setTermValue(vName);
		v.setShortTermId(vid);
		v.setDefinition(vDef);
		v.setMeasures(measures);

		if (vScaleName.length() != 0 && !vScaleName.equals("-")) {
			MeasurementScale ms = scales.get(vScaleName);
			if (ms == null)
				throw new Exception("Can't find scale named '" + vScaleName
						+ "'!");
			v.setScale(ms);
		}

		v.getOoevvSet().add(this.exptVbSet);
		this.exptVbSet.getOoevvEls().add(v);

		exVbLookup.put(v.getShortTermId(), v);

	}

	vSheet = "OoEVV Processes";
	vDim = this.getMatrixDimensions(vSheet);
	ch = getColumnHeadings(vSheet);
	int nProcesses = vDim.height - 1;

	for (int i = 0; i < nProcesses; i++) {

		Integer idCol = ch.get("shortId");
		Integer nameCol = ch.get("name");
		Integer defCol = ch.get("definition");
		Integer termCol = ch.get("obi");
		Integer comCol = ch.get("comments");
		Integer curCol = ch.get("curator");

		if (idCol == null || nameCol == null || defCol == null
				|| termCol == null || comCol == null || curCol == null)
			throw new Exception(
					"Misnamed Variable Definition column headings");

		String vid = this.getData(i + 1, idCol, vSheet);

		// trim leading and trailing whitespace
		vid = vid.replaceAll("(\\s+)$", "");
		vid = vid.replaceAll("^(\\s+)", "");

		String vName = this.getData(i + 1, nameCol, vSheet);
		String vDef = this.getData(i + 1, defCol, vSheet);
		String vObi = this.getData(i + 1, termCol, vSheet);
		String vComments = this.getData(i + 1, comCol, vSheet);
		String vCurator = this.getData(i + 1, curCol, vSheet);

		if (vName.length() == 0)
			continue;

		Matcher m1 = whitespacePattern.matcher(vid);
		/*
		 * if( m1.find() ) { throw new BadlyFormedTermNameException(
		 * "White space is not allowed in term names"); }
		 * 
		 * Matcher m2 = percentPattern.matcher(vid); if( m2.find() ) { throw
		 * new BadlyFormedTermNameException(
		 * "Percentage signs are not allowed in term names"); }
		 * 
		 * Matcher m3 = slashPattern.matcher(vid); if( m2.find() ) { throw
		 * new BadlyFormedTermNameException(
		 * "Slash signs are not allowed in term names"); }
		 *

		Term obi = null;
		if (vObi.length() > 0 && includeLookup) {

			Integer ontId = new Integer(1123);
			List<Ontology> ontHits = new ArrayList<Ontology>();
			List<Term> measureHits = new ArrayList<Term>();
			try {
				measureHits = bps.termSearch(ontId, vObi);
				ontHits = bps.ontologySearch(ontId);
			} catch (IOException e) {
				log.debug("Bioportal seems to be down");
				continue;
			}

			if (measureHits.size() == 1) {

				obi = measureHits.get(0);
				Ontology o = ontHits.get(0);
				obi.setOntology(o);

			} else {

				throw new Exception(vObi + " returns " + measureHits.size()
						+ " results, should be unique.");

			}

		}

		OoevvProcess v = new OoevvProcess();
		v.setElementType("OoevvProcess");
		v.setTermValue(vName);
		v.setShortTermId(vid);
		v.setDefinition(vDef);

		v.setObiTerm(obi);

		v.getOoevvSet().add(this.exptVbSet);
		this.exptVbSet.getOoevvEls().add(v);

	}

	vSheet = "OoEVV Entities";
	vDim = this.getMatrixDimensions(vSheet);
	ch = getColumnHeadings(vSheet);
	int nEntities = vDim.height - 1;

	for (int i = 0; i < nEntities; i++) {

		Integer idCol = ch.get("shortId");
		Integer nameCol = ch.get("name");
		Integer defCol = ch.get("definition");
		Integer termCol = ch.get("obi");
		Integer comCol = ch.get("comments");
		Integer curCol = ch.get("curator");

		if (idCol == null || nameCol == null || defCol == null
				|| termCol == null || comCol == null || curCol == null)
			throw new Exception(
					"Misnamed Variable Definition column headings");

		String vid = this.getData(i + 1, idCol, vSheet);

		// trim leading and trailing whitespace
		vid = vid.replaceAll("(\\s+)$", "");
		vid = vid.replaceAll("^(\\s+)", "");

		String vName = this.getData(i + 1, nameCol, vSheet);
		String vDef = this.getData(i + 1, defCol, vSheet);
		String vObi = this.getData(i + 1, termCol, vSheet);
		String vComments = this.getData(i + 1, comCol, vSheet);
		String vCurator = this.getData(i + 1, curCol, vSheet);

		if (vName.length() == 0)
			continue;

		Matcher m1 = whitespacePattern.matcher(vid);
		/*
		 * if( m1.find() ) { throw new BadlyFormedTermNameException(
		 * "White space is not allowed in term names"); }
		 * 
		 * Matcher m2 = percentPattern.matcher(vid); if( m2.find() ) { throw
		 * new BadlyFormedTermNameException(
		 * "Percentage signs are not allowed in term names"); }
		 * 
		 * Matcher m3 = slashPattern.matcher(vid); if( m2.find() ) { throw
		 * new BadlyFormedTermNameException(
		 * "Slash signs are not allowed in term names"); }
		 *

		Term obi = null;
		if (vObi.length() > 0 && includeLookup) {

			Integer ontId = new Integer(1123);
			List<Ontology> ontHits = new ArrayList<Ontology>();
			List<Term> measureHits = new ArrayList<Term>();
			try {
				measureHits = bps.termSearch(ontId, vObi);
				ontHits = bps.ontologySearch(ontId);
			} catch (IOException e) {
				log.debug("Bioportal seems to be down");
			}

			if (measureHits.size() == 1) {

				obi = measureHits.get(0);
				Ontology o = ontHits.get(0);
				obi.setOntology(o);

			} else {

				throw new Exception(vObi + " returns " + measureHits.size()
						+ " results, should be unique.");

			}

		}

		OoevvEntity e = new OoevvEntity();
		e.setElementType("OoevvEntity");
		e.setTermValue(vName);
		e.setShortTermId(vid);
		e.setDefinition(vDef);
		e.getOoevvSet().add(this.exptVbSet);
		e.setObiTerm(obi);

		this.exptVbSet.getOoevvEls().add(e);

	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Now that we have a good list of variables constructed,
	// we can link the multi-variable scales.
	//
	Pattern patt = Pattern.compile("^\\w+$");
	Iterator<CompositeScale> compIt = compositeScales.keySet().iterator();
	SCALE: while (compIt.hasNext()) {
		CompositeScale mvcs = compIt.next();
		List<String> subVbList = compositeScales.get(mvcs);
		for (String subVbId : subVbList) {

			Matcher match = patt.matcher(subVbId);
			if (match.find() || subVbId.length() == 0) {
				continue SCALE;
			}

			if (!exVbLookup.containsKey(subVbId)) {
				throw new Exception("Can't find " + subVbId
						+ " in specification of "
						+ "MultiVariableCompositeScale: "
						+ mvcs.getShortTermId());
			} else {
				ExperimentalVariable ev = exVbLookup.get(subVbId);
				SubVariable sv = (SubVariable) ev;
				mvcs.getHasParts().add(sv);
				sv.getPartOf().add(mvcs);
			}
		}
	}

	return this.exptVbSet;

	}*/
	
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
