package de.tud.plt.r43ples.merging.model.structure;

import de.tud.plt.r43ples.merging.TripleObjectTypeEnum;


/**
 * Stores one triple.
 * 
 * @author Stephan Hensel
 *
 */
public class Triple {

	/** The subject of the triple. **/
	private String subject;
	/** The predicate of the triple. **/
	private String predicate;
	/** The object of the triple. **/
	private String object;
	/** The object type. **/
	private TripleObjectTypeEnum objectType;
	
	
	/**
	 * The constructor.
	 * 
	 * @param subject the subject
	 * @param predicate the predicate
	 * @param object the object
	 * @param objectType the object type
	 */
	public Triple(String subject, String predicate, String object, TripleObjectTypeEnum objectType) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.objectType = objectType;
	}


	/**
	 * Get the subject.
	 * 
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}


	/**
	 * Set the subject
	 * 
	 * @param subject the subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}


	/**
	 * Get the predicate.
	 * 
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}


	/**
	 * Set the predicate
	 * 
	 * @param predicate the predicate
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}


	/**
	 * Get the object.
	 * 
	 * @return the object
	 */
	public String getObject() {
		return object;
	}


	/**
	 * Set the object.
	 * 
	 * @param object the object
	 */
	public void setObject(String object) {
		this.object = object;
	}


	/**
	 * Get the object type.
	 * 
	 * @return the object type
	 */
	public TripleObjectTypeEnum getObjectType() {
		return objectType;
	}


	/**
	 * Set the object type.
	 * 
	 * @param objectType the object type
	 */
	public void setObjectType(TripleObjectTypeEnum objectType) {
		this.objectType = objectType;
	}
	
}
