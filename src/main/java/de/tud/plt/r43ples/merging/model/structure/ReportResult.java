package de.tud.plt.r43ples.merging.model.structure;

/**
 * The report result.
 * 
 * @author Stephan Hensel
 *
 */
public class ReportResult {

	/** Counter of the not approved conflicts. **/
	private int conflictsNotApproved = 0;
	/** Counter of manually changed difference resolution states. **/
	private int differencesResolutionChanged = 0;
	
	
	/**
	 * Get the number of not approved conflicts.
	 * 
	 * @return the conflictsNotApproved
	 */
	public int getConflictsNotApproved() {
		return conflictsNotApproved;
	}
	
	
	/**
	 * Increment the number of not approved conflicts.
	 */
	public void incrementCounterConflictsNotApproved() {
		conflictsNotApproved++;
	}
	
	
	/**
	 * Set the number of not approved conflicts.
	 * 
	 * @param conflictsNotApproved the conflictsNotApproved to set
	 */
	public void setConflictsNotApproved(int conflictsNotApproved) {
		this.conflictsNotApproved = conflictsNotApproved;
	}
	
	
	/**
	 * Get the number of manually changed difference resolution states.
	 * 
	 * @return the differencesResolutionChanged
	 */
	public int getDifferencesResolutionChanged() {
		return differencesResolutionChanged;
	}
	
	
	/**
	 * Increment the number of manually changed difference resolution states.
	 */
	public void incrementCounterDifferencesResolutionChanged() {
		differencesResolutionChanged++;
	}
	
	/**
	 * decrement the number of manually changed difference resolution states.
	 */
	public void decrementCounterDifferencesResolutionChanged() {
		differencesResolutionChanged--;
	}
	
	/**
	 * Set the number of manually changed difference resolution states.
	 * 
	 * @param differencesResolutionChanged the differencesResolutionChanged to set
	 */
	public void setDifferencesResolutionChanged(int differencesResolutionChanged) {
		this.differencesResolutionChanged = differencesResolutionChanged;
	}
	
}
