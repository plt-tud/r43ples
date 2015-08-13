package de.tud.plt.r43ples.merging.model.structure;


/**store the information of the difference model with as table row
 * 
 * @author Xinyu Yang
 * 
 * */

public class ReportTableRow {
	String subject;
	String predicate;
	String object;
	String stateA;
	String stateB;
	String revisionA;
	String revisionB;
	String conflicting;
	String automaticResolutionState;
	String resolutionState;
	String approved;
	public ReportTableRow(String subject, String predicate, String object,
			String stateA, String stateB, String revisionA, String revisionB,
			String conflicting, String automaticResolutionState,
			String resolutionState, String approved) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.stateA = stateA;
		this.stateB = stateB;
		this.revisionA = revisionA;
		this.revisionB = revisionB;
		this.conflicting = conflicting;
		this.automaticResolutionState = automaticResolutionState;
		this.resolutionState = resolutionState;
		this.approved = approved;
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
	public String getAutomaticResolutionState() {
		return automaticResolutionState;
	}
	public void setAutomaticResolutionState(String automaticResolutionState) {
		this.automaticResolutionState = automaticResolutionState;
	}
	public String getResolutionState() {
		return resolutionState;
	}
	public void setResolutionState(String resolutionState) {
		this.resolutionState = resolutionState;
	}
	public String getApproved() {
		return approved;
	}
	public void setApproved(String approved) {
		this.approved = approved;
	}
	
	
}


