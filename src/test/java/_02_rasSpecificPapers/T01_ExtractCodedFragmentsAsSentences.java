package _02_rasSpecificPapers;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.utils.springContext.BmkegProperties;

public class T01_ExtractCodedFragmentsAsSentences {

	File propFile = new File("/Users/Gully/bmkeg/bigMech.properties");
	String corpus = "PathwayLogicFullText";
	BmkegProperties props;
	File out;
	
	
	@Before
	public void setUp() throws Exception {
		
		this.props = new BmkegProperties(propFile);
		
		URL url = ClassLoader.getSystemResource("02_rasSpecificPapers_data/pmids.txt");
		out = new File(url.getFile());
		
	}

	// Leave the files as they are. 
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetEntryFromZippedJarFile() throws Exception {
		
		String[] args = new String[] { 
				"-db", props.getDbUrl(), 
				"-l", props.getDbUser(),
				"-p", props.getDbPassword(), 
				"-wd", props.getWorkingDirectory(),
				"-corpus", corpus,
				"-outDir", out.getParent(),
				"-frgType", "orca-ex"				
				};
		
		S01_ExtractCodedFragmentsAsSentences.main(args);					
		
	}

}
