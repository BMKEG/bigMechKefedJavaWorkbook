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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
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

import bioc.type.UimaBioCDocument;
import bioc.type.UimaBioCPassage;
import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;


/**
 * First attempt to write a classifier to identify experimental types from figure legends
 * 
 * @author Gully
 */
public class ExperimentTypeClassifier extends JCasAnnotator_ImplBase {
	
	private static Logger logger = Logger.getLogger(ExperimentTypeClassifier.class);
	
	public final static String PARAM_OUTPUT_FILE = ConfigurationParameterFactory
			.createConfigurationParameterName(ExperimentTypeClassifier.class,
					"outFile");
	@ConfigurationParameter(mandatory = true, description = "Output file.")
	String outFile;
	
	private CleartkExtractor<UimaBioCPassage, Token> unigramExtractor; 
	private CleartkExtractor<DocumentAnnotation, Token> bigramExtractor;
			
	private PrintWriter outWriter;
	
	Pattern patt = Pattern.compile("Figure\\s+(\\d+)");
	
	public static URI createTokenTfIdfDataURI(File outputDirectoryName, String code) {
		File f = new File(outputDirectoryName, code + "_tfidf_extractor.dat");
		return f.toURI();
	}

	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		
		super.initialize(context);		
				
		FeatureExtractor1<Token> lowerCaseExtractor1 = new FeatureFunctionExtractor<Token>(
				new CoveredTextExtractor<Token>(),
				BaseFeatures.EXCLUDE,
				new LowerCaseFeatureFunction());
						
		unigramExtractor = new CleartkExtractor<UimaBioCPassage, Token>(
				Token.class,
				lowerCaseExtractor1,
				new CleartkExtractor.Count(new CleartkExtractor.Covered())
				);
		
	    FeatureExtractor1<Token> lowerCaseExtractor2 = new FeatureFunctionExtractor<Token>(
				new CoveredTextExtractor<Token>(),
				BaseFeatures.EXCLUDE,
				new LowerCaseFeatureFunction());

		CleartkExtractor<Token, Token> biExtractor = new CleartkExtractor<Token, Token>(Token.class,
				lowerCaseExtractor2,
			    new Ngram(new Preceding(1), new Focus()));

		this.bigramExtractor = new CleartkExtractor<DocumentAnnotation, Token>(Token.class,
				biExtractor, new CleartkExtractor.Count(new CleartkExtractor.Covered()));
		
		try {
			File f = new File(outFile);
			if( f.exists() )
				f.delete();
			outWriter = new PrintWriter(new BufferedWriter(
					new FileWriter(f, true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

				
	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		Map<String, String> dInf = BioCUtils.convertInfons(uiD.getInfons());
		
		List<UimaBioCPassage> passages = JCasUtil.selectCovered(UimaBioCPassage.class, uiD);
		for (UimaBioCPassage uiP : passages) {			
		
			Map<String, String> psgInf = BioCUtils.convertInfons(uiP.getInfons());
			if( psgInf.containsKey("type") && psgInf.get("type").equals("fig") ){
				
				Matcher m = patt.matcher(uiP.getCoveredText());
				int figNumber = -1;
				if( m.find() ) {
					figNumber = new Integer(m.group(1));
				}
				
				List<UimaBioCPassage> captions = JCasUtil.selectCovered(UimaBioCPassage.class, uiP);
				for (UimaBioCPassage caption : captions) {			

					Map<String, String> capInf = BioCUtils.convertInfons(caption.getInfons());
					if( capInf.containsKey("type") && capInf.get("type").equals("caption") ){

						List<Sentence> sentences = JCasUtil.selectCovered(Sentence.class, caption);
						for (Sentence sentence : sentences) {			
						
							if( sentence.getCoveredText().contains("precipitat") ) {
								System.out.println( uiD.getId() 
										+ ", fig" + ((figNumber!=-1)?figNumber+"":"?") + ": " 
										+ sentence.getCoveredText().replaceAll("\\n", " ") );
								outWriter.write( uiD.getId() 
										+ ", fig" + ((figNumber!=-1)?figNumber+"":"?") + ": " 
										+ sentence.getCoveredText().replaceAll("\\n", " ") );
								outWriter.write( "\n" );
							}
							
						}
						
					}
					
				}
								/*for( Feature f : this.unigramExtractor.extract(jCas, uiP) ) {
					System.out.println(f.getName() + " " + f.getValue() ); 
				}*/

			}
				
		}
		
	}
	
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
	
		super.collectionProcessComplete();
		outWriter.close();
	
	}
	
	
}
