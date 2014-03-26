package de.tud.plt.r43ples.adminInterface;
import java.util.HashMap;


/**
 * Structure class. Contains ontology in Java classes.
 * 
 * @author Stephan Hensel
 *
 */
public class Structure {

	/** the ontology name */
	private String ontologyName = null;
	/** has map with all classes */
	private HashMap<String, StructureClass> classes = null;
	/** has map with all properties (data type properties and object properties) */
	private HashMap<String, StructureProperty> properties = null;
	
	
	/**
	 * The constructor of Structure.
	 * 
	 * @param ontologyName the ontology name
	 * @param classes the classes hash map
	 * @param properties the properties hash map (data type properties and object properties)
	 */
	public Structure(String ontologyName, HashMap<String, StructureClass> classes,  HashMap<String, StructureProperty> properties) {
		this.ontologyName = ontologyName;
		this.classes = classes;
		this.properties = properties;
	}

	
	/**
	 * Get the ontology name.
	 * 
	 * @return the ontology name
	 */
	public String getOntologyName() {
		return ontologyName;
	}

	
	/**
	 * Get a hash map with all classes.
	 * 
	 * @return the classes
	 */
	public HashMap<String, StructureClass> getClasses() {
		return classes;
	}
	
	
	/**
	 * Get a hash map with all properties (data type properties and object properties).
	 * 
	 * @return the properties
	 */
	public HashMap<String, StructureProperty> getProperties() {
		return properties;
	}

}
