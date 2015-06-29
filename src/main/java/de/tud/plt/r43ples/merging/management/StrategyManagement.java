package de.tud.plt.r43ples.merging.management;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeManagement;
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
				+ "MERGE GRAPH <%s> -ff SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
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
	public static void saveGraphVorFastForward(String graph, String format){
		revisionInformation = RevisionManagement.getRevisionInformation(graph, format);
	}
	
	/**
	 * load old revision information of Graph */
	public static String loadGraphVorFastForward(){
		return revisionInformation;
	}
	
}

















