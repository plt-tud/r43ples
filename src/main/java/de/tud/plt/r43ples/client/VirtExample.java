package de.tud.plt.r43ples.client;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

import virtuoso.jena.driver.*;

public class VirtExample {

	/**
	 * Executes a SPARQL query against a virtuoso url and prints results.
	 */
	public static void main(String[] args) {

		String url;
		if(args.length == 0)
		    url = "jdbc:virtuoso://localhost:1111";
		else
		    url = args[0];

/*			STEP 1			*/
		VirtGraph set = new VirtGraph (url, "dba", "dba");

/*			STEP 2			*/


/*			STEP 3			*/
/*		Select all data in virtuoso	*/
		Query sparql = QueryFactory.create("SELECT DISTINCT ?graph WHERE { GRAPH ?graph { ?s ?p ?o } } limit 100");

/*			STEP 4			*/
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, set);

		ResultSet results = vqe.execSelect();
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
		    RDFNode graph = result.get("graph");
		    System.out.println(graph);
		}
	}
}