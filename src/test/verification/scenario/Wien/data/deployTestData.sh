#/bin/bash

cd ../../..

GRAPH=http://test.com/benchmark/scenario_1

DIR=src/test/python/scenario_1

CONFIG_R43PLES="r43ples.stardog.conf"

# create graph 
mvn exec:java --quiet -Dconsole -Dexec.args="--config $CONFIG_R43PLES --new --graph $GRAPH"
for i in {1..20}
do
    echo "Inserting revision $i"
    ADD=$DIR/scenario_1_commit_${i}_inserts.nt
    DEL=$DIR/scenario_1_commit_${i}_deletes.nt
    mvn exec:java --quiet -Dconsole -Dexec.args="--config $CONFIG_R43PLES -g $GRAPH -a $ADD -d $DEL -m 'benchmark commit $i'"
done 
