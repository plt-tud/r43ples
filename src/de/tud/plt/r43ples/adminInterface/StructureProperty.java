package de.tud.plt.r43ples.adminInterface;
import com.hp.hpl.jena.graph.Node;


/**
 * StructureProperty class. Contains detail information of a single owl property.
 * 
 * @author Stephan Hensel
 *
 */
public class StructureProperty {
	
	/** the type of the property */
	private String type = null;
	/** the label of the property */
	private String label = null;
	/** the uri of the class */
	private Node uri = null;
	/** the range of the property */
	private String range = null;
	
	
	/**
	 * The constructor of StructureClass.
	 * 
	 * @param type the type
	 * @param label the label
	 * @param uri the uri
	 * @param range the range
	 */
	public StructureProperty(String type, String label, Node uri, String range) {
		this.type = type;
		this.label = label;
		this.uri = uri;
		this.range = range;

	}

	
	/**
	 * Get the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
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
	 * Get the range.
	 * 
	 * @return the range
	 */
	public String getRange() {
		return range;
	}

}