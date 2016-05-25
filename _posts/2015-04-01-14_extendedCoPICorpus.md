---
title: Extended Coprecipitation Frames v2 
description:  Adding some data to the core annotated data for Binding / Coprecipitation.  
layout: defaultTOC
prevPage: 13coPIFramesV2.html
nextPage: 13coPIFramesV2.html
---

# Extending the  Molecular Interaction Corpus. 

Here we extend the existing available annotated data with 14 extra files by broadening 
the search for papers involved in binding experiments beyond the set annotated within 
Pathway Logic. 

# All papers in expanded set

* 10508858
* 10601358
* 10871282
* 11157988
* 11777939
* 12515821
* 12865932
* 14517278
* 15479739
* 15798771
* 16492808
* 16520382
* 18411307
* 18950493
* 19050761
* 19112497
* 19234442
* 19274086
* 19568437
* 20337593
* 21573184
* 21629263
* 22205990
* 22216253
* 22641287
* 22833096
* 23142775
* 23405264
* 23637769
* 23750284

# Annotated data

Here we present a larger set of papers marked up for coprecipitation experiments (this
file includes the data presented previously).

* [coPI\_binds\_corpus_03-30-15-1637.zip](data/coPI_binds/copi_binds_corpus_04-30-15-1836.zip.zip)

# Organization of the annotations (brat markup)

```
!Protocol
	process
	entity

!Coprecipitation-Context-Object
	cell-type
	transfection-molecule
	mutation-molecule
	incubation-reagent
	ip-molecule
	molecular-weight
	time-point
	assay-molecule
	immunodepletion
	mutation

!Coreference-Object
	coreference-target

!Measured-Object
	m-obj

!Binds-Objects
	binds-strength
	binds-catalysis
	binds-constituent1
	binds-constituent2
	binds-constituent3
	binds-controller
	binds-complex
	binds-site

!Measured-Value
	m-val
	m-comp
	
[attributes]

[relations]

<OVERLAP>	Arg1:<ENTITY>, Arg2:<ENTITY>, <OVL-TYPE>:<ANY>

[events]

```

# How the reports for different frame elements distribute over the discourse types

Frame | fact | goal | hypothesis | implication | method | other-hypothesis | other-implication | other-result | problem | result |  Total
-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----|
**binds** |  |  |  |  |  |  |  |  |  |  | 2499
binds-catalysis | 1 | 8 |  | 9 | 11 |  |  | 9 | 6 | 49 | 93
binds-complex | 3 | 13 | 3 | 19 | 28 |  | 1 | 10 | 1 | 41 | 119
binds-constituent1 | 8 | 70 | 12 | 92 | 180 | 2 | 2 | 82 | 29 | 320 | 797
binds-constituent2 | 7 | 64 | 8 | 90 | 172 | 5 | 2 | 75 | 26 | 300 | 749
binds-constituent3 | 1 | 9 | 1 | 10 | 12 | 1 |  | 9 | 3 | 18 | 64
binds-controller | 1 | 16 | 2 | 31 | 41 |  |  | 14 | 3 | 86 | 194
binds-site |  | 16 | 2 | 38 | 84 | 1 | 1 | 28 |  | 128 | 298
binds-strength | 2 | 21 | 4 | 13 | 24 |  |  | 22 | 16 | 83 | 185
|-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----|
**copi**  |  |  |  |  |  |  |  |  |  |  |  2817
assay-molecule |  | 22 |  | 29 | 196 |  |  | 25 | 4 | 160 | 436 
cell-type | 2 | 26 | 1 | 23 | 130 |  |  | 31 | 14 | 91 | 318
coreference-target |  | 14 | 1 | 30 | 55 |  |  | 18 | 1 | 68 | 187
entity |  | 15 |  | 35 | 117 |  | 2 | 19 |  | 73 | 261
incubation-reagent |  | 12 |  | 15 | 87 |  |  | 11 | 3 | 77 | 205
ip-molecule |  | 21 |  | 37 | 181 |  |  | 26 | 3 | 158 | 426
m-comp |  |  |  | 1 | 1 |  |  |  |  | 13 | 15
m-obj |  |  |  |  | 1 |  |  |  |  | 2 | 3
m-val |  | 10 |  | 13 | 30 |  |  | 9 | 2 | 87 | 151
molecular-weight |  |  |  | 2 | 6 |  |  |  |  | 10 | 18
mutation |  |  | 1 |  | 1 |  |  |  |  |  | 2
mutation-molecule |  |  |  |  | 4 |  |  |  |  |  | 4
process |  | 26 |  | 53 | 217 |  |  | 35 |  | 125 | 456
time-point |  | 5 |  |  | 7 |  |  | 5 | 5 | 15 | 37
transfection-molecule |  | 22 |  | 40 | 134 |  | 2 | 22 | 5 | 73 | 298
Grand Total | 25 | 390 | 35 | 580 | 1719 | 9 | 10 | 450 | 121 | 1977 | 5316 
