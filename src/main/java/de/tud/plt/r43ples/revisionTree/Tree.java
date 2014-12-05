package de.tud.plt.r43ples.revisionTree;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class provides a tree structure to store revision structure.
 * 
 * @author Stephan Hensel
 *
 */
public class Tree {

	/** The map to store tree data */
	private HashMap<String, NodeSpecification> map = new HashMap<String, NodeSpecification>();
	
	
	/**
	 * Add new node to tree.
	 * 
	 * @param revisionNumber 
	 * 			revision number
	 * @param revisionUri 
	 * 			URI which specifies this revision
	 * @param fullGraph
	 * 			URI of the graph which holds a full copy if available
	 */
	public void addNode(String revisionNumber, String revisionUri, String fullGraph) {
		NodeSpecification node = new NodeSpecification(revisionNumber, revisionUri, fullGraph);
		map.put(revisionNumber, node);
	}
	
	/**
	 * Add edge between to nodes.
	 * 
	 * @param successor revision number of the successor node
	 * @param predecessor revision number of the predecessor node
	 */
	public void addEdge(String successor, String predecessor) {
		NodeSpecification nodeS = map.get(successor);
		NodeSpecification nodeP = map.get(predecessor);
		
		nodeS.addPredecessor(nodeP);
		nodeP.addSuccessor(nodeS);
	}	
	
	/**
	 * Calculate the path from specified revision to a referenced revision.
	 * 
	 * @param revisionNumber the revision number to calculate the path for
	 * @return linked list with all revisions from specified revision number to referenced revision 
	 */
	public LinkedList<NodeSpecification> getPathToRevision(String revisionNumber) {
		// TODO: make path resolving more clever; not always using first successor but breadth-first search
		LinkedList<NodeSpecification> list = new LinkedList<NodeSpecification>();
		NodeSpecification node = map.get(revisionNumber);
		
		// Add all revision numbers from specified revision to first found revision with a full graph
		list.addFirst(node);
		while (node.getFullGraph().equals("")) {
			node = node.getFirstSuccessor();
			list.addFirst(node);
		}
		return list;
	}	
	
}
