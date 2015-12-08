#! /bin/bash



DATASETPATH=data


function createRdfFile {
  TRIPLES=$1
  RDF_FILE=$2
  CONNECTIONS=2
  NODES=`echo "$TRIPLES/$CONNECTIONS" | bc `

  java -jar random2rdf.jar Poissonian $NODES $CONNECTIONS > $RDF_FILE.ttl
  rapper -g -o ntriples $RDF_FILE.ttl > $RDF_FILE
  rm $RDF_FILE.ttl
  echo "Dataset created: $RDF_FILE ($TRIPLES)"
}


echo "Create Datasets"
createRdfFile 100 $DATASETPATH/dataset-100.nt
createRdfFile 1000 $DATASETPATH/dataset-1000.nt
createRdfFile 10000 $DATASETPATH/dataset-10000.nt
createRdfFile 100000 $DATASETPATH/dataset-100000.nt
createRdfFile 1000000 $DATASETPATH/dataset-1000000.nt


NUMBER_CHANGES=20
echo "Create Changesets"
for i in $(seq $NUMBER_CHANGES); do
    createRdfFile 10 $DATASETPATH/addset-10-$i.nt
    createRdfFile 20 $DATASETPATH/addset-20-$i.nt
    createRdfFile 30 $DATASETPATH/addset-30-$i.nt
    createRdfFile 40 $DATASETPATH/addset-40-$i.nt
    createRdfFile 50 $DATASETPATH/addset-50-$i.nt
    createRdfFile 60 $DATASETPATH/addset-60-$i.nt
    createRdfFile 70 $DATASETPATH/addset-70-$i.nt
    createRdfFile 80 $DATASETPATH/addset-80-$i.nt
    createRdfFile 90 $DATASETPATH/addset-90-$i.nt
    createRdfFile 100 $DATASETPATH/addset-100-$i.nt
    notify-send "Changeset creation" "Run $i/$NUMBER_CHANGES completed"
done


notify-send -t 0 "VOS Performance Test" "All Test completed"
