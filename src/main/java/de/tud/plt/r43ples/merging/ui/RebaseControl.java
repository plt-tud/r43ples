package de.tud.plt.r43ples.merging.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.merging.ResolutionStateEnum;
import de.tud.plt.r43ples.merging.SDDTripleStateEnum;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.MergeCommitModel;
import de.tud.plt.r43ples.merging.model.structure.Triple;

public class RebaseControl {
	private static Logger logger = Logger.getLogger(RebaseControl.class);
	private MergeCommitModel commitModel;
	private DifferenceModel differenceModel = new DifferenceModel();
	
	private String differenceGraphModel = null;
	
	
	/**save the commit information and later output it */
	public void createCommitModel(String graphName, String sddName, String user, String message, String branch1, String branch2, String strategy, String type){
		commitModel = new MergeCommitModel(graphName, sddName, user, message, branch1, branch2, strategy, type);
	}
	
	/**save the commit information and later output it */
	public MergeCommitModel getCommitModel(){
		return commitModel;
	}
	
	
	
	/**manual rebase begin, for each patch in patch group will a new revision created 
	 * @throws InternalErrorException 
	 * */
	public void manualRebaseProcess() throws InternalErrorException {
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		
		logger.info("difference Graph Model by manualRebase: "+ differenceGraphModel);
		responseBuilder.entity(this.differenceGraphModel);
		
		//Response response = responseBuilder.build();	
				
		//mergingControl.getMergeProcess(response, commitModel.getGraphName(), commitModel.getBranch1(), commitModel.getBranch2());
	}
	
	/**read the difference model and set the automatic resolution state for the triple and create */
	public ArrayList<String> getAutomaticAddedTriplesAndRemovedTriples() {
		ArrayList<String> list = new ArrayList<String>();
		
		String triplesToInsert = "";
		String triplesToDelete = "";
		
		// Iterate over all difference groups
		Iterator<String> iteDifferenceGroups = differenceModel.getDifferenceGroups().keySet().iterator();
		while (iteDifferenceGroups.hasNext()) {
			String differenceGroupKey = iteDifferenceGroups.next();
			DifferenceGroup differenceGroup = differenceModel.getDifferenceGroups().get(differenceGroupKey);
			
			// Get the triple state to use
			SDDTripleStateEnum tripleState;
			
			// Use the automatic resolution state
			tripleState = differenceGroup.getAutomaticResolutionState();
		
			
			Iterator<Triple> iteDifferences = differenceGroup.getDifferences().keySet().iterator();
			while (iteDifferences.hasNext()) {
				Triple differenceKey = iteDifferences.next();
				Difference difference = differenceGroup.getDifferences().get(differenceKey);
								
				// Get the triple
				Triple triple = difference.getTriple();					
				
				// Add the triple to the corresponding string
				if (tripleState.equals(SDDTripleStateEnum.ADDED) || tripleState.equals(SDDTripleStateEnum.ORIGINAL)) {
					triplesToInsert += triple + "%n";				
				} else if (tripleState.equals(SDDTripleStateEnum.DELETED) || tripleState.equals(SDDTripleStateEnum.NOTINCLUDED)) {
					triplesToDelete += triple + "%n";
				} else {
					// Error occurred - state was not changed
					logger.error("Triple state was used which has no internal representation.");
				}
			}
		}
		
		// Add the string to the result list
		list.add(String.format(triplesToInsert));
		list.add(String.format(triplesToDelete));
		
		logger.info("automatic insert list: "+ list.get(0).toString());
		logger.info("automatic delete list: "+ list.get(1).toString());

		
		return list;
	}
	
	
	/**read the difference model and set the automatic resolution state for the triple and create */
	public ArrayList<String> getManualAddedTriplesAndRemovedTriples() {
		// The result list
		ArrayList<String> list = new ArrayList<String>();
		
		String triplesToInsert = "";
		String triplesToDelete = "";
		
		// Iterate over all difference groups
		Iterator<String> iteDifferenceGroups = differenceModel.getDifferenceGroups().keySet().iterator();
		while (iteDifferenceGroups.hasNext()) {
			String differenceGroupKey = iteDifferenceGroups.next();
			DifferenceGroup differenceGroup = differenceModel.getDifferenceGroups().get(differenceGroupKey);
			
			Iterator<Triple> iteDifferences = differenceGroup.getDifferences().keySet().iterator();
			while (iteDifferences.hasNext()) {
				Triple differenceKey = iteDifferences.next();
				Difference difference = differenceGroup.getDifferences().get(differenceKey);
				
				// Get the triple state to use
				SDDTripleStateEnum tripleState;
				if (difference.getResolutionState().equals(ResolutionStateEnum.RESOLVED)) {
					// Use the approved triple state
					tripleState = difference.getTripleResolutionState();					
				} else {
					// Use the automatic resolution state
					tripleState = differenceGroup.getAutomaticResolutionState();
				}
				
				// Get the triple
				Triple triple = difference.getTriple();					
				
				// Add the triple to the corresponding string
				if (tripleState.equals(SDDTripleStateEnum.ADDED) || tripleState.equals(SDDTripleStateEnum.ORIGINAL)) {
					triplesToInsert += triple + "%n";				
				} else if (tripleState.equals(SDDTripleStateEnum.DELETED) || tripleState.equals(SDDTripleStateEnum.NOTINCLUDED)) {
					triplesToDelete += triple + "%n";
				} else {
					// Error occurred - state was not changed
					logger.error("Triple state was used which has no internal representation.");
				}
			}
		}
		
		// Add the string to the result list
		list.add(String.format(triplesToInsert));
		list.add(String.format(triplesToDelete));
		
		return list;
	}
	
	
	
	
	/**
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * ##                                                                                                                                                                      ##
	 * ## create Rebase Dialog : get rebase dialog and the information                                                                                                                            ##
	 * ##                                                                                                                                                                      ##
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * @throws IOException 
	 * @throws TemplateException 
	 * @throws ConfigurationException 
	 */
	
	public String showRebaseDialogView() {
		Map<String, Object> scope = new HashMap<String, Object>();
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/merge/rebaseDialog.mustache");
	    StringWriter sw = new StringWriter(); 
		
		scope.put("graphName", commitModel.getGraphName());
		scope.put("clientName", commitModel.getUser());
		
		mustache.execute(sw, scope);		
		return sw.toString();	
		
		
	}
	
	/** get the HTML result graph view by rebase of the named graph
	 * @param graphName
	 * */
	public String getRebaseReportView(String graphName) {
		Map<String, Object> scope = new HashMap<String, Object>();
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/merge/mergingResultView.mustache");
		StringWriter sw = new StringWriter();		
		scope.put("graphName", graphName);
		scope.put("commit", commitModel);
		scope.put("merging_active", true);
		
		mustache.execute(sw, scope);	
		return sw.toString();	
	}
	
	
	
	/**update difference model*/
	public void updateRebaseDifferenceModel(DifferenceModel updatedDifferenceModel){		
		differenceModel = updatedDifferenceModel;
	}

}
