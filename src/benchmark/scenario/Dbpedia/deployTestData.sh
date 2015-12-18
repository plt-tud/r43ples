#/bin/bash

# Add dpbedia data to Stardog
# Take care to go to data directory beforehand
# Take care to configure and epxort STARDOG_HOME and put Stardog binaries on PATH


GRAPH="http://dbpedia.org"
CONFIG_STARDOG="../../conf/r43ples.stardog.conf"
CONFIG=$CONFIG_STARDOG
JAR=../../../../target/r43ples-console-client-jar-with-dependencies.jar


stardog-admin server start

# create database
stardog-admin db create -n dbpedia -o "strict.parsing=false" -v 

# add ontology data
stardog data add dbpedia -v --named-graph $GRAPH data/dbpedia_3.9.owl
# Added 391.015.190 triples in 67:01:30.856 (ca. 32 GB dataset seize)
stardog data add dbpedia -v --named-graph $GRAPH data/dbpedia_2015_06_02.nt







java -jar $JAR --config $CONFIG --new --graph $GRAPH

# for dir in data/changesets/*/*/*/*
for dir in data/changesets/2015/06/03/*
do
    echo "Going into $dir"
    i=1
    f=`printf '%06i' $i`
    f_add="$dir/$f.added.nt"
    f_removed="$dir/$f.removed.nt"
    f_reinserted="$dir/$f.reinserted.nt"
    #f_clear="$dir/$f.clear.nt"
    until [ ! -f "$f_add" ]
    do
            echo " Performing changeset $f ..."
            
            cat $f_add $f_reinserted > $dir/tmp_add.nt
            cat $f_removed > $dir/tmp_del.nt
            
            java -jar $JAR --config $CONFIG -g $GRAPH -a $dir/tmp_add.nt -d $dir/tmp_del.nt -m "benchmark commit $i in dir $dir"
            
            i=$(( i+1 ))
            f=`printf '%06i' $i`
            f_add="$dir/$f.added.nt"
            f_removed="$dir/$f.removed.nt"
            f_reinserted="$dir/$f.reinserted.nt"
            #f_clear="$dir/$f.clear.nt"
    done
done 
