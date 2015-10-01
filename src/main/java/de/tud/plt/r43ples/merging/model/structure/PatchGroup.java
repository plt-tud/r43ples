package de.tud.plt.r43ples.merging.model.structure;

import java.util.LinkedHashMap;


/**store the patch of branchA in patch map and the last revision of the branchB
 * 
 * @author Xinyu Yang
 * 
 * */
public class PatchGroup {
	
	String basisRevisionNumber;
	
	LinkedHashMap<String, Patch> patchMap;
	
	public PatchGroup(String basisRevisionNumber,
			LinkedHashMap<String, Patch> patchMap) {
		this.basisRevisionNumber = basisRevisionNumber;
		this.patchMap = patchMap;
	}
	
	
	public String getBasisRevisionNumber() {
		return basisRevisionNumber;
	}
	
	public void setBasisRevisionNumber(String basisRevisionNumber) {
		this.basisRevisionNumber = basisRevisionNumber;
	}
	
	public LinkedHashMap<String, Patch> getPatchMap() {
		return patchMap;
	}
	
	public void setPatchMap(LinkedHashMap<String, Patch> patchMap) {
		this.patchMap = patchMap;
	}
	
}
