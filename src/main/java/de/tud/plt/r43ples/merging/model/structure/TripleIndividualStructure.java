package de.tud.plt.r43ples.merging.model.structure;

/**
 * Stores all information of an individual triple.
 * 
 * @author Stephan Hensel
 *
 */
public class TripleIndividualStructure {

	/** The triple. **/
	private Triple triple;
	/** The difference. **/
	private Difference difference;
	

	/**
	 * The constructor.
	 * 
	 * @param triple the triple
	 * @param difference the difference
	 */
	public TripleIndividualStructure(Triple triple, Difference difference) {
		this.setTriple(triple);
		this.setDifference(difference);
	}


	/**
	 * Get the triple.
	 * 
	 * @return the triple
	 */
	public Triple getTriple() {
		return triple;
	}


	/**
	 * Set the triple.
	 * 
	 * @param triple the triple to set
	 */
	public void setTriple(Triple triple) {
		this.triple = triple;
	}


	/**
	 * Get the difference.
	 * 
	 * @return the difference
	 */
	public Difference getDifference() {
		return difference;
	}


	/**
	 * Set the difference.
	 * 
	 * @param difference the difference to set
	 */
	public void setDifference(Difference difference) {
		this.difference = difference;
	}

}
