package de.tud.plt.r43ples.merging.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.ResolutionStateEnum;
import de.tud.plt.r43ples.merging.SDDTripleStateEnum;
import de.tud.plt.r43ples.merging.TripleObjectTypeEnum;
import de.tud.plt.r43ples.merging.model.structure.Difference;
import de.tud.plt.r43ples.merging.model.structure.DifferenceGroup;
import de.tud.plt.r43ples.merging.model.structure.DifferenceModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeRenaming;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeTableModel;
import de.tud.plt.r43ples.merging.model.structure.HighLevelChangeTableRow;
import de.tud.plt.r43ples.merging.model.structure.TableModel;
import de.tud.plt.r43ples.merging.model.structure.TableRow;
import de.tud.plt.r43ples.merging.model.structure.TreeNode;
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
	 * Get the triples of the MERGE WITH query.
	 * 
	 * @param differenceModel the difference model
	 * @return the triples of the MERGE WITH query
	 */
	public static String getTriplesOfMergeWithQuery(DifferenceModel differenceModel) {
		
		// Contains all triples to add
		String triples = "";
		
		// Iterate over all difference groups
		Iterator<String> iteDifferenceGroupNames = differenceModel.getDifferenceGroups().keySet().iterator();
		while (iteDifferenceGroupNames.hasNext()) {
			String differenceGroupName = iteDifferenceGroupNames.next();
			DifferenceGroup differenceGroup = differenceModel.getDifferenceGroups().get(differenceGroupName);
			if (differenceGroup.isConflicting()) {
				// Iterate over all difference of current conflicting difference group
				Iterator<Triple> iteDifferenceNames = differenceGroup.getDifferences().keySet().iterator();
				while (iteDifferenceNames.hasNext()) {
					Triple currentDifferenceName = iteDifferenceNames.next();
					Difference difference = differenceGroup.getDifferences().get(currentDifferenceName);

					if (difference.getTripleResolutionState().equals(SDDTripleStateEnum.ADDED)) {
						String triple = difference.getTriple().toString();						
						triples += triple + "\n";
					}
				}
			}
		}

		return triples;
	}
	
	
	
	
	/** Create the individual models of both branches*/
	
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
	
	
	
	
	
	
	/**create high level change renaming model for high level view*/
	
	/**
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * ##                                                                                                                                                                      ##
	 * ## High level change generation.                                                                                                                                        ##
	 * ##                                                                                                                                                                      ##
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 */
	
	
	/**
	 * Create the high level change renaming model.
	 * 
	 * @param highLevelChangeModel the high level change model
	 * @param differenceModel the difference model
	 */
//	public static HighLevelChangeModel createHighLevelChangeRenamingModel(DifferenceModel differenceModel) {
//		HighLevelChangeModel highLevelChangeModel = new HighLevelChangeModel();
//		
//		// Get all differences of state combination DELETED-ORIGINAL
//		DifferenceGroup delOrig = differenceModel.getDifferenceGroups().get(SDDTripleStateEnum.DELETED + "-" + SDDTripleStateEnum.ORIGINAL);
//		
//		// Get all differences of state combination DELETED-ADDED
//		DifferenceGroup delAdd = differenceModel.getDifferenceGroups().get(SDDTripleStateEnum.DELETED + "-" + SDDTripleStateEnum.ADDED);
//		
//		// Get all differences of state combination ADDED-NOTINCLUDED
//		DifferenceGroup addNotInc = differenceModel.getDifferenceGroups().get(SDDTripleStateEnum.ADDED + "-" + SDDTripleStateEnum.NOTINCLUDED);
//
//		if ((addNotInc != null) && ((delOrig != null) || (delAdd != null))) {
//			// Get all possible prefixes
//			HashMap<Triple, Difference> possiblePrefixes = getAllPrefixesOfDifferenceMap(addNotInc.getDifferences());
//			// Iterate over all possible prefixes
//			Iterator<String> itePossiblePrefixes = possiblePrefixes.keySet().iterator();
//			while (itePossiblePrefixes.hasNext()) {
//				String currentPrefix = itePossiblePrefixes.next();
//				// Get possible mappings of DELETED-ORIGINAL map
//				ArrayList<Difference> mappingsDelOrig = new ArrayList<Difference>();
//				if (delOrig != null) {
//					mappingsDelOrig = getAllDifferencesByPrefix(currentPrefix, delOrig.getDifferences());	
//				}
//				// Get possible mappings of DELETED-ADDED map
//				ArrayList<Difference> mappingsDelAdd = new ArrayList<Difference>();
//				if (delAdd != null) {
//					mappingsDelAdd = getAllDifferencesByPrefix(currentPrefix, delAdd.getDifferences());
//				}
//				
//				HighLevelChangeRenaming highLevelChangeRenaming = null;
//				
//				if ((mappingsDelOrig.size() == 1) && mappingsDelAdd.isEmpty()) {
//					// Original found
//					highLevelChangeRenaming = new HighLevelChangeRenaming(mappingsDelOrig.get(0), possiblePrefixes.get(currentPrefix));
//				} else if (mappingsDelOrig.isEmpty() && (mappingsDelAdd.size() == 1)) {
//					// Added found
//					highLevelChangeRenaming = new HighLevelChangeRenaming(mappingsDelAdd.get(0), possiblePrefixes.get(currentPrefix));
//				}	
//	
//				if (highLevelChangeRenaming != null) {
//					highLevelChangeModel.addHighLevelChangeRenaming(tripleToString(highLevelChangeRenaming.getAdditionDifference().getTriple()), highLevelChangeRenaming);
//				}		
//			}
//		}
//		return highLevelChangeModel;
//	}
//	
	
	/**
	 * Get all prefixes of difference map and corresponding difference. Prefix is equal to triple string which contains only subject and predicate.
	 * Object must be a literal and the difference should not be approved.
	 * 
	 * @param differenceMap the difference map
	 * @return return distinct map of prefix difference combinations
	 */
	public static HashMap<Triple, Difference> getAllPrefixesOfDifferenceMap(HashMap<String, Difference> differenceMap) {
		// Create the result array list
		HashMap<Triple, Difference> resultList = new HashMap<Triple, Difference>();
		
		// Iterate over all differences
		Iterator<String> iteDifferences = differenceMap.keySet().iterator();
		while (iteDifferences.hasNext()) {
			String currentKey = iteDifferences.next();
			Difference currentDifference = differenceMap.get(currentKey);
			Triple currentTriple = currentDifference.getTriple();
			String currentPrefix = "<" + currentTriple.getSubject() + "> <" + currentTriple.getPredicate() + "> ";
			if (!resultList.containsKey(currentPrefix) && currentTriple.getObjectType().equals(TripleObjectTypeEnum.LITERAL) && !currentDifference.getResolutionState().equals(ResolutionStateEnum.RESOLVED)) {
				//resultList.put(currentPrefix, currentDifference);
			}
		}
		
		return resultList;
	}
	
	
	/**
	 * Get all differences by specified prefix.
	 * Object must be a literal and the difference should not be approved.
	 * 
	 * @param prefix the prefix
	 * @param differenceMap the difference map
	 * @return the differences which could be identified by specified prefix
	 */
	public static ArrayList<Difference> getAllDifferencesByPrefix(String prefix, HashMap<String, Difference> differenceMap) {
		// The result list
		ArrayList<Difference> result = new ArrayList<Difference>();
		// Tree map for sorting entries of hash map
		TreeMap<String, Difference> treeMap = new TreeMap<String, Difference>();
		treeMap.putAll(differenceMap);
		// Tail the tree map
		SortedMap<String, Difference> tailMap = treeMap.tailMap(prefix);
		if (!tailMap.isEmpty() && tailMap.firstKey().startsWith(prefix)) {
			Iterator<String> iteTailMap = tailMap.keySet().iterator();
			while (iteTailMap.hasNext()) {
				String currentKey = iteTailMap.next();
				if (currentKey.startsWith(prefix)) {
					Difference currentDifference = tailMap.get(currentKey);
					Triple currentTriple = currentDifference.getTriple();
					if (currentTriple.getObjectType().equals(TripleObjectTypeEnum.LITERAL) && !currentDifference.getResolutionState().equals(ResolutionStateEnum.RESOLVED)) {
						// Add corresponding difference to result list
						result.add(currentDifference);
					}
				} else {
					// Return the result map because there are no further keys which will start with the specified prefix
					return result;
				}
			}
		}
		
		return result;
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
	
	/**
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * ##                                                                                                                                                                      ##
	 * ## Difference tree.                                                                                                                                                     ##
	 * ##                                                                                                                                                                      ##
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 */	
	
	
	/**
	 * Create the differences tree root node.
	 * 
	 * @param differenceModel the differences model to use
	 * @return void aber read differenceModel and write the treeList
	 */
	
	/** read difference model and create treelist*/
	
	public static List<TreeNode> createDifferenceTree(DifferenceModel differenceModel) {
		List<TreeNode> treeList = new ArrayList<TreeNode>();
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()){
			//get Triple List von jede DifferenceGroup
			List<String> tripleList = new ArrayList<String>();
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			
			String groupName = (String) entryDG.getKey();
			
			logger.info("Tree test groupName: "+ groupName);
			//get jede differenceGroup
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			
			//get conflict status von differenceGroup
			boolean status = differ.isConflicting();
			
			logger.info("Tree test Status: "+ status);

			//get String name of each triple
			Iterator<Entry<Triple, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				Entry<Triple, Difference> entryDF = iterDIF.next();
				Triple tripleName = entryDF.getKey();
				logger.info("Tree test TripleName: "+ tripleName);
				
				Triple triple = entryDF.getValue().getTriple();
				
				String subject = triple.getSubject();
				String predicate = triple.getPredicate();
				String object = triple.getObject();
				
				StringBuilder TripleBuilder = new StringBuilder();
				String prefixTriple = TripleBuilder.append(subject).append(" ").append(predicate).append(" ").append(object).toString();
				
				//get alle tripleNames in der DifferenceGroup
				tripleList.add(prefixTriple);
			}
			// create treeNode
			TreeNode treeNode = new TreeNode(groupName, tripleList, status);
			treeList.add(treeNode);
		}
		return treeList;
	}
	/**read difference model and create table model
	 * */
	public static TableModel createTableModel(DifferenceModel differenceModel) {
		TableModel tableModel = new TableModel();
		
		//get difference group
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()){
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			boolean isconflicting = differ.isConflicting();
			SDDTripleStateEnum stateA = differ.getTripleStateA();
			SDDTripleStateEnum stateB = differ.getTripleStateB();
			
			//get difference 
			Iterator<Entry<Triple, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				
				Entry<Triple, Difference> entryDF = iterDIF.next();
				//get triple
				Triple triple = entryDF.getValue().getTriple();
				
				ResolutionStateEnum resolutionState = entryDF.getValue().getResolutionState();
				
				SDDTripleStateEnum autoResolutionState = entryDF.getValue().getTripleResolutionState();
				
				String subject = triple.getSubject();
				String predicate = triple.getPredicate();
				String object = triple.getObject();
				//get revision number
				String revisionA = entryDF.getValue().getReferencedRevisionLabelA();
				String revisionB = entryDF.getValue().getReferencedRevisionLabelB();
				
				//read each TableRow
				tableModel.readTableRow(new TableRow(triple, subject, predicate, object, stateA.toString(), 
			                            stateB.toString(), revisionA, revisionB, isconflicting, autoResolutionState.toString(),resolutionState.toString()));						
			}			
			
		}
		return tableModel;		
	}
	
		
	/**
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * ##                                                                                                                                                                      ##
	 * ## create properties list.                                                                                                                                                     ##
	 * ##                                                                                                                                                                      ##
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 */	
	
	/**
	 * Get all properties of specified revision.
	 * 
	 * @param graphName the graph name
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the array list of property URIs
	 * @throws InternalErrorException 
	 */
	public static ArrayList<String> getPropertiesOfDifferenceModel(Model model) throws InternalErrorException {
		// Result array list
		ArrayList<String> list = new ArrayList<String>();

    	// Query all properties
		String query = Config.getPrefixes() + RevisionManagement.prefixes +
				"SELECT DISTINCT ?propertyUri "
				+ "WHERE { "
				+ "	[] rdf:predicate ?propertyUri."
				+ "} "
				+ "ORDER BY ?propertyUri";
		
		QueryExecution qeProperties = QueryExecutionFactory.create(query, model);
		ResultSet resultSetProperties = qeProperties.execSelect();
		
		// Iterate over all properties
		while (resultSetProperties.hasNext()) {
			QuerySolution qs = resultSetProperties.next();
			String kqs = model.qnameFor(qs.getResource("?propertyUri").getURI());
			list.add(kqs);
		}
		return list;
	}
	
	
	
	/**
	 * Get all triples divided into insert and delete.
	 * 
	 * @param differenceModel the difference model
	 * @return array list which has two entries (entry 0 contains the triples to insert; entry 1 contains the triples to delete)
	 */
	public static ArrayList<String> getAllTriplesDividedIntoInsertAndDelete(DifferenceModel differenceModel, Model model) {
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
	
}
