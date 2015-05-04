package _10_experimentTypesInOAPMC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.io.BioCDocumentWriter;
import bioc.io.BioCFactory;
import bioc.type.UimaBioCDocument;

import com.google.gson.Gson;

import edu.isi.bmkeg.digitalLibrary.utils.BioCUtils;

public class SaveAsBioCDocuments extends JCasAnnotator_ImplBase {

	public final static String PARAM_FILE_PATH = ConfigurationParameterFactory
			.createConfigurationParameterName(SaveAsBioCDocuments.class,
					"outDirPath");
	@ConfigurationParameter(mandatory = true, description = "The place to put the document files to be classified.")
	String outDirPath;

	public static String XML = ".xml";
	public static String JSON = ".json";
	public final static String PARAM_FORMAT = ConfigurationParameterFactory
			.createConfigurationParameterName(SaveAsBioCDocuments.class,
					"outFileFormat");
	@ConfigurationParameter(mandatory = true, description = "The format of the output.")
	String outFileFormat;

	private File outDir;
	private BioCCollection collection;

	public void initialize(UimaContext context)
			throws ResourceInitializationException {

		super.initialize(context);

		this.outDirPath = (String) context
				.getConfigParameterValue(PARAM_FILE_PATH);
		this.outDir = new File(this.outDirPath);

		this.collection = new BioCCollection();

	}

	public void process(JCas jCas) throws AnalysisEngineProcessException {

		for (UimaBioCDocument uiD : JCasUtil.select(jCas,
				UimaBioCDocument.class)) {
			
			try {

				BioCDocument d = BioCUtils.convertUimaBioCDocument(uiD);
				String relPath = d.getInfon("relative-source-path").replaceAll("\\.txt", outFileFormat);
				File outFile = new File(outDirPath + "/" + relPath);
				if( !outFile.getParentFile().exists() ) {
					outFile.getParentFile().mkdirs();
				}
				
				if (outFileFormat.equals(XML)) {
					
					BioCDocumentWriter writer = BioCFactory.newFactory(
							BioCFactory.STANDARD).createBioCDocumentWriter(
							new FileWriter(outFile));

					writer.writeDocument(d);

					writer.close();

				} else if (outFileFormat.equals(JSON)) {

					Gson gson = new Gson();
					String json = gson.toJson(d);

					PrintWriter out = new PrintWriter(new BufferedWriter(
							new FileWriter(outFile, true)));

					out.write(json);

					out.close();
					
				} else {
					
					throw new AnalysisEngineProcessException(
							new Exception("Please write to an *.xml or a *.json file")
							);
				
				}

			} catch (IOException | XMLStreamException e) {

				throw new AnalysisEngineProcessException(e);

			}			
			
		}

	}

}
