package _16_figRefMethodExtractionFromIntact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import bioc.BioCAnnotation;

class SimpleXmlHandler extends DefaultHandler {

	boolean error = false;

	private List<SimpleIntactExperimentCode> intactData;
	private Map<Integer,SimpleIntactExperimentCode> experiments;
	
	BioCAnnotation currentAnnotation;
	String currentAttribute = "";
	String currentMatch = "";
	
	int pos = 0;
	String content = "";
	
	Pattern whiteReplace = Pattern.compile("\\s+");
			
	List<Exception> exceptions = new ArrayList<Exception>();
	private String interactionCode;
	private String participantCode;
	private String pmid;
	private Integer exptId;
	private String interactionLabel;
	private String participantLabel;
	
	private Integer lookupId;
	private String fig;

	private boolean figFlag;

	public void startDocument() {
		intactData = new ArrayList<SimpleIntactExperimentCode>();
		experiments = new HashMap<Integer,SimpleIntactExperimentCode>();
	}

	public void endDocument() {
		int pause = 0;
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += "." + qName;
		this.currentAttribute = attributes.getValue("IdType");
			
		if(currentMatch.endsWith("interactionDetectionMethod.xref.primaryRef")) {

			interactionCode = attributes.getValue("id");

		} else if(currentMatch.endsWith("participantIdentificationMethod.xref.primaryRef")) {
		
			participantCode = attributes.getValue("id");
		
		} else if(currentMatch.endsWith("experimentList.experimentDescription.bibref.xref.primaryRef")) {
		
			pmid = attributes.getValue("id");
		
		} else if(currentMatch.endsWith("experimentList.experimentDescription")) {
		
			exptId = new Integer(attributes.getValue("id"));
		
		}  else if(currentMatch.endsWith("attributeList.attribute")) {

			figFlag = false;
			for( int i=0; i<attributes.getLength(); i++) {
				if( attributes.getValue(i).equals("MI:0599") ){
					figFlag = true;
					break;
				}
			}
			
		} 
		
	}

	public void endElement(String uri, String localName, String qName) {

		if(currentMatch.endsWith("experimentList.experimentDescription")) {
			
			SimpleIntactExperimentCode intactRecord = new SimpleIntactExperimentCode();
			intactRecord.exptId = this.exptId;
			intactRecord.interactionCode = this.interactionCode;
			intactRecord.partipantCode = this.participantCode;
			intactRecord.pmid = this.pmid;
			intactRecord.interactionLabel = this.interactionLabel;
			intactRecord.participantLabel = this.participantLabel;
			intactData.add(intactRecord);
			experiments.put(exptId, intactRecord);

			this.exptId = null;
			this.interactionCode = null;
			this.participantCode = null;
			this.pmid = null;
			pos++;

		} else if(currentMatch.endsWith("interactionList.interaction")) {
			
			if( lookupId != null && experiments.containsKey(lookupId)) {
				SimpleIntactExperimentCode intactRecord = experiments.get(lookupId);
				intactRecord.figCode = fig;
			} else {
				int pause =0;
			}
		
		}
				
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));

	}

	public void characters(char[] ch, int start, int length) {
		
		String value = new String(ch, start, length);

		if(currentMatch.endsWith("interactionDetectionMethod.names.fullName")) {
			
			interactionLabel = value;
		
		} else if(currentMatch.endsWith("participantIdentificationMethod.names.fullName")) {
		
			participantLabel = value;
		
		} else if(currentMatch.endsWith("participantIdentificationMethod.names.fullName")) {
		
			participantLabel = value;
		
		} else if(currentMatch.endsWith("interactionList.interaction.experimentList.experimentRef")) {
			
			lookupId = new Integer(value);
		
		} else if(currentMatch.endsWith("attributeList.attribute") && figFlag) {
			
			fig = value;
		
		}
	 
	}

	public List<SimpleIntactExperimentCode> getIntactData() {
		return intactData;
	}

	public void setIntactData(List<SimpleIntactExperimentCode> intactData) {
		this.intactData = intactData;
	}
	
	
}
