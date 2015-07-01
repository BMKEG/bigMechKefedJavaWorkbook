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
public class FigureCodeAnnotator extends JCasAnnotator_ImplBase {
	
	private static Logger logger = Logger.getLogger(FigureCodeAnnotator.class);
	
	Pattern figNumber = Pattern.compile("Figure\\s+(\\d+)");
	List<Pattern> figPatterns;
	List<String> codes;
	String bioChunk;

	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		
		super.initialize(context);		

		figPatterns = new ArrayList<Pattern>();
		figPatterns.add( Pattern.compile("(?:\\s+|^)\\(([A-Za-z]|[Tt]op|[Mm]iddle|[Bb]otton)\\)(?:\\s+|$|\\.|,|;)") ); // '. (A) '
		
		figPatterns.add( Pattern.compile(
				"(?:\\s+|^)\\({0,1}([A-Za-z]), ([A-Za-z]), ([A-Za-z]),{0,1} and ([A-Za-z])\\){0,1}(?:\\s+|$|\\.|,)") 
				); // '(a, b, c and d) '
		figPatterns.add( Pattern.compile(
				"(?:\\s+|^)\\({0,1}([A-Za-z]), ([A-Za-z]),{0,1} and ([A-Za-z])\\){0,1}(?:\\s+|$|\\.|,)") 
				); // '(a, b and c) '
		figPatterns.add( Pattern.compile(
				"(?:\\s+|^)\\({0,1}([A-Za-z]) and ([A-Za-z])\\){0,1}(?:\\s+|$|\\.|,)") 
				); // '(a and b) '
		
		figPatterns.add( Pattern.compile("\\s+([A-Za-z])\\.") ); // ' A. '
		figPatterns.add( Pattern.compile("(?:\\s+|^)([A-Za-z])(?:,|\\.|;|$)") ); // ' A, '
		figPatterns.add( Pattern.compile("(?:\\s+|^)\\(([A-Za-z])\\)(?:,|\\.|;|$)") ); // ' (A) '
		figPatterns.add( Pattern.compile("\\.\\s+\\d+([A-Za-z])\\s+") ); // '. 1A '
		figPatterns.add( Pattern.compile("\\.\\s+\\d+([A-Za-z])\\s+") ); // '. 1A '
				
	}
	
	public static String readCode(Pattern p, Sentence s) {
		
		Matcher matcher = p.matcher(s.getCoveredText());
		List<String> codes = new ArrayList<String>();
		
		while( matcher.find() ) {									
			codes.add(matcher.group(1));
			for(int i=1; i<matcher.groupCount(); i++) {
				codes.add(matcher.group(i+1));	
			}
		} 
		
		String code = "";
		for(String c: codes) {
			if(code.length()>0)
				code += ", ";
			
			code += c;
		}
		
		return code;
		
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		Map<String, String> dInf = BioCUtils.convertInfons(uiD.getInfons());
		
		List<UimaBioCAnnotation> annotations = JCasUtil.selectCovered(UimaBioCAnnotation.class, uiD);
		for (UimaBioCAnnotation uiA1 : annotations) {			
		
			Map<String, String> a1Inf = BioCUtils.convertInfons(uiA1.getInfons());
			if( a1Inf.containsKey("type") && a1Inf.get("type").equals("fig") ){
				
				Matcher m = figNumber.matcher(uiA1.getCoveredText());
				int figNumber = -1;
				if( m.find() ) {
					figNumber = new Integer(m.group(1));
				}
				
				List<UimaBioCAnnotation> captions = JCasUtil.selectCovered(UimaBioCAnnotation.class, uiA1);
				for (UimaBioCAnnotation caption : captions) {			

					Map<String, String> capInf = BioCUtils.convertInfons(caption.getInfons());
					if( capInf.containsKey("type") && capInf.get("type").equals("caption") ){

						List<Sentence> sentences = JCasUtil.selectCovered(Sentence.class, caption);
						bioChunk = "o";
						
						for (Sentence s : sentences) {			

							String code = "";
							for (Pattern patt: figPatterns) {
								code = readCode(patt, s);
								if( code.length() > 0 )
									break;
							}

							if( code.length() > 0 )
								bioChunk = "b";
							else if( bioChunk.equals("b") ) 
								bioChunk = "i";
								
							UimaBioCAnnotation uiA = new UimaBioCAnnotation(jCas);
							uiA.setBegin(s.getBegin());
							uiA.setEnd(s.getEnd());
							Map<String,String> infons = new HashMap<String, String>();
							infons.put("type", "sub-figure-sentence");
							infons.put("code", code);
							infons.put("figNumber", figNumber + "");
							infons.put("bio", bioChunk);
							uiA.setInfons(UimaBioCUtils.convertInfons(infons, jCas));
							uiA.addToIndexes();
							
							FSArray locations = new FSArray(jCas, 1);
							uiA.setLocations(locations);
							UimaBioCLocation uiL = new UimaBioCLocation(jCas);
							locations.set(0, uiL);
							uiL.setOffset(s.getBegin());
							uiL.setLength(s.getEnd() - s.getBegin());

						}
						
					}
					
				}

			}
				
		}
		
	}
	
}
