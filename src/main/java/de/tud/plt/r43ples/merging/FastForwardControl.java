package de.tud.plt.r43ples.merging;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

import java.util.Iterator;
import java.util.LinkedList;

public class FastForwardControl {

	/**
	 * Fast forwards branch A to branch B
	 * 
	 * Note: branch B remains the same, only branch A is updated
	 * 
	 * @param graph URI of revision graph where fast forward should take place
	 * @param branchNameA name of branch A which should be forwarded
	 * @param branchNameB name of branch which should be forwarded to
	 * @param user	username which is responsible for the commit
	 * @param dateTime timestamp
	 * @param message commit message for fast forward
	 * 
	 * @return true, if fast forward was successful
	 * @throws InternalErrorException 
	 * 
	 */
	public static boolean performFastForward(final RevisionGraph graph, final String branchNameA, final String branchNameB,
			final String user, final String dateTime, final String message) throws InternalErrorException{
		String revisionGraph = graph.getRevisionGraphUri();
		if (!FastForwardControl.fastForwardCheck(graph, branchNameA, branchNameB)) {
			return false;
		}
		String branchUriA = graph.getBranchUri(branchNameA);
		String branchUriB = graph.getBranchUri(branchNameB);
		
		String fullGraphUriA = graph.getFullGraphUri(branchUriA);
		String fullGraphUriB = graph.getFullGraphUri(branchUriB);
	
		String revisionUriA = graph.getRevisionUri(branchNameA);
		String revisionUriB = graph.getRevisionUri(branchNameB);
		
		moveBranchReference(revisionGraph, branchUriA, revisionUriA, revisionUriB);
		
		String commitUri = revisionGraph+"-ff-commit-"+branchNameA+"-"+branchNameB;
		String query = Config.prefixes + String.format(""
				+ "INSERT DATA { GRAPH <%s> { "
				+ "  <%s> a rmo:FastForwardCommit;"
				+ "     prov:used <%s>, <%s>, <%s>;"
				+ "     prov:wasAssociatedWith <%s>;"
				+ "     prov:atTime \"%s\"^^xsd:dateTime; "
				+ "     dc-terms:title \"%s\". "
				+ "} }",
				revisionGraph, commitUri, branchUriA, revisionUriA, revisionUriB, user, dateTime, message);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
		updateBelongsTo(revisionGraph, branchUriA, revisionUriA, revisionUriB);
		fullGraphCopy(fullGraphUriB, fullGraphUriA);
		return true;
	}
	
	
	/**check the condition for fast forward strategy
	 * @param revisionGraph	name of named graph
	 * @param branch1	name of branch1
	 * @param branch2	name of branch2
	 */
	
	public static boolean fastForwardCheck(RevisionGraph revisionGraph, String branch1 , String branch2) {
		if(branch1.equals(branch2)) {
			return false;
		}
		
		try {
			//get last revision of each branch
			String revisionUriA = revisionGraph.getRevisionUri(branch1);
			String revisionUriB = revisionGraph.getRevisionUri(branch2);
		
			String query = Config.prefixes
				+ String.format("ASK { GRAPH <%s> { "
						+ "<%s> prov:wasDerivedFrom+ <%s> ."
						+ " }} ",
						revisionGraph.getRevisionGraphUri(), revisionUriB, revisionUriA);
			
			return TripleStoreInterfaceSingleton.get().executeAskQuery(query);
		}
		catch (InternalErrorException e){
			return false;
		}
	}

	/**
	 * Move the reference in the specified revision graph from the old revision to the new one.
	 *
	 * @param revisionGraph revision graph in the triplestore
	 * @param branchName name of the branch
	 * @param revisionOld uri of the old revision
	 * @param revisionNew uri of the new revision
	 *  */
	public static void moveBranchReference(final String revisionGraph, final String branchName, final String revisionOld, final String revisionNew) {
		// delete old reference and create new one
		String query = Config.prefixes	+ String.format(""
						+ "DELETE DATA { GRAPH <%1$s> { <%2$s> rmo:references <%3$s>. } };"
						+ "INSERT DATA { GRAPH <%1$s> { <%2$s> rmo:references <%4$s>. } }",
				revisionGraph, branchName, revisionOld, revisionNew);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
	}

	/**
	 * updates all revisions between A and B and let them belong to the specified branch
	 *
	 * @param revisionGraph
	 * @param branch URI of the branch
	 * @param revisionStart uri of start revision
	 * @param revisionStop uri of last revision
	 * */
	public static void updateBelongsTo(final String revisionGraph, String branch, String revisionStart, String revisionStop ){
		LinkedList<String> revisionList =  MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, revisionStart, revisionStop);

		Iterator<String> riter = revisionList.iterator();
		while(riter.hasNext()) {
			String revision = riter.next();

			String query = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> { <%s> rmo:belongsTo <%s>. } };%n",
					revisionGraph, revision, branch);

//			logger.debug("revisionlist info" + revision);
//			logger.debug("updated info" + query);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
		}
	}

	/** copy graph of branchA to fullgraph of branchB
	 * @param sourceGraph uri of source graph
	 * @param targetGraph uri of target graph
	 * */
	public static void fullGraphCopy(String sourceGraph, String targetGraph) {
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(
				"COPY GRAPH <" + sourceGraph + "> TO GRAPH <"+ targetGraph + ">");
	}
	
}
