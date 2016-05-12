package de.tud.plt.r43ples.merging.management;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class StrategyManagement {
	private static Logger logger = Logger.getLogger(StrategyManagement.class);
		
	
	/**get commitUri of the revisionUri
	 * @param uri of the revision*/
	public static String getCommitUri(String revisionGraph, String revisionUri){
		
		String query = String.format(
				  "PREFIX prov: <http://www.w3.org/ns/prov#> %n"
				+ "SELECT DISTINCT ?commit  %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { %n"
				+ "		?commit prov:generated <%s>."
				+ " }"
				+ "}", revisionGraph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?commit").toString();
		}
		
		logger.info("No commit could be found.");
		return null;
	}
	
	
	/** get the delta removed width versionUri
	 * @param uri of the added or removed triple set*/
	public static LinkedList<String> createAddedOrRemovedTripleSet(String addedOrRemovedDelta) {
		String query = RevisionManagement.prefixes + String.format(""
				+"SELECT DISTINCT ?s ?p ?o %n"
				+"WHERE{ GRAPH <%s> %n"
				+"		{ ?s ?p ?o . } %n"
				+"}", addedOrRemovedDelta);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		LinkedList<String> tripleList = new LinkedList<String>();
		while(resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			String subject = "<" + qs.getResource("?s").toString() + ">";
			String predicate = "<" + qs.getResource("?p").toString() + ">";
			String object = "";
			
			if (qs.get("?o").isLiteral()) {
				object = "\"" + qs.getLiteral("?o").toString() + "\"";
			} else {
				object = "<" + qs.getResource("?o").toString() + ">";
			}
			
			String triple = subject + " " + predicate + " " + object;
			tripleList.add(triple);
			logger.info("patch--triple" + triple);
		}
		
		return tripleList;
	}
	
	/** get number of revision
	 * @param uri of revision*/
	public static String getRevisionNumber(String revisionGraph, String revisionUri){
		String query = RevisionManagement.prefixes + String.format(""
				+"SELECT DISTINCT ?revisionNumber %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> rmo:revisionNumber ?revisionNumber. } }%n",
				revisionGraph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getLiteral("?revisionNumber").toString();
		}
		
		logger.warn("No revision number could be found.");
		return null;
	}
	
	/** get client name
	 * @param uri of commit */
	public static String getCommitUserUri(String revisionGraph, String commitUri) {
		String query = RevisionManagement.prefixes + String.format(""
				+"SELECT DISTINCT ?user %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> prov:wasAssociatedWith ?user. } }%n",
				revisionGraph, commitUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			String userUri = qs.getResource("?user").toString();		
			int count = userUri.split("/").length;
			return userUri.split("/")[count-1];
		}
		
		logger.info("No revision number could be found.");
		return null;
	}
	
	/** get client message
	 * @param uri of commit*/
	public static String getCommitMessage(String revisionGraph, String commitUri) {
		String query = RevisionManagement.prefixes + String.format(""
				+"SELECT DISTINCT ?message %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> dc-terms:title ?message. } }%n",
				revisionGraph, commitUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getLiteral("?message").toString();
		}
		
		logger.info("No revision number could be found.");
		return null;
	}	
	
}
