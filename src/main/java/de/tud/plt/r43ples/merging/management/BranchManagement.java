package de.tud.plt.r43ples.merging.management;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;


public class BranchManagement {
	private static Logger logger = Logger.getLogger(BranchManagement.class);
	
	/**
	 * get the names of all branches of a named graph
	 * @param graphName name of graph
	 * @return list of the name of branch*/
	public static ArrayList<String> getAllBranchNamesOfGraph(String graphName) {
		logger.info("Get all branch names of graph "+ graphName);
		ArrayList<String> list = new ArrayList<String>();	
		if (graphName != null) {
			String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
			String sparqlQuery = RevisionManagement.prefixes
					+ String.format(
					  "SELECT DISTINCT ?label %n"
					+ "FROM <%s> %n"
					+ "WHERE { %n"
					+ "	?branch a rmo:Branch ;"
					+ "		rdfs:label ?label . "
					+ "} %n"
					+ "ORDER BY ?label", revisionGraph);
			
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				list.add(qs.getLiteral("label").toString());
			}		
		}
		logger.debug("All branches: " +list);
		return list;
	}
}
















