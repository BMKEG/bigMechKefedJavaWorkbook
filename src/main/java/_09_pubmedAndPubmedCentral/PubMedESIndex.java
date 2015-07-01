package _09_pubmedAndPubmedCentral;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

/**
 * A class to provide lookup objects of id-mappings from PubMedCentral and
 * Pubmed. This uses Elastic Search to create, store and query the index.
 * 
 * @author Gully
 */

public class PubMedESIndex {

	public static String FILE_LIST = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/file_list.txt";
	public static String PDFS = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/file_list.pdf.txt";
	public static String BASE = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc";
	public static String PMCIDS = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/PMC-ids.csv.gz";

	Pattern p1 = Pattern
			.compile("\\w{2}\\/\\w{2}\\/(.*?_\\d{4}_.*)\\.tar\\.gz$");
	Pattern p2 = Pattern.compile("\\w{2}\\/\\w{2}\\/(.*?)_\\d{4}_");

	Client esClient;

	private static Logger logger = Logger
			.getLogger(S09_02_RetrievePmcArticlesFromSearchList.class);

	public PubMedESIndex(File pmcRepo, File pmcFileListDir) throws IOException,
			ClassNotFoundException {

		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "pubmed").build();
		esClient = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"127.0.0.1", 9300));

		try {
			SearchRequestBuilder response = esClient.prepareSearch("pubmed");
		} catch (NoNodeAvailableException e) {
			throw e;
		}

		File f = new File(pmcFileListDir + "/" + readFilename(FILE_LIST));
		if (!f.exists()) {
			logger.info("Getting " + FILE_LIST);
			this.dumpToTextFile(FILE_LIST, f);
		}
		logger.info("Loading NXML information");

		CountResponse response = esClient.prepareCount("pubmed")
				.setQuery(termQuery("_type", "nxml")).execute()
				.actionGet();
		if (response.getCount() == 0) {
			buildXmlIndex(pmcRepo, f);
		}

		f = new File(pmcFileListDir + "/" + readFilename(PDFS));
		if (!f.exists()) {
			logger.info("Getting " + PDFS);
			this.dumpToTextFile(PDFS, f);
		}
		logger.info("Loading PDF information");
		response = esClient.prepareCount("pubmed")
				.setQuery(termQuery("_type", "pdf")).execute()
				.actionGet();
		if (response.getCount() == 0) {
			buildPdfMaps(pmcRepo, f);
		}

	}

	public PubMedESIndex() throws IOException, ClassNotFoundException {

		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "pubmed").build();
		esClient = new TransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(
						"127.0.0.1", 9300));

		try {
			SearchRequestBuilder response = esClient.prepareSearch("pubmed");
		} catch (NoNodeAvailableException e) {
			throw e;
		}

	}

	private void buildXmlIndex(File pmcRepo, File f)
			throws FileNotFoundException, IOException, ClassNotFoundException {

		BufferedReader in2 = new BufferedReader(new InputStreamReader(
				new FileInputStream(f)));
		String inputLine2;
		boolean notFirstLine = false;
		int i = 0, j = 0;
		while ((inputLine2 = in2.readLine()) != null) {
			String[] fields = inputLine2.split("\t");
			if (notFirstLine && fields.length == 4) {
				try {
					String pmcId = fields[2];
					String pmidStr = fields[3].replaceAll("PMID:", "");
					String tgz = fields[0];
					String citation = fields[1];
					Matcher m2 = p2.matcher(tgz);
					if (m2.find()) {
						String dirStem = m2.group(1);
						Matcher m1 = p1.matcher(tgz);
						if (m1.find()) {
							String xmlStem = m1.group(1);
							String xmlFilePath = dirStem + "/" + xmlStem
									+ ".nxml";
							IndexResponse response = esClient
									.prepareIndex("pubmed", "nxml",
											pmidStr)
									.setSource(
											jsonBuilder()
													.startObject()
													.field("pmid", pmidStr)
													.field("pmcId", pmcId)
													.field("citation", citation)
													.field("nxml_location", xmlFilePath)
													.endObject()).execute()
									.actionGet();
							i++;
						}
					}
				} catch (NumberFormatException e) {
					j++;
					// just ignore these, skip to the next.
				}
			} else {
				notFirstLine = true;
				j++;
			}
		}

		logger.info("Indexed " + i + " documents, " + j + " missed.");

		in2.close();

	}

	private void buildPdfMaps(File targetDir, File f)
			throws FileNotFoundException, IOException, ClassNotFoundException {

		BufferedReader in2 = new BufferedReader(new InputStreamReader(
				new FileInputStream(f)));
		String inputLine2;
		boolean notFirstLine = false;
		int i = 0, j = 0;
		while ((inputLine2 = in2.readLine()) != null) {
			String[] fields = inputLine2.split("\t");
			if (notFirstLine && fields.length == 4) {
				try {
					String pmcId = fields[2];
					String pmidStr = fields[3].replaceAll("PMID:", "");
					String pdf = BASE + "/" + fields[0];

					IndexResponse response = esClient
							.prepareIndex("pubmed", "pdf", pmidStr)
							.setSource(
									jsonBuilder().startObject()
											.field("pmid", pmidStr)
											.field("pmcId", pmcId)
											.field("pdf_location", pdf)
											.endObject()).execute().actionGet();
					i++;

				} catch (NumberFormatException e) {
					e.printStackTrace();
					j++;
					// just ignore these, skip to the next.
				}
			} else {
				notFirstLine = true;
				j++;
			}
		}

		logger.info("Indexed " + i + " documents, " + j + " missed.");

		in2.close();

	}

	private void dumpToTextFile(String urlString, File f) throws IOException {
		URL url = new URL(urlString);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f,
				true)));
		BufferedReader in2 = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine2;
		while ((inputLine2 = in2.readLine()) != null)
			out.println(inputLine2);
		in2.close();
		out.close();
	}

	private void dumpToUnzippedFile(String urlString, File f)
			throws IOException {
		byte[] buffer = new byte[1024];
		URL url = new URL(urlString);
		GZIPInputStream gzis = new GZIPInputStream(url.openStream());
		FileOutputStream os = new FileOutputStream(f);
		int len;
		while ((len = gzis.read(buffer)) > 0) {
			os.write(buffer, 0, len);
		}
		gzis.close();
		os.close();

	}

	private String readFilename(String s) {
		return s.substring(s.lastIndexOf("/") + 1, s.length());
	}

	public boolean hasEntry(String term, String value, String type) {

		CountResponse response = esClient.prepareCount("pubmed")
				.setQuery(QueryBuilders.matchQuery(term, value)).execute()
				.actionGet();

		if (response.getCount() > 0)
			return true;
		else
			return false;

	}
	
	public Map<String,Object> getMapFromTerm(String term, String value, String type) {

		SearchResponse response = esClient.prepareSearch("pubmed")
				.setTypes(type)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.matchQuery(term, value)).execute()
				.actionGet();

		Iterator<SearchHit> nxmlHits = response.getHits().iterator();
		if (nxmlHits.hasNext()) {
			SearchHit h = nxmlHits.next();
			return h.getSource();
		} else {
			return null;
		}

	}
}
