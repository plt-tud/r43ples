package de.tud.plt.r43ples.merging.management;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.RebaseQueryTypeEnum;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class StrategyManagement {
	private static Logger logger = Logger.getLogger(StrategyManagement.class);
	
	private static String revisionInformation;
	
	private static HashMap<String, String> oldRevisionGraphMap = new HashMap<String, String>();
	

	/**check if fast forward can
	 * @param last revision uri of branch A
	 * @param last revision uri of branch B
	 * */
	public static boolean isFastForward(String revisionGraph, String revisionBranchA , String revisionBranchB){
		String query = RevisionManagement.prefixes
				+ String.format("ASK { GRAPH <%s> { "
						+ "<%s> prov:wasDerivedFrom+ <%s> ."
						+ " }} ",
						revisionGraph, revisionBranchA, revisionBranchB);
			
		return TripleStoreInterfaceSingleton.get().executeAskQuery(query);	
	}
	

	
	
	/**
	 * save old revision information of Graph
	 * @param name of graph
	 * @param format of the information */
	public static void saveGraphVorMerging(String graph, String format){
		revisionInformation = RevisionManagement.getRevisionInformation(graph, format);
	}
	
	/**
	 * save old revision information of Graph 
	 * @param name of graph
	 * @param format of the information
	 * @throws InternalErrorException */
	public static void saveGraphVorMergingInMap(String graph, String format) throws InternalErrorException{
		//can not parallel on the same name graph execute
			String oldRevisionGraph = RevisionManagement.getRevisionInformation(graph, format);
			oldRevisionGraphMap.put(graph, oldRevisionGraph);
	}
	
	/**
	 * load old revision information of Graph 
	 * @param name of the named graph
	 * @throws InternalErrorException */
	public static String loadGraphVorMergingFromMap(String graphName) throws InternalErrorException {
		
		if(oldRevisionGraphMap.containsKey(graphName)) {
			String oldGraphInfo = oldRevisionGraphMap.get(graphName);
			oldRevisionGraphMap.remove(graphName);
			return oldGraphInfo;
			
		}else{
			throw new InternalErrorException("Error in parallel access to the same graph by load old graph information vor merging");
		}		
	}
	
	
	
	
	/**
	 * load old revision information of Graph */
	public static String loadGraphVorMerging(){
		return revisionInformation;
	}
	
	/**create rebase query
	 * @param graphName
	 * @param sdd model name
	 * @param client name 
	 * @param messsage of client
	 * @param name of branch A
	 * @param name of branch B
	 * @param type of rebase
	 * @param triple set*/
	public static String createRebaseQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB, RebaseQueryTypeEnum type, String triples){
		logger.info("Execute rebase query of type " + type.toString());
		String query = "";
		
		String queryTemplateCommon = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "REBASE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
				
		String queryTemplateAuto = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "REBASE AUTO GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		String queryTemplateForce = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "REBASE FORCE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		String queryTemplateManual = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "REBASE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
				+ "	%s"
				+ "}";
		
		String queryTemplateWith = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "REBASE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
				+ "	%s"
				+ "}";
		
		if (type.equals(RebaseQueryTypeEnum.COMMON)) {
			query = String.format(queryTemplateCommon, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(RebaseQueryTypeEnum.AUTO)) {
			query = String.format(queryTemplateAuto, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(RebaseQueryTypeEnum.FORCE)) {
			query = String.format(queryTemplateForce, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(RebaseQueryTypeEnum.MANUAL)) {
			query = String.format(queryTemplateManual, user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
		} else if (type.equals(RebaseQueryTypeEnum.WITH)) {
			query = String.format(queryTemplateWith, user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
		}
		
		return query;
	}
	
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
	
	/**get delta added with versionUri
	 * @param uri of the added set*/
	public static String getaddSetUri(String revisionGraph, String revisionUri) {
		String query = RevisionManagement.prefixes + String.format(""
				+"SELECT DISTINCT ?addSet %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> rmo:addSet ?addSet. } }%n",
				revisionGraph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?addSet").toString();
		}
		else {
			logger.warn("No addSet could be found.");
			return null;
		}
	}
	
	/** get the delta removed width versionUri
	 * @param uri of the deleted set*/
	public static String getdeleteSetUri(String revisionGraph, String revisionUri) {
		String query = RevisionManagement.prefixes + String.format(""
				+"SELECT DISTINCT ?deleteSet %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> rmo:deleteSet ?deleteSet. } } %n",
				revisionGraph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?deleteSet").toString();
		}
		else {
			logger.warn("No deleteSet could be found.");
			return null;
		}
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
	public static String getPatchUserUri(String revisionGraph, String commitUri) {
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
	public static String getPatchMessage(String revisionGraph, String commitUri) {
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

















