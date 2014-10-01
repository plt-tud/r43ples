R43ples
=======

R43ples (Revision for triples) is Revision Management Tool for the Semantic Web.

It is based on storing the differences of revisions of graphs in additional Named Graphs which are then referenced in a revision graph. It provides an extended SPARQL interface which offers the possibility specify revision of named graphs which should be used for answering the query.

[![Build Status](https://travis-ci.org/plt-tud/r43ples.png?branch=master)](https://travis-ci.org/plt-tud/r43ples)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/2125/badge.svg)](https://scan.coverity.com/projects/2125)
[![Ohloh Project Status](https://www.ohloh.net/p/r43ples/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/r43ples)


This project provides a service for revision management of named graphs in a triple store.
The service can be interconnected in front of an existing SPARQL endpoint of a Triple Store. 
This service in the associated Triple Store creates and manages a revision management of graphs. 
It provides a SPARQL endpoint where all queries have to be directed to. 
The SPARQL query defines whether only a revision is queried, or a new revision needs to be created. 
Furthermore, the service provides an interface for revision management and the import or export of data from the Computer Aided Engineering tools.

The javdoc can be found at [http://plt-tud.github.io/r43ples/javadoc/](http://plt-tud.github.io/r43ples/javadoc/).


Dependencies
------------
* JDK 1.7
* Running Triplestore with SPARQL 1.1 endpoint (tested with Virtuoso 7)


Starting
--------
Ant is used for compiling and starting

    ant run


Interfaces
---------
SPARQL endpoint is available at:

	[uri]:[port]/r43ples/sparql

It directly accepts SPARQL queries with HTTP GET parameters for **query** and **format**: 

    [uri]:[port]/r43ples/sparql?query=[]&format=(HTML|JSON)


There is a command line admin interface which can be started separately. However, it is deprecated and will be removed in the future.


Configuration
-------------
There is a configuration file named *resources/r43ples.conf* where all parameters are configured.
* *sparql.endpoint* - SPARQL endpoint of triplestore which stores all information
* *sparql.username* - username for connected SPARQL endpoint allowed to write data
* *sparql.password* - password for specified user
* *revision.graph* - named graph which is used by R43plesto store revision graph information
* *service.port* - port under which R43ples provides its services
* *service.uri* - URI under which R43ples provides its services

The logging configuration is stored in **resources/log4j.properties**

SPARQL Interface
----------------
* Create graph

		CREATE GRAPH <graph>
		
* Select query

		SELECT * FROM <graph> REVISION "23" WHERE {?s ?p ?o}
		
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


SPARQL Join option
------------------
There is a new option for R43ples which improves the performance. The necessary revision is not temporarily generated anymore.
The SPARQL query is rewritten in such a way that the branch and the change sets are directly joined inside the query. This includes the order of the change sets.
It is currently under development und further research. The option can be enabled by
```
#OPTION r43ples:SPARQL_JOIN
```

For details, have a look into the **doc** directory.

It currently supports:
* Multiple Graphs
* Multiple TriplePath
* FILTER
* MINUS


Algorithm
-----------
Without SPARQL Join option the algorithm are like the following
```
For each named graph 'g' in a query, a temporary graph 'TempGraph_g_r' is generated for the specified revision 'r' according to this formula ('g_x' = full materialized revision 'x' of graph 'g'):
    TempGraph_g_r = g_nearestBranch + SUM[revision i= nearestBranch to r]( deleteSet_g_i - addSet_g_i )
```

```
def select_query(query_string):
    for (graph,revision) in query_string.get_named_graphs_and_revisions():   
        execQuery("COPY GRAPH <"+graph+"> TO GRAPH <tmp-"+graph+"-"+revision+">")
        for rev in graph.find_shortest_path_to_revision(revision):
            execQuery("REMOVE GRAPH "+ rev.add_set_graph+" FROM GRAPH <tmp-"+graph+"-"+revision+">")
            execQuery("ADD GRAPH "+ rev.delete_set_graph+" TO GRAPH <tmp-"+graph+"-"+revision+">")
        query_string.replace(graph, "tmp-"+graph+"-"+revision)
    result = execQuery(query_string)
    execQuery("DROP GRAPH <tmp-*>")
    return result
```
  
``` 
def update_query(query_string):
    for (graph,revision) in query_string.get_named_graphs_and_revisions():
        newRevision = revision +1
        execQuery("ADD GRAPH "+ rev.delete_set_graph+" TO GRAPH <tmp-"+graph+"-"+revision+">")
        ...
```
    
