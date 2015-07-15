package de.tud.plt.r43ples.merging.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.MergeQueryTypeEnum;
import de.tud.plt.r43ples.management.RebaseQueryTypeEnum;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class StrategyManagement {
	private static Logger logger = Logger.getLogger(StrategyManagement.class);
	
	private static String revisionInformation;
	
	public static final String prefixes = 
			  "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n" 
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd: <http://eatld.et.tu-dresden.de/sdd#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";


	
	public static boolean isFastForward(String revisionBranchA , String revisionBranchB){

		
		String query = prefixes
				+ String.format("ASK { GRAPH <%s> { "
						+ "<%s> prov:wasDerivedFrom+ <%s> ."
						+ " }} ",
						Config.revision_graph, revisionBranchA, revisionBranchB);
			
		return TripleStoreInterfaceSingleton.get().executeAskQuery(query);
			
	}
	
	public static String createFastForwardQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB ){
		String query = "";
		
		String queryTemplateFastForward = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE ff GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		query = String.format(queryTemplateFastForward, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		
		logger.info("fast forward query" + query);
		return query;
		
	}
	
	public static void moveBranchReference(String branchNameB, String revisionUriB, String revisionUriA){
		// delete alte reference
		String query = prefixes + String.format("DELETE DATA { GRAPH <%s> { <%s> rmo:references <%s>. } };%n",
				Config.revision_graph, branchNameB, revisionUriB);
		// added new reference
		query += String.format("INSERT DATA { GRAPH <%s> { <%s> rmo:references <%s>. } } ;%n", Config.revision_graph,
				branchNameB, revisionUriA);
		
		logger.info("move info" + query);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
				
	}
	
	public static void updateRevisionOfBranch(String branchUriB, String revisionUriB, String revisionUriA ){
		LinkedList<String> revisionList =  MergeManagement.getPathBetweenStartAndTargetRevision(revisionUriB, revisionUriA);
		
		logger.info("revisionlist size: "+ revisionList.size());
		Iterator<String> riter = revisionList.iterator();
		while(riter.hasNext()) {
			String revision = riter.next();
			String query = prefixes + String.format("INSERT DATA { GRAPH <%s> { <%s> rmo:revisionOfBranch <%s>. } };%n",
					Config.revision_graph, revision, branchUriB);
			
			logger.info("revisionlist info" + revision);

			logger.info("updated info" + query);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);			
		}
		
	}
	
	/**
	 * save old revision information of Graph */
	public static void saveGraphVorMerging(String graph, String format){
		revisionInformation = RevisionManagement.getRevisionInformation(graph, format);
	}
	
	/**
	 * load old revision information of Graph */
	public static String loadGraphVorMerging(){
		return revisionInformation;
	}
	
	/**create rebase query*/
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
				+ "MERGE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
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
		}
		
		return query;
	}
	
	/**get commitUri of the revisionUri*/
	public static String getCommitUri(String revisionUri){
		
		String query = String.format(
				  "PREFIX prov: <http://www.w3.org/ns/prov#> %n"
				+ "SELECT DISTINCT ?commit  %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { %n"
				+ "		?commit prov:generated <%s>."
				+ " }"
				+ "}", Config.revision_graph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?commit").toString();
		}
		
		logger.info("No commit could be found.");
		return null;
	}
	
	/**get delta added with versionUri*/
	public static String getDeltaAddedUri(String revisionUri) {
		String query = prefixes + String.format(""
				+"SELECT DISTINCT ?deltaAdded %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> rmo:deltaAdded ?deltaAdded. } }%n",
				Config.revision_graph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?deltaAdded").toString();
		}
		
		logger.info("No deltaAdded could be found.");
		return null;
	}
	
	/** get the delta removed width versionUri*/
	public static String getDeltaRemovedUri(String revisionUri) {
		String query = prefixes + String.format(""
				+"SELECT DISTINCT ?deltaRemoved %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> rmo:deltaRemoved ?deltaRemoved. } } %n",
				Config.revision_graph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?deltaRemoved").toString();
		}
		
		logger.info("No deltaRemoved could be found.");
		return null;
	}
	
	/** get the delta removed width versionUri*/
	public static LinkedList<String> createAddedOrRemovedTripleSet(String addedOrRemovedDelta) {
		String query = prefixes + String.format(""
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
	
	public static String getRevisionNumber(String revisionUri){
		String query = prefixes + String.format(""
				+"SELECT DISTINCT ?revisionNumber %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> rmo:revisionNumber ?revisionNumber. } }%n",
				Config.revision_graph, revisionUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getLiteral("?revisionNumber").toString();
		}
		
		logger.info("No revision number could be found.");
		return null;
	}
	
	public static String getPatchUserUri(String commitUri) {
		String query = prefixes + String.format(""
				+"SELECT DISTINCT ?user %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> prov:wasAssociatedWith ?user. } }%n",
				Config.revision_graph, commitUri);
		
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
	
	public static String getPatchMessage(String commitUri) {
		String query = prefixes + String.format(""
				+"SELECT DISTINCT ?message %n"
				+"WHERE{ GRAPH <%s> %n"
				+"   {<%s> dc-terms:title ?message. } }%n",
				Config.revision_graph, commitUri);
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getLiteral("?message").toString();
		}
		
		logger.info("No revision number could be found.");
		return null;
	}
	
	
	
}

















