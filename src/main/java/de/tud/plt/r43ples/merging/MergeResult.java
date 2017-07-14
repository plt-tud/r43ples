package de.tud.plt.r43ples.merging;

import de.tud.plt.r43ples.management.R43plesMergeCommit;

public class MergeResult {
	
	public String graph;
	public String branchA;
	public String branchB;
	
	public boolean hasConflict = false;
	
	public String newRevisionNumber;
	public String conflictModel;
	public String graphDiff;
	public String commonRevision;
	public String graphStrategy;  // for rebase only TODO: create subclass RebaseResult
	
	
	public MergeResult(R43plesMergeCommit commit) {
		this.graph = commit.graphName;
		this.branchA = commit.branchNameA;
		this.branchB = commit.branchNameB;
	}

}
