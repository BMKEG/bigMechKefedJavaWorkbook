package _09_pubmedAndPubmedCentral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class ESearchPubmedHandler extends DefaultHandler {

	String currentAttribute = "";
	String currentMatch = "";
	
	private int total;
	private int offset;
	
	String id = null;

	private List<Map<String,String>> entries = null;
	
	ArrayList<Exception> exceptions = new ArrayList<Exception>();

	public void startDocument() {
		setEntries(new ArrayList<Map<String,String>>());
	}

	public void endDocument() {
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += "." + qName;
		this.currentAttribute = attributes.getValue("IdType");
				
	}

	public void endElement(String uri, String localName, String qName) {
		
		if( this.currentMatch.endsWith(".Id") ) {
			if( id != null ) {

				Map<String, String> map = new HashMap<String, String>();
				map.put("id", id);
				getEntries().add(map);
				
			}
			
			id = null;
			
		}
		
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));
		
	}

	public void characters(char[] ch, int start, int length) {
				
		String value = new String(ch, start, length);

		try {

			if( this.currentMatch.endsWith(".Id") ) {
				id = value;
			}
			
			if( this.currentMatch.endsWith("eSearchResult.Count") ) {
				total = new Integer(value);
			}

			if( this.currentMatch.endsWith("eSearchResult.RetStart") ) {
				offset = new Integer(value);
			}
			
		} catch (Exception e) {

			this.exceptions.add(e);

		}

	}

	public int getTotal() {
		return total;
	}

	private void setTotal(int total) {
		this.total = total;
	}

	public int getOffset() {
		return offset;
	}

	private void setOffset(int offset) {
		this.offset = offset;
	}

	List<Map<String,String>> getEntries() {
		return entries;
	}

	void setEntries(List<Map<String,String>> entries) {
		this.entries = entries;
	}
	
}
