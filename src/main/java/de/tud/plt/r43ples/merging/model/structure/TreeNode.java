package de.tud.plt.r43ples.merging.model.structure;

import java.util.List;

/**Tree Node fuer jede Triple in difference Model
 *  The data will in Html display
 * 
 * @author Xinyu Yang
 * 
 * */

public class TreeNode {
	public String differenceGroup;
	public List<String> tripleList;
	public boolean status;
	
	public TreeNode(String differenceGroup, List<String> tripleList, boolean status){
		this.differenceGroup = differenceGroup;
		this.tripleList = tripleList;
		this.status = status;
	}

	public String getDifferenceGroup() {
		return differenceGroup;
	}

	public void setDifferenceGroup(String differenceGroup) {
		this.differenceGroup = differenceGroup;
	}

	public List<String> getTripleList() {
		return tripleList;
	}

	public void setTripleList(List<String> tripleList) {
		this.tripleList = tripleList;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
		
}
