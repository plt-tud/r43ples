package de.tud.plt.r43ples.revisionTree;

public class Branch {
	
	private String name;
	private Commit ref;
	
	public Branch(String name, Commit ref) {
		this.name = name;
		this.ref = ref;
	}
	
	public String getName() {
		return name;
	}
	
	public Commit getReference() {
		return ref;
	}
}
