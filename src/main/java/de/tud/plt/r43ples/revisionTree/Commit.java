package de.tud.plt.r43ples.revisionTree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit implements Comparable<Commit> {
	private String message;
	private Date time;
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
	public Commit(String message, Date time, String author, String baseRev, String nextRev) {
		this.message = message;
		this.time = time;
		this.author = author;
		this.baseRev = baseRev;
		this.nextRev = nextRev;
		
		Predecessors = new ArrayList<Commit>();
		Successors = new ArrayList<Commit>();
	}

	public String getMessage() {
		return message;
	}
	
	public Date getTime() {
		return time;
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

	@Override
	public int compareTo(Commit o) {
		if(findInPredecessors(o))
			return 1;
		
		if(findInSuccessors(o))
			return -1;
		
		return time.compareTo(o.getTime());
	}
	
	private boolean findInPredecessors(Commit o)
	{
		if(this.Predecessors.contains(o))
			return true;
		for(Commit c : Predecessors)
			if(c.findInPredecessors(o))
				return true;
		return false;
	}
	
	private boolean findInSuccessors(Commit o)
	{
		if(this.Successors.contains(o))
			return true;
		for(Commit c : Successors)
			if(c.findInSuccessors(o))
				return true;
		return false;
	}
}
