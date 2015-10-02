package de.tud.plt.r43ples.merging.control;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.ResolutionStateEnum;
import de.tud.plt.r43ples.merging.SDDTripleStateEnum;
import de.tud.plt.r43ples.merging.management.ProcessManagement;
import de.tud.plt.r43ples.merging.management.StrategyManagement;
import de.tud.plt.r43ples.merging.model.structure.CommitModel;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.Patch;
import de.tud.plt.r43ples.merging.model.structure.PatchGroup;
import de.tud.plt.r43ples.merging.model.structure.Triple;

public class RebaseControl {
	private static Logger logger = Logger.getLogger(FastForwardControl.class);
	private CommitModel commitModel;
	private PatchGroup patchGroup = new PatchGroup(null, null);
	private DifferenceModel differenceModel = new DifferenceModel();
	
	private String differenceGraphModel = null;
	

	private MergingControl mergingControl = null;
	
	
	/**create the RebaseControl*/
	public RebaseControl(MergingControl mergingControl){
		this.mergingControl = mergingControl;
	}
	
	/**save the commit information and later output it */
	public void createCommitModel(String graphName, String sddName, String user, String message, String branch1, String branch2, String strategy, String type){
		commitModel = new CommitModel(graphName, sddName, user, message, branch1, branch2, strategy, type);
	}
	
	/**save the commit information and later output it */
	public CommitModel getCommitModel(){
		return commitModel;
	}
	
	/**for each revision in branchA , create a patch */
	public void createPatchGroupOfBranch(String basisRevisionUri, LinkedList<String> revisionList) {
		
		LinkedHashMap<String, Patch> patchMap = new LinkedHashMap<String, Patch>();
		
		Iterator<String> rIter  = revisionList.iterator();
		
		while(rIter.hasNext()) {
			String revisionUri = rIter.next();
			String commitUri = StrategyManagement.getCommitUri(revisionUri);
			
			String deltaAdded = StrategyManagement.getDeltaAddedUri(revisionUri);
			String deltaRemoved = StrategyManagement.getDeltaRemovedUri(revisionUri);
			
			String patchNumber = StrategyManagement.getRevisionNumber(revisionUri);
			String patchUser = StrategyManagement.getPatchUserUri(commitUri);
			String patchMessage = StrategyManagement.getPatchMessage(commitUri);
			
			patchMap.put(patchNumber, new Patch(patchNumber, patchUser, patchMessage, deltaAdded, deltaRemoved));			
		}
		
		String basisRevisionNumber = StrategyManagement.getRevisionNumber(basisRevisionUri);
		
		patchGroup.setBasisRevisionNumber(basisRevisionNumber);
		patchGroup.setPatchMap(patchMap);
		
		logger.info("patchGroup initial successful!" + patchGroup.getPatchMap().size());
	}
	
	/**
	 * force rebase begin, for each patch in patch group will a new revision created 
	 * @throws InternalErrorException 
	 * */
	public String forceRebaseProcess( String graphName ) throws InternalErrorException{
		
		logger.info("patchGroup 1:" + patchGroup.getBasisRevisionNumber());
		logger.info("patchGroup 2:" + patchGroup.getPatchMap().size());

		
		LinkedHashMap<String, Patch> patchMap = patchGroup.getPatchMap();
		String basisRevisionNumber = patchGroup.getBasisRevisionNumber();
				
		Iterator<Entry<String, Patch>> pIter = patchMap.entrySet().iterator();
		
		while(pIter.hasNext()) {
			Entry<String, Patch> pEntry = pIter.next();
			Patch patch = pEntry.getValue();
		
			String newRevisionNumber = RevisionManagement.createNewRevisionWithPatch(graphName, patch.getAddedSetUri(), patch.getRemovedSetUri(),
					patch.getPatchUser(), patch.getPatchMessage(), basisRevisionNumber);
			
			basisRevisionNumber = newRevisionNumber;
		}
		return basisRevisionNumber;	
	}
	
	/**manual rebase beginn , for each patch in patch graup will a new revision created 
	 * @throws InternalErrorException 
	 * */
	public void manualRebaseProcess() throws InternalErrorException {
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		
		logger.info("difference Graph Model by manualRebase: "+ differenceGraphModel);
		responseBuilder.entity(this.differenceGraphModel);
		
		Response response = responseBuilder.build();	
		
		
		mergingControl.openRebaseModel();
		logger.info("open rebase model: "+ mergingControl.getClass().toString());
		
		logger.info("is Rebase: "+ mergingControl.getIsRebase());
		
		mergingControl.getMergeProcess(response, commitModel.getGraphName(), commitModel.getBranch1(), commitModel.getBranch2(),"text/html");
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
		
			
			Iterator<String> iteDifferences = differenceGroup.getDifferences().keySet().iterator();
			while (iteDifferences.hasNext()) {
				String differenceKey = iteDifferences.next();
				Difference difference = differenceGroup.getDifferences().get(differenceKey);
								
				// Get the triple
				String triple = ProcessManagement.tripleToString( difference.getTriple());					
				
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
			
			Iterator<String> iteDifferences = differenceGroup.getDifferences().keySet().iterator();
			while (iteDifferences.hasNext()) {
				String differenceKey = iteDifferences.next();
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
				String triple = ProcessManagement.tripleToString( difference.getTriple());					
				
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
	
	
	
	
	
	
	
	/**common manual rebase end , get the updated difference model and check the create new Revision
	 * @throws InternalErrorException 
	 * 
	 * */
	public void createCommonManualRebaseProcess() throws InternalErrorException {
		//check action command , auto or common
		
		ArrayList<String> addedAndRemovedTriples = getManualAddedTriplesAndRemovedTriples();
		String addedAsNTriples = addedAndRemovedTriples.get(0);
		String removedAsNTriples = addedAndRemovedTriples.get(1);
		
		String basisRevisionNumber = forceRebaseProcess(commitModel.getGraphName());
		RevisionManagement.createNewRevision(commitModel.getGraphName(), addedAsNTriples, removedAsNTriples,
				commitModel.getUser(), commitModel.getMessage(), basisRevisionNumber);
		
 			
	}
	

	
	/** rebase process beginn, read the difference model and check the freundlichkeit of Rebase
	 * @param differGraphModel	string of difference model for this rebase
	 * @param graphName	name of the graph to be rebased
	 * @param branchNameA name of branch A
	 * @param branchNameB name of branch B
	 * @param format rdf serialisation format of differGraphModel
	 * @throws InternalErrorException */
	public boolean checkRebaseFreundlichkeit( String differGraphModel, String graphName, String branchNameA, String branchNameB, String format) throws InternalErrorException{
				
		this.differenceGraphModel = differGraphModel;
		boolean isRebaseFreundlich = true;
	
		
		logger.info("difference graph Model by check unfreundlich:" + differenceGraphModel);
		ProcessManagement.readDifferenceModel(differenceGraphModel, differenceModel, format);
		
		//get difference group
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
//		isRebaseFreundlich = true;
		while(iterDM.hasNext()){
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differenceGroup = (DifferenceGroup) entryDG.getValue();
			
			
			SDDTripleStateEnum tripleStateInBranchA = differenceGroup.getTripleStateA();
			SDDTripleStateEnum tripleStateInBranchB = differenceGroup.getTripleStateB();
			SDDTripleStateEnum automaticResolutionState = differenceGroup.getAutomaticResolutionState();
			
			// folgend 4 condition for the rebase freundlichkeit
			if(tripleStateInBranchA == SDDTripleStateEnum.ADDED && automaticResolutionState == SDDTripleStateEnum.DELETED) {
				isRebaseFreundlich = false;
			}
			
			if(tripleStateInBranchA == SDDTripleStateEnum.DELETED && automaticResolutionState == SDDTripleStateEnum.ADDED) {
				isRebaseFreundlich = false;
			}
			
			if(tripleStateInBranchA == SDDTripleStateEnum.NOTINCLUDED &&tripleStateInBranchB == SDDTripleStateEnum.ADDED 
					&& automaticResolutionState == SDDTripleStateEnum.DELETED){
				isRebaseFreundlich = false;
			}
			
			if(tripleStateInBranchA == SDDTripleStateEnum.ORIGINAL &&tripleStateInBranchB == SDDTripleStateEnum.DELETED 
					&& automaticResolutionState == SDDTripleStateEnum.ADDED){
				isRebaseFreundlich = false;
			}		
			
		}
		logger.info("check rebase freundlich: " + isRebaseFreundlich);
		return isRebaseFreundlich;
				
	}
	
	/**get die triples, die rebase unfreundlich ausloesen
	 * @throws IOException */
	public ArrayList<Triple> getRebaseUnfreundlichbehaftetTriples(String differGraphModel, String format) throws IOException{
		ArrayList<Triple> tripleList = new ArrayList<Triple>();
		
		ProcessManagement.readDifferenceModel(differGraphModel, differenceModel, format);
		
		//get difference group
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		
		while(iterDM.hasNext()){
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differenceGroup = (DifferenceGroup) entryDG.getValue();
			
			
			SDDTripleStateEnum tripleStateInBranchA = differenceGroup.getTripleStateA();
			SDDTripleStateEnum tripleStateInBranchB = differenceGroup.getTripleStateB();
			SDDTripleStateEnum automaticResolutionState = differenceGroup.getAutomaticResolutionState();
			
			// folgend 4 condition for the rebase freundlichkeit
			if((tripleStateInBranchA == SDDTripleStateEnum.ADDED && automaticResolutionState == SDDTripleStateEnum.DELETED) || 
					(tripleStateInBranchA == SDDTripleStateEnum.DELETED && automaticResolutionState == SDDTripleStateEnum.ADDED)||
					(tripleStateInBranchA == SDDTripleStateEnum.NOTINCLUDED &&tripleStateInBranchB == SDDTripleStateEnum.ADDED 
					&& automaticResolutionState == SDDTripleStateEnum.DELETED) ||
					(tripleStateInBranchA == SDDTripleStateEnum.ORIGINAL &&tripleStateInBranchB == SDDTripleStateEnum.DELETED 
					&& automaticResolutionState == SDDTripleStateEnum.ADDED)) {
				
				Iterator<Entry<String, Difference>> differIter = differenceGroup.getDifferences().entrySet().iterator();
				while(differIter.hasNext()){
					Entry<String, Difference> differ = differIter.next();
					Triple triple = differ.getValue().getTriple();
					
					tripleList.add(triple);
					
				}		
			}						
		}
		
		return tripleList;
		
	}
	
	/**get the right triples in with set
	 * @throws IOException */
	
	public String filterUnfreundlichTriples(String differGraphModel, String triples, String format) throws IOException {
		ArrayList<Triple> unfreundlichTripleList = getRebaseUnfreundlichbehaftetTriples(differGraphModel, format);
		
		// MERGE WITH query - conflicting triple
		Model model = JenaModelManagement.readNTripleStringToJenaModel(triples);
		
		//create new Triples, die rebase unfreundlich sind.
		String newTriples = "";
		
		Iterator<Triple> tripleIter = unfreundlichTripleList.iterator();
		while (tripleIter.hasNext()) {
			Triple triple = tripleIter.next();
			String subject = ProcessManagement.getSubject(triple);
			String object = ProcessManagement.getObject(triple);
			String predicate = ProcessManagement.getPredicate(triple);
					
			
			logger.info("rebase unfreundlich spo: " + subject + object + predicate);
			// Create ASK query which will check if the model contains the specified triple
			String queryAsk = String.format(
					  "ASK { %n"
					+ " %s %s %s %n"
					+ "}", subject, predicate, object);
			Query query = QueryFactory.create(queryAsk);
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			boolean resultAsk = qe.execAsk();
			qe.close();
			model.close();
			if (resultAsk) {
				// Model contains the specified triple
				// Triple should be added
				newTriples += subject + " " + predicate + " " + object + " . \n";
			} 		
		}
		
		return newTriples;
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
		differenceModel.clear();
		differenceModel = updatedDifferenceModel;
	}

}
