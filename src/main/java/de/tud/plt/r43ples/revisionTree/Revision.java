package de.tud.plt.r43ples.revisionTree;

import java.util.ArrayList;

/**
 * The Revision class saves the predecessors and the successors of a node.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * 
 */
public class Revision {

	/** The successors of the node */
	private ArrayList<Revision> successor;
	/** The full graph of a tag or branch if available */
	private String fullGraph = "";
	/** The revision number of the node */
	private String revisionNumber;
	/** revision URI of the node */
	private String revisionUri;
	
	/**
	 * The constructor.
	 */
	public Revision(String revisionNumber, String revisionUri, String fullGraph) {
		successor = new ArrayList<Revision>();
		this.revisionNumber = revisionNumber;
		this.revisionUri = revisionUri;
		this.fullGraph = fullGraph;
	}


	
	/**
	 * @return the first successor
	 */
	public Revision getFirstSuccessor() {
		return successor.get(0);
	}
	
	

	
	/**
	 * @param successor the successor to add
	 */
	public void addSuccessor(Revision successor) {
		this.successor.add(successor);
	}


	/**
	 * @return the fullGraph
	 */
	public String getFullGraph() {
		return fullGraph;
	}


	/**
	 * @param fullGraph the fullGraph to set
	 */
	public void setFullGraph(String fullGraph) {
		this.fullGraph = fullGraph;
	}


	/**
	 * @return the revisionNumber
	 */
	public String getRevisionNumber() {
		return revisionNumber;
	}
	
	/**
	 * @return the revisionUri
	 */
	public String getRevisionUri() {
		return revisionUri;
	}
}
