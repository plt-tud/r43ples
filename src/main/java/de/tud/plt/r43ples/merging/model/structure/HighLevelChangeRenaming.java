package de.tud.plt.r43ples.merging.model.structure;

/**
 * The high level change (renaming).
 * 
 * @author Stephan Hensel
 *
 */
public class HighLevelChangeRenaming {

	/** The deletion difference (ORIGINAL-DELETED or ADDED-DELETED). **/
	private Difference deletionDifference;
	/** The addition difference (NOTINCLUDED-ADDED). **/
	private Difference additionDifference;
	
	
	/**
	 * The constructor.
	 * 
	 * @param deletetionDifference the deletion difference
	 * @param additionDifference the addition difference
	 */
	public HighLevelChangeRenaming(Difference deletetionDifference, Difference additionDifference) {
		this.deletionDifference = deletetionDifference;
		this.additionDifference = additionDifference;
	}


	/**
	 * Get the deletion difference.
	 * 
	 * @return the deletion difference
	 */
	public Difference getDeletionDifference() {
		return deletionDifference;
	}


	/**
	 * Set the deletion difference.
	 * 
	 * @param deletionDifference the deletion difference to set
	 */
	public void setDeletionDifference(Difference deletionDifference) {
		this.deletionDifference = deletionDifference;
	}


	/**
	 * Get the addition difference.
	 * 
	 * @return the addition difference
	 */
	public Difference getAdditionDifference() {
		return additionDifference;
	}


	/**
	 * Set the addition difference.
	 * 
	 * @param additionDifference the addition difference to set
	 */
	public void setAdditionDifference(Difference additionDifference) {
		this.additionDifference = additionDifference;
	}
	
}
