package _09_pubmedAndPubmedCentral;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


import edu.isi.bmkeg.utils.Converters;

/**
 * This script runs through serialized JSON files from the model and converts
 * them to VPDMf KEfED models, including the data.
 * 
 * @author Gully
 * 
 */
public class S09_05_SimpleRunNxmlToText {

	public static class Options {

		@Option(name = "-inDir", usage = "Input Directory", required = true, metaVar = "INPUT")
		public File inDir;

		@Option(name = "-outDir", usage = "Output", required = true, metaVar = "OUTPUT")
		public File outDir;

		@Option(name = "-execPath", usage = "Path to the nxml2text executable", required = true, metaVar = "OUTPUT")
		public File execPath;

	}

	private static Logger logger = Logger
			.getLogger(S09_05_SimpleRunNxmlToText.class);

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

		if( !options.outDir.exists() )
			options.outDir.mkdirs();
				
		List<ExecWorker> workers = new ArrayList<ExecWorker>();

		Map<String, File> nxmlFiles = Converters.recursivelyListFiles(
				options.inDir,
				Pattern.compile("\\.nxml"));
		
		for(File f : nxmlFiles.values()) {
			
			String newPath = f.getPath().replaceAll(
					options.inDir.getPath(),
					options.outDir.getPath());
			File txtFile = new File(newPath.replaceAll("\\.nxml$", ".txt"));
			File annFile = new File(newPath.replaceAll("\\.nxml$", ".so"));
			File logFile = new File(newPath.replaceAll("\\.nxml$", "_nxml2txt.log"));
			txtFile.getParentFile().mkdirs();
			
			String command = options.execPath.getPath() + " " + f.getPath() 
					+ " " + txtFile.getPath()
					+ " " + annFile.getPath() + "";

			ProcessBuilder pb = new ProcessBuilder(command.split(" "));
			Map<String,String> env = pb.environment();
			env.put("PYTHONPATH", "/usr/local/lib/python2.7/site-packages");
			Process p = pb.start();
			
			if (p == null) {
				throw new Exception("Can't execute " + command);
			}

			InputStream in = p.getErrorStream();
			BufferedInputStream buf = new BufferedInputStream(in);
			InputStreamReader inread = new InputStreamReader(buf);
			BufferedReader bufferedreader = new BufferedReader(inread);
			String line, out = "";

			while ((line = bufferedreader.readLine()) != null) {
				out += line;
			}
			
			try {
				if (p.waitFor() != 0) {
					System.out.println("CMD: " + command);
					System.out.println("OUT: " + out);
				}
			} catch (Exception e) {
				System.err.println(out);
			} finally {
				// Close the InputStream
				bufferedreader.close();
				inread.close();
				buf.close();
				in.close();
			}

			
		}
		
	}

}
