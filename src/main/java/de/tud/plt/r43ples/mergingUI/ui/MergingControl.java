package de.tud.plt.r43ples.mergingUI.ui;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.Interface;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.mergingUI.TripleObjectTypeEnum;
import de.tud.plt.r43ples.mergingUI.management.ProcessManagement;
import de.tud.plt.r43ples.mergingUI.model.structure.*;
import de.tud.plt.r43ples.mergingUI.model.structure.*;
import org.apache.log4j.Logger;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MergingControl {
	private static Logger logger = Logger.getLogger(MergingControl.class);
	
	private DifferenceModel differenceModel = new DifferenceModel();
	
	private HighLevelChangeModel highLevelChangeModel = new HighLevelChangeModel();
	private HighLevelChangeTableModel highLevelChangeTableModel = new HighLevelChangeTableModel();
	
	/** Merge Query Model. **/
	private MergeCommitModel commitModel;
	private List<Individual> individualList;

	private String commonRevision;
	
		
	
	/**show triple merging view*/
	public String getViewHtmlOutput() {	
		Map<String, Object> scope = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
	    MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/merge_process.mustache");
	    scope.put("merging_active", true);
	    scope.put("commitModel", commitModel);	
		scope.put("tripleDifferenceModel", differenceModel);
		scope.put("individualList", individualList);
		scope.put("highLevelRowList", highLevelChangeTableModel.getTripleRowList());
		
		scope.put("commonRevision", commonRevision);
		
		mustache.execute(sw, scope);		
		return sw.toString();		
	}
	
	

//	/**
//	 *
//	 * @param response
//	 * @param graphName
//	 * @param branchNameA
//	 * @param branchNameB
//	 * @param format
//	 * @throws InternalErrorException
//	 */
//	public void getMergeProcess(MergeCommitModel commitModel, MergeResult mresult) throws InternalErrorException {
//		logger.info("Merge query produced conflicts.");
//		this.commitModel = commitModel;
//		this.commonRevision = mresult.commonRevision;
//		String conflictModel = mresult.conflictModel;
//
//
//        Model jenaModel = JenaModelManagement.readTurtleStringToJenaModel(conflictModel);
//
//        // create model for triple view
//		differenceModel = ProcessManagement.readDifferenceModel(jenaModel);
//
//		// Create the individual models of both branches
//		individualList = createIndividualsList(commitModel, jenaModel);
//
//		// create high level view instances
//		//highLevelChangeModel = ProcessManagement.createHighLevelChangeRenamingModel(differenceModel);
//		highLevelChangeTableModel = ProcessManagement.createHighLevelChangeTableModel(highLevelChangeModel);
//	}
	
		
	
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
	public List<Individual> createIndividualsList(MergeCommitModel commitModel, Model jenaModel) throws InternalErrorException {
		List<Individual> individualList = new ArrayList<Individual>();	

		
		// Get all subjects from difference model
		String querySubject = Config.prefixes +
				  "SELECT DISTINCT ?subject "
				+ "WHERE { "
				+ " ?triple rdf:subject ?subject."
				+ "}";
		QueryExecution qeSubject = QueryExecutionFactory.create(querySubject, jenaModel);
		ResultSet resultSetSubject = qeSubject.execSelect();
		while(resultSetSubject.hasNext()) {
	    	QuerySolution qsSubject = resultSetSubject.next();
	    	String individualUri = qsSubject.getResource("?subject").toString();
	    	Individual individual = new Individual();
			individual.resourceUri = individualUri;
	    	individual.triplesBranchA = addTriplesOfIndividual(individualUri, commitModel.getBranch1());
	    	individual.triplesBranchB = addTriplesOfIndividual(individualUri, commitModel.getBranch2());
	    	individualList.add(individual);
		}	
		return individualList;
	}
	
	private HashMap<Triple, Boolean> addTriplesOfIndividual(String individualUri, String branchName) throws InternalErrorException{
		HashMap<Triple, Boolean> triples = new HashMap<Triple, Boolean>();
		String query = Config.prefixes + String.format(
				  "SELECT ?predicate ?object %n"
				+ "FROM <%s> REVISION \"%s\" %n"
				+ "WHERE { %n"
				+ "	<%s> ?predicate ?object . %n"
				+ "}"
				+ "ORDER BY ?predicate ?object", commitModel.getGraphName(), branchName, individualUri);
		
		R43plesRequest request = new R43plesRequest(query, "text/xml");
		
		String resultBranch1 = Interface.sparqlSelectConstructAsk(request, false);
		ResultSet resultSetBranch1 = ResultSetFactory.fromXML(resultBranch1);
		while(resultSetBranch1.hasNext()) {
	    	QuerySolution qsBranch1 = resultSetBranch1.next();
	    	String predicate = qsBranch1.getResource("?predicate").toString();
	    	String object;
			TripleObjectTypeEnum objectType = null;
			if (qsBranch1.get("?object").isLiteral()) {
				object = qsBranch1.getLiteral("?object").toString();
				objectType = TripleObjectTypeEnum.LITERAL;
			} else {
				object = qsBranch1.getResource("?object").toString();
				objectType = TripleObjectTypeEnum.RESOURCE;
			}
	    	// Create the triple
			Triple triple = new Triple(individualUri, predicate, object, objectType);
			// Check if there is a corresponding difference
			boolean isConflict = ProcessManagement.isConflicting(triple, differenceModel);
	    	
	    	triples.put(triple, isConflict);
		}
		return triples;
	}
}
