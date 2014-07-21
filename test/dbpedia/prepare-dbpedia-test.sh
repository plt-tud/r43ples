#!/bin/bash

wget http://live.dbpedia.org/dumps/dbpedia_2013_07_18.nt.bz2
bunzip2 dbpedia_2013_07_18.nt.bz2

wget -r -np -nH -L -e robots=off -A "gz, html" http://live.dbpedia.org/changesets/2013/11/04/13/