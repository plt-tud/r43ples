package de.tud.plt.r43ples.merging.model.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**store all renaming information in HighLevelChangeTableModel
 * read HighLevelChangeModel and create HighLevelChangeTableModel*/

public class HighLevelChangeTableModel {
	/**currentTripleId of Triple*/
	private Integer currentTripleId = 1;
	private List<HighLevelChangeTableRow> tripleRowList;
	private Map<String, HighLevelChangeTableRow> manuellTriple;
	
	public HighLevelChangeTableModel(){
		this.setTripleRowList(new ArrayList<HighLevelChangeTableRow>());
		this.setManuellTriple(new HashMap<String, HighLevelChangeTableRow>());	
	}
	
	public void readTableRow(HighLevelChangeTableRow tableRow){
		tableRow.setTripleId(currentTripleId.toString());
		this.tripleRowList.add(tableRow);
		this.manuellTriple.put(tableRow.getTripleId(), tableRow);
		currentTripleId ++;
	}
	
	
	public List<HighLevelChangeTableRow> getTripleRowList() {
		return tripleRowList;
	}
	public void setTripleRowList(List<HighLevelChangeTableRow> tripleRowList) {
		this.tripleRowList = tripleRowList;
	}
	public Map<String, HighLevelChangeTableRow> getManuellTriple() {
		return manuellTriple;
	}
	public void setManuellTriple(HashMap<String, HighLevelChangeTableRow> manuellTriple) {
		this.manuellTriple = manuellTriple;
	}
	
	public void clear(){
		this.currentTripleId = 1;
		this.tripleRowList.clear();
		this.manuellTriple.clear();
	}
	
}
