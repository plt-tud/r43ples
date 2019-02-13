package de.tud.plt.r43ples.core;

/**
 * The merge triple state enum.
 * 
 * @author Stephan Hensel
 *
 */
public enum MergeTripleStateEnum {
	ADDED("http://eatld.et.tu-dresden.de/mmo#Added", "http://eatld.et.tu-dresden.de/mmo#Added"),
	DELETED("http://eatld.et.tu-dresden.de/mmo#Deleted", "http://eatld.et.tu-dresden.de/mmo#Deleted"),
	ORIGINAL("http://eatld.et.tu-dresden.de/mmo#Original", "http://eatld.et.tu-dresden.de/mmo#Original"),
	NOTINCLUDED("http://eatld.et.tu-dresden.de/mmo#NotIncluded", null);
	
	/** The StructuralDefinition representation. **/
	private String sdRepresentation;
	/** The DifferenceGroup representation. **/
	private String dgRepresentation;
	
	
	/**
	 * The constructor.
	 * 
	 * @param sddRepresentation the StructuralDefinition representation
	 * @param dgRepresentation the DifferenceGroup representation
	 */
	MergeTripleStateEnum(String sddRepresentation, String dgRepresentation) {
		this.sdRepresentation = sddRepresentation;
		this.dgRepresentation = dgRepresentation;
	}
	
	
	/**
	 * Get the StructuralDefinition representation.
	 * 
	 * @return the StructuralDefinition representation
	 */
	public String getSdRepresentation() {
		return sdRepresentation;
	}
	
	
	/**
	 * Get the DifferenceGroup representation.
	 * 
	 * @return the DifferenceGroup representation
	 */
	public String getDgRepresentation() {
		return dgRepresentation;
	}
	
}