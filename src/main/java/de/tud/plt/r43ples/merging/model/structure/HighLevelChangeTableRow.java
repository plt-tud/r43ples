package de.tud.plt.r43ples.merging.model.structure;

public class HighLevelChangeTableRow {
	String tripleId = null;
	HighLevelChangeRenaming  highLevelChangeRenaming;
	String subject;
	String predicate;
	String objectAlt;
	String objectNew;
		
	
	public HighLevelChangeTableRow(HighLevelChangeRenaming highLevelChangeRenaming, String subject,
			String predicate, String objectAlt, String objectNew) {
		this.highLevelChangeRenaming = highLevelChangeRenaming;
		this.subject = subject;
		this.predicate = predicate;
		this.objectAlt = objectAlt;
		this.objectNew = objectNew;
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
	
	
}
