package _11_recordLinkageInPathwayCommons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class PathwayCommonsSearchHandler extends DefaultHandler {

	String currentAttribute = "";
	String currentMatch = "";
	
	
	String id = null;

	private Set<String> names = null;
	
	ArrayList<Exception> exceptions = new ArrayList<Exception>();

	private int total;
	private int hitsPerPage;
	private int pageNo;

	public void startDocument() {
		setNames(new HashSet<String>());
	}

	public void endDocument() {
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += "." + qName;
		this.currentAttribute = attributes.getValue("IdType");
		
		if( this.currentMatch.endsWith(".searchResponse") ) {
			this.total = new Integer(attributes.getValue("numHits"));
			this.setHitsPerPage(new Integer(attributes.getValue("maxHitsPerPage")));
			this.setPageNo(new Integer(attributes.getValue("pageNo")));
		}				
	}

	public void endElement(String uri, String localName, String qName) {
		
		String c = this.currentMatch;
		this.currentMatch = c.substring(0, c.lastIndexOf("." + qName));
		
	}

	public void characters(char[] ch, int start, int length) {
				
		String value = new String(ch, start, length);

		try {

			if( this.currentMatch.endsWith(".searchHit.name") ) {
				getNames().add( value );
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

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		this.names = names;
	}

	public Integer getHitsPerPage() {
		return hitsPerPage;
	}

	public void setHitsPerPage(Integer hitsPerPage) {
		this.hitsPerPage = hitsPerPage;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
}
