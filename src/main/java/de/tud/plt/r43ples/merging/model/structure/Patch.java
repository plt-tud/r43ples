package de.tud.plt.r43ples.merging.model.structure;

/**patch model for the rebase process, store all the information about the Version
 * 
 * @author Xinyu Yang
 * 
 * */
public class Patch {
	String patchNumber;
	String patchUser;
	String patchMessage;
	String addedSetUri;
	String removedSetUri;
	
	public Patch(String patchNumber, String patchUser, String patchMessage,
			String addedSetUri,
			String removedSetUri) {
		
		this.patchNumber = patchNumber;
		this.patchUser = patchUser;
		this.patchMessage = patchMessage;
		this.addedSetUri = addedSetUri;
		this.removedSetUri = removedSetUri;	
	}

	public String getPatchNumber() {
		return patchNumber;
	}

	public void setPatchNumber(String patchNumber) {
		this.patchNumber = patchNumber;
	}

	public String getPatchUser() {
		return patchUser;
	}

	public void setPatchUser(String patchUser) {
		this.patchUser = patchUser;
	}

	public String getPatchMessage() {
		return patchMessage;
	}

	public void setPatchMessage(String patchMessage) {
		this.patchMessage = patchMessage;
	}

	public String getAddedSetUri() {
		return addedSetUri;
	}

	public void setAddedSetUri(String addedSetUri) {
		this.addedSetUri = addedSetUri;
	}

	public String getRemovedSetUri() {
		return removedSetUri;
	}

	public void setRemovedSetUri(String removedSetUri) {
		this.removedSetUri = removedSetUri;
	}
	
	
}
