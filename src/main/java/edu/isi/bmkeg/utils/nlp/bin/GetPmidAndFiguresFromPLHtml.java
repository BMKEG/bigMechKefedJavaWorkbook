package edu.isi.bmkeg.utils.nlp.bin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.isi.bmkeg.utils.Converters;


/**
 * This command-line utility loads a file of OA PMC identifiers, loads their citations, 
 * downloads PDFs and XML data for each file
 * 
 * @author burns
 *
 */
public class GetPmidAndFiguresFromPLHtml {
	
	public static class Options {

		@Option(name = "-pmids", usage = "File of pmids to query", required = true, metaVar = "FILTER")
		public File filter;

		@Option(name = "-outFile", usage = "Output file", required = true, metaVar = "OUT")
		public File out;

		
	}

	private static Logger logger = Logger.getLogger(GetPmidAndFiguresFromPLHtml.class);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Options options = new Options();
		CmdLineParser parser = new CmdLineParser(options);
		
		try {
				
			parser.parseArgument(args);
			
			Pattern p2 = Pattern.compile("source: (\\d+)(.*)$");

			String datum = "";
			String assay = "";
			String pmid = "";
			String fig = "";

			Set<String> outSet = new HashSet<String>(); 

			String filterString = FileUtils.readFileToString(options.filter);

			for( String p : filterString.split("\n") ) {
		
				String html = retrievePathwayLogicDatumsForPmid(p);
				
				Document doc = Jsoup.parse(html);

				Elements bodyEls = doc.getElementsByClass("datum");
				for (Element bodyEl : bodyEls) {	
					
					assay = "";
					for (Node n : bodyEl.getElementsByClass("firstline")) {
						datum = ((Element) n).text();
						String[] datumSplit = datum.split("\\s+");
						assay = datumSplit[1];
					}			

					for (Node n : bodyEl.select("li")) {
						String text = ((Element) n).text();
						Matcher m2 = p2.matcher(text);
						if( m2.find() ) {
							pmid = m2.group(1);
							fig = m2.group(2);
							outSet.add( pmid + "\t" + fig + "\t" + assay + "\n" );
							break;
						}
					}
					
				}

			}
			
			List<String> outList = new ArrayList<String>(outSet);
			Collections.sort(outList);
			
			FileWriter fr = new FileWriter(options.out); 
			fr.write("pmid\tfig\tdatum\n");
			for( String l : outList ) {
				fr.write(l);
			}
			fr.close();
						
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

	private static String retrievePathwayLogicDatumsForPmid(String p)
			throws IOException {
		String url = "http://light.csl.sri.com/datum/search"; 
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(url);
		postMethod.addParameter("query[source][pmid]", p);
		postMethod.addParameter("authenticity_token", "BJfBbVDIIQE9yjqNW88I+pFR5jVwcnEHs1QmwZnDpPg=");
		try {
			httpClient.executeMethod(postMethod);
		} catch (HttpException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		        e.printStackTrace();
		}

		if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
			return postMethod.getResponseBodyAsString();
		} else {
			return null;
		}
	}
	
}
