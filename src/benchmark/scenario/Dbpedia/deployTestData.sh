#/bin/bash

# Add dpbedia data to Stardog
# Take care to go to data directory beforehand
# Take care to configuer and epxort STARDOG_HOME and put Stardog binaries on PATH


GRAPH="http://dbpedia.org"


stardog-admin server start

# create database and add compressed data
stardog-admin db create -n dbpedia -o "strict.parsing=false" -v 

# add ontology data
stardog data add dbpedia -v --named-graph $GRAPH data/dbpedia_3.9.owl
# Added 391.015.190 triples in 67:01:30.856 (ca. 32 GB dataset seize)
stardog data add dbpedia -v --named-graph $GRAPH data/dbpedia_2013_07_18.nt





CONFIG_STARDOG="../../../r43ples.stardog.dbpedia.conf"

CONFIG=$CONFIG_STARDOG

cd ../../
JAR=../../../target/r43ples-console-client-jar-with-dependencies.jar



java -jar $JAR --config $CONFIG --new --graph $GRAPH

java -jar $JAR --config $CONFIG -g $GRAPH -a $P/000277.added.nt -m 'benchmark commit'
java -jar $JAR --config $CONFIG -g $GRAPH -a $P/000276.added.nt -m 'benchmark commit'
