package de.tud.plt.r43ples.merging.control;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.management.StrategyManagement;

public class FastForwardControl {

	
	/**check the condition for fast forward strategy
	 * @param revisionGraph : name of named graph
	 * @param branch1 : name of branch1
	 * @param branch2 : name of branch2
	 * @throws InternalErrorException */
	
	public static boolean fastForwardCheck(String revisionGraph, String branch1 , String branch2) throws InternalErrorException {
		if(branch1.equals(branch2)) {
			return false;
		}
		
		//get last revision of each branch
		String revisionUriA = RevisionManagement.getRevisionUri(revisionGraph, branch1);
		String revisionUriB = RevisionManagement.getRevisionUri(revisionGraph, branch2);
		
		return StrategyManagement.isFastForward(revisionGraph, revisionUriA, revisionUriB);
	}
	
	
	/**
	 * 
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * 
	 * @return if fast-forward was successful
	 * @throws InternalErrorException 
	 */
	public static boolean executeFastForward(String graphName, String branchNameA, String branchNameB) throws InternalErrorException
	{
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		if (!FastForwardControl.fastForwardCheck(revisionGraph, branchNameA, branchNameB)) {
			return false;
		}
		
		String branchUriA = RevisionManagement.getBranchUri(revisionGraph, branchNameA);
		String branchUriB = RevisionManagement.getBranchUri(revisionGraph, branchNameB);
		
		String fullGraphUriA = RevisionManagement.getFullGraphUri(revisionGraph, branchUriA);
		String fullGraphUriB = RevisionManagement.getFullGraphUri(revisionGraph, branchUriB);

		
		String revisionUriA = RevisionManagement.getRevisionUri(revisionGraph, branchNameA);
		String revisionUriB = RevisionManagement.getRevisionUri(revisionGraph, branchNameB);
		
		StrategyManagement.moveBranchReference(revisionGraph, branchUriB, revisionUriB, revisionUriA);
		StrategyManagement.updatebelongsTo(revisionGraph, graphName, branchUriB, revisionUriB, revisionUriA);	
		StrategyManagement.fullGraphCopy(fullGraphUriA, fullGraphUriB);
		return true;
	}
	
}
