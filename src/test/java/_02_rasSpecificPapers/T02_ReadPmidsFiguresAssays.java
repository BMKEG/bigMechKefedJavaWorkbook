package _02_rasSpecificPapers;

import java.io.File;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class T02_ReadPmidsFiguresAssays {

	File pmidsFile;
	File pmids_figs_assaysFile;
	File pmids_figs_datumsFile;
	
	@Before
	public void setUp() throws Exception {
		
		URL url = ClassLoader.getSystemResource("02_rasSpecificPapers_data/pmids.txt");
		String pmids = url.getFile();

		pmidsFile = new File(pmids);
		pmids_figs_assaysFile = new File(pmidsFile.getParent() + "/pmids_figs_assays.txt");
		pmids_figs_datumsFile = new File(pmidsFile.getParent() + "/pmids_figs_datums.txt");
		
		if( pmids_figs_assaysFile.exists() ) 
			pmids_figs_assaysFile.delete();
		
	}

	// Leave the files as they are. 
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetEntryFromZippedJarFile() throws Exception {
		
		String[] args = new String[] { 
				"-pmids", pmidsFile.getPath(),
				"-assays", pmids_figs_assaysFile.getPath(),
				"-datums", pmids_figs_datumsFile.getPath()
				};
		
		S02_ReadPmidsFiguresAssays.main(args);					
		
	}

}
