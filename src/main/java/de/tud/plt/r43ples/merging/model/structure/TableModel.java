package de.tud.plt.r43ples.merging.model.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**stores all information in TableModel 
 * 
 * @author Xinyu Yang
 * 
 * */
public class TableModel {
	/**currentTripleId of Triple*/
	private Integer currentTripleId = 1;
	private List<TableRow> tripleRowList;
	private Map<String, Triple> manuellTriple;
	
	public TableModel(){
		this.setTripleRowList(new ArrayList<TableRow>());
		this.setManuellTriple(new HashMap<String, Triple>());	
	}
	
	public void readTableRow(TableRow tableRow){
		tableRow.setTripleId(currentTripleId.toString());
		this.tripleRowList.add(tableRow);
		this.manuellTriple.put(tableRow.getTripleId(), tableRow.getTriple());
		currentTripleId ++;
	}
	
	
	public List<TableRow> getTripleRowList() {
		return tripleRowList;
	}
	public void setTripleRowList(List<TableRow> tripleRowList) {
		this.tripleRowList = tripleRowList;
	}
	public Map<String, Triple> getManuellTriple() {
		return manuellTriple;
	}
	public void setManuellTriple(HashMap<String, Triple> manuellTriple) {
		this.manuellTriple = manuellTriple;
	}
	
	public void clear(){
		this.currentTripleId = 1;
		this.tripleRowList.clear();
		this.manuellTriple.clear();
	}
		
}
