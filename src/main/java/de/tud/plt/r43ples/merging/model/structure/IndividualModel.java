package de.tud.plt.r43ples.merging.model.structure;

import java.util.HashMap;

/**
 * The individual model provides the structure for storing individual information of a revision.
 * 
 * @author Stephan Hensel
 *
 */
public class IndividualModel {

	/** The hash map which contains all individual. Key is the individual URI. **/
	private HashMap<String, IndividualStructure> individualStructures;
	
	
	/**
	 * The constructor.
	 */
	public IndividualModel() {
		setIndividualStructures(new HashMap<String, IndividualStructure>());
	}


	/**
	 * Get the individual structures.
	 * 
	 * @return the individual structures
	 */
	public HashMap<String, IndividualStructure> getIndividualStructures() {
		return individualStructures;
	}


	/**
	 * Set the individual structures
	 * 
	 * @param individualStructures the individual structures to set
	 */
	public void setIndividualStructures(HashMap<String, IndividualStructure> individualStructures) {
		this.individualStructures = individualStructures;
	}

	
	/**
	 * Add an individual structure. If the individual structure identifier already exists the old individual structure will be overwritten.
	 * 
	 * @param identifier the identifier
	 * @param individualStructure the individual structure
	 */
	public void addIndividualStructure(String identifier, IndividualStructure individualStructure) {
		this.individualStructures.put(identifier, individualStructure);
	}
	
	
	/**
	 * Remove entry from individual structures.
	 * 
	 * @param identifier the identifier of the individual structure to remove
	 */
	public void removeIndividualStructure(String identifier) {
		this.individualStructures.remove(identifier);
	}
	
	
	/**
	 * Clear the individual model.
	 */
	public void clear() {
		individualStructures.clear();
	}

}
