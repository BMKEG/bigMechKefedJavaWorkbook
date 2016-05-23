---
title: Deploying the BioScholar System
description:  What do you need to do to deploy the BioScholar system to a server. 
layout: defaultTOC
prevPage: 05evaluatingEvidenceExtraction.html
nextPage: 07heuristicsForResultText.html
---

Here we just document the various pieces needed to run the BioScholar system within Tomcat on a Linux server.  

1. `bmkeg.properties` 
---

You will need to place a `bmkeg.properties` file that you will need to refer to when you run Tomcat.  

	bmkeg.workingDirectory=/nfs/amber/burns/Projects/2_active/bigMechanisms/workingDirectory/wd
	bmkeg.dbDriver=com.mysql.jdbc.Driver
	bmkeg.dbUrl=dbName
	bmkeg.dbUser=dbLogin
	bmkeg.dbPassword=dbPassword
	
This refers to several pieces that we will elaborate on here:

### 1.1 `workingDirectory`

This is where all the `*.pdf`, `*.xml` and `*.txt` files go. Typically, a working directory will have the following substructure.

	+ wd
	  +=> pdfs
	    +=> journal
	      +=> year
	        +=> vol
	          +=> 12345.pdf
	          +=> 12345.swf
	          +=> 12345_pmc.xml
	          +=> 12345.txt
	  +=> lod
	  +=> owl
	  +=> elsevierKeyFile.txt
	  +=> webapp.properties
		
* `elsevierKeyFile.txt` is for logging into ScienceDirect for full text of Elsevier articles.
* `lod` and `owl` are development directories designed to use with semantic web extensions to BioScholar.  
* `webapp.properties` are locations of executables for use of `swftools` and `brat` from within the interface. 

The format is as shown below: 

	swftools.bin.path=/usr/local/bin
	bratData.bin.path=/path/to/brat/installation/brat/brat-v1.3_Crunchy_Frog/

<!-- You will need a predefined wd tar archive -->
<!-- You will also need swftools installed on the system -->

2. MySQL installation
---

Update the `/etc/my.cnf` configuration file with the following:

	max_allowed_packet = 10485760
	innodb_log_file_size = 125242880
	
The `bmkeg.properties` file must have a current login to the local MySQL system with sufficient permissions to add and delete data.  

3. `VPDMf` data
---

The BioScholar system uses the 'View-Primitive-Data-Modeling framework' as its basis This is a local system developed within the BMKEG (see [https://github.com/BMKEG/vpdmfProject](https://github.com/BMKEG/vpdmfProject) which permits a data archive to be easily installed into the underlying MySQL database (or saved from an existing database). 

VPDMf Binary `jar` file: [data/vpdmfCore-1.1.5-SNAPSHOT-jar-with-dependencies.jar](data/vpdmfCore-1.1.5-SNAPSHOT-jar-with-dependencies.jar) (34MB)

Installing the database is then pretty easy using this command:

	java -cp vpdmfCore-1.1.5-SNAPSHOT-jar-with-dependencies.jar
	 	edu.isi.bmkeg.vpdmf.bin.BuildDatabaseFromVpdmfArchive
	 	/path/to/vpdmf/archive.zip
		dbName
		dbLogin
		dbPassword

<!-- You will need a predefined vpdmf data dump -->

Dumping a VPDMf database to a zip file is also pretty easy with this command (you need to refer to a previous vpdmf archive to build the system): 

	java -cp vpdmfCore-1.1.5-SNAPSHOT-jar-with-dependencies.jar
	 	edu.isi.bmkeg.vpdmf.bin.DumpDatabaseToVpdmfArchive
	 	/path/to/old/vpdmf/archive.zip
		dbName
		dbLogin
		dbPassword
	 	/path/to/new/vpdmf/archive.zip		

### 4. Running tomcat 

In order to inform Tomcat where the working directory is, you need to set some environment variables.

	export TOMCAT_HOME=/path/to/tomcat
	export CATALINA_OPTS="-Dbmkeg.propertiesfile=/path/to/bmkeg.properties -Xmx1096M -Xms512M"
	
You will then need to build the BioScholar `*.war` file (and place it into the `$TOMCAT_HOME/webapps` directory). This is 111MB in size and so is a little too big for this site. 
	
Then all you do is `TOMCAT_HOME/bin/startup.sh` to start up the system. 
