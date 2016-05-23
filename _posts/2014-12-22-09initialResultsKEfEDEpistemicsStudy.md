---
title: Initial Extraction Study of Results-Based Epistemics
description:  Here we describe an annotation study of epistemics, PL Datums and KEfED models for five papers pertaining to the Ras pathway.  
layout: defaultTOC
prevPage: 08pathwayLogicExperimentTypes.html
nextPage: 10kefedModelsOfCoprecipitationPapers.html
---

### Papers of interest:

From the 71 "open access" pmids, there are 1716 datums.
24 have Hras, Braf, Raf1 or Rac1 as subject.

These come from 8 papers: [16492808, 11448999, 11777939, 12515821, 19050761, 20929976, 16520382, 12876277]

Of these, 5 papers containing at least one coprecipitation study; we use these as the initial basis of this small-scale study.

* [16492808](http://www.ncbi.nlm.nih.gov/pubmed/?term=16492808): ten Klooster et al. 2006. 
* [11777939](http://www.ncbi.nlm.nih.gov/pubmed/?term=11777939): Innocenti et al. 2002
* [12515821](http://www.ncbi.nlm.nih.gov/pubmed/?term=12515821): Innocenti et al. 2002
* [19050761](http://www.ncbi.nlm.nih.gov/pubmed/?term=19050761): Meyer et al.  2008
* [16520382](http://www.ncbi.nlm.nih.gov/pubmed/?term=16520382): Khanday et al. 2006

Click for [ZIP file of PDF, PMC XML + TXT files](data/fivePapers/pdfsXmlTxt.zip)

### ORCA Markup 

We used the ORCA codes that are currently in the BioScholar system (listed in [this excel spreadsheet](data/fivePapers/orcaTerminology.xls)). This consists of Anita's original encoding and one additional code to denote a description by authors of '*what they did*' in the execution of the experiment. This is intended as a placeholder to be substituted out later. 

Within the results sections of papers, we expect to find the following high level argument structure: 

1. The authors make some interpretive assertion about the phenomenon, citing other work as a starting point for the experiment (this isn't shown in the text above, but this does happen a lot). [typical ORCA code: `v2_bD_cN`]
2. Establish the purpose of the experiment. Usually this looks very much like the sentence above saying something like  'To investigate XYZ' or some variation. [typical ORCA code: `v1_bR_cA`]
3. Say what they did:  "U2OS cells were immunostained ... used preferentially" [typical ORCA code: `methods`]
4. Say what the results were and maybe interpret them directly: "... the vast majority of 14-3-3 had a diffuse cytoplasmic and perinuclear distribution in unsychronized cells..." [typical ORCA code: `v3_bD_cA`]
5. Put this finding into context with other data: "consistent with findings reported by others" or summarize the main findings of a set of experiments. [typical ORCA code: `v2_bD_cN`]

I worked through the results sections of all files to mark up all elements of the experimental narrative that directly pertains to experimental results from within the paper with ORCA codes.  These may be viewed (and edited) in the CMU installation of the BioScholar system: [lagos.lti.cs.cmu.edu:8080/bioscholar/digLib.jsp](lagos.lti.cs.cmu.edu:8080/bioscholar/digLib.jsp)  

> Use the search button marked `?` at the bottom of the article list of and then select `orca-ex` from the Fragment pane to see the annotations in the tool. 

We dumped these to brat to show them as isolated text annotations: [brat_orcaFiles.tar.gz](data/fivePapers/brat_orcaFiles.tar.gz). These are also available for viewing on the CMU system: 

[http://lagos.lti.cs.cmu.edu/~gully/brat/#](http://lagos.lti.cs.cmu.edu/~gully/brat/#)

### Correction: Move to epistemic segment types

After feedback from Anita, we changed the codes from `orca` codes to *'epistemic segment types'*, which more accurately describe the requirements. We switched these over in the database very easily using mysql's REPLACE function:

```
	UPDATE FTDFragment SET frgType = REPLACE(frgType, 'orca-ex', 'epistSeg')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'orca-ex', 'epistSeg')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'implication: v2_bD_sA', 'implication')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'v1_bD_sN', 'goal')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'cited-result: v3_bD_sN', 'other-result')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'hypothesis: v1_b0_sA', 'hypothesis')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'result: v3_bD_sA', 'result')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'v1_bR_sA', 'goal')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'v1_bD_sA', 'goal')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'fact: v3_b0_s0', 'fact')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'cited-implication: v2_bD_', 'other-implication')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'v2_bR_sA', 'fact')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'cited-hypothesis\': v1_b0_', 'other-hypothesis')
	UPDATE FTDFragmentBlock SET code = REPLACE(code, 'problem: v0_b0_s0', 'problem')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple,'implication: v2_bD_sA', 'implication')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'v1_bD_sN', 'goal')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'cited-result: v3_bD_sN', 'other-result')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'hypothesis: v1_b0_sA', 'hypothesis')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'result: v3_bD_sA', 'result')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'v1_bR_sA', 'goal')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'v1_bD_sA', 'goal')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'fact: v3_b0_s0', 'fact')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'cited-implication: v2_bD_', 'other-implication')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'v2_bR_sA', 'fact')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'cited-hypothesis\': v1_b0_', 'other-hypothesis')
	UPDATE ViewTable SET indexTuple = REPLACE(indexTuple, 'problem: v0_b0_s0', 'problem')
```

This maps all previous codes to their new values. 

### 6 month plan before the July PI meeting. 

***Outcome***: have an automatic system to identify experimental passages in the text (i.e., given a collection of open access articles, we want a system that generates a table with the following columns: 
	
1. Code for the group of experiments pertaining to a set of implications (usually as interpretive statements)
2. Fragment text for the implications + hypothesis + cited-results + cited-implications that introduce these experiments
3. Fragment text for the implications + hypothesis + cited-results + cited-implications that explain these experiments
4. The experimental code pertaining to the sub-figure (or sub-figures for a specific experiment).  
5. Fragment text for the goal of this experiment 
6. Fragment text for the methodology of this experiment (this might be drawn from the results, figure legend and methods section) 
7. Fragment text for the results of the experiment
8. Bitmap images from the figure for this experiment (future work but worth mentioning here)

***Milestones + Tasks***: (broken down as monthly staging points working backwards from the final outcome). This is structured "We need X ... To accomplish this, we will do Y ...".

* **6 month milestone** (Jul 2015): We need to have the full table as shown above for all available XML documents (column 8 is optional depending on how we work with image analyzers). 

	* *Tasks*: To accomplish this, we will perform (A) information extraction experiments (F-Score > 0.95)  that correctly delineate clauses and assign the correct epistemic segment type codes and (B) development of a sequential process for examining the groups of experiments that group together in a text passage and identifying the sub-figure numbers that are referenced in that group of experiments.   

* **5 month milestone** (Jun 2015): We need to have the experiments complete and performing at F-Score > 0.9 and the experiment identification algorithm finished. 

	* *Tasks*: To accomplish this, we will execute experiments in our process, doing good feature engineering and improving performance for the information extraction task. We will continuously add good quality data to the gold-standard annotation set as we go. 

* **4 month milestone** (May 2015): We need to be on track with an effective experimental process for extraction working and in use based on the gold standard epistemic statement type annotations.

	* *Tasks*: To accomplish this, we will develop, iterate and exercise the experimental pipeline, documenting our experiments and improving performance as we go. The software supporting this should be in Github and the experimental setup should be easily reproducible in different environments. Pay attention to these simple rules when managing data (http://journals.plos.org/ploscollections/article?id=10.1371/journal.pcbi.1003542). We will continuously add good quality data to the gold-standard annotation set as we go. 

* **3 month milestone** (Apr 2015): We need to have a well-defined gold-standard training set with annotations for (A) epistemic statement types and (B) experiment labels with inter-annotator agreement data to confirm it's validity.  

	* *Tasks*: To accomplish this, we will have used our annotation process to create a solid gold-standard corpus by setting quotas and meeting regularly (2x week) to collaborate and discuss and improve the annotations.   

* **2 month milestone** (Mar 2015): We need to have an effective working process for creating annotations for epistemic statement types and experiment codes with automated computation of inter-annotator agreement.  

	* *Tasks*: To accomplish this, we will have started using the tool for developing inter-annotator agreement between at least two annotators (Anita & Gully?). This will require regular phone conferences, working through a small corpus of papers to establish reliable curation guidelines. 

* **1 month milestone** (Feb 2015): We need a practical annotation tool with inter-annotator functionality + prototype experimental  methods for IE and detecting experimental labels.    

	* *Tasks*: To accomplish this, we will have developed and tested an annotation tool for epistemic types that can permits different users to log in separately / concurrently with different usernames and enter their own annotations independently of one another. The system should be able to compare the annotations and provide feedback to an administrator.    
