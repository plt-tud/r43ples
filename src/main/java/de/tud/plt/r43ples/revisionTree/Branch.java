package de.tud.plt.r43ples.revisionTree;

public class Branch extends Reference {

	public Branch(String uri) {
		super(uri, null, null);
	}

	public Branch(String uri, String name, Commit ref) {
		super(uri, name, ref);
	}

}
