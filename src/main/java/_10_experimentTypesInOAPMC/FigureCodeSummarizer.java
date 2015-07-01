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
package _10_experimentTypesInOAPMC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.feature.extractor.CleartkExtractor;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Focus;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Ngram;
import org.cleartk.ml.feature.extractor.CleartkExtractor.Preceding;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor.BaseFeatures;
import org.cleartk.ml.feature.function.LowerCaseFeatureFunction;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import bioc.type.UimaBioCAnnotation;
import bioc.type.UimaBioCDocument;
import bioc.type.UimaBioCLocation;
import bioc.type.UimaBioCPassage;
import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;
import edu.isi.bmkeg.uimaBioC.UimaBioCUtils;


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

	File outFile;
	StringBuffer sb;
	
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		
		super.initialize(context);		
		
		this.outFilePath = (String) context
				.getConfigParameterValue(PARAM_OUT_FILE);
		this.outFile = new File(this.outFilePath);

		this.sb = new StringBuffer();
		this.sb.append( "pmid\tfigure\tcode\tbio\tbegin\tend\ttext\n" );
		
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		
		List<UimaBioCAnnotation> annotations = JCasUtil.selectCovered(UimaBioCAnnotation.class, uiD);
		for (UimaBioCAnnotation uiA : annotations) {			
		
			Map<String, String> inf = BioCUtils.convertInfons(uiA.getInfons());
			if( !inf.containsKey("type") || !inf.get("type").equals("sub-figure-sentence") )
				continue;
		
			this.sb.append(uiD.getId() 
					+ "\t" + inf.get("figNumber") 
					+ "\t" + inf.get("code") 
					+ "\t" + inf.get("bio") 
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
