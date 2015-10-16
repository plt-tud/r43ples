package de.tud.plt.r43ples.merging.control;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.management.StrategyManagement;

public class FastForwardControl {

	
	/**check the condition for fast forward strategy
	 * @param graphName : name of named graph
	 * @param branch1 : name of branch1
	 * @param branch2 : name of branch2
	 * @throws InternalErrorException */
	
	public static boolean fastForwardCheck(String graphName, String branch1 , String branch2) throws InternalErrorException {
		if(branch1.equals(branch2)) {
			return false;
		}
		
		//get last revision of each branch
		String revisionUriA = RevisionManagement.getRevisionUri(graphName, branch1);
		String revisionUriB = RevisionManagement.getRevisionUri(graphName, branch2);
		
		return StrategyManagement.isFastForward(revisionUriA, revisionUriB);
	}
	
	public static boolean executeFastForward(final String graphName, final String branchNameA, final String branchNameB) throws InternalErrorException{
		if (!fastForwardCheck(graphName, branchNameA, branchNameB)) {
			return false;
		}
		String branchUriA = RevisionManagement.getBranchUri(graphName, branchNameA);
		String branchUriB = RevisionManagement.getBranchUri(graphName, branchNameB);
		
		String fullGraphUriA = RevisionManagement.getFullGraphUri(branchUriA);
		String fullGraphUriB = RevisionManagement.getFullGraphUri(branchUriB);

		String revisionUriA = RevisionManagement.getRevisionUri(graphName, branchNameA);
		String revisionUriB = RevisionManagement.getRevisionUri(graphName, branchNameB);
		
		StrategyManagement.moveBranchReference(branchUriB, revisionUriB, revisionUriA);
		// TODO: add reference commit with user and commit message
		StrategyManagement.updateRevisionOfBranch(branchUriB, revisionUriB, revisionUriA);	
		StrategyManagement.fullGraphCopy(fullGraphUriA, fullGraphUriB);
		return true;
	}
	
}
