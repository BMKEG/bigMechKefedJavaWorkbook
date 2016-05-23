---
title: Building a Database of Observations from Result Text
description:  Here we describe simple heuristics for extracting the text pertaining to a specific experiment from a pathway papers. 
layout: defaultTOC
prevPage: 06deployingBioScholar.html
nextPage: 08pathwayLogicExperimentTypes.html
---

Typically, experimental papers are split into sections. This is evident from the structure of the XML of the full text articles. A typical structure would be:

1. Title + Abstract
2. Introduction
3. Materials and Methods (sometimes this is at the end or in supplemental material)
4. Results (this is what we care about and contains Figure Legends)
    4a. Figure Legends
5. Discussion

As an example, let's take [http://www.ncbi.nlm.nih.gov/pubmed/23142775](http://www.ncbi.nlm.nih.gov/pubmed/23142775), which is one of the Open Access papers from PL. This is interesting because in Figure 5a and 5b, the paper talks about data from a 'coprecipitation' assay, which is a technique used to examine what proteins complexes are made of. 

This is the assay we're going to look at in detail for the documents in the PL corpus. The XML of the results section of the PubMedCentral articles reads thus: 

	<sec sec-type="results" id="S1">
      <title>RESULTS</title>
       ...
       <sec id="S5">
        <title>DDX41-dependent signaling downstream of c-di-GMP and c-di-AMP</title>
            <p id="P10">
            DDX41 interacts and co-localizes with the STING adaptor (<bold><xref 
        	ref-type="supplementary-material" rid="SD1">Supplementary Fig. 4a,b</xref></bold>) 
        	to facilitate DNA ligand dependent signal transduction<sup><xref ref-type="bibr" rid="R17">17</xref></sup>. 
        	Introduction of either c-di-GMP or c-di-AMP into D2SC cells led to enhanced DDX41-STING complex
        	formation (<bold><xref ref-type="fig" rid="F5">Fig. 5a</xref></bold>). In DNA dependent signaling
        	 pathways, STING further binds to the downstream kinase TBK1 to activate the type I IFN 
        	 response<sup><xref ref-type="bibr" rid="R18">18</xref>, <xref ref-type="bibr" rid="R19">19</xref></sup>. 
        	 Both c-di-GMP and c-di-AMP when transfected into control D2SC cells activated the formation of a STING-
        	 TBK1 complex, however, c-di-GMP and c-di-AMP mediated activation of the STING-TBK1 complex was 
        	 almost completely abrogated in DDX41-shRNA cells (<bold><xref ref-type="fig" rid="F5">Fig. 5b</xref>
        	 </bold>). Consequently, c-di-GMP and c-di-AMP mediated activation of TBK1, IRF3 and the downstream type 
        	 I IFN effector, STAT1 was impaired in DDX41-shRNA cells (<bold><xref ref-type="fig" rid="F5">Fig. 5c</xref> 
        	 and <xref ref-type="supplementary-material" rid="SD1">Supplementary Fig. 5</xref></bold>). Activation of 
        	 NF-&#x3BA;B was also impaired in DDX41-shRNA cells in response to either c-di-GMP or c-di-AMP 
        	 (<bold><xref ref-type="fig" rid="F5">Fig. 5c</xref></bold>). Together, our findings suggest that DDX41 is a 
        	 critical PRR for c-di-GMP and c-di-AMP mediated IFN induction, and that its absence generates a defect in 
        	 downstream STING-dependent signaling.
        	 </p>
	 </sec>
      ...
    </sec>
    
This paragraph, when parsed out using modified JATS XSLT transforms yields this text (with sentences of interest highlighted and tokenization from our text processing pipeline ):

> DDX41 interacts and co-localizes with the STING adaptor ( Supplementary Fig. 4a,b  ) to facilitate DNA ligand dependent signal transduction  <sup>17</sup> . <font color="red">Introduction of either c-di-GMP or c-di-AMP into D2SC cells led to enhanced DDX41-STING complex formation (  Fig. 5a  ).</font> In DNA dependent signaling pathways, STING further binds to the downstream kinase TBK1 to activate the type I IFN response  <sup>18  ,  19</sup>  . <font color="red"> Both c-di-GMP and c-di-AMP when transfected into control D2SC cells activated the formation of a STING-TBK1 complex, however, c-di-GMP and c-di-AMP mediated activation of the STING-TBK1 complex was almost completely abrogated in DDX41-shRNA cells (  Fig. 5b  ) .</font> Consequently, c-di-GMP and c-di-AMP mediated activation of TBK1, IRF3 and the downstream type I IFN effector, STAT1 was impaired in DDX41-shRNA cells (  Fig. 5c  and  Supplementary Fig. 5  ). Activation of NF-κB was also impaired in DDX41-shRNA cells in response to either c-di-GMP or c-di-AMP (  Fig. 5c  ). Together, our findings suggest that DDX41 is a critical PRR for c-di-GMP and c-di-AMP mediated IFN induction, and that its absence generates a defect in downstream STING-dependent signaling. 

Note that the sentences to either side of the desired text either contain citations (as superscripted numbers in the text or `<xref ref-type="bibr" rid="R17">` elements in the XML) or references to other figures in the document (also denoted by `xref` tags in the xml).
	
In this XML, the figure legends of interest occur in a completely separate part of the file (right at the end and separate from the other parts) 

	  <floats-group>
	  ...
    		<fig id="F5" orientation="portrait" position="float">
      			<label>Figure 5</label>
      			<caption>
        			<p>c-di-GMP and c-di-AMP require DDX41 for STING dependent signaling.
        			 (<bold>a</bold>) Immunoprecipitation and immunoblot analysis of DDX41-
        			 STING interactions is D2SC cells transfected with c-di-GMP or c-di-AMP 
        			 for 4 h. (<bold>b)</bold> Immunoprecipitation and immunoblot analysis of 
        			 STING-TBK1 interactions in control-shRNA or DDX41-shRNA D2SC cells 
        			 transfected as in <bold>a</bold>. (<bold>c</bold>) Immunoblot analysis of 
        			 TBK1, IRF3, p65 and STAT1 phosphorylations in control-shRNA, DDX41-
        			 shRNA or STING-shRNA D2SC mDCs transfected with c-di-GMP, c-di-
        			 AMP, Poly (I:C) or B-DNA for 4 h. Data are representative of at least two 
        			 independent experiments.
        			</p>
        		</caption>
        		<graphic xlink:href="nihms-410677-f0005"/>
        	</fig>
        ...
        </floats-group>
	  
This yields the following text: 

> Figure 5 c-di-GMP and c-di-AMP require DDX41 for STING dependent signaling. <font color="red">(  a  ) Immunoprecipitation and immunoblot analysis of DDX41-STING interactions is D2SC cells transfected with c-di-GMP or c-di-AMP for 4 h. </font> <font color="red">(  b )  Immunoprecipitation and immunoblot analysis of STING-TBK1 interactions in control-shRNA or DDX41-shRNA D2SC cells transfected as in  a  . </font>(  c  ) Immunoblot analysis of TBK1, IRF3, p65 and STAT1 phosphorylations in control-shRNA, DDX41-shRNA or STING-shRNA D2SC mDCs transfected with c-di-GMP, c-di-AMP, Poly (I:C) or B-DNA for 4 h. Data are representative of at least two independent experiments. 
  Ideally, what we would like would be a table of all subfigures (1a, 1b, 1c, ... , 6f), with their location in the text (this could be described in any way that we could reconstruct easily) and the text of the sentences as shown.

Files for this example are listed here:

* [23142775_pmc.xml](data/23142775/23142775_pmc.xml)
* [23142775.pdf](data/23142775/23142775.pdf)
* [23142775.html](data/23142775/23142775.html)
* [23142775.txt](data/23142775/23142775.txt)

Note that the text file has a bunch of tokens that look like this: `__s_A__` or `__e_A__`. These are 'start' and 'end' tags for formatting and links that we might find useful in the information extraction process. How we use these is open for discussion.

### Strategic Plan

1. Build heuristics to extract  `Results Statements` and `Figure Statements` from example texts provided by Pathway Logic
2. Develop an annotated test set of this data based on manually annotated documents (from the BioScholar Fragmenter)
3. Classify each fragment according to experiment type (or 'assay type' as defined by PL)
	* Based on discussions with Merrill and Carolyn at Pathway Logic, we are focussing our effort on the following PL assays:
		1. Coprecipitation
		1. Phosphorylation
		1. Protein Expression
		1. In vitro Kinase Studies
		1. Location assays 
		1. GTP Assocation
4. Perform entity detection and information extraction over these fragments and normalize them to a joint KEfED  / PL Datum object model.
5. Build a large-scale extracted database of scientific observations from cancer pathways.

### Work Action Plan

#### Pradeep

1. Run preliminary extraction for result sentences over the 33 coprecipitation papers. 
2. Express the results as JSON (so that Gully can fit this into the BioScholar system)

#### Gully

1. Start work on fragmenting the PL corpus to provide training data.
2. Fit training data from PDFs to offset annotations within the text documents
3. Develop formatting specification for data from fragmenter in JSON
	* *Let's make this JSON-LD to prepare for a Linked Open Data approach.*   	 