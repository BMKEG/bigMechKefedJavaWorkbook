---
title: Pathway Logic Experiment Types
description:  Here we describe working with the different assay types within the Pathway Logic database and how this might fit in to a KEfED-based strategy.  
layout: defaultTOC
prevPage: 07heuristicsForResultText.html
nextPage: 09initialResultsKEfEDEpistemicsStudy.html
---

Based on our simple parsing of html forms from the Pathway Logic database, we have identified ~1966 papers that have been curated into their system. 

Of these 1966, only 73 are available in the open access dataset. Here, we list these by assay type. These are also listed in the digital library under the Corpus labeled `PathwayLogicFullText`. 

<table>
<tr><td>Assay</td><td># papers in OA</td></tr><tr><td>coprecipitation</td><td>33</td></tr><tr><td>Phosphorylation</td><td>28</td></tr><tr><td>ProteinExpression</td><td>14</td></tr><tr><td>inVitroKinase</td><td>13</td></tr><tr><td>location</td><td>13</td></tr><tr><td>mRNAExpression</td><td>13</td></tr><tr><td>InFraction</td><td>12</td></tr><tr><td>SnaggedBy</td><td>11</td></tr><tr><td>directProteinBinding</td><td>9</td></tr><tr><td>OligoBinding</td><td>6</td></tr><tr><td>Oligomerization</td><td>6</td></tr><tr><td>GTPAssoc</td><td>5</td></tr><tr><td>SurfaceExpression</td><td>4</td></tr><tr><td>Ubiquitination</td><td>4</td></tr><tr><td>Upshift</td><td>4</td></tr><tr><td>GenePromoterReporter</td><td>3</td></tr><tr><td>chromatin</td><td>3</td></tr><tr><td>colocalization</td><td>3</td></tr><tr><td>gal4binding</td><td>3</td></tr><tr><td>inVitroGEFActivity</td><td>3</td></tr><tr><td>Polymerization</td><td>1</td></tr><tr><td>acetylation</td><td>1</td></tr><tr><td>inVitroHat</td><td>1</td></tr><tr><td>inVitroPPhase</td><td>1</td></tr><tr><td>methylation</td><td>1</td></tr><tr><td>GTPHydrol</td><td>0</td></tr><tr><td>Neddylation</td><td>0</td></tr><tr><td>NuclearExport</td><td>0</td></tr><tr><td>NuclearImport</td><td>0</td></tr><tr><td>ProteinStability</td><td>0</td></tr><tr><td>cleavage</td><td>0</td></tr><tr><td>gdpDissosc</td><td>0</td></tr><tr><td>internalization</td><td>0</td></tr> </table>
 
Since `Coprecipitation` is the largest contributor in this list, we now list all figures from the OA data set that describe Coprecipitation experiments. This provides an initial training set that we may now attempt to model and understand with 

<table>
<th><td>pmid</td><td>figure</td></th><tr><td>23142775</td><td>-Fig-5a</td></tr><tr><td>23142775</td><td>-Fig-5b</td></tr><tr><td>10790433</td><td>-Fig-9a</td></tr><tr><td>18583988</td><td>-Fig-2e</td></tr><tr><td>19274086</td><td>-Fig-4d</td></tr><tr><td>19274086</td><td>-Fig-4b</td></tr><tr><td>22833096</td><td>-Fig-2d</td></tr><tr><td>22833096</td><td>-Fig-2a</td></tr><tr><td>22833096</td><td>-Fig-2c</td></tr><tr><td>14517278</td><td>-Fig-3b</td></tr><tr><td>10790433</td><td>-Fig-8a</td></tr><tr><td>10871282</td><td>-Fig-4b</td></tr><tr><td>10790433</td><td>-Fig-7e</td></tr><tr><td>11524436</td><td>(D)</td></tr><tr><td>16520382</td><td>-Fig-2a</td></tr><tr><td>11777939</td><td>-Fig-7a</td></tr><tr><td>7561682</td><td>-Fig-1a</td></tr><tr><td>16717130</td><td>-Fig-1d</td></tr><tr><td>11864996</td><td>-Fig-5c</td></tr><tr><td>11777939</td><td>-Fig-5[tl]</td></tr><tr><td>10871282</td><td>-Fig-1d</td></tr><tr><td>10871282</td><td>-Fig-2b</td></tr><tr><td>19783983</td><td>-Fig-5a</td></tr><tr><td>19783983</td><td>-Fig-5b</td></tr><tr><td>9625770</td><td>-Fig-3a</td></tr><tr><td>10601358</td><td>-Fig-5c</td></tr><tr><td>15767370</td><td>-Fig-1a,1c</td></tr><tr><td>15767370</td><td>-Fig-1a</td></tr><tr><td>18583988</td><td>-Fig-4b</td></tr><tr><td>9625770</td><td>-Fig-2a</td></tr><tr><td>18411307</td><td>-Fig-1a</td></tr><tr><td>18411307</td><td>-Fig-1c</td></tr><tr><td>18411307</td><td>-Fig-2c</td></tr><tr><td>18411307</td><td>-Fig-1e</td></tr><tr><td>18411307</td><td>-Fig-7a</td></tr><tr><td>15314656</td><td>-Fig-4a</td></tr><tr><td>19734906</td><td>-Fig-6c</td></tr><tr><td>19734906</td><td>-Fig-6b</td></tr><tr><td>12515821</td><td>-Fig-S1b</td></tr><tr><td>12515821</td><td>-Fig-1a</td></tr><tr><td>12515821</td><td>-Fig-1e</td></tr><tr><td>12515821</td><td>-Fig-3a</td></tr><tr><td>20337593</td><td>-Fig-2b</td></tr><tr><td>16520382</td><td>-Fig-2d</td></tr><tr><td>16520382</td><td>-Fig-2e</td></tr><tr><td>16520382</td><td>-Fig-2b</td></tr><tr><td>22833096</td><td>-Fig-2b</td></tr><tr><td>14517278</td><td>-Fig-6c</td></tr><tr><td>14517278</td><td>-Fig-6b</td></tr><tr><td>23142775</td><td>-Fig-S4a</td></tr><tr><td>12370254</td><td>-Fig-2a</td></tr><tr><td>19783983</td><td>-Fig-S2a</td></tr><tr><td>20337593</td><td>-Fig-2a</td></tr><tr><td>16492808</td><td>-Fig-1c</td></tr><tr><td>19234442</td><td>-Fig-3e</td></tr><tr><td>19234442</td><td>-Fig-2b</td></tr><tr><td>19234442</td><td>-Fig-5d</td></tr><tr><td>19234442</td><td>-Fig-5c</td></tr><tr><td>19234442</td><td>-Fig-3b</td></tr><tr><td>19234442</td><td>-Fig-2a</td></tr><tr><td>19234442</td><td>-Fig-3d</td></tr><tr><td>19234442</td><td>-Fig-5e</td></tr><tr><td>19234442</td><td>-Fig-5f</td></tr><tr><td>20026654</td><td>-Fig-4b</td></tr><tr><td>20026654</td><td>-Fig-4a</td></tr><tr><td>19274086</td><td>-Fig-4a</td></tr><tr><td>21573184</td><td>-Fig-2a</td></tr><tr><td>21573184</td><td>-Fig-3a</td></tr><tr><td>21573184</td><td>-Fig-3b</td></tr><tr><td>21573184</td><td>-Fig-3c</td></tr><tr><td>21573184</td><td>-Fig-S1c</td></tr><tr><td>21573184</td><td>-Fig-1a</td></tr><tr><td>21573184</td><td>-Fig-1c</td></tr><tr><td>16729043</td><td>-Fig-5b</td></tr><tr><td>11777939</td><td>-Fig-4a</td></tr><tr><td>10871282</td><td>-Fig-3b</td></tr><tr><td>19675569</td><td>-Fig-3b</td></tr><tr><td>10601358</td><td>-Fig-3b</td></tr><tr><td>11777939</td><td>-Fig-8c</td></tr><tr><td>11777939</td><td>-Fig-8a</td></tr><tr><td>11777939</td><td>-Fig-8b</td></tr><tr><td>19734906</td><td>-Fig-6a</td></tr><tr><td>12515821</td><td>-Fig-3c</td></tr><tr><td>11777939</td><td>-Fig-1c</td></tr><tr><td>11777939</td><td>-Fig-1b</td></tr><tr><td>11777939</td><td>-Fig-3b</td></tr><tr><td>16520382</td><td>-Fig-2c</td></tr><tr><td>19112497</td><td>-Fig-2d</td></tr><tr><td>19112497</td><td>-Fig-1d</td></tr><tr><td>19112497</td><td>-Fig-1c</td></tr><tr><td>19112497</td><td>-Fig-1b</td></tr><tr><td>19112497</td><td>-Fig-2c</td></tr><tr><td>21629263</td><td>-Fig-6c</td></tr><tr><td>21629263</td><td>-Fig-6d</td></tr><tr><td>19050761</td><td>-Fig-5j</td></tr><tr><td>19050761</td><td>-Fig-1c</td></tr><tr><td>18215320</td><td>-Fig-S1</td></tr></table>

An example of coprecipitation is shown below: 

From `Parvatiyar-2012-13-1155` (pmid: 23142775), Fig-5a

From page 1159, narrative text:
 
> The introduction of either c-di-GMP or c-di-AMP into D2SC cells led to enhanced formation of the DDX41-STING complex (Fig. 5a). 

Based on this figure: 

![](images/23142775-fig5a.jpg)

After manual curation, this provides this model:

This model generates this KEfED data structure:

![](images/23142775-fig5a-kefed.jpg)

Which when represented as a data structure looks like this:

	?protein-concentration 
		[?cell-type][?reagent][?duration][?primary-antibody][?primary-antibody] 

The experiment shows the behavior of  DDX41 / STING 
 complex concentration based on this value (which corresponds is the top row of the figure).  

	?protein-concentration 
		[D2SC][?reagent][4h][DDX41][STING] 

See this link for definitions of Pathway Logic Assays: [http://pl.csl.sri.com/CurationNotebook/index.html](http://pl.csl.sri.com/CurationNotebook/index.html) 

### Identifying the most relevant assays for Ras-based work.

From the 71 "open access" pmids, there are 1716 datums.
24 have Hras, Braf, Raf1 or Rac1 as subject.

These come from 8 papers: [16492808, 11448999, 11777939, 12515821, 19050761, 20929976, 16520382, 12876277]

The majority of these were about Rac1

They include the following assays
[copptby, GTP-association, phos, boundto, IVKA]

### Highest priority assays

* coprecipitation	
* Phosphorylation	
* ProteinExpression	
* inVitroKinase
* location
* GTPAssoc

## Open Access Phosphorylation Papers

231427751927408610790433186041981671713019112497128762778376945126548981698464517470642171016931995308522833096164613391576737096257702162926315320955196755691841130718583988114831581923444219783983153146562002665416618811


