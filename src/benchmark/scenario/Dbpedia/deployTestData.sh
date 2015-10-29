#/bin/bash

# Add dpbedia data to Stardog
# Take care to go to data directory beforehand
# Take care to configuer and epxort STARDOG_HOME and put Stardog binaries on PATH


DATA_DIRECTORY=data
GRAPH="http://dbpedia.org"


stardog-admin server start

# create database and add compressed data
stardog-admin db create -n dbpedia -v --named-graph $GRAPH $DATA_DIRECTORY/dbpedia_2013_07_18.nt.bz2

# add ontology data
stardog data add dbpedia -v --named-graph $GRAPH $DATA_DIRECTORY/dbpedia_2014.owl.bz2

stardog data add dbpedia -v --named-graph $GRAPH $DATA_DIRECTORY/2015_06_18/instance_types_en.nt.bz2




CONFIG_STARDOG="../../../r43ples.stardog.dbpedia.conf"

CONFIG=$CONFIG_STARDOG

cd ../../
JAR=../../../target/r43ples-console-client-jar-with-dependencies.jar



java -jar $JAR --config $CONFIG --new --graph $GRAPH

java -jar $JAR --config $CONFIG -g $GRAPH -a $P/000277.added.nt -m 'benchmark commit'
java -jar $JAR --config $CONFIG -g $GRAPH -a $P/000276.added.nt -m 'benchmark commit'