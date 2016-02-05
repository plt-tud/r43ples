package de.tud.plt.r43ples.management;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.merging.management.StrategyManagement;
import de.tud.plt.r43ples.merging.model.structure.Patch;
import de.tud.plt.r43ples.merging.model.structure.PatchGroup;

public class RebaseControl {
	
	/** The logger. **/
	private static Logger logger = Logger.getLogger(RebaseControl.class);
	
	/** simple checks if rebase could be possible for these two branches of a graph
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * @throws InternalErrorException throws an error if it is not possible
	 */
	public static void checkIfRebaseIsPossible(String graphName, String branchNameA,
			String branchNameB) throws InternalErrorException {
		// Check if graph already exists
		if (!RevisionManagement.checkGraphExistence(graphName)){
			logger.error("Graph <"+graphName+"> does not exist.");
			throw new InternalErrorException("Graph <"+graphName+"> does not exist.");
		}
	
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		// Check if A and B are different revisions
		if (RevisionManagement.getRevisionNumber(revisionGraph, branchNameA).equals(RevisionManagement.getRevisionNumber(revisionGraph, branchNameB))) {
			// Branches are equal - throw error
			throw new InternalErrorException("Specified branches are equal");
		}
		
		// Check if both are terminal nodes
		if (!(RevisionManagement.isBranch(graphName, branchNameA) && RevisionManagement.isBranch(graphName, branchNameB))) {
			throw new InternalErrorException("Non terminal nodes were used ");
		}
	}
	
	/**for each revision in branchA , create a patch */
	public static PatchGroup createPatchGroupOfBranch(String revisionGraph, String basisRevisionUri, LinkedList<String> revisionList) {
		
		LinkedHashMap<String, Patch> patchMap = new LinkedHashMap<String, Patch>();
		
		Iterator<String> rIter  = revisionList.iterator();
		
		while(rIter.hasNext()) {
			String revisionUri = rIter.next();
			String commitUri = StrategyManagement.getCommitUri(revisionGraph, revisionUri);
			
			String addSet = StrategyManagement.getAddSetUri(revisionGraph, revisionUri);
			String deleteSet = StrategyManagement.getDeleteSetUri(revisionGraph, revisionUri);
			
			String patchNumber = StrategyManagement.getRevisionNumber(revisionGraph, revisionUri);
			String patchUser = StrategyManagement.getCommitUserUri(revisionGraph, commitUri);
			String patchMessage = StrategyManagement.getCommitMessage(revisionGraph, commitUri);
			
			patchMap.put(patchNumber, new Patch(patchNumber, patchUser, patchMessage, addSet, deleteSet));			
		}
		
		String basisRevisionNumber = StrategyManagement.getRevisionNumber(revisionGraph, basisRevisionUri);
		
		PatchGroup patchGroup = new PatchGroup(basisRevisionNumber, patchMap);
		
		logger.info("patchGroup initial successful!" + patchGroup.getPatchMap().size());
		return patchGroup;
	}
	
}
