package de.tud.plt.r43ples.merging.management;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.merging.ResolutionStateEnum;
import de.tud.plt.r43ples.merging.SDDTripleStateEnum;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.ReportResult;
import de.tud.plt.r43ples.merging.model.structure.ReportTableRow;
import de.tud.plt.r43ples.merging.model.structure.Triple;

public class ReportManagement {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(ReportManagement.class);
	
	
	/**initial report result and create report list
	 * @param difference model
	 * */
	public static ReportResult initialReportResult(DifferenceModel differenceModel) {
		ReportResult reportResult = new ReportResult();
		
		// Count for Conflict;
		int count = 0;
		
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()) {
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			
			Iterator<Entry<Triple, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				Entry<Triple, Difference> entryDF = iterDIF.next();
								
				Difference difference = entryDF.getValue();
				
				if(difference.getResolutionState().equals(ResolutionStateEnum.CONFLICT)){
					count ++;
				}
				
			}
		}
		//initial number of Triple with Conflict	
		reportResult.setConflictsNotApproved(count);
		logger.info("initial Result Report: "+ reportResult.getConflictsNotApproved()+"--"+reportResult.getDifferencesResolutionChanged());
		
		return reportResult;
	}
	
	
	/**read neue difference model and create report table row list
	 * @param difference model
	 * */
	public static List<ReportTableRow> createReportTableRowList(DifferenceModel differenceModel) {
		
		//new a List for ReportTableRow
		List<ReportTableRow> reportTableRowList = new ArrayList<ReportTableRow>();
 		
		//get difference group
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()){
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			boolean isconflicting = differ.isConflicting();
			SDDTripleStateEnum stateA = differ.getTripleStateA();
			SDDTripleStateEnum stateB = differ.getTripleStateB();
			
			SDDTripleStateEnum automaticResolutionState = differ.getAutomaticResolutionState();
			
			String conflicting = (isconflicting ) ? "1" : "0";
			//get difference 
			Iterator<Entry<Triple, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				
				Entry<Triple, Difference> entryDF = iterDIF.next();
				
				Difference difference = entryDF.getValue();
				//get triple
				Triple triple = difference.getTriple();
				
				ResolutionStateEnum approvedState = difference.getResolutionState();
				
				SDDTripleStateEnum tripleResolutionState = difference.getTripleResolutionState();
				
				String subject = triple.getSubject();
				String predicate = triple.getPredicate();
				String object = triple.getObject();
				//get revision number
				String revisionA = difference.getReferencedRevisionLabelA();
				String revisionB = difference.getReferencedRevisionLabelB();
				
				//read each reportTableRowList
				
				reportTableRowList.add(new ReportTableRow(subject, predicate, object, stateA.toString(), stateB.toString(), revisionA, revisionB, 
						conflicting, automaticResolutionState.toString(), tripleResolutionState.toString(), approvedState.toString()));											
			}				
		}
		return reportTableRowList;
	}
}
