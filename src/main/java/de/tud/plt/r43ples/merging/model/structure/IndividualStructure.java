package de.tud.plt.r43ples.merging.model.structure;

import java.util.HashMap;

/**
 * Stores all information of an individual.
 * 
 * @author Stephan Hensel
 *
 */
public class IndividualStructure {

	/** The individual URI. **/
	private String individualUri;
	/** The hash map which contains all corresponding triples of individual. **/
	private HashMap<String, TripleIndividualStructure> triples;
	
	
	/**
	 * The constructor.
	 * 
	 * @param individualUri the individual URI
	 */
	public IndividualStructure(String individualUri) {
		this.setIndividualUri(individualUri);
		triples = new HashMap<String, TripleIndividualStructure>();
	}


	/**
	 * Get the individual URI.
	 * 
	 * @return the individual URI
	 */
	public String getIndividualUri() {
		return individualUri;
	}


	/**
	 * Set the individual URI.
	 * 
	 * @param individualUri the individual URI to set
	 */
	public void setIndividualUri(String individualUri) {
		this.individualUri = individualUri;
	}


	/**
	 * Get the corresponding triples of individual.
	 * 
	 * @return the triples
	 */
	public HashMap<String, TripleIndividualStructure> getTriples() {
		return triples;
	}


	/**
	 * Set the corresponding triples of individual.
	 * 
	 * @param triples the triples to set
	 */
	public void setTriples(HashMap<String, TripleIndividualStructure> triples) {
		this.triples = triples;
	}
	
	
	/**
	 * Add a triple. If the triple identifier already exists the old triple will be overwritten.
	 * 
	 * @param identifier the identifier (triple to string)
	 * @param triple the triple
	 */
	public void addTriple(String identifier, TripleIndividualStructure triple) {
		triples.put(identifier, triple);
	}
		
	
	/**
	 * Remove entry from triples.
	 * 
	 * @param identifier the identifier (triple to string) of the triple to remove
	 */
	public void removeTriple(String identifier) {
		this.triples.remove(identifier);
	}
	
}
