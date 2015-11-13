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
		return RevisionManagement.performFastForward(revisionGraph, branchNameA, branchNameB, "user", RevisionManagement.getDateString(), "Online merging ff");
	}
	
}
