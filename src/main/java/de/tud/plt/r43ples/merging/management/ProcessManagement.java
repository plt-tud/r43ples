package de.tud.plt.r43ples.merging.management;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merge.SDDTripleStateEnum;
import de.tud.plt.r43ples.merging.ResolutionStateEnum;
import de.tud.plt.r43ples.merging.TripleObjectTypeEnum;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeRenaming;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeTableModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeTableRow;
import de.tud.plt.r43ples.merging.model.structure.Triple;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;




public class ProcessManagement {
	/** The logger. */
	private static Logger logger = Logger.getLogger(ProcessManagement.class);
	
	
	/**
	 * Read difference model to java representation.
	 * 
	 * @param differenceModelToRead the difference model to read in turtle serialisation
	 * @param differenceModel the difference model where the result should be stored
	 * @return 
	 */
	public static DifferenceModel readDifferenceModel(Model model) {
		DifferenceModel differenceModel =  new DifferenceModel();
		
		
		// Query all difference groups
		String queryDifferenceGroups = RevisionManagement.prefixes + 
				  "SELECT ?uri ?tripleStateA ?tripleStateB ?automaticResolutionState ?conflicting "
				+ "WHERE { "
				+ "	?uri a rpo:DifferenceGroup ; "
				+ "		sddo:hasTripleStateA ?tripleStateA ; "
				+ "		sddo:hasTripleStateB ?tripleStateB ; "
				+ "		sddo:automaticResolutionState ?automaticResolutionState ; "
				+ "		sddo:isConflicting ?conflicting . "
				+ "}";
		QueryExecution qeDifferenceGroups = QueryExecutionFactory.create(queryDifferenceGroups, model);
		ResultSet resultSetDifferenceGroups = qeDifferenceGroups.execSelect();
	    while(resultSetDifferenceGroups.hasNext()) {
	    	QuerySolution qsDifferenceGroups = resultSetDifferenceGroups.next();
	    	String uri = qsDifferenceGroups.getResource("?uri").toString();
	    	SDDTripleStateEnum tripleStateA = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?tripleStateA").toString());
	    	SDDTripleStateEnum tripleStateB = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?tripleStateB").toString());
	    	SDDTripleStateEnum automaticResolutionState = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?automaticResolutionState").toString());
	    	boolean conflicting = qsDifferenceGroups.getLiteral("?conflicting").toString().equals("true^^http://www.w3.org/2001/XMLSchema#boolean");   
	    	ResolutionStateEnum resolutionState = ResolutionStateEnum.DIFFERENCE;
	    	if (conflicting) {
	    		resolutionState = ResolutionStateEnum.CONFLICT;
	    	}
	    	
	    	DifferenceGroup differenceGroup = new DifferenceGroup(tripleStateA, tripleStateB, automaticResolutionState, conflicting);
	    	
	    	// Query all differences
			String queryDifferences = RevisionManagement.prefixes + String.format(
					  "SELECT ?subject ?predicate ?object ?referencedRevisionA ?referencedRevisionB %n"
					+ "WHERE { %n"
					+ "	<%s> a rpo:DifferenceGroup ; %n"
					+ "		rpo:hasDifference ?differenceUri . %n"
					+ "	?differenceUri a rpo:Difference ; %n"
					+ "		rpo:hasTriple ?tripleUri . %n"
					+ "	OPTIONAL { ?differenceUri rpo:referencesA ?referencedRevisionA . } %n"
					+ "	OPTIONAL { ?differenceUri rpo:referencesB ?referencedRevisionB . } %n"
					+ "	?tripleUri rdf:subject ?subject ; %n"
					+ "		rdf:predicate ?predicate ; %n"
					+ "		rdf:object ?object . %n"
					+ "}", uri);
			QueryExecution qeDifferences = QueryExecutionFactory.create(queryDifferences, model);
			ResultSet resultSetDifferences = qeDifferences.execSelect();
		    while(resultSetDifferences.hasNext()) {
		    	QuerySolution qsDifferences = resultSetDifferences.next();
		    	
		    	String subject = model.qnameFor(qsDifferences.getResource("?subject").toString());
		    	String predicate = model.qnameFor(qsDifferences.getResource("?predicate").toString());
	    	
		    	// Differ between literal and resource
				String object = "";
				TripleObjectTypeEnum objectType = null;
				if (qsDifferences.get("?object").isLiteral()) {
					object = qsDifferences.getLiteral("?object").toString();
					objectType = TripleObjectTypeEnum.LITERAL;
				} else {
					object = model.qnameFor(qsDifferences.getResource("?object").toString());
					objectType = TripleObjectTypeEnum.RESOURCE;
				}
		    	
		    	Triple triple = new Triple(subject, predicate, object, objectType);
		    	
		    	String referencedRevisionA = null;
		    	if (qsDifferences.getResource("?referencedRevisionA") != null) {
		    		referencedRevisionA = qsDifferences.getResource("?referencedRevisionA").toString();
		    	}
		    	String referencedRevisionB = null;
		    	if (qsDifferences.getResource("?referencedRevisionB") != null) {
		    		referencedRevisionB = qsDifferences.getResource("?referencedRevisionB").toString();
		    	}
		    	
		    	// Add further information to difference
		    	// Get the revision number if available
		    	String referencedRevisionLabelA = null;
		    	String referencedRevisionLabelB = null;
		    	
				if ((referencedRevisionA != null) && (referencedRevisionB == null)) {
					String query = RevisionManagement.prefixes + String.format(
							  "SELECT ?rev %n"
							+ "FROM <%s> %n"
							+ "WHERE { %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?rev . %n"
							+ "}", Config.revision_graph, referencedRevisionA);
					
					ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
					
					if (resultSet.hasNext()) {
						QuerySolution qs = resultSet.next();
						referencedRevisionLabelA = qs.getLiteral("?rev").toString();
					}
				} else if ((referencedRevisionA == null) && (referencedRevisionB != null)) {
					String query = RevisionManagement.prefixes + String.format(
							  "SELECT ?rev %n"
							+ "FROM <%s> %n"
							+ "WHERE { %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?rev . %n"
							+ "}", Config.revision_graph, referencedRevisionB);
					
					ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
					
					// Iterate over all labels
					if (resultSet.hasNext()) {
						QuerySolution qs = resultSet.next();
						referencedRevisionLabelB = qs.getLiteral("?rev").toString();
					}
				} else if ((referencedRevisionA != null) && (referencedRevisionB != null)) {
					String query = RevisionManagement.prefixes + String.format(
							  "SELECT ?revA ?revB %n"
							+ "FROM <%s> %n"
							+ "WHERE { %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?revA . %n"
							+ "	<%s> a rmo:Revision ; %n"
							+ "		rmo:revisionNumber ?revB . %n"
							+ "}", Config.revision_graph, referencedRevisionA, referencedRevisionB);
					
					ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
					// Iterate over all labels
					if (resultSet.hasNext()) {
						QuerySolution qs = resultSet.next();
						referencedRevisionLabelA = qs.getLiteral("?revA").toString();
						referencedRevisionLabelB = qs.getLiteral("?revB").toString();
					}
				}	    	
		    	
		    	Difference difference = new Difference(triple, 
		    			referencedRevisionA, referencedRevisionLabelA, tripleStateA, 
		    			referencedRevisionB, referencedRevisionLabelB, tripleStateB,
		    			conflicting, automaticResolutionState, resolutionState);
		    	differenceGroup.addDifference(triple, difference);
		    	differenceModel.addDifference(difference);
		    }
	    	differenceModel.addDifferenceGroup(differenceGroup.getTripleStateA().toString() + "-" + differenceGroup.getTripleStateB().toString(), differenceGroup);
	    }
	    
	    logger.info("Difference model successfully read.");	
		return differenceModel;
	}
	
	
	
	
	/**
	 * Convert SDD state string to SDD triple state. If value does not exists in enum null will be returned.
	 * 
	 * @param state the state to convert
	 * @return the SDD triple state
	 */
	
	private static SDDTripleStateEnum convertSDDStringToSDDTripleState(String state) {
		if (state.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
			return SDDTripleStateEnum.ADDED;
		} else if (state.equals(SDDTripleStateEnum.DELETED.getSddRepresentation())) {
			return SDDTripleStateEnum.DELETED;
		} else if (state.equals(SDDTripleStateEnum.ORIGINAL.getSddRepresentation())) {
			return SDDTripleStateEnum.ORIGINAL;
		} else if (state.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
			return SDDTripleStateEnum.NOTINCLUDED;
		} else {
			return null;
		}
	}
	
	
	
	/**
	 * Get difference by triple. If the difference model does not contain the triple null will be returned.
	 * 
	 * @param triple the triple to look for
	 * @param differenceModel the difference model
	 * @return the difference or null if triple is not included in difference model
	 */
	public static boolean isConflicting(Triple triple, DifferenceModel differenceModel) {
		if (differenceModel.allDifferences.contains(triple)){
			int pos = differenceModel.allDifferences.indexOf(triple);
			return differenceModel.allDifferences.get(pos).conflicting;
		}
		else 
			return false;
	}
	

	
	/**create High level change Table Model from high level change Model
	 * @param highLevelChangeModel the model to be read 
	 * @return hightLevelChangeTableModel the Model to be created 
	 *  */
	public static HighLevelChangeTableModel createHighLevelChangeTableModel (HighLevelChangeModel highLevelChangeModel) {
		HighLevelChangeTableModel highLevelChangeTableModel = new HighLevelChangeTableModel();
		Iterator<Entry<String, HighLevelChangeRenaming>> iterHL = highLevelChangeModel.getHighLevelChangesRenaming().entrySet().iterator();
		while(iterHL.hasNext()){
			HighLevelChangeRenaming hlcr = iterHL.next().getValue();
			//get added Difference and deleted Difference
			Difference additionDifference = hlcr.getAdditionDifference();
			Difference deletionDifference = hlcr.getDeletionDifference();
			Triple additionTriple = additionDifference.getTriple();
			Triple deletionTriple = deletionDifference.getTriple();
					
			String subject = additionTriple.getSubject();
			String predicate = additionTriple.getPredicate();
			String altObject = deletionTriple.getObject();
			String newObject = additionTriple.getObject();
			
			//get checkBox state
			//get approved button state
			String isRenaming = "no";
			String isResolved = "no";
			if(additionDifference.getResolutionState() == ResolutionStateEnum.RESOLVED
					&& deletionDifference.getResolutionState() == ResolutionStateEnum.RESOLVED) {
				if(additionDifference.getTripleResolutionState() == SDDTripleStateEnum.ADDED
						&& deletionDifference.getTripleResolutionState() == SDDTripleStateEnum.DELETED) {
					isRenaming = "yes";
					isResolved = "yes";	
				} else if (additionDifference.getTripleResolutionState() == SDDTripleStateEnum.DELETED 
						&& deletionDifference.getTripleResolutionState() == SDDTripleStateEnum.ADDED) {
					isRenaming = "no";
					isResolved = "yes";
				}
			}else if(additionDifference.getTripleResolutionState() == SDDTripleStateEnum.ADDED
					&& deletionDifference.getTripleResolutionState() == SDDTripleStateEnum.DELETED ){
				isRenaming = "yes";
			}
			
			highLevelChangeTableModel.readTableRow(new HighLevelChangeTableRow(hlcr, subject, predicate, altObject, newObject, isResolved, isRenaming));
		}
		return highLevelChangeTableModel;
	}

	
}
