package edu.isi.bmkeg.utils.nlp;

import java.io.File;
import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import edu.isi.bmkeg.utils.Converters;

public class PmcXmlUtilsTest extends TestCase
{

	File inputDir, outputDir;
	File f1, f2, f3;
	
	protected void setUp() throws Exception { 
				
		URL u = this.getClass().getClassLoader().getResource("pathwayLogicOA/coprecipitation");
		inputDir = new File( u.getPath() );
				
	}

	protected void tearDown() throws Exception	{
		
		//
		// Comment these out to leave the files to look at and debug. 
		//
		//Converters.cleanContentsFiles(inputDir, "html");
		//Converters.cleanContentsFiles(inputDir, "txt");
		
	}

	@Test
	public void testXmlToHtml() throws Exception
	{		
		Map<String,File> inputFileMap = Converters.recursivelyListFiles(inputDir);
		
		for( String path : inputFileMap.keySet() ) {

			File inputFile = inputFileMap.get(path);
	
			String f = inputFile.getPath();
			int pos = f.lastIndexOf("_pmc.xml");
			if( pos != -1 ) {
				f = f.substring(0, pos);
				File outputFile = new File( f + "_pmc.html" );
			
				XmlUtils.writePmcXmlToHtmlFile(inputFile, outputFile);
			}			
		}
		
	}
	
	@Test
	public void testHtmlToText() throws Exception
	{		
		Map<String,File> inputFileMap = Converters.recursivelyListFiles(inputDir);
		
		for( String path : inputFileMap.keySet() ) {

			File inputFile = inputFileMap.get(path);
	
			String f = inputFile.getPath();
			int pos = f.lastIndexOf("_pmc.xml");
			if( pos != -1 ) {
				f = f.substring(0, pos);
				File htmlFile = new File( f + "_pmc.html" );
				File outputFile = new File( f + ".txt" );
	
				XmlUtils.writePmcXmlToHtmlFile(inputFile, htmlFile);
				XmlUtils.writePmcHtmlToTxtFile(htmlFile, outputFile);
			}
			
		}
		
	}
	
}
