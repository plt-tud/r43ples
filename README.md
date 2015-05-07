R43ples
=======

R43ples (Revision for triples) is an open source Revision Management Tool for the Semantic Web.

It provides different revisions of named graphs via a SPARQL interface. All information about revisions, changes, commits, branches and tags are stored in additional named graphs beside the original graph in an attached external triple store.

[![Build Status](https://travis-ci.org/plt-tud/r43ples.png?branch=master)](https://travis-ci.org/plt-tud/r43ples)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/2125/badge.svg)](https://scan.coverity.com/projects/2125)
[![Ohloh Project Status](https://www.ohloh.net/p/r43ples/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/r43ples)


This project provides an enhanced SPARQL endpoint for revision management of named graphs.
R43ples uses an internal Jena TDB is attached to an existing SPARQL endpoint of a Triple Store and acts as another endpoint both for normal SPARQL queries
as well as for revision-enhanced SPARQL queries, named R43ples queries.
The R43ples endpoint allows to specify revisions which should be queried for each named graph used inside a SPARQL query.
The whole revision information is stored in additional graphs in the attached Jena TDB.

The javadoc can be found at the [website](http://plt-tud.github.io/r43ples) under [http://plt-tud.github.io/r43ples/site/apidocs/](http://plt-tud.github.io/r43ples/site/apidocs/).

A running test server should be available under [http://eatld.et.tu-dresden.de:9998/r43ples/sparql](http://eatld.et.tu-dresden.de:9998/r43ples/sparql)


Dependencies
------------
* JDK 1.7
* Maven

```
sudo apt-get install maven default-jdk
```

Releases
--------
Releases are stored on [GitHub](https://github.com/plt-tud/r43ples/releases).
They just have to be unzipped and started with Java

    java -jar r43ples-*-with-dependencies.jar

    
Debian packages are going to be deployed soon. 

Compiling
---------
Maven is used for compiling

    mvn exec:java
    
Packages (JAR with dependencies for the webservice, a console client and a debian package) can be be built with:

    mvn package
    

Configuration
-------------
There is a configuration file named *resources/r43ples.conf*. The most important ones are the following:

* *service.port* - port under which R43ples provides its services
* *service.uri* - URI under which R43ples provides its services
* *revision.graph* - named graph which is used by R43ples to store revision graph information
* *sdd.graph* - named graph for storing the SDD

R43ples can be attached to different triplestores which need specific parameters specified in the configuration file:
    
* Jena TDB
    * *jena.tdb.directory* (database/dataset)
* Virtuoso via JDBC
    * *virtuoso.url* (jdbc:virtuoso://localhost:1111)
    * *virtuoso.user* (dba)
    * *virtuoso.password* (dba)
* HTTP SPARQL endpoint interface (succesfully tested against StarDog)
    * *http.url* (http://localhost:5820/myDB/query)
    * *http.user* (admin)
    * *http.password* (admin)

The logging configuration is stored in *resources/log4j.properties*


Interfaces
---------
SPARQL endpoint is available at:

    [uri]:[port]/r43ples/sparql

The endpoint directly accepts SPARQL queries with HTTP GET or HTTP POST parameters for *query* and *format*: 

    [uri]:[port]/r43ples/sparql?query=[]&format=[]

### Supported Formats

The formats can be specified as URL Path Parameter *format*, as HTTP post paramter *format* or as HTTP header parameter *Accept*: 

* text/turtle
* application/json
* application/rdf+xml
* text/html
* text/plain


### R43ples keywords

There are some additional keywords which can be used to control the revisions of graphs:

* Create graph

        CREATE GRAPH <graph>
        
* Select query

        SELECT * 
        WHERE { 
        	GRAPH <graph> REVISION "23" {?s ?p ?o}
    	}
        
* Update query

        USER "mgraube" MESSAGE "test commit" 
        INSERT {
            GRAPH <test> REVISION "2" {
                <a> <b> <c> .
            }
        }

* Branching

        USER "mgraube"
        MESSAGE "test commit"
        BRANCH GRAPH <test> REVISION "2" TO "unstable"
        
* Tagging

        USER "mgraube"
        MESSAGE "test commit"
        TAG GRAPH <test> REVISION "2" TO "v0.3-alpha"

* Merging

		USER "mgraube"
		MESSAGE "merge example"
		MERGE GRAPH <test> BRANCH "branch-1" INTO "branch-2"


SPARQL Join option
------------------
There is a new option for R43ples which improves the performance. The necessary revision is not temporarily generated anymore.
The SPARQL query is rewritten in such a way that the branch and the change sets are directly joined inside the query. This includes the order of the change sets.
It is currently under development and further research.

The option can be enabled by passing an additional parameter "join_option=true"

It currently supports:

* Multiple Graphs
* Multiple TriplePath
* FILTER
* MINUS

For more details, have a look into the *doc/* directory.


Used libraries and frameworks
------------------------------
Following libraries are used in R43ples:

* [Jersey](https://jersey.java.net/) for RestFul web services in Java
* [Grizzly](https://grizzly.java.net/) as web server
* [Jena ARQ](https://jena.apache.org/documentation/query/index.html) for processing SPARQL results
* [Jena TDB](https://jena.apache.org/documentation/tdb/index.html) as triplestore
* [jQuery](http://jquery.com/) as JavaScript framework
* [Bootstrap](http://getbootstrap.com/) as HTML, CSS and JS framework
* [Mustache](https://mustache.github.io/) as template engine
 
