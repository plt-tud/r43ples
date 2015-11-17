#/bin/bash





DIR=`pwd`/data

# CONFIG_R43PLES="config/r43ples.stardog.conf"
CONFIG_TDB="conf/r43ples.tdb.conf"
CONFIG_STARDOG="conf/r43ples.stardog.conf"

CONFIG=$CONFIG_TDB

cd ../../
JAR=../../../target/r43ples-console-client-jar-with-dependencies.jar


function deployScenario {
    CONFIG=$1
    DATASET=$2
    CHANGESIZE=$3
    
    GRAPH=http://test.com/benchmark/LDQ2014/dataset-$DATASET-$CHANGESIZE
    
    java -jar $JAR --config $CONFIG --new --graph $GRAPH
    java -jar $JAR --config $CONFIG -g $GRAPH -a $DIR/dataset-$DATASET.nt -m 'benchmark commit initial'
    for i in {1..20}
    do
        echo "Inserting revision $i"
        ADD=$DIR/addset-$CHANGESIZE-${i}.nt
        java -jar $JAR --config $CONFIG -g $GRAPH -a $ADD -m "benchmark commit $i"
    done 
}

deployScenario $CONFIG 100 50
deployScenario $CONFIG 1000 50
deployScenario $CONFIG 10000 50
deployScenario $CONFIG 100000 50
deployScenario $CONFIG 1000000 50


deployScenario $CONFIG 10000 10
deployScenario $CONFIG 10000 30
deployScenario $CONFIG 10000 70
deployScenario $CONFIG 10000 90