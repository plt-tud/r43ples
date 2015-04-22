#! /bin/bash

ENDPOINT=http://localhost:9998/r43ples/sparql

GRAPH=http://test.com/scenario_1

TIME_FILE=time.`date +%Y-%m-%d_%H:%M:%S`.log

RUNS=20
TIMEFORMAT=%R

DATASETPATH=../resources/dataset

function singleTest {
 echo -n "ISQL-SPARQL $TRIPLES  " >> $TIME_FILE
    {
        time {
            curl $ENDPOINT $VOS_USER $VOS_PW $SQL_FILE;
        }
    } 2>>$TIME_FILE
}


echo "# Run on $ENDPOINT at `date`" > $TIME_FILE
echo "Mode  Triples  Time" >> $TIME_FILE

for runs in $(seq $RUNS); do
    for n in $(seq 2 5); do
        N=`echo "1*10^$n" | bc`
        singleTest $N
        N=`echo "2*10^$n" | bc`
        singleTest $N
        N=`echo "5*10^$n" | bc`
        singleTest $N
    done
    notify-send "R43ples Performance Test" "Run $runs/$RUNS completed"
done


Rscript EvaluatePerformance.R $TIME_FILE
notify-send -t 0 "R43ples Performance Test" "All Test completed"
