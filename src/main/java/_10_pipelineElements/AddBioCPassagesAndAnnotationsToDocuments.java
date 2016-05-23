package _10_pipelineElements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import bioc.BioCDocument;
import bioc.io.BioCDocumentWriter;
import bioc.io.BioCFactory;
import bioc.type.MapEntry;
import bioc.type.UimaBioCAnnotation;
import bioc.type.UimaBioCDocument;
import bioc.type.UimaBioCPassage;

import com.google.gson.Gson;

import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;

public class AddBioCPassagesAndAnnotationsToDocuments extends JCasAnnotator_ImplBase {

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		UimaBioCDocument uiD = JCasUtil.selectSingle(jCas, UimaBioCDocument.class);
		
		List<UimaBioCPassage> passages = JCasUtil.selectCovered(UimaBioCPassage.class, uiD);
		FSArray psgArray = new FSArray(jCas, passages.size());
		int passageCount = 0;
		uiD.setPassages(psgArray);
		for (UimaBioCPassage uiP : passages) {			
			psgArray.set(passageCount, uiP);
			passageCount++;
						
			List<UimaBioCAnnotation> annotations = JCasUtil.selectCovered(UimaBioCAnnotation.class, uiP);
			FSArray annArray = new FSArray(jCas, annotations.size());
			int annotationCount = 0;
			uiP.setAnnotations(annArray);
			for (UimaBioCAnnotation uiA : annotations) {
				annArray.set(annotationCount, uiA);
				annotationCount++;
			}
			
		}

	}

}
