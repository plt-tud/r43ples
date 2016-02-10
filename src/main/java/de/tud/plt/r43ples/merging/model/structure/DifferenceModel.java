package de.tud.plt.r43ples.merging.model.structure;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The difference model provides the structure for storing the returned difference model after a MERGE query.
 * 
 * @author Stephan Hensel
 *
 */
public class DifferenceModel {

	/** The hash map which contains all difference groups. **/
	private HashMap<String, DifferenceGroup> differenceGroups = new HashMap<String, DifferenceGroup>();
	
	public ArrayList<Difference> allDifferences = new ArrayList<Difference>(); 
	
	
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
	 * Add a difference group. If the difference group identifier already exists the old difference group will be overwritten.
	 * 
	 * @param identifier the identifier
	 * @param differenceGroup the difference group
	 */
	public void addDifference(Difference difference) {
		this.allDifferences.add(difference);
	}
	
	
	/**
	 * Remove entry from difference groups.
	 * 
	 * @param identifier the identifier of the difference group to remove
	 */
	public void removeDifferenceGroup(String identifier) {
		this.differenceGroups.remove(identifier);
	}


	public HashMap<String, DifferenceGroup> getDifferenceGroups() {
		return differenceGroups;
	}
	
	
}
