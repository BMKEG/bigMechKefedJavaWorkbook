---
title: Generating the Big Mechanisms Evaluation Corpus
description:  Preliminary work for the program in downloading the full text of documents from the PMC lists .
layout: defaultTOC
prevPage: 01aGenerativeModelForTextFromExperimentalObservations.html
nextPage: 04kefed1.html
---

1. BigMech Wiki Instructions + Redundancy 
---
This [link on the Big Mechanisms wiki](https://www.schafertmd.com/darpa/i2o/bigmechanism/wiki/images/f/f8/BigMech_PMC_OA_subset_IDs.txt) provides a list of **1,741** PMC id values. Since some of the articles occur in more than one query, only **840** of these are unique. Here is a [list of these unique PMC id values](https://raw.githubusercontent.com/BMKEG/systems-biology-kefed/master/docs/BigMech_PMC_OA_unique_IDs.txt).

2. Preliminary Corpora
------
Consistent with this list, we have attempted to download these documents to provide to the community as a shared resource. Pending additional bug checking, we now provide this as a resource for the community. 

Given [the latest lists of the open access xml from PMC](http://www.ncbi.nlm.nih.gov/pmc/tools/ftp/), we were able to download **812** of these **840** documents .

3. Corpus Organization
---
* We organize directories of the corpus `Journal`/`Year`/`Volume` 
	* `space` characters are replaced with `_` in Journal and Volume names. 
* Each article's files are named according to it's PubMed ID (pmid)
	* [`pmid`].pdf                  - The pdf file
	* [`pmid`]_pmc.xml        - The xml full text

We host the files for this on Amazon, so that they may only be downloaded from links on this site. 

* [Corpus 1: PDF + XML Files (955.8MB)](https://s3-us-west-2.amazonaws.com/bmkeg2/000_bigMech/bigMechCorpus_pdf%2Bxml.zip)
* [Corpus 2: XML Files (19.9MB)](https://s3-us-west-2.amazonaws.com/bmkeg2/000_bigMech/bigMechCorpus_xml.zip)

 

