package de.tud.plt.r43ples.management;

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
	 * Add new node to tree or extend old node.
	 * 
	 * @param node the node to create or change
	 * @param predecessor the predecessor to add
	 */
	public void addNode(String revisionNumber, String revisionUri, String fullGraph) {
		NodeSpecification node = new NodeSpecification(revisionNumber, revisionUri, fullGraph);
		map.put(revisionNumber, node);
	}
	
	/**
	 * Add new node to tree or extend old node.
	 * 
	 * @param node the node to create or change
	 * @param predecessor the predecessor to add
	 */
	public void addEdgeNode(String node, String predecessor) {
		NodeSpecification nodeS = map.get(node);
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
	public LinkedList<String> getPathToRevision(String revisionNumber) {
		// TODO: make path resolving more clever; not always using first successor but breadth-first search
		LinkedList<String> list = new LinkedList<String>();
		NodeSpecification node = map.get(revisionNumber);
		
		// Add all revision numbers from specified revision to first found revision with a full graph
		list.addFirst(node.getRevisionNumber());
		while (node.getFullGraph().equals("")) {
			node = node.getFirstSuccessor();
			list.addFirst(node.getRevisionNumber());
		}
		return list;
	}
	
	/**
	 * Calculate the path from specified revision to a referenced revision.
	 * 
	 * @param revisionNumber the revision number to calculate the path for
	 * @return linked list with all revisions from specified revision number to referenced revision 
	 */
	public LinkedList<String> getPathToRevisionWithUri(String revisionNumber) {
		// TODO: make path resolving more clever; not always using first successor but breadth-first search
		LinkedList<String> list = new LinkedList<String>();
		NodeSpecification node = map.get(revisionNumber);
		
		// Add all revision numbers from specified revision to first found revision with a full graph
		list.addFirst(node.getRevisionUri());
		while (node.getFullGraph().equals("")) {
			node = node.getFirstSuccessor();
			list.addFirst(node.getRevisionUri());
		}
		return list;
	}
	
	
	/**
	 * Get the map.
	 * 
	 * @return the map
	 */
	public HashMap<String, NodeSpecification> getMap() {
		return map;
	}
	
}
