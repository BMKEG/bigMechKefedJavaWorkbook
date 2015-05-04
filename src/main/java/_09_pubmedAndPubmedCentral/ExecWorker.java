package _09_pubmedAndPubmedCentral;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;


public class ExecWorker extends SwingWorker<Integer, String> {

	private static Logger logger = Logger
			.getLogger(S09_04_MultithreadedRunNxmlToText.class);

	
	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted");
		}
	}

	String command;
	
	/**
	 * Creates an instance of the worker
	 * 
	 * @param url
	 *            The URL to search and process.
	 * @param file
	 *            The output file
	 */
	public ExecWorker(String command) {
		this.command = command;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		
		ProcessBuilder pb = new ProcessBuilder(this.command.split(" "));
		Map<String,String> env = pb.environment();
		env.put("PYTHONPATH", "/usr/local/lib/python2.7/site-packages");
		Process p = pb.start();
		
		if (p == null) {
			throw new Exception("Can't execute " + this.command);
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
				System.out.println(p.exitValue());
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
		
		return 1;
	}
	
}
