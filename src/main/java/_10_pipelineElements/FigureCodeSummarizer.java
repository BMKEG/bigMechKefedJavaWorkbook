/** 
 * Copyright (c) 2012, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package _10_pipelineElements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import bioc.type.UimaBioCAnnotation;
import bioc.type.UimaBioCDocument;
import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;


/**
 * First attempt to write a classifier to identify experimental types from figure legends
 * 
 * @author Gully
 */
public class FigureCodeSummarizer extends JCasAnnotator_ImplBase {
	
	private static Logger logger = Logger.getLogger(FigureCodeSummarizer.class);
	
	public final static String PARAM_OUT_FILE = ConfigurationParameterFactory
			.createConfigurationParameterName(FigureCodeSummarizer.class,
					"outFilePath");
	@ConfigurationParameter(mandatory = true, description = "The summary file.")
	String outFilePath;

	public final static String PARAM_FIG_LABEL_FILE = ConfigurationParameterFactory
			.createConfigurationParameterName(FigureCodeSummarizer.class,
					"figLabelFile");
	@ConfigurationParameter(mandatory = true, description = "File with figure labels.")
	String figLabelFile;
	
	File outFile;
	StringBuffer sb;
	
	// lookup.get(pmid).get(fig).get("indexCard|exptCode")
	Map<String,Map<String,Map<String,String>>> lookup;
	
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		
		super.initialize(context);		
		
		this.outFilePath = (String) context
				.getConfigParameterValue(PARAM_OUT_FILE);
		this.outFile = new File(this.outFilePath);

		this.sb = new StringBuffer();
		this.sb.append( "id\tpmid\tfigure\tcode\tbio\tindexCard\texptCode\tbegin\tend\ttext\n" );
		
		lookup = new HashMap<String,Map<String,Map<String,String>>>();
		try (BufferedReader br = new BufferedReader(new FileReader(figLabelFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       String[] fields = line.split("\\t");
		       String indexCard = fields[0];
		       String exptCode = fields[1];
		       String pmid = fields[2];
		       
		       String fig = fields[3];
		       fig = fig.substring(fig.lastIndexOf("-")+1, fig.length()).toLowerCase();
		       
		       if(!lookup.containsKey(pmid))
		    	   lookup.put(pmid, new HashMap<String,Map<String,String>>());
		       if(!lookup.get(pmid).containsKey(fig))
		    	   lookup.get(pmid).put(fig, new HashMap<String,String>());
		       lookup.get(pmid).get(fig).put("exptCode", exptCode);
		       lookup.get(pmid).get(fig).put("indexCard", indexCard);
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceInitializationException(e);
		}
		
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		
		List<UimaBioCAnnotation> annotations = JCasUtil.selectCovered(UimaBioCAnnotation.class, uiD);
		int i = 0;
		for (UimaBioCAnnotation uiA : annotations) {			
		
			Map<String, String> inf = BioCUtils.convertInfons(uiA.getInfons());
			if( !(inf.containsKey("type") 
					&& inf.get("type").equals("formatting") 
					&& inf.get("value").equals("sub-figure-sentence")) )
				continue;
			
			String pmid = uiD.getId();
			String figNumber = inf.get("figNumber");
			String code = inf.get("code");
			String[] codes = code.split(", ");
			String indexCards = "";
			String exptCodes = "";
			if( lookup.containsKey(pmid) ) {
				if( lookup.get(pmid).containsKey(figNumber)) {
					if( indexCards.length() > 0)
						indexCards += ", ";
					indexCards += lookup.get(pmid).get(figNumber).get("indexCard");
					if( exptCodes.length() > 0)
						exptCodes += ", ";
					exptCodes += lookup.get(pmid).get(figNumber).get("exptCode");
				} else {
					for( String c : codes) {
						String fc = figNumber + c.toLowerCase();
						if( lookup.get(pmid).containsKey(fc)) {
							if( indexCards.length() > 0)
								indexCards += ", ";
							indexCards += lookup.get(pmid).get(fc).get("indexCard");
							if( exptCodes.length() > 0)
								exptCodes += ", ";
							exptCodes += lookup.get(pmid).get(fc).get("exptCode");
						}
					}
				}
			}
			
		
			this.sb.append(i++ 
					+ "\t" + uiD.getId() 
					+ "\t" + inf.get("figNumber") 
					+ "\t" + inf.get("code") 
					+ "\t" + inf.get("bio") 
					+ "\t" + indexCards 
					+ "\t" + exptCodes 
					+ "\t" + uiA.getBegin() 
					+ "\t" + uiA.getEnd() 
					+ "\t" + uiA.getCoveredText().replaceAll("\\n", " ") + "\n");
			
		}
		
	}
	
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();

		try {

			if(outFile.exists())
				outFile.delete();
			
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(outFile, true)));

			out.print(sb);
			
			out.close();
			
		} catch (IOException e) {

			throw new AnalysisEngineProcessException(e);

		}

	}
	
}
