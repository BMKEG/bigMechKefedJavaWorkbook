package edu.isi.bmkeg.utils.nlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class XmlUtils {

	public static String convertPmcXmlToHtml(File pmcXmlFile) 
			throws IOException, TransformerException {

		if (!pmcXmlFile.exists())
			throw new FileNotFoundException(pmcXmlFile.getPath() + " does not exist");
		
		FileReader inputReader = new FileReader(pmcXmlFile);
		StringWriter outputWriter = new StringWriter();

		TransformerFactory tf = TransformerFactory.newInstance();

		// stylesheet
		Resource xslResource = new ClassPathResource(
				"jatsPreviewStyleSheets/xslt/main/jats-html.xsl");
		StreamSource xslt = new StreamSource(xslResource.getInputStream());
		Transformer transformer = tf.newTransformer(xslt);

		StreamSource source = new StreamSource(inputReader);
		StreamResult result = new StreamResult(outputWriter);
		transformer.transform(source, result);
		String html = outputWriter.toString();

		return html;
				
	}
	
	public static String convertPmcXmlToHtml(String pmcXml) 
			throws IOException, TransformerException {

		StringReader inputReader = new StringReader(pmcXml);
		StringWriter outputWriter = new StringWriter();

		TransformerFactory tf = TransformerFactory.newInstance();

		// stylesheet
		Resource xslResource = new ClassPathResource(
				"jatsPreviewStyleSheets/xslt/main/jats-html.xsl");
		StreamSource xslt = new StreamSource(xslResource.getInputStream());
		Transformer transformer = tf.newTransformer(xslt);

		StreamSource source = new StreamSource(inputReader);
		StreamResult result = new StreamResult(outputWriter);
		transformer.transform(source, result);
		String html = outputWriter.toString();

		return html;
				
	}
	
	public static void writePmcXmlToHtmlFile( File xmlFile, File htmlFile ) 
			throws IOException, TransformerException {

		String htmlText = convertPmcXmlToHtml(xmlFile);
		
		FileUtils.writeStringToFile(htmlFile, htmlText);
	
	}
		
	public static String convertPmcHtmlToTxt(File htmlFile) throws IOException {
	
		String html = FileUtils.readFileToString(htmlFile);
		
		return convertPmcHtmlToTxt(html);

	}
	
	public static String convertPmcHtmlToTxt(String html) throws IOException {
		
		Document doc = Jsoup.parse(html);

		String plainText = "";
		String title = "";
		String abstr = "";

		Elements bodyEls = doc.select("div");
		for (Element bodyEl : bodyEls) {
			for (Node n : bodyEl.getElementsByClass("document-title")) {
				title += ((Element)n).text();
			}
			for (Node n : bodyEl.select("tr")) {
				n.remove();
			}
			for (Node n : bodyEl.getElementsByClass("object-id")) {
				n.remove();
			}
			for (Node n : bodyEl.getElementsByClass("ref-list")) {
				if( n.parentNode() != null ) {
					n.remove();
				}
			}
			for (Node n : bodyEl.getElementsByClass("footnote")) {
				if( n.parentNode() != null ) {
					n.remove();
				}
			}
			for (Node n : bodyEl.getElementsByClass("metadata")) {
				if( n.parentNode() != null && !n.toString().contains("Abstract")) {
					n.remove();
				}
			}
			for (Node n : bodyEl.getElementsByClass("branding")) {
				if( n.parentNode() != null ) {
					n.remove();
				}
			}			
			for (Node n : bodyEl.select("a")) {
				addFormattingSuffixes((Element) n, "A");
			}
			for (Node n : bodyEl.select("i")) {
				addFormattingSuffixes((Element) n, "I");
			}
			for (Node n : bodyEl.select("b")) {
				addFormattingSuffixes((Element) n, "B");
			}
			for (Node n : bodyEl.select("sup")) {
				addFormattingSuffixes((Element) n, "SUP");
			}
			for (Node n : bodyEl.select("sub")) {
				addFormattingSuffixes((Element) n, "SUB");
			}
			for (Node n : bodyEl.select("h1")) {
				addFormattingSuffixes((Element) n, "H");
			}
			for (Node n : bodyEl.select("h2")) {
				addFormattingSuffixes((Element) n, "H");
			}
			for (Node n : bodyEl.select("h3")) {
				addFormattingSuffixes((Element) n, "H");
			}
			for (Node n : bodyEl.select("h4")) {
				addFormattingSuffixes((Element) n, "H");
			}
			for (Node n : bodyEl.select("p")) {
				addFormattingSuffixes((Element) n, "P");
			}
		}

		String t = doc.text();
		t = t.replaceAll("__s_H__", "\n");
		t = t.replaceAll("__s_P__", "");

		// put a period after headings for sentence detection
		t = t.replaceAll("__e_H__", ".\n");
		t = t.replaceAll("\\.\\s*\\.", ".");
		t = t.replaceAll("__e_P__", "\n") + "\n";

		plainText += t;
		plainText = title + plainText;

		return plainText;

	}
	
	public static void writePmcHtmlToTxtFile( File htmlFile, File txtFile ) throws IOException {

		String plainText = convertPmcHtmlToTxt(htmlFile);
		
		FileUtils.writeStringToFile(txtFile, plainText);
	
	}
	
	private static Element addFormattingSuffixes(Element el, String suffix) {
		String t = el.text();;
		String s = " __s_" + suffix + "__ ";
		String e = " __e_" + suffix + "__ ";
		
		if( t.indexOf("__s_") == -1  && t.length() > 0) {
			el.text( s + t + e );
		} 
		
		return el;
	}
	
}
