package de.tud.plt.r43ples.merging;

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
	
	
	public MergeResult(String graph, String branchA, String branchB) {
		this.graph = graph;
		this.branchA = branchA;
		this.branchB = branchB;
	}

}
