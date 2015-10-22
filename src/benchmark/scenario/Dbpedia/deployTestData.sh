#/bin/bash

P=/media/mgraube/CV_IE_VMs1/dbpedia/

# add ontology data
# stardog data add dbpedia -v --named-graph http://dbpedia.org $P/2015_06_18/dbpedia_2014.owl.bz2


# stardog data add dbpedia -v --named-graph http://dbpedia.org $P/2015_06_18/instance_types_en.nt.bz2


# stardog data add dbpedia -v --named-graph http://dbpedia.org $P/dbpedia_2013_07_18.nt.bz2

CONFIG_STARDOG="../../../r43ples.stardog.dbpedia.conf"

CONFIG=$CONFIG_STARDOG

cd ../../
JAR=../../../target/r43ples-console-client-jar-with-dependencies.jar

GRAPH=http://dbpedia.org


java -jar $JAR --config $CONFIG --new --graph $GRAPH

java -jar $JAR --config $CONFIG -g $GRAPH -a $P/000277.added.nt -m 'benchmark commit'
java -jar $JAR --config $CONFIG -g $GRAPH -a $P/000276.added.nt -m 'benchmark commit'