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
 * Need to add some code to fix sentence segmentation
 * 
 * @author Gully
 */
public class FixSentenceAnnotator extends JCasAnnotator_ImplBase {
	
	private static Logger logger = Logger.getLogger(FixSentenceAnnotator.class);
	
	Pattern figNumber = Pattern.compile("Figure\\s+(\\d+)");
	List<Pattern> figPatterns;
	List<String> codes;
	String bioChunk;

	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		
		super.initialize(context);		
		
	}
	
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		Map<String, String> dInf = BioCUtils.convertInfons(uiD.getInfons());
		
		List<Sentence> sentences = JCasUtil.selectCovered(Sentence.class, uiD);
		for (Sentence s : sentences) {			

			List<UimaBioCAnnotation> annotations = JCasUtil.selectCovered(UimaBioCAnnotation.class, s);
			for (UimaBioCAnnotation uiA1 : annotations) {			
			
				Map<String, String> a1Inf = BioCUtils.convertInfons(uiA1.getInfons());
				if( a1Inf.containsKey("type") 
						&& a1Inf.get("type").equals("formatting") ) {
				
					String type = a1Inf.get("value");				
					if( type.equals("title") || 
							type.equals("subtitle") ||  
							type.equals("p") ||  
							type.equals("sec") ||  
							type.equals("fig") ){

						//
						// If the sentence is overlapping the ends of the annotation, 
						// then it should be trimmed and an additional sentence added. 
						//
						if( uiA1.getBegin() > s.getBegin() && 
								uiA1.getEnd() == s.getEnd() ) {
							
							s.setEnd(uiA1.getBegin()-1);
							Sentence ss = new Sentence(jCas);
							ss.setBegin(uiA1.getBegin());
							ss.setEnd(uiA1.getEnd());
							ss.addToIndexes();
						
						} else if( uiA1.getBegin() == s.getBegin() && 
								uiA1.getEnd() < s.getEnd() ) {
							
							s.setBegin(uiA1.getEnd()+1);
							Sentence ss = new Sentence(jCas);
							ss.setBegin(uiA1.getBegin());
							ss.setEnd(uiA1.getEnd());
							ss.addToIndexes();
							
						} else if( uiA1.getBegin() > s.getBegin() && 
								uiA1.getEnd() < s.getEnd() ) {
							
							int e = s.getEnd();
							s.setEnd(uiA1.getBegin()-1);

							Sentence ss1 = new Sentence(jCas);
							ss1.setBegin(uiA1.getBegin());
							ss1.setEnd(uiA1.getEnd());
							ss1.addToIndexes();
							
							Sentence ss2 = new Sentence(jCas);
							ss2.setBegin(uiA1.getEnd()+1);
							ss2.setEnd(e);
							ss2.addToIndexes();
							
						}
						
					}
					
				}

			}
			
		}

		/* DEBUGGING
		 * sentences = JCasUtil.selectCovered(Sentence.class, uiD);
		for (Sentence s : sentences) {			
			System.out.println("\t" + s.getCoveredText());
		}*/
				
	}
	
}
