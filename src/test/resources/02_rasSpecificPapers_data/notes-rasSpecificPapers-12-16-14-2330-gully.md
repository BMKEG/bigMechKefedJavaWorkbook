Organizing deep reading data for KEfED models. (12/16/14)
----

We here are pulling together some example pieces for reading against models using KEfED and datum objects based on a small subset  papers as a working example. 

The data we need consists of the following pieces.

1. PL Open Access pmid + figure legends 
2. PL datum objects 
3. Experiment text + figure legend text
	* automatically extracted + corrections  
4. KEfED Motif
5. KEfED Data

What data should we put together and how? 
---
This is a first walk through the overall process as a whole.

1. All PMID + Figures from all HTML files downloaded from PL as a list 
	* Code this in Java. 
2. How to get these data from the PL database? 
	* Issue queries to the PL database to get HTML back.
	* Need to read through HTML pages and extract a whole bunch of patterns from them. Basically, given PL's site at [http://light.csl.sri.com/datum](http://light.csl.sri.com/datum), we need to submit data through the form and then poll their data from that. 
	* Ask Carolyn for data access to a JSON store.
3. Experimental text + figure legend text from ALL files 
	* Run Pradeep's script on the data files. 
	* use `ExtractCodedFragmentsAsSentences` to read specified fragments from the digital library into a local text. 
4. Use existing system to build KEfED Motif and Data for each type... Dump these out to data structures for discussion with Hans. 

Scaled back to make it practical
---
Work through this example for the coprecipitation studies only. Provide 10 marked up data elements and use these to directly correct Pradeep's data and use only these. 

*Apologize profusely for taking so long and losing focus on what I needed to be working on. Fix it and get on with it.*
  
Get datum entities from these papers to link to the text. 

These are the papers relevant to RAS:

* 16492808 (in) 
* 11777939 (in)
* 12515821 (in)
* 19050761 (in)
* 16520382 (in)

* 11448999 (out)
* 20929976 (out)
* 12876277 (out)

> I don't know why the `out` papers aren't available. That gives us 5 papers with data curated into Pathway Logic Datums to start work with. 

For these papers, which `Assay` types are used?
---

```
copptby[WB]GTP-association[BDPD]IVGefA(Hras)[3H-GDP]IVGefA(Rac1)[3H-GDP]IVKA(MBP)[32P-ATP]IVLKA(PtdIns)[TLC]phos(S446)[phosAb]phos(T416)[phosAb]phos(TEY)[phosAb]phos(Y1054)[phosAb]phos(Y1059)[phosAb]phos(Y1175)[phosAb]phos(Y1214)[phosAb]phos[WBMS]snaggedby[WB]Yphos[pYAb]
```

**copptby[DetectionMethod]Hook**

* Evidence provided:  the amount of Subject coprecipitated by Hook
* Possible Subject(s): Proteins, Family, or Composites; IP not OK
* Possible DetectionMethod(s): WB  Xlink                   
* Assay Description:
	* Using WB:
		Hook is immunoprecipitated from a cell lysate. A western blot of the immunoprecipitate is stained for Subject.
	* Using Xlink: Cells are treated with a chemical crosslinker before lysis. Hook is immunoprecipitated from cell lysate. A western blot of the immunoprecipitate is stained for Subject.

**GTP-association[DetectionMethod]**

* Evidence provided:  the amount of GTP bound to Subject
* Possible Subject(s): recombinant Protein
* Possible DetectionMethod(s): 35S-GTPgS  32P-GTP  Mant-GTP
* Assay Description: Subject is incubated with labeled GTP and a putative guanine nucleotide exchange factor. Subject is bound to nitrocellulose and washed to removed unbound GTP. 
* The amount of GTP bound is detected by:
	* liquid scintillation counting (for 35S-GTP or 32P-GTP)
	* Cherenkov counting (for 32P-GTP)
	* fluorimeter (for Mant-GTP)

**GTP-bdpd[DetectionMethod]**

* Evidence provided:  the amount of Subject bound to GTP
* Possible Subject(s): Protein, Family, or Composite; IP not OK
* Possible DetectionMethod(s): WB  FRET
* Assay Description:
	* Using WB: The GTPase binding domain of a GTPase effector is added to a cell lysate. The GTPase binding domain is precipitated from a cell lysate. A western blot of the immunoprecipitate is stained for Subject.
	* Using FRET: Cells are transfected with plasmids expressing a chimeric protein consisting of a GTP-binding protein (Subject), the binding domain of a protein that only binds to Subject bound to GTP, and yellow-emitting (YFP) and cyan-emitting (CFP) GFP mutants. GTP binding to Subject increases the efﬁciency of FRET between CFP and YFP.

>  Note that `pmid:11777939-Fig-4b` has the assay set to `GTP-assoc[BDPD]`. Not sure what that means. 

**IVGefA(Substrate(s))[DetectionAssay]**

* Evidence provided:  the ability of the complex immunoprecipitated with Subject to cause exchange of GDP to GTP from a substrate
* Possible Subject(s): Protein, Family, or Composite; IP required
* Possible Substrate(s): Protein
* Possible DetectionMethod(s): 3H-GDP  Mant-GDP
* Assay description:
	* An immunoprecipitate is incubated with a recombinant GTP-binding Protein preloaded with labeled GDP
	* The amount of GDP released is detected by:
		* liquid scintillation counting (3H-GDP)
		* fluorimeter (for Mant-GDP)

**IVKA(Substrate(s))(Site(s))[DetectionMethod]**

* Evidence provided:  the ability of the complex immunoprecipitated with Subject to phosphorylate a substrate
* Possible Subject(s): Protein, Family, or Composite; IP or recombinant Protein required
* Possible Substrate(s): Protein(s), Peptide, auto
* More than one Protein (separated by commas) can be used as substrate to represent coupled reactions.
* Site(s) are only used when phosAb is used as a detection method.
* Possible DetectionMethod(s):  32P-ATP   phosAb pYAb  InGelKinase
* Assay Description:
	* Using 32P-ATP: An immunoprecipitate or recombinant protein is incubated with substrate in a kinase buffer containing required co-factors and [32P]-ATP. The reaction mixture is separated on a SDS-PAGE gel.  The amount of 32P transferred from [32P]-ATP to the substrate is detected by autoradiography.
	* Using phosAb: An immunoprecipitate or recombinant protein is incubated with substrate in a kinase buffer containing required co-factors and ATP. The reaction mixture is separated on a SDS-PAGE gel. The phosphorylation state of the substrate is determined using  an antibody that detects a specific phosphorylation site when it is phosphorylated.
	* Using pYAb: An immunoprecipitate or recombinant protein is incubated with substrate in a kinase buffer containing required co-factors and ATP. The reaction mixture is separated on a SDS-PAGE gel. The phosphorylation state of the substrate is determined using  an antibody to phosphotyrosine.
	* Using InGelKinase: An immunoprecipitate or recombinant protein is separated on a SDS-PAGE gel. The gel is incubated with substrate in a kinase buffer containing required co-factors and [32P]-ATP. The phosphorylation state of the substrate is determined by autoradiography.

**IVLKA(Substrate(s))[DetectionMethod]**

* Evidence provided:  the ability of the complex immunoprecipitated with Subject to phosphorylate a lipid substrate
* Possible Subject(s): Protein, Family, or Composite; IP required
* Substrate(s) can be: Lipid, More than one lipid (separated by slashes) can be used as substrates
* Possible DetectionMethod(s): TLC
* Assay description: An immunoprecipitate is incubated with lipid-substrate in a kinase buffer containing required co-factors and [32P]-ATP. Reaction components are separated by Thin Layer Chromatography (TLC). Production of phosphorylated lipid substrate is measured by autoradiography.

**phos[DetectionMethod]**

* Evidence provided:   the total phosphorylation of Subject
* Possible Subject(s): Protein or Family; IP OK
* Possible DetectionMethod(s):  32P-ATP  32Pi  WBMS  WBMS/PPase
* Assay Description:
	* Using 32P-ATP: Subject is incubated with a putative kinase (Treatment) in a kinase buffer containing required co-factors and [32P]-ATP. The reaction mixture is separated on a SDS-PAGE gel. The amount of 32P transferred from [32P]-ATP to Subject is detected by autoradiography.
	* Using 32Pi: Cells are preincubated with radioactive inorganic phosphate (32Pi). Subject is immunoprecipitated and run on an SDS-PAGE gel. The amount of 32P in the immunoprecipitate is detected by autoradiography.
	* Using WBMS: A western blot of a cell lysate or immunoprecipitate is stained with an antibody to Subject. Phosphorylation is implied by a decrease in the mobility shift of Subject.
	* Using WBMS/PPase: A western blot of a cell lysate or immunoprecipitate is stained with an antibody to Subject. Phosphorylation is implied by a decrease in the mobility shift of Subject. Phosphorylation is confirmed by a loss of mobility shift in response to treating lysate or immunoprecipitate with a phosphatase before western blot.

**snaggedby[DetectionMethod]Hook**

* Evidence provided:  the amount of Subject "pulled-down" from a lysate by Hook
* Possible Subject: Protein, Family, or Composites; IP not OK
* Possible Hook: recombinant or purified Protein or Chemical
* Possible DetectionMethod(s): WB
* Assay Description: 
	* Using WB: Hook is added to a cell lysate. Hook is precipitated from a cell lysate. A western blot of the precipitate is stained for Subject.

**Yphos[DetectionMethod]**

* Evidence provided:  the phosphorylation of Subject on tyrosine
* Possible Subject(s): Protein, Family, or Composite; IP OK
* Possible DetectionMethod(s): pYAb  pYAb-ppt  32Pi-PAA
* Assay description:
	* Using pYAb: A western blot of an anti-Subject immunoprecipitate is stained with an antibody to phosphotyrosine.
	* Using pYAb-ppt: A western blot of an anti-phosphotyrosine immunoprecipitate is stained with an antibody to Subject.
	* Using 32Pi-PAA: Cells are preincubated with radioactive inorganic phosphate (32Pi). Subject is immunoprecipitated and run on an SDS-PAGE gel. The band containing Subject is cut out of the gel and analyzed for serine and threonin phosphorylation by Phosphoamino Acid Analysis.

ORCA Codes to improve experimental text segmentation
---

1. The authors make some interpretive assertion about the phenomenon, citing other work as a starting point for the experiment (this isn't shown in the text above, but this does happen a lot). [typical ORCA code: v2_bD_cN]
2. Establish the purpose of the experiment. Usually this looks very much like the sentence above saying something like  'To investigate XYZ' or some variation. [typical ORCA code: v1_bR_cA]
3. Say what they did:  "U2OS cells were immunostained ... used preferentially" [typical ORCA code: ????, not sure]
4. Say what the results were and maybe interpret them directly: "... the vast majority of 14-3-3 had a diffuse cytoplasmic and perinuclear distribution in unsychronized cells..." [typical ORCA code: v3_bD_cA]
5. Put this finding into context with other data: "consistent with findings reported by others" or summarize the main findings of a set of experiments. [typical ORCA code: v2_bD_cN]

ORCA Annotation Notes
---

1. We need an extra code for methodological statements saying 'we did x,y,z'.
2. Use whole figure numbers to denote general narrative sections of the paper
3. Combine figure numbers together where necessary. 
4. Use `v1_bD_sA` to denote passages where authors were specifying they were going to do something *in order to* examine some underlying mechanism. 
5. Use `1u` as a fragment code to denote 'unpublished data' pertaining to the information surrounding Figure 1. 
6. We are currently ignoring large-scale interpretations. We need to code and examine these as well. 
7. Try to preserve the order of the narrative by picking fragment numbers appropriately.
