#! /bin/bash

EP=http://localhost:9998/r43ples/sparql
TIME_FILE=logs/time.`date +%Y-%m-%d_%H:%M:%S`.log

RUNS=4
TIMEFORMAT=%R




function singleTest {
    MODE=$1
    REVISION=$2
    ENDPOINT=$3
    QUERY_TEMPLATE=$4
    QUERY=`sed -e "s/%%%REV%%%/$REVISION/" $QUERY_TEMPLATE`
    echo -n "$MODE; $QUERY_TEMPLATE; $REVISION; " >> $TIME_FILE
    {
        time {
            curl -H "Accept: application/sparql-results+xml" --data "join_option=$MODE&query=$QUERY" $ENDPOINT;
        }
    } 2>>$TIME_FILE
}


echo "# Run on $ENDPOINT at `date`" > $TIME_FILE
echo "Join; Query; Revision; Time" >> $TIME_FILE



# simple
for runs in $(seq $RUNS); do
    for revision in {1..5}; do
        singleTest "new" $revision $EP scenario/query1.rq
        singleTest "off" $revision $EP scenario/query1.rq
    done
#     notify-send "R43ples Performance Test" "Run $runs/$RUNS completed"
done

# Wien
for runs in $(seq $RUNS); do
    for revision in {1..20}; do
        singleTest "new" $revision $EP scenario/Wien/query4.rq
        singleTest "off" $revision $EP scenario/Wien/query4.rq
    done
#     notify-send "R43ples Performance Test" "Run $runs/$RUNS completed"
done

# LDQ 2014
for runs in $(seq $RUNS); do
    for revision in {1..20}; do
        singleTest "new" $revision $EP scenario/LDQ2014/query-100-50.rq
        singleTest "off" $revision $EP scenario/LDQ2014/query-100-50.rq
        singleTest "new" $revision $EP scenario/LDQ2014/query-1000-50.rq
        singleTest "off" $revision $EP scenario/LDQ2014/query-1000-50.rq
        singleTest "new" $revision $EP scenario/LDQ2014/query-10000-50.rq
        singleTest "off" $revision $EP scenario/LDQ2014/query-10000-50.rq
        singleTest "new" $revision $EP scenario/LDQ2014/query-100000-50.rq
        singleTest "off" $revision $EP scenario/LDQ2014/query-100000-50.rq
    done
#     notify-send "R43ples Performance Test" "Run $runs/$RUNS completed"
done

# Rscript EvaluatePerformance.R $TIME_FILE
notify-send -t 0 "R43ples Performance Test" "All Test completed"
