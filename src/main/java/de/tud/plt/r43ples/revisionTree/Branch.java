package de.tud.plt.r43ples.revisionTree;

public class Branch extends ReferenceToCommit {

	public Branch(String uri) {
		super(uri);
	}

	public Branch(String uri, String name, Commit ref) {
		super(uri, name, ref);
	}

}
