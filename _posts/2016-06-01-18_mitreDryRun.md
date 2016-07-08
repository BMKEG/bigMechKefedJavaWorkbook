---
title: Dry Run II Papers from MITRE
description: Dry Run Evaluation Papers from MITRE to identify level 1 statements
layout: defaultTOC
---
PMCID | PMID | NXML?
|----+----+----|
PMC1234335 | 16135815 | n
PMC3178447 | 20179705 | y
PMC3690480 | 23706742 | y
PMC4345513 | 24602610 | y
PMC534114 |  15550174 | y
PMC4329006 | 25449683 | y
PMC3595493 | 23392125 | y
PMC4729484 | 26816343 | y
PMC2841635 | 20333297 | y
PMC4052680 | 24467442 | y

Not all of these papers are available as *.nxml. All of them are available as PDF.

# Running Preprocessing 

Note that FRIES preprocessing does not seem to work currently. May need to update REACH.

# Running SciDP on Soweto 

The embeddings are here `/usr1/shared/projects/bigmech/data/embeddings/pyysalo_et_al`

Pradeep's version of the code is here: `/usr1/home/pdasigi/workspace/sciDP/`

Importantly, this includes the model files: `model_att=*`, which we copy into a shared  
version of sciDP here: `/usr1/shared/projects/bigmech/tools/sciDP`

**Training**

```
python nn_passage_tagger.py /usr1/shared/projects/bigmech/data/embeddings/pyysalo_et_al/PubMed-and-PMC-w2
v.txt.gz --train_file /usr1/shared/projects/bigmech/data/discourse_tagging/train+test_data/passage_train.txt --use_attention
```

**Testing**


## Setting up dependencies locally so that I can run sciDP:

* Use Anaconda2-2.5.0-Linux-x86_64.sh
* `conda install theano`
* `conda install --channel https://conda.anaconda.org/kundajelab keras`

# Running the classifier: 

```
   python nn_passage_tagger.py 
      /usr1/shared/projects/bigmech/data/embeddings/pyysalo_et_al/PubMed-and-PMC-w2v.txt.gz 
      --test_files /usr1/shared/projects/bigmech/corpora/2016-06-01-DryRun2MITRE/ --use_attention
```

