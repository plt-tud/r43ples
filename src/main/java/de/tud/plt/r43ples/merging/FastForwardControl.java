package de.tud.plt.r43ples.merging;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class FastForwardControl {

	/**
	 * Fast forwards branch A to branch B
	 * 
	 * Note: branch B remains the same, only branch A is updated
	 * 
	 * @param revisionGraph URI of revision graph where fast forward should take place
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
		
		RevisionManagement.moveBranchReference(revisionGraph, branchUriA, revisionUriA, revisionUriB);
		
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
		RevisionManagement.updateBelongsTo(revisionGraph, branchUriA, revisionUriA, revisionUriB);	
		RevisionManagement.fullGraphCopy(fullGraphUriB, fullGraphUriA);
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
	
}
