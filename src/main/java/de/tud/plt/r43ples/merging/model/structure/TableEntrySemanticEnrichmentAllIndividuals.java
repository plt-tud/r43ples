package de.tud.plt.r43ples.merging.model.structure;


/**
 * Table entry of resolution triples table.
 * 
 * @author Stephan Hensel
 *
 */
public class TableEntrySemanticEnrichmentAllIndividuals {
	/** The individual structure A. **/
	private IndividualStructure individualStructureA;
	/** The individual structure B. **/
	private IndividualStructure individualStructureB;
	/** The row data. **/
	private Object[] rowData;
	
		
	/**
	 * The constructor.
	 * 
	 * @param individualStructureA the individual structure A
	 * @param individualStructureB the individual structure B
	 * @param rowData the row data
	 */
	public TableEntrySemanticEnrichmentAllIndividuals(IndividualStructure individualStructureA, IndividualStructure individualStructureB, Object[] rowData) {
		this.setIndividualStructureA(individualStructureA);
		this.setIndividualStructureB(individualStructureB);
		this.setRowData(rowData);
	}


	/**
	 * Get the individual structure A.
	 * 
	 * @return the individual structure A
	 */
	public IndividualStructure getIndividualStructureA() {
		return individualStructureA;
	}


	/**
	 * Set the individual structure A.
	 * 
	 * @param individualStructure the individual structure A to set
	 */
	public void setIndividualStructureA(IndividualStructure individualStructure) {
		this.individualStructureA = individualStructure;
	}
	
	
	/**
	 * Get the individual structure B.
	 * 
	 * @return the individual structure B
	 */
	public IndividualStructure getIndividualStructureB() {
		return individualStructureB;
	}


	/**
	 * Set the individual structure B.
	 * 
	 * @param individualStructure the individual structure B to set
	 */
	public void setIndividualStructureB(IndividualStructure individualStructure) {
		this.individualStructureB = individualStructure;
	}


	/**
	 * Get the row data.
	 * 
	 * @return the row data
	 */
	public Object[] getRowData() {
		return rowData;
	}


	/**
	 * Set the row data.
	 * 
	 * @param rowData the row data
	 */
	public void setRowData(Object[] rowData) {
		this.rowData = rowData;
	}
	
}
