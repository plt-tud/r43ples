#/bin/bash





DIR=`pwd`/data

CONFIG_R43PLES="r43ples.stardog.conf"

# create graph 
cd ../../../../..

# Dataset 1000
function deployScenario {
    DATASET=$1
    CHANGESIZE=$2

    GRAPH=http://test.com/benchmark/LDQ2014/dataset-$DATASET-$CHANGESIZE
    mvn exec:java --quiet -Dconsole -Dexec.args="--config $CONFIG_R43PLES --new --graph $GRAPH"
    mvn exec:java --quiet -Dconsole -Dexec.args="--config $CONFIG_R43PLES -g $GRAPH -a $DIR/dataset-$DATASET.nt -m 'benchmark commit initial'"
    for i in {1..20}
    do
        echo "Inserting revision $i"
        ADD=$DIR/addset-$CHANGESIZE-${i}.nt
        mvn exec:java --quiet -Dconsole -Dexec.args="--config $CONFIG_R43PLES -g $GRAPH -a $ADD -m 'benchmark commit $i'"
    done 
}

deployScenario 100 50
deployScenario 1000 50
deployScenario 10000 50
deployScenario 100000 50
deployScenario 1000000 50


deployScenario 10000 10
deployScenario 10000 30
deployScenario 10000 70
deployScenario 10000 90