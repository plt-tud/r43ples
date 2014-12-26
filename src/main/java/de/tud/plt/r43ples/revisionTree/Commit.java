package de.tud.plt.r43ples.revisionTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Commit {
	private String message;
	private String author;
	private String baseRev;
	private String nextRev;
	public List<Commit> Predecessors;
	public List<Commit> Successors;
	
	
	/**
	 * @param message commit message
	 * @param author author of the commit
	 * @param baseRev name of revision this commit is based on
	 * @param nextRev name of revision this commit generated
	 */
	public Commit(String message, String author, String baseRev, String nextRev) {
		this.message = message;
		this.author = author;
		this.baseRev = baseRev;
		this.nextRev = nextRev;
		
		Predecessors = new ArrayList<Commit>();
		Successors = new ArrayList<Commit>();
	}

	public String getMessage() {
		return message;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getBaseRevision() {
		return baseRev;
	}
	
	public String getNextRevision() {
		return nextRev;
	}
}
