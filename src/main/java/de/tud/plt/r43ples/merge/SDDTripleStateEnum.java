package de.tud.plt.r43ples.merge;

/**
 * The SDD triple state.
 * 
 * @author Stephan Hensel
 *
 */
public enum SDDTripleStateEnum {
	ADDED("http://eatld.et.tu-dresden.de/sddo#added", "http://eatld.et.tu-dresden.de/rpo#added"),
	DELETED("http://eatld.et.tu-dresden.de/sddo#deleted", "http://eatld.et.tu-dresden.de/rpo#removed"),
	ORIGINAL("http://eatld.et.tu-dresden.de/sddo#original", "http://eatld.et.tu-dresden.de/rpo#original"),
	NOTINCLUDED("http://eatld.et.tu-dresden.de/sddo#notIncluded", null);
	
	/** The SDD representation. **/
	private String sddRepresentation;
	/** The RPO representation. **/
	private String rpoRepresentation;
	
	
	/**
	 * The constructor.
	 * 
	 * @param sddRepresentation the SDD representation
	 * @param rpoRepresentation the RPO representation
	 */
	private SDDTripleStateEnum(String sddRepresentation, String rpoRepresentation) {
		this.sddRepresentation = sddRepresentation;
		this.rpoRepresentation = rpoRepresentation;
	}
	
	
	/**
	 * Get the SDD representation.
	 * 
	 * @return the SDD representation
	 */
	public String getSddRepresentation() {
		return sddRepresentation;
	}
	
	
	/**
	 * Get the RPO representation.
	 * 
	 * @return the RPO representation
	 */
	public String getRpoRepresentation() {
		return rpoRepresentation;
	}
	
}