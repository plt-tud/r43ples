package de.tud.plt.r43ples.merging.model.structure;

import java.util.HashMap;

/**
 * The difference model provides the structure for storing the returned difference model after a MERGE query.
 * 
 * @author Stephan Hensel
 *
 */
public class DifferenceModel {

	/** The hash map which contains all difference groups. **/
	private HashMap<String, DifferenceGroup> differenceGroups;
	
	
	/**
	 * The constructor.
	 */
	public DifferenceModel() {
		setDifferenceGroups(new HashMap<String, DifferenceGroup>());
	}


	/**
	 * Get the difference groups.
	 * 
	 * @return the hash map with all difference groups
	 */
	public HashMap<String, DifferenceGroup> getDifferenceGroups() {
		return differenceGroups;
	}


	/**
	 * Set the difference groups.
	 * 
	 * @param differenceGroups the difference groups to set
	 */
	public void setDifferenceGroups(HashMap<String, DifferenceGroup> differenceGroups) {
		this.differenceGroups = differenceGroups;
	}
	
	
	/**
	 * Add a difference group. If the difference group identifier already exists the old difference group will be overwritten.
	 * 
	 * @param identifier the identifier
	 * @param differenceGroup the difference group
	 */
	public void addDifferenceGroup(String identifier, DifferenceGroup differenceGroup) {
		this.differenceGroups.put(identifier, differenceGroup);
	}
	
	
	/**
	 * Remove entry from difference groups.
	 * 
	 * @param identifier the identifier of the difference group to remove
	 */
	public void removeDifferenceGroup(String identifier) {
		this.differenceGroups.remove(identifier);
	}
	
	
	/**
	 * Clear the difference model.
	 */
	public void clear() {
		differenceGroups.clear();
	}

}
