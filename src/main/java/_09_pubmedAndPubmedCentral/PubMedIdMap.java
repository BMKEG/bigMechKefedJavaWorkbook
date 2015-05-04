package _09_pubmedAndPubmedCentral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

/**
 * A class to provide lookup objects of id-mappings from PubMedCentral and
 * Pubmed
 * 
 * @author Gully
 */

public class PubMedIdMap {

	public static String FILE_LIST = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/file_list.txt";
	public static String PDFS = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/file_list.pdf.txt";
	public static String BASE = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc";
	public static String PMCIDS = "ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/PMC-ids.csv.gz";

	Pattern p1 = Pattern
			.compile("\\w{2}\\/\\w{2}\\/(.*?_\\d{4}_.*)\\.tar\\.gz$");
	Pattern p2 = Pattern.compile("\\w{2}\\/\\w{2}\\/(.*?)_\\d{4}_");

	Map<Integer, String> pm_to_pmc;
	//Map<String, Integer> pmc_to_pm;
	Map<Integer, String> pm_to_xml;
	//Map<String, String> pmc_to_xml;
	Map<Integer, String> pm_to_pdf;
	//Map<String, String> pmc_to_pdf;

	private static Logger logger = Logger
			.getLogger(S09_02_RetrievePmcArticlesFromSearchList.class);

	public PubMedIdMap(File pmcRepo) throws IOException, ClassNotFoundException {

		File placeholder = new File(ClassLoader.getSystemResource(
				"placeholder.txt").getFile());
		File targetDir = placeholder.getParentFile();

		File f = new File(targetDir + "/" + readFilename(FILE_LIST));
		if (!f.exists()) {
			logger.info("Getting " + FILE_LIST);
			this.dumpToTextFile(FILE_LIST, f);
		}
		logger.info("Loading XML information");
		buildXmlMaps(targetDir, pmcRepo, f);

		f = new File(targetDir + "/" + readFilename(PDFS));
		if (!f.exists()) {
			logger.info("Getting " + PDFS);
			this.dumpToTextFile(PDFS, f);
		}
		logger.info("Loading PDF information");
		buildPdfMaps(targetDir, f);
		
		/*f = new File(targetDir + "/"
				+ readFilename(PMCIDS).replaceAll("\\.gz", ""));
		if (!f.exists()) {
			logger.info("Saving " + PMCIDS);
			this.dumpToUnzippedFile(PMCIDS, f);
		}
		logger.info("Loading mapping between PMC and PMID");
		buildIdMaps(targetDir, f);*/

	}

	public PubMedIdMap() throws IOException, ClassNotFoundException {

		File placeholder = new File(ClassLoader.getSystemResource(
				"placeholder.txt").getFile());
		File targetDir = placeholder.getParentFile();

		File f = new File(targetDir + "/" + readFilename(PDFS));
		if (!f.exists()) {
			logger.info("Getting " + PDFS);
			this.dumpToTextFile(PDFS, f);
		}
		logger.info("Loading PDF information");
		buildPdfMaps(targetDir, f);
	
	}
	
	private void buildXmlMaps(File targetDir, File pmcRepo, File f)
			throws FileNotFoundException, IOException, ClassNotFoundException {

		File f2 = new File(targetDir.getPath() + "/pm_to_xml.obj");

		if (!f2.exists()) {
			pm_to_xml = new HashMap<Integer, String>();
			//pmc_to_xml = new HashMap<String, String>();
	
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String inputLine2;
			boolean notFirstLine = false;
			while ((inputLine2 = in2.readLine()) != null) {
				String[] fields = inputLine2.split("\t");
				if (notFirstLine && fields.length == 4) {
					try {
						String pmcId = fields[2];
						String pmidStr = fields[3].replaceAll("PMID:", "");
						String tgz = fields[0];
						Matcher m2 = p2.matcher(tgz);
						if (m2.find()) {
							String dirStem = m2.group(1);
							File subdir = new File(pmcRepo.getPath() + "/"
									+ dirStem);
							Matcher m1 = p1.matcher(tgz);
							if (m1.find()) {
								String xmlStem = m1.group(1);
								File xmlFile = new File(subdir.getPath() + "/"
										+ xmlStem + ".nxml");
								pm_to_xml.put(new Integer(pmidStr),
										xmlFile.getPath());
								//pmc_to_xml.put(pmcId, xmlFile.getPath());
							}
						}
					} catch (NumberFormatException e) {
						// just ignore these, skip to the next.
					}
				} else {
					notFirstLine = true;
				}
			}
			
			in2.close();
			
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(targetDir.getPath() + "/pm_to_xml.obj"));
			out.writeObject(pm_to_xml);
	
			/*out = new ObjectOutputStream(new FileOutputStream(
					targetDir.getPath() + "/pmc_to_xml.obj"));
			out.writeObject(pmc_to_xml);*/
		
		} else {
			
			ObjectInputStream out = new ObjectInputStream(new FileInputStream(
					targetDir.getPath() + "/pm_to_xml.obj"));
			pm_to_xml = (Map<Integer, String>) out.readObject();
			out.close();

			/*out = new ObjectInputStream(new FileInputStream(targetDir.getPath()
					+ "/pmc_to_xml.obj"));
			pmc_to_xml = (Map<String, String>) out.readObject();
			out.close();*/
			
		}
		
	}


	private void buildPdfMaps(File targetDir, File f)
			throws FileNotFoundException, IOException, ClassNotFoundException {

		File f2 = new File(targetDir.getPath() + "/pm_to_pdf.obj");

		if (!f2.exists()) {
			pm_to_pdf = new HashMap<Integer, String>();
			//pmc_to_pdf = new HashMap<String, String>();
	
			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String inputLine2;
			boolean notFirstLine = false;
			while ((inputLine2 = in2.readLine()) != null) {
				String[] fields = inputLine2.split("\t");
				if (notFirstLine && fields.length == 4) {
					try {
						String pmcId = fields[2];
						String pmidStr = fields[3].replaceAll("PMID:", "");
						String pdf = BASE + "/" + fields[0];
						pm_to_pdf.put(new Integer(pmidStr), pdf);
						//pmc_to_pdf.put(pmcId, pdf);
					} catch (NumberFormatException e) {
						// just ignore these, skip to the next.
					}
				} else {
					notFirstLine = true;
				}
			}
			
			in2.close();
			
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(targetDir.getPath() + "/pm_to_pdf.obj"));
			out.writeObject(pm_to_pdf);
	
			/*out = new ObjectOutputStream(new FileOutputStream(
					targetDir.getPath() + "/pmc_to_pdf.obj"));
			out.writeObject(pmc_to_pdf);*/
		
		} else {
			
			ObjectInputStream out = new ObjectInputStream(new FileInputStream(
					targetDir.getPath() + "/pm_to_pdf.obj"));
			pm_to_pdf = (Map<Integer, String>) out.readObject();
			out.close();

			/*out = new ObjectInputStream(new FileInputStream(targetDir.getPath()
					+ "/pmc_to_pdf.obj"));
			pmc_to_pdf = (Map<String, String>) out.readObject();
			out.close();*/
			
		}
		
	}
	
	private void buildIdMaps(File targetDir, File f) throws FileNotFoundException,
			IOException, ClassNotFoundException {

		File f2 = new File(targetDir.getPath() + "/pm_to_pmc.obj");

		pm_to_pmc = new HashMap<Integer, String>();
		//pmc_to_pm = new HashMap<String, Integer>();

		if (!f2.exists()) {

			BufferedReader in2 = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String inputLine2;
			boolean notFirstLine = false;
			while ((inputLine2 = in2.readLine()) != null) {
				String[] fields = inputLine2.split(",");
				String pmidStr = fields[9];
				String pmcId = fields[8];
				if (notFirstLine) {
					try {
						pm_to_pmc.put(new Integer(pmidStr), pmcId);
						//pmc_to_pm.put(pmcId, new Integer(pmidStr));
					} catch (NumberFormatException e) {
						// just ignore these, skip to the next.
					}
				} else {
					notFirstLine = true;
				}
			}
			in2.close();

			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(targetDir.getPath() + "/pm_to_pmc.obj"));
			out.writeObject(pm_to_pmc);

			/*out = new ObjectOutputStream(new FileOutputStream(
					targetDir.getPath() + "/pmc_to_pm.obj"));
			out.writeObject(pmc_to_pm);*/

		} else {

			ObjectInputStream out = new ObjectInputStream(new FileInputStream(
					targetDir.getPath() + "/pm_to_pmc.obj"));
			pm_to_pmc = (Map<Integer, String>) out.readObject();
			out.close();

			/*out = new ObjectInputStream(new FileInputStream(targetDir.getPath()
					+ "/pmc_to_pm.obj"));
			pmc_to_pm = (Map<String, Integer>) out.readObject();
			out.close();*/

		}

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

}
