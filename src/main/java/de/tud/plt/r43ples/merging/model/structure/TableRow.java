package de.tud.plt.r43ples.merging.model.structure;

/**TableRow record the data in each row of table
 *  tripleId and triple for manual resolution state
 * 
 * @author Xinyu Yang
 * 
 * */

public class TableRow {
	
	String tripleId = null;
	Triple triple;
	String subject;
	String predicate;
	String object;
	String stateA;
	String stateB;
	String revisionA;
	String revisionB;
	String conflicting;
	String resolutionState;
	String state;
	
	public TableRow(Triple triple, String subject, String predicate, String object, String stateA, 
			String stateB, String revisionA, String revisionB, String conflicting, String resolutionState,String state){
		this.triple = triple;
		this.subject = subject;
		this.object = object;
		this.predicate = predicate;
		this.stateA = stateA;
		this.stateB = stateB;
		this.revisionA = revisionA;
		this.revisionB = revisionB;
		this.conflicting = conflicting;	
		this.resolutionState = resolutionState;
		this.state =state;
	}

	public String getTripleId() {
		return tripleId;
	}

	public void setTripleId(String tripleId) {
		this.tripleId = tripleId;
	}

	public Triple getTriple() {
		return triple;
	}

	public void setTriple(Triple triple) {
		this.triple = triple;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getStateA() {
		return stateA;
	}

	public void setStateA(String stateA) {
		this.stateA = stateA;
	}

	public String getStateB() {
		return stateB;
	}

	public void setStateB(String stateB) {
		this.stateB = stateB;
	}

	public String getRevisionA() {
		return revisionA;
	}

	public void setRevisionA(String revisionA) {
		this.revisionA = revisionA;
	}

	public String getRevisionB() {
		return revisionB;
	}

	public void setRevisionB(String revisionB) {
		this.revisionB = revisionB;
	}

	public String getConflicting() {
		return conflicting;
	}

	public void setConflicting(String conflicting) {
		this.conflicting = conflicting;
	}	
	
	public String getResolutionState() {
		return resolutionState;
	}

	public void setResolutionState(String resolutionState) {
		this.resolutionState = resolutionState;
	}	
	
	public void setState(String state){
		this.state = state;
	}
	
	public String getState(){
		return this.state;
	}
}
