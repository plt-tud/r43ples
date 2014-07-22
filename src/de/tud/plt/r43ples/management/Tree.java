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
	private HashMap<String, NodeSpecification> map = null;
	
	/**
	 * The constructor.
	 */
	public Tree() {
		map = new HashMap<String, NodeSpecification>();
	}
	
	
	/**
	 * Add new node to tree or extend old node.
	 * 
	 * @param node the node to create or change
	 * @param predecessor the predecessor to add
	 */
	public void addNode(String node, String predecessor) {
		NodeSpecification nodeS, nodeP = null;
		if (map.containsKey(node)) {
			// extend old node
			nodeS = map.get(node);
		} else {
			// create new node
			nodeS = new NodeSpecification(node);
			map.put(node, nodeS);
		}
		if (map.containsKey(predecessor)) {
			// extend old node
			nodeP = map.get(predecessor);
		} else {
			// create new node
			nodeP = new NodeSpecification(predecessor);
			map.put(predecessor, nodeP);
		}
		
		nodeS.addPredecessor(nodeP);
		nodeP.addSuccessor(nodeS);
	}
	
	
	/**
	 * Add new graph name of full graph to existing node.
	 * 
	 * @param node the node to be updated
	 * @param graphName URI of the full graph
	 */
	public void addFullGraphOfNode(String node, String graphName) {
		if (map.containsKey(node)) {
			// extend old node
			NodeSpecification nodeS = map.get(node);
			nodeS.setFullGraph(graphName);
		}
	}	
	
	
	/**
	 * Calculate the path from revision 0 to a specified revision number.
	 * 
	 * @param revisionNumber the revision number to calculate the path for
	 * @return linked list with all revisions from 0 to specified revision number 
	 */
	public LinkedList<String> getPathToRevision(String revisionNumber) {
		// TODO: make path resolving more clever; not always using first successor but breadth-first search
		LinkedList<String> list = new LinkedList<String>();
		NodeSpecification node = map.get(revisionNumber);
		
		// Add all revision numbers from specified revision to first found revision with a full graph
		list.addFirst(node.getRevisionNumber());
		while (node.getFullGraph()==null) {
			node = node.getFirstSuccessor();
			list.addFirst(node.getRevisionNumber());
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
