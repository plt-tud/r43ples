package de.tud.plt.r43ples.revisionTree;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Commit implements Comparable<Commit> {
	private String uri;
	private String message;
	private Date time;
	private String author;
	private List<String> baseRev;
	private String nextRev;
	public List<Commit> Predecessors;
	public List<Commit> Successors;
	
	/**
	 * @param uri
	 * @param message
	 *            commit message
	 * @param time
	 *            time of execution
	 * @param author
	 *            author of the commit
	 * @param baseRev
	 *            name of revision this commit is based on
	 * @param nextRev
	 *            name of revision this commit generated
	 */
	public Commit(String uri, String message, Date time, String author, String baseRev, String nextRev) {
		this(uri);
		this.message = message;
		this.time = time;
		this.author = author;
		this.baseRev = new LinkedList<String>();
		this.baseRev.add(baseRev);
		this.nextRev = nextRev;
	}

	public Commit(String uri) {
		this.uri = uri;
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
	
	public List<String> getBaseRevisions() {
		return baseRev;
	}
	
	public String getNextRevision() {
		return nextRev;
	}

	@Override
	public int compareTo(Commit o) {
		if(o.equals(this))
			return 0;
		
		if(findInPredecessors(o))
			return 1;
		
		if(findInSuccessors(o))
			return -1;
		
		return time.compareTo(o.getTime());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Commit)
		{
			return ((Commit) obj).uri.equals(this.uri);
		}
		return false;
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
