#! /bin/bash

JAR=../../../target/r43ples-0.8.7-SNAPSHOT-jar-with-dependencies.jar

# Start R43ples
java -jar $JAR --config conf/r43ples.tdb.conf &     # TDB     -> 9998
java -jar $JAR --config conf/r43ples.stardog.conf & # Stardog -> 9997
# java -jar $JAR --config conf/r43ples.virtuoso.conf &

