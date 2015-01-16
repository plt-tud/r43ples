package de.tud.plt.r43ples.revisionTree;

public class Branch {
	
	private String uri;
	private String name;
	private Commit ref;
	
	public Branch(String uri) {
		this.uri = uri;
	}

	public Branch(String uri, String name, Commit ref) {
		this.uri = uri;
		this.name = name;
		this.ref = ref;
	}
	
	public String getUri() {
		return uri;
	}

	public String getName() {
		return name;
	}
	
	public Commit getReference() {
		return ref;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Branch) {
			Branch b = (Branch) obj;
			return this.uri.equals(b.uri);
		}
		return super.equals(obj);
	}
}
