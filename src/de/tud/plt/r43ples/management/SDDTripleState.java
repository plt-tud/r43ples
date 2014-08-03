package de.tud.plt.r43ples.management;

/**
 * The SDD triple state.
 * 
 * @author Stephan
 *
 */
public enum SDDTripleState {
	ADDED("http://eatld.et.tu-dresden.de/sddo#Added", "http://eatld.et.tu-dresden.de/rmo#added"),
	DELETED("http://eatld.et.tu-dresden.de/sddo#Deleted", "http://eatld.et.tu-dresden.de/rmo#removed"),
	ORIGINAL("http://eatld.et.tu-dresden.de/sddo#Original", "http://eatld.et.tu-dresden.de/rmo#original"),
	NOTINCLUDED("http://eatld.et.tu-dresden.de/sddo#NotIncluded", null);
	
	/** The SDD representation. **/
	private String sddRepresentation;
	/** The RMO representation. **/
	private String rmoRepresentation;
	
	
	/**
	 * The constructor.
	 * 
	 * @param sddRepresentation the SDD representation
	 * @param rmoRepresentation the RMO representation
	 */
	private SDDTripleState(String sddRepresentation, String rmoRepresentation) {
		this.sddRepresentation = sddRepresentation;
		this.rmoRepresentation = rmoRepresentation;
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
	 * Get the RMO representation.
	 * 
	 * @return the RMO representation
	 */
	public String getRmoRepresentation() {
		return rmoRepresentation;
	}
	
}