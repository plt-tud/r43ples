package de.tud.plt.r43ples.merging.model.structure;


/**store the information of difference model as high level table
 * 
 * @author Xinyu Yang
 * 
 * */

public class HighLevelChangeTableRow {
	String tripleId = null;
	HighLevelChangeRenaming  highLevelChangeRenaming;
	String subject;
	String predicate;
	String objectAlt;
	String objectNew;
	String isResolved;
	String isRenaming;
		
	
	public HighLevelChangeTableRow(HighLevelChangeRenaming highLevelChangeRenaming, String subject,
			String predicate, String objectAlt, String objectNew, String isResolved, String isRenaming) {
		this.highLevelChangeRenaming = highLevelChangeRenaming;
		this.subject = subject;
		this.predicate = predicate;
		this.objectAlt = objectAlt;
		this.objectNew = objectNew;
		this.isResolved = isResolved;
		this.isRenaming = isRenaming;
	}


	public String getTripleId() {
		return tripleId;
	}


	public void setTripleId(String tripleId) {
		this.tripleId = tripleId;
	}


	public HighLevelChangeRenaming getHighLevelChangeRenaming() {
		return highLevelChangeRenaming;
	}


	public void setHighLevelChangeRenaming(
			HighLevelChangeRenaming highLevelChangeRenaming) {
		this.highLevelChangeRenaming = highLevelChangeRenaming;
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


	public String getObjectAlt() {
		return objectAlt;
	}


	public void setObjectAlt(String objectAlt) {
		this.objectAlt = objectAlt;
	}


	public String getObjectNew() {
		return objectNew;
	}


	public void setObjectNew(String objectNew) {
		this.objectNew = objectNew;
	}


	public String getIsResolved() {
		return isResolved;
	}


	public void setIsResolved(String isResolved) {
		this.isResolved = isResolved;
	}


	public String getIsRenaming() {
		return isRenaming;
	}


	public void setIsRenaming(String isRenaming) {
		this.isRenaming = isRenaming;
	}
	
}
