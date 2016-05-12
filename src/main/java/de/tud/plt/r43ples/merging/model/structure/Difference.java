package de.tud.plt.r43ples.merging.model.structure;

import de.tud.plt.r43ples.merging.ResolutionStateEnum;
import de.tud.plt.r43ples.merge.SDDTripleStateEnum;


/**
 * Stores all information of a difference.
 * 
 * @author Stephan Hensel
 *
 */
public class Difference {

	/** The triple. **/
	private Triple triple;
	/** The referenced revision URI in A. Characterizes in which revision the difference occurred. **/
	private String referencedRevisionA;
	/** The revision label of referenced revision A. **/
	private String referencedRevisionLabelA;
	/** The referenced revision URI in B. Characterizes in which revision the difference occurred. **/
	private String referencedRevisionB;
	/** The revision label of referenced revision B. **/
	private String referencedRevisionLabelB;
	/** The triple resolution state. **/
	private SDDTripleStateEnum tripleResolutionState;
	/** The resolution state of this difference. **/
	private ResolutionStateEnum resolutionState;
	public SDDTripleStateEnum tripleStateA;
	public SDDTripleStateEnum tripleStateB;
	public boolean conflicting;
	
	
	/**
	 * The constructor.
	 * 
	 * @param triple the triple
	 * @param referencedRevisionA the referenced revision in A
	 * @param referencedRevisionLabelA the referenced revision label in A
	 * @param referencedRevisionB the referenced revision in B
	 * @param referencedRevisionLabelB the referenced revision label in B
	 * @param tripleResolutionState the triple resolution state
	 * @param resolutionState the resolution state
	 */
	public Difference(Triple triple, 
			String referencedRevisionA, String referencedRevisionLabelA, SDDTripleStateEnum tripleStateA, 
			String referencedRevisionB, String referencedRevisionLabelB, SDDTripleStateEnum tripleStateB,
			boolean conflicting, SDDTripleStateEnum tripleResolutionState, ResolutionStateEnum resolutionState) {
		this.triple = triple;
		this.referencedRevisionA = referencedRevisionA;
		this.referencedRevisionLabelA = referencedRevisionLabelA;
		this.tripleStateA = tripleStateA;
		this.referencedRevisionB = referencedRevisionB;
		this.referencedRevisionLabelB = referencedRevisionLabelB;
		this.tripleStateB = tripleStateB;
		this.conflicting = conflicting;
		this.tripleResolutionState = tripleResolutionState;
		this.resolutionState = resolutionState;
	}


	/**
	 * Get the triple
	 * 
	 * @return the triple
	 */
	public Triple getTriple() {
		return triple;
	}


	/**
	 * Set the triple
	 * 
	 * @param triple the triple
	 */
	public void setTriple(Triple triple) {
		this.triple = triple;
	}


	/**
	 * Get the referenced revision in A.
	 * 
	 * @return the referenced revision in A
	 */
	public String getReferencedRevisionA() {
		return referencedRevisionA;
	}


	/**
	 * Set the referenced revision in A.
	 * 
	 * @param referencedRevisionA the referenced revision in A
	 */
	public void setReferencedRevisionA(String referencedRevisionA) {
		this.referencedRevisionA = referencedRevisionA;
	}


	/**
	 * Get the referenced revision label in A.
	 * 
	 * @return the referencedRevisionLabelA
	 */
	public String getReferencedRevisionLabelA() {
		return referencedRevisionLabelA;
	}


	/**
	 * Set the referenced revision label in A.
	 * 
	 * @param referencedRevisionLabelA the referencedRevisionLabelA to set
	 */
	public void setReferencedRevisionLabelA(String referencedRevisionLabelA) {
		this.referencedRevisionLabelA = referencedRevisionLabelA;
	}


	/**
	 * Get the referenced revision in B.
	 * 
	 * @return the referenced revision in B
	 */
	public String getReferencedRevisionB() {
		return referencedRevisionB;
	}


	/**
	 * Set the referenced revision in B.
	 * 
	 * @param referencedRevisionB the referenced revision in B
	 */
	public void setReferencedRevisionB(String referencedRevisionB) {
		this.referencedRevisionB = referencedRevisionB;
	}


	/**
	 * Get the referenced revision label in B.
	 * 
	 * @return the referencedRevisionLabelB
	 */
	public String getReferencedRevisionLabelB() {
		return referencedRevisionLabelB;
	}


	/**
	 * Set the referenced revision label in B.
	 * 
	 * @param referencedRevisionLabelB the referencedRevisionLabelB to set
	 */
	public void setReferencedRevisionLabelB(String referencedRevisionLabelB) {
		this.referencedRevisionLabelB = referencedRevisionLabelB;
	}


	/**
	 * Get the triple resolution state.
	 * 
	 * @return the triple resolution state
	 */
	public SDDTripleStateEnum getTripleResolutionState() {
		return tripleResolutionState;
	}


	/**
	 * Set the triple resolution state.
	 * 
	 * @param tripleResolutionState the triple resolution state
	 */
	public void setTripleResolutionState(SDDTripleStateEnum tripleResolutionState) {
		this.tripleResolutionState = tripleResolutionState;
	}


	/**
	 * Get the resolution state.
	 * 
	 * @return the resolutionState
	 */
	public ResolutionStateEnum getResolutionState() {
		return resolutionState;
	}


	/**
	 * Set the resolution state.
	 * 
	 * @param resolutionState the resolutionState to set
	 */
	public void setResolutionState(ResolutionStateEnum resolutionState) {
		this.resolutionState = resolutionState;
	}
	
}
