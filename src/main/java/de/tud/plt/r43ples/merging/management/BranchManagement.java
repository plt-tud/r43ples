package de.tud.plt.r43ples.merging.management;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;


public class BranchManagement {
	private static Logger logger = Logger.getLogger(RevisionManagement.class);
	/** The SPARQL prefixes. **/
	public static final String prefixes = 
			  "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n" 
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd: <http://eatld.et.tu-dresden.de/sdd#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";

	
	
	
	public static ArrayList<String> getAllBranchNamesOfGraph(String graphName) throws IOException {
		logger.info("Get all branch names of graph.");
		ArrayList<String> list = new ArrayList<String>();	
		if (graphName != null) {
			
			String sparqlQuery = prefixes
					+ String.format(
					  "SELECT DISTINCT ?label %n"
					+ "FROM <%s> %n"
					+ "WHERE { %n"
					+ "	?branch a rmo:Branch ; %n"
					+ "		rdfs:label ?label . %n"
					+ "	?rev rmo:revisionOfBranch ?branch ;"
					+ "		rmo:revisionOf <%s> . %n"
					+ "} %n"
					+ "ORDER BY ?label",Config.revision_graph, graphName);
			
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				list.add(qs.getLiteral("label").toString());
			}		
		}
		logger.info(list);
		return list;
	}
}
















