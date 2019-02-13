#! /bin/bash


RUNS=10
TIMEFORMAT=%R


function init_log_file {
    export TIME_FILE=logs/time.`date +%Y-%m-%d_%H:%M:%S`.log
    
    export EP_TDB=http://localhost:9998/r43ples/sparql
    export EP_STARDOG=http://localhost:9997/r43ples/sparql
    
    echo "# R43ples Performance test at `date`" > $TIME_FILE
    
}


function singleQuery {
    ENDPOINT=$1
    MODE=$2
    QUERY=$3

    {
        time {
            curl -H "Accept: application/sparql-results+xml" --data "query_rewriting=$MODE&query=$QUERY" $ENDPOINT 
        }
    } 2>>$TIME_FILE
}


function dbpeda_query {
    REVISION=$1
    QUERY=`sed -e "s/%%%REVISION%%%/$REVISION/;" query_template.rq`

    echo -n "$REVISION;STARDOG;new;" >> $TIME_FILE
    singleQuery $EP_STARDOG new "$QUERY"
    
#     echo -n "$REVISION;TDB;new;" >> $TIME_FILE
#     singleQuery $EP_TDB new "$QUERY"
}


# DBpedia
function dbpedia_test {
    init_log_file
    echo "#DBpedia scenario"db >> $TIME_FILE
    echo "Revision;Endpoint;Mode;Time" >> $TIME_FILE
     
    for runs in $(seq $RUNS); do
        for revision in {130..120}; do
            echo $revision
             dbpeda_query $revision
        done
        notify-send -t 0 "R43ples Performance Test" "Run $runs/$RUNS completed"
    done
    
    notify-send -t 0 "R43ples Performance Test" "All Test completed"
}


# dbpedia_test

