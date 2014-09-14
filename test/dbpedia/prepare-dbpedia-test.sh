#!/bin/bash

# ontology

bunzip2 dbpedia_3.9.owl.bz2


# instance data
wget http://live.dbpedia.org/dumps/dbpedia_2013_07_18.nt.bz2
bunzip2 dbpedia_2013_07_18.nt.bz2
split --number=l/100 -d dbpedia_2013_07_18.nt dbpedia_2013_07_18.split.

wget -r -np -nd -nH -L -e robots=off -A "gz, html" http://live.dbpedia.org/changesets/2013/11/04/13/
gunzip *.nt.gz
