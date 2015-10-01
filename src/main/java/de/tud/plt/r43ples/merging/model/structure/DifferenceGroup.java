package de.tud.plt.r43ples.merging.model.structure;

import java.util.HashMap;

import de.tud.plt.r43ples.merging.SDDTripleStateEnum;


/**
 * Stores all information of a difference group.
 * 
 * @author Stephan Hensel
 *
 */
public class DifferenceGroup {

	/** The triple state in A. **/
	private SDDTripleStateEnum tripleStateA;
	/** The triple state in B. **/
	private SDDTripleStateEnum tripleStateB;
	/** The automatic resolution state. **/
	private SDDTripleStateEnum automaticResolutionState;
	/** The conflicting property. Specifies if this difference is a conflict or not. **/
	private boolean conflicting;
	/** The array list which contains all differences. **/
	private HashMap<String, Difference> differences;
	
	
	/**
	 * The constructor.
	 * 
	 * @param tripleStateA the triple state in A
	 * @param tripleStateB the triple state in B
	 * @param automaticResolutionState the automatic resolution state
	 * @param conflicting is conflicting
	 */
	public DifferenceGroup(SDDTripleStateEnum tripleStateA, SDDTripleStateEnum tripleStateB, SDDTripleStateEnum automaticResolutionState, boolean conflicting) {
		this.tripleStateA = tripleStateA;
		this.tripleStateB = tripleStateB;
		this.automaticResolutionState = automaticResolutionState;
		this.conflicting = conflicting;
		setDifferences(new HashMap<String, Difference>());
	}

	
	/**
	 * Get the triple state in A.
	 * 
	 * @return the triple state
	 */
	public SDDTripleStateEnum getTripleStateA() {
		return tripleStateA;
	}

	
	/**
	 * Set the triple state of A.
	 * 
	 * @param tripleStateA the triple state in A
	 */
	public void setTripleStateA(SDDTripleStateEnum tripleStateA) {
		this.tripleStateA = tripleStateA;
	}


	/**
	 * Get the triple state in B.
	 * 
	 * @return the triple state
	 */
	public SDDTripleStateEnum getTripleStateB() {
		return tripleStateB;
	}


	/**
	 * Set the triple state of B.
	 * 
	 * @param tripleStateB the triple state in B
	 */
	public void setTripleStateB(SDDTripleStateEnum tripleStateB) {
		this.tripleStateB = tripleStateB;
	}


	/**
	 * Get the automatic resolution state.
	 * 
	 * @return the automatic resolution state
	 */
	public SDDTripleStateEnum getAutomaticResolutionState() {
		return automaticResolutionState;
	}


	/**
	 * Set the automatic resolution state.
	 * 
	 * @param automaticResolutionState the automatic resolution state
	 */
	public void setAutomaticResolutionState(SDDTripleStateEnum automaticResolutionState) {
		this.automaticResolutionState = automaticResolutionState;
	}


	/**
	 * Is this difference group a conflicting group.
	 * 
	 * @return the is conflicting property
	 */
	public boolean isConflicting() {
		return conflicting;
	}


	/**
	 * Set the conflicting property
	 * 
	 * @param conflicting the conflicting property
	 */
	public void setConflicting(boolean conflicting) {
		this.conflicting = conflicting;
	}
	

	/**
	 * Get the differences.
	 * 
	 * @return the hash map with all differences
	 */
	public HashMap<String, Difference> getDifferences() {
		return differences;
	}


	/**
	 * Set the differences.
	 * 
	 * @param differences the differences to set
	 */
	public void setDifferences(HashMap<String, Difference> differences) {
		this.differences = differences;
	}
	
	
	/**
	 * Add a difference. If the difference identifier already exists the old difference will be overwritten.
	 * 
	 * @param identifier the identifier
	 * @param difference the difference
	 */
	public void addDifference(String identifier, Difference difference) {
		this.differences.put(identifier, difference);
	}
	
	
	/**
	 * Remove entry from difference.
	 * 
	 * @param identifier the identifier of the difference to remove
	 */
	public void removeDifference(String identifier) {
		this.differences.remove(identifier);
	}
	
}
