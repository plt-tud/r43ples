#! /bin/bash

VOS_HOST=localhost
RDF_FILE=test.ttl
GRAPH=test.graph
SQL_FILE=update.sql
TIME_FILE=time.`date +%Y-%m-%d_%H:%M:%S`.log

VOS_USER=dba
VOS_PW=dba

TIMEFORMAT=%R

DATASETPATH=../resources/dataset


function createRdfFile {
  TRIPLES=$1
  RDF_FILE=$2
  CONNECTIONS=2
  NODES=`echo "$TRIPLES/$CONNECTIONS" | bc `

  java -jar random2rdf.jar Poissonian $NODES $CONNECTIONS > $RDF_FILE.with.duplicates
  rapper -g -o ntriples $RDF_FILE.with.duplicates > $RDF_FILE
  echo "Dataset created: $RDF_FILE ($TRIPLES)"
}


# echo "Create Datasets"
# createRdfFile 100 $DATASETPATH/dataset-100.nt
# createRdfFile 1000 $DATASETPATH/dataset-1000.nt
# createRdfFile 10000 $DATASETPATH/dataset-10000.nt
# createRdfFile 100000 $DATASETPATH/dataset-100000.nt


# rm $DATASETPATH
NUMBER_CHANGES=20
echo "Create Changesets"
for i in $(seq $NUMBER_CHANGES); do
    createRdfFile 10 $DATASETPATH/addset-10-$i.nt
    createRdfFile 30 $DATASETPATH/addset-30-$i.nt
    createRdfFile 50 $DATASETPATH/addset-50-$i.nt
    createRdfFile 70 $DATASETPATH/addset-70-$i.nt
    createRdfFile 90 $DATASETPATH/addset-90-$i.nt
    notify-send "Changeset creation" "Run $i/$NUMBER_CHANGES completed"
done


notify-send -t 0 "VOS Performance Test" "All Test completed"
