package de.tud.plt.r43ples.merging.ui;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.rdf.model.Model;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.merging.management.ProcessManagement;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeTableModel;
import de.tud.plt.r43ples.merging.model.structure.IndividualModel;
import de.tud.plt.r43ples.merging.model.structure.IndividualStructure;
import de.tud.plt.r43ples.merging.model.structure.MergeCommitModel;
import de.tud.plt.r43ples.merging.model.structure.TableEntrySemanticEnrichmentAllIndividuals;
import de.tud.plt.r43ples.merging.model.structure.TableModel;
import de.tud.plt.r43ples.merging.model.structure.TreeNode;


public class MergingControl {
	private static Logger logger = Logger.getLogger(MergingControl.class);
	private DifferenceModel differenceModel = new DifferenceModel();
	private List<TreeNode> treeList = new ArrayList<TreeNode>();
	private TableModel tableModel = new TableModel();
	
	
	private HighLevelChangeModel highLevelChangeModel = new HighLevelChangeModel();
	private HighLevelChangeTableModel highLevelChangeTableModel = new HighLevelChangeTableModel();
	
	/** The individual model of branch A. **/
	private IndividualModel individualModelBranchA;
	/** The individual model of branch B. **/
	private IndividualModel individualModelBranchB;	
	/** The properties array list. **/
	private ArrayList<String> propertyList;
	/** Merge Query Model. **/
	private MergeCommitModel commitModel;
	private String conflictModel;
	private List<TableEntrySemanticEnrichmentAllIndividuals> individualTableList;
	
		
	
	/**show triple merging view*/
	public String getViewHtmlOutput() {	
		
		/** 
		 * conList for conflict triple
		 * diffList for deference triple
		 * */
		List<TreeNode> conList = new ArrayList<TreeNode>();
		List<TreeNode> diffList = new ArrayList<TreeNode>();
		Iterator<TreeNode> itG = treeList.iterator();
	 	
		
		
		/**create conList and diffList*/
	 	boolean containConflict = false;
	 	while(itG.hasNext()){
	 		TreeNode node = itG.next();
	 		
	 		if(node.status == true){
	 			containConflict = true;
		 		conList.add(node);
	 		}else{
	 			diffList.add(node);
	 		}	 		
	 	}
	 	
	 	

	 	Map<String, Object> scope = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
	    MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/merge_process.mustache");
	 	scope.put("commitModel", commitModel);	
	 	scope.put("merging_active", true);
		scope.put("conList",conList);
		scope.put("diffList",diffList);
		scope.put("conStatus", containConflict);
		scope.put("propertyList", propertyList);
		scope.put("treeList", treeList);
		scope.put("tableModel", tableModel);
		scope.put("individualTableList", individualTableList);
		scope.put("highLevelRowList", highLevelChangeTableModel.getTripleRowList());
		scope.put("conflictModel", conflictModel);
		
		mustache.execute(sw, scope);		
		return sw.toString();		
	}
	
	

	/**
	 * 
	 * @param response
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * @param format
	 * @throws InternalErrorException
	 */
	public void getMergeProcess(MergeCommitModel commitModel, String conflictModel) throws InternalErrorException {
		logger.info("Merge query produced conflicts.");
		
		String graphName = commitModel.getGraphName();
		String branchNameA = commitModel.getBranch1();
		String branchNameB = commitModel.getBranch2();
		
		this.conflictModel = conflictModel;
		logger.info("differenceModelToRead: "+ conflictModel);
		Model model = JenaModelManagement.readStringToJenaModel(conflictModel, "TURTLE");
		
		// create model for triple view
		differenceModel = ProcessManagement.readDifferenceModel(model);		
		treeList = ProcessManagement.createDifferenceTree(differenceModel);		
		tableModel = ProcessManagement.createTableModel(differenceModel);

		// Create the individual models of both branches
		individualModelBranchA = ProcessManagement.createIndividualModelOfRevision(graphName, branchNameA, differenceModel);
		individualModelBranchB = ProcessManagement.createIndividualModelOfRevision(graphName, branchNameB, differenceModel);
		individualTableList = createTableModelSemanticEnrichmentAllIndividualsList(individualModelBranchA, individualModelBranchB);
		
		// Create the property list of revisions
		propertyList = ProcessManagement.getPropertiesOfDifferenceModel(model);
		
		// create high level view instances
		highLevelChangeModel = ProcessManagement.createHighLevelChangeRenamingModel(differenceModel);
		highLevelChangeTableModel = ProcessManagement.createHighLevelChangeTableModel(highLevelChangeModel);
	}
	
		
	
	/**
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * ##                                                                                                                                                                      ##
	 * ## Semantic enrichment - individuals.                                                                                                                                   ##
	 * ##                                                                                                                                                                      ##
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 */
	
	
	/**
	 * Create the semantic enrichment List of all individuals.
	 * @throws InternalErrorException 
	 */
	public List<TableEntrySemanticEnrichmentAllIndividuals> createTableModelSemanticEnrichmentAllIndividualsList(
			IndividualModel individualModelBranchA, IndividualModel individualModelBranchB) throws InternalErrorException {
		List<TableEntrySemanticEnrichmentAllIndividuals> individualTableList = new ArrayList<TableEntrySemanticEnrichmentAllIndividuals>();
		
		if(individualModelBranchA == null || individualModelBranchB == null) {
			throw new InternalErrorException("Error");
		}

		// Get key sets
		ArrayList<String> keySetIndividualModelBranchA = new ArrayList<String>(individualModelBranchA.getIndividualStructures().keySet());
		ArrayList<String> keySetIndividualModelBranchB = new ArrayList<String>(individualModelBranchB.getIndividualStructures().keySet());
		
		// Iterate over all individual URIs of branch A
		Iterator<String> iteKeySetIndividualModelBranchA = keySetIndividualModelBranchA.iterator();
		while (iteKeySetIndividualModelBranchA.hasNext()) {
			String currentKeyBranchA = iteKeySetIndividualModelBranchA.next();
			
			// Add all individual URIs to table model which are in both branches
			if (keySetIndividualModelBranchB.contains(currentKeyBranchA)) {
				TableEntrySemanticEnrichmentAllIndividuals tableEntry = new TableEntrySemanticEnrichmentAllIndividuals(individualModelBranchA.getIndividualStructures().get(currentKeyBranchA), individualModelBranchB.getIndividualStructures().get(currentKeyBranchA), new Object[]{currentKeyBranchA, currentKeyBranchA});
				
				individualTableList.add(tableEntry);
				// Remove key from branch A key set copy
				keySetIndividualModelBranchA.remove(currentKeyBranchA);
				// Remove key from branch B key set copy
				keySetIndividualModelBranchB.remove(currentKeyBranchA);
			}
		}
		
		// Iterate over all individual URIs of branch A (will only contain the individuals which are not in B)
		Iterator<String> iteKeySetIndividualModelBranchAOnly = keySetIndividualModelBranchA.iterator();
		while (iteKeySetIndividualModelBranchAOnly.hasNext()) {
			String currentKeyBranchA = iteKeySetIndividualModelBranchAOnly.next();
			TableEntrySemanticEnrichmentAllIndividuals tableEntry = new TableEntrySemanticEnrichmentAllIndividuals(individualModelBranchA.getIndividualStructures().get(currentKeyBranchA), new IndividualStructure(null), new Object[]{currentKeyBranchA, ""});
			individualTableList.add(tableEntry);

		}
		
		// Iterate over all individual URIs of branch B (will only contain the individuals which are not in A)
		Iterator<String> iteKeySetIndividualModelBranchBOnly = keySetIndividualModelBranchB.iterator();
		while (iteKeySetIndividualModelBranchBOnly.hasNext()) {
			String currentKeyBranchB = iteKeySetIndividualModelBranchBOnly.next();
			TableEntrySemanticEnrichmentAllIndividuals tableEntry = new TableEntrySemanticEnrichmentAllIndividuals(new IndividualStructure(null), individualModelBranchB.getIndividualStructures().get(currentKeyBranchB), new Object[]{"", currentKeyBranchB});
			individualTableList.add(tableEntry);


		}
		
		return individualTableList;
	}
	
}
