package de.tud.plt.r43ples.adminInterface;
import java.util.ArrayList;
import java.util.HashMap;

import com.hp.hpl.jena.graph.Node;


/**
 * Saves the differences for an element.
 * 
 * @author Stephan Hensel
 *
 */
public class Differences {
	
	/** list with all added elements */
	private ArrayList<Node> added = null;
	/** list with all removed elements */
	private ArrayList<Node> removed = null;
	/** list with all elements, which are in both graphs */
	private ArrayList<Node> same = null;
	/** map: key=uri of a specific element, value=map with all differences (in sub elements of element) */
	private HashMap<Node, ArrayList<Node>> differences = null;
	
	/**
	 * Constructor.
	 * 
	 * @param added the list with all added elements
	 * @param removed the list with all removed elements
	 * @param same the list with all elements, which are in both graphs
	 * @param differences map: key=uri of a specific element, value=array list with all differences (in sub elements of element)
	 */
	public Differences(ArrayList<Node> added, ArrayList<Node> removed, ArrayList<Node> same, HashMap<Node, ArrayList<Node>> differences) {
		this.added = added;
		this.removed = removed;
		this.same = same;
		this.differences = differences;
	}

	/**
	 * Get the list with all added elements.
	 * 
	 * @return the added array list
	 */
	public ArrayList<Node> getAdded() {
		return added;
	}

	/**
	 * Get the list with all removed elements.
	 * 
	 * @return the removed array list
	 */
	public ArrayList<Node> getRemoved() {
		return removed;
	}

	/**
	 * Get the list with all elements, which are in both graphs.
	 * 
	 * @return the same array list
	 */
	public ArrayList<Node> getSame() {
		return same;
	}

	/**
	 * Get the differences map. map: key=uri of a specific element, value=map with all differences (in sub elements of element) 
	 * 
	 * @return the differences map
	 */
	public HashMap<Node, ArrayList<Node>> getDifferences() {
		return differences;
	}
	
}
