package de.tud.plt.r43ples.merging.model.structure;

import java.util.HashMap;

/**
 * The high level change model.
 * 
 * @author Stephan Hensel
 *
 */
public class HighLevelChangeModel {

	/** The hash map which contains all high level changes (renaming). **/
	private HashMap<String, HighLevelChangeRenaming> highLevelChangesRenaming;
	
	
	/**
	 * The constructor.
	 */
	public HighLevelChangeModel() {
		setHighLevelChangesRenaming(new HashMap<String, HighLevelChangeRenaming>());
	}


	/**
	 * Get the high level changes (renaming).
	 * 
	 * @return the hash map with all high level changes (renaming)
	 */
	public HashMap<String, HighLevelChangeRenaming> getHighLevelChangesRenaming() {
		return highLevelChangesRenaming;
	}


	/**
	 * Set the high level changes (renaming).
	 * 
	 * @param highLevelChangesRenaming the high level changes (renaming) to set
	 */
	public void setHighLevelChangesRenaming(HashMap<String, HighLevelChangeRenaming> highLevelChangesRenaming) {
		this.highLevelChangesRenaming = highLevelChangesRenaming;
	}
	
	
	/**
	 * Add a high level change (renaming). If the difference group identifier already exists the old high level change (renaming) will be overwritten.
	 * 
	 * @param identifier the identifier
	 * @param highLevelChangeRenaming the high level change (renaming)
	 */
	public void addHighLevelChangeRenaming(String identifier, HighLevelChangeRenaming highLevelChangeRenaming) {
		this.highLevelChangesRenaming.put(identifier, highLevelChangeRenaming);
	}
	
	
	/**
	 * Remove entry from high level changes (renaming).
	 * 
	 * @param identifier the identifier of the high level change (renaming) to remove
	 */
	public void removeHighLevelChangeRenaming(String identifier) {
		this.highLevelChangesRenaming.remove(identifier);
	}
	
	
	/**
	 * Clear the high level change model.
	 */
	public void clear() {
		highLevelChangesRenaming.clear();
	}

}