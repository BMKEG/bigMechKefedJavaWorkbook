---
title: KEfED Database Construction
description:  Here we describe the development of a database representation of KEfED data based on generating a linked set of context / value relations.  
layout: defaultTOC
prevPage: 10modelsOfCoprecipitationPapers.html
nextPage: 11kefedDatabaseConstruction.html
---

### Rationale:

Curating models & data into the previous BioScholar system based on a simple JSON store is now outdated and should be updated into the current KEfED VPDMf model. The goal here is *not* to fully rely on the VPDMf architecture (which is a little cumbersome), but to implement the data simply as a relational database store with minimal overhead.  

### Backup of previous data:

Changes to the model 

1. Removed additional data constructs (such as `StatisticalAssertion`). If needed, we will add these back in. It is likely that we will develop these as data workflow elements that occur downstream of primary measurements.  
2. Altered the curation elements in `vpdmf-bioscholar.xml`