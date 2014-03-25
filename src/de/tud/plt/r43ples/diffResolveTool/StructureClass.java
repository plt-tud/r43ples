package de.tud.plt.r43ples.diffResolveTool;
import java.util.ArrayList;

import com.hp.hpl.jena.graph.Node;


/**
 * StructureClass class. Contains detail information of a single owl class.
 * 
 * @author Stephan Hensel
 *
 */
public class StructureClass {

	/** the label of the class */
	private String label = null;
	/** the uri of the class */
	private Node uri = null;
	/** array list with all properties of class (uri list)*/
	private ArrayList<String> properties = null;
	
	
	/**
	 * The constructor of StructureClass.
	 * 
	 * @param label the label
	 * @param uri the uri
	 * @param properties the properties array list
	 */
	public StructureClass(String label, Node uri, ArrayList<String> properties) {
		this.label = label;
		this.uri = uri;
		this.properties = properties;
	}

	
	/**
	 * Get the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	
	/**
	 * Get the uri.
	 * 
	 * @return the uri
	 */
	public Node getUri() {
		return uri;
	}

	
	/**
	 * Get the properties array list.
	 * 
	 * @return the properties array list
	 */
	public ArrayList<String> getProperties() {
		return properties;
	}
	
}
