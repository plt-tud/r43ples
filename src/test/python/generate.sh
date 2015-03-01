#/bin/bash

rm -R scenario_1 scenario_2
mkdir scenario_1
mkdir scenario_2

python testdata.py 1 -c -f 10 nt scenario_1
python testdata.py 2 -c -f 10 nt scenario_2


cd ../../..

GRAPH=http://test.com/benchmark/scenario_1

DIR=src/test/python/scenario_1



# create graph 
mvn exec:java -Dconsole -Dexec.args="--create --graph $GRAPH"
for i in {1..20}
do
    echo $i
    ADD=$DIR/scenario_1_commit_${i}_inserts.nt
    echo $ADD
    DEL=$DIR/scenario_1_commit_${i}_deletes.nt
    echo $DEL
    mvn exec:java -Dconsole -Dexec.args="-g $GRAPH -a $ADD -d $DEL"
done


