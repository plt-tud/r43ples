package de.tud.plt.r43ples.merging.management;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.ResolutionState;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.ReportResult;

public class ReportManagement {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(ProcessManagement.class);
	
	public static ReportResult initialReportResult(DifferenceModel differenceModel) {
		ReportResult reportResult = new ReportResult();
		
		
		// Count for Conflict;
		int count = 0;
		
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()) {
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			
			Iterator<Entry<String, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				Entry<String, Difference> entryDF = iterDIF.next();
								
				Difference difference = entryDF.getValue();
				
				if(difference.getResolutionState().equals(ResolutionState.CONFLICT)){
					count ++;
				}
				
			}
		}
		//initial number of Triple with Conflict	
		reportResult.setConflictsNotApproved(count);
		logger.info("initial Result Report: "+ reportResult.getConflictsNotApproved()+"--"+reportResult.getDifferencesResolutionChanged());
		
		return reportResult;
	}
}
