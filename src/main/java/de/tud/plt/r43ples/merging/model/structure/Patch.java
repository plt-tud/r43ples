package de.tud.plt.r43ples.merging.model.structure;

import java.util.LinkedList;

/**patch model for the rebase process, store all the information about the Version
 * 
 * @author Xinyu Yang
 * 
 * */
public class Patch {
	String patchNumber;
	String patchUser;
	String patchMessage;
	LinkedList<String> addedTripleList;
	LinkedList<String> removedTripleList;
	
	public Patch(String patchNumber, String patchUser, String patchMessage,
			LinkedList<String> addedTripleList,
			LinkedList<String> removedTripleList) {
		
		this.patchNumber = patchNumber;
		this.patchUser = patchUser;
		this.patchMessage = patchMessage;
		this.addedTripleList = addedTripleList;
		this.removedTripleList = removedTripleList;
		
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

	public LinkedList<String> getAddedTripleList() {
		return addedTripleList;
	}

	public void setAddedTripleList(LinkedList<String> addedTripleList) {
		this.addedTripleList = addedTripleList;
	}

	public LinkedList<String> getRemovedTripleList() {
		return removedTripleList;
	}

	public void setRemovedTripleList(LinkedList<String> removedTripleList) {
		this.removedTripleList = removedTripleList;
	}	
	
}
