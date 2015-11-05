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
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		String revisionUriA = RevisionManagement.getRevisionUri(revisionGraph, branch1);
		String revisionUriB = RevisionManagement.getRevisionUri(revisionGraph, branch2);
		
		return StrategyManagement.isFastForward(revisionUriA, revisionUriB);
	}
	
}
