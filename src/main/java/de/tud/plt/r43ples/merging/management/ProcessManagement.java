package de.tud.plt.r43ples.merging.management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.Interface;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.merging.MergeQueryTypeEnum;
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
import de.tud.plt.r43ples.merging.model.structure.IndividualModel;
import de.tud.plt.r43ples.merging.model.structure.IndividualStructure;
import de.tud.plt.r43ples.merging.model.structure.TableModel;
import de.tud.plt.r43ples.merging.model.structure.TableRow;
import de.tud.plt.r43ples.merging.model.structure.TreeNode;
import de.tud.plt.r43ples.merging.model.structure.Triple;
import de.tud.plt.r43ples.merging.model.structure.TripleIndividualStructure;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;




public class ProcessManagement {
	/** The logger. */
	private static Logger logger = Logger.getLogger(ProcessManagement.class);
	/** The SPARQL prefixes. **/
	private static final String prefixes = 
			  "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd: <http://eatld.et.tu-dresden.de/sdd#> \n"
			+ "PREFIX rpo: <http://eatld.et.tu-dresden.de/rpo#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";

	
	/**create the merge query
	 * @param graphName name of graph
	 * @param sdd the sdd model name 
	 * @param user client name 
	 * @param commitMessage messsage of client
	 * @param type type of merging
	 * @param branchNameA name of branch 1
	 * @param branchNameB name of branch 2
	 * @param type of merging
	 * @param triples triple set*/
	
	public static String createMergeQuery(String graphName, String sdd, String user, String commitMessage, MergeQueryTypeEnum type, String branchNameA, String branchNameB, String triples) {
		logger.info("Execute merge query of type " + type.toString());
		String query = "";
		
		String queryTemplateCommon = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		String queryTemplateWith = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
				+ "	%s"
				+ "}";
		
		String queryTemplateAuto = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE AUTO GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"";
		
		String queryTemplateManual = 
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "MERGE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
				+ "	%s"
				+ "}";
		if (type.equals(MergeQueryTypeEnum.COMMON)) {
			query = String.format(queryTemplateCommon, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(MergeQueryTypeEnum.WITH)) {
			query = String.format(queryTemplateWith, user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
		} else if (type.equals(MergeQueryTypeEnum.AUTO)) {
			query = String.format(queryTemplateAuto, user, commitMessage, graphName, sdd, branchNameA, branchNameB);
		} else if (type.equals(MergeQueryTypeEnum.MANUAL)) {
			query = String.format(queryTemplateManual, user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
		}
		
		return query;
	}
	
	
	
	
	/**
	 * Read difference model to java representation.
	 * 
	 * @param differenceModelToRead the difference model to read
	 * @param differenceModel the difference model where the result should be stored
	 */
	public static void readDifferenceModel(String differenceModelToRead, DifferenceModel differenceModel) {
		logger.info("Start reading difference model.");
		differenceModel.clear();
		
		logger.info("differenceModelToRead: "+ differenceModelToRead);
		Model model = JenaModelManagement.readStringToJenaModel(differenceModelToRead, "TURTLE");
		
		// Query all difference groups
		String queryDifferenceGroups = prefixes + String.format(
				  "SELECT ?uri ?tripleStateA ?tripleStateB ?automaticResolutionState ?conflicting %n"
				+ "WHERE { %n"
				+ "	?uri a rpo:DifferenceGroup ; %n"
				+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
				+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
				+ "		sddo:automaticResolutionState ?automaticResolutionState ; %n"
				+ "		sddo:isConflicting ?conflicting . %n"
				+ "}");
		logger.debug(queryDifferenceGroups);
		
		// query execution
		QueryExecution qeDifferenceGroups = QueryExecutionFactory.create(queryDifferenceGroups, model);
		ResultSet resultSetDifferenceGroups = qeDifferenceGroups.execSelect();
		// Iterate over all difference groups
	    while(resultSetDifferenceGroups.hasNext()) {
	    	QuerySolution qsDifferenceGroups = resultSetDifferenceGroups.next();
	    	String uri = qsDifferenceGroups.getResource("?uri").toString();
	    	SDDTripleStateEnum tripleStateA = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?tripleStateA").toString());
	    	SDDTripleStateEnum tripleStateB = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?tripleStateB").toString());
	    	SDDTripleStateEnum automaticResolutionState = convertSDDStringToSDDTripleState(qsDifferenceGroups.getResource("?automaticResolutionState").toString());
	    	boolean conflicting = qsDifferenceGroups.getLiteral("?conflicting").toString().equals("true^^http://www.w3.org/2001/XMLSchema#boolean");   
	    	logger.debug("Original value of conflict: "+ qsDifferenceGroups.getLiteral("?conflicting").toString());

	    	ResolutionStateEnum resolutionState = ResolutionStateEnum.DIFFERENCE;
	    	if (conflicting) {
	    		resolutionState = ResolutionStateEnum.CONFLICT;
	    	}
	    	
	    	DifferenceGroup differenceGroup = new DifferenceGroup(tripleStateA, tripleStateB, automaticResolutionState, conflicting);
	    	
	    	// Query all differences
			String queryDifferences = prefixes + String.format(
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
			logger.debug(queryDifferences);
			
			// query execution
			QueryExecution qeDifferences = QueryExecutionFactory.create(queryDifferences, model);
			ResultSet resultSetDifferences = qeDifferences.execSelect();
			// Iterate over all differences
		    while(resultSetDifferences.hasNext()) {
		    	QuerySolution qsDifferences = resultSetDifferences.next();
		    	
		    	String subject = qsDifferences.getResource("?subject").toString();
		    	String predicate = qsDifferences.getResource("?predicate").toString();
	    	
		    	// Differ between literal and resource
				String object = "";
				TripleObjectTypeEnum objectType = null;
				if (qsDifferences.get("?object").isLiteral()) {
					object = qsDifferences.getLiteral("?object").toString();
					objectType = TripleObjectTypeEnum.LITERAL;
				} else {
					object = qsDifferences.getResource("?object").toString();
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
					String query = prefixes + String.format(
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
					String query = prefixes + String.format(
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
					String query = prefixes + String.format(
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
		    	
		    	Difference difference = new Difference(triple, referencedRevisionA, referencedRevisionLabelA, referencedRevisionB, referencedRevisionLabelB, automaticResolutionState, resolutionState);
		    	differenceGroup.addDifference(tripleToString(triple), difference);
		    	logger.debug("tree: "+ differenceGroup.getDifferences().entrySet().toString());
		    }
	    	differenceModel.addDifferenceGroup(differenceGroup.getTripleStateA().toString() + "-" + differenceGroup.getTripleStateB().toString(), differenceGroup);
	    }
	    
	    logger.info("Difference model successfully read.");	
		logger.info("DT"+differenceModel.getDifferenceGroups().toString());
	}
	
	
	
	
	/**
	 * Convert SDD state string to SDD triple state. If value does not exists in enum null will be returned.
	 * 
	 * @param state the state to convert
	 * @return the SDD triple state
	 */
	
	public static SDDTripleStateEnum convertSDDStringToSDDTripleState(String state) {
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
	 * Create a string representation of the triple.
	 * 
	 * @param triple the triple
	 * @return the string representation
	 */
	
	public static String tripleToString(Triple triple) {
		if (triple.getObjectType().equals(TripleObjectTypeEnum.LITERAL)) {
			logger.debug(String.format("<%s> %s \"%s\" .", triple.getSubject(), getPredicate(triple), triple.getObject()));
			return String.format("<%s> %s \"%s\" .", triple.getSubject(), getPredicate(triple), triple.getObject());
		} else {
			logger.debug(String.format("<%s> %s <%s> .", triple.getSubject(), getPredicate(triple), triple.getObject()));
			return String.format("<%s> %s <%s> .", triple.getSubject(), getPredicate(triple), triple.getObject());
		}
	}
	
	
	/**
	 * Get the subject of triple.
	 * 
	 * @param triple the triple
	 * @return the formatted subject
	 */
	
	public static String getSubject(Triple triple) {
		return "<" + triple.getSubject() + ">";
	}
	
	
	/**
	 * Get the predicate of triple. If predicate equals rdf:type 'a' will be returned.
	 * 
	 * @param triple the triple
	 * @return the formatted predicate
	 */
	
	public static String getPredicate(Triple triple) {
		
		return "<" + triple.getPredicate() + ">";
	}
	
	
	/**
	 * Get the object of triple.
	 * 
	 * @param triple the triple
	 * @return the formatted object
	 */
	
	public static String getObject(Triple triple) {
		if (triple.getObjectType().equals(TripleObjectTypeEnum.LITERAL)) {
			if (triple.getObject().contains("@")) {
				return "\"" + triple.getObject().substring(0, triple.getObject().lastIndexOf("@")) + "\"@" + triple.getObject().substring(triple.getObject().lastIndexOf("@") + 1, triple.getObject().length());
			} else {
				return "\"" + triple.getObject() + "\"";
			}
		} else {
			return "<" + triple.getObject() + ">";		
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
				Iterator<String> iteDifferenceNames = differenceGroup.getDifferences().keySet().iterator();
				while (iteDifferenceNames.hasNext()) {
					String currentDifferenceName = iteDifferenceNames.next();
					Difference difference = differenceGroup.getDifferences().get(currentDifferenceName);

					if (difference.getTripleResolutionState().equals(SDDTripleStateEnum.ADDED)) {
						String triple = tripleToString(difference.getTriple());						
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
	 * Get all individuals of specified revision.
	 * 
	 * @param graphName the graph name
	 * @param revisionName the revision name
	 * @return the array list of individual URIs
	 * @throws InternalErrorException 
	 */
	public static ArrayList<String> getAllIndividualsOfRevision(String graphName, String revisionName) throws InternalErrorException {
		logger.info("Get all individuals of revision.");
		
		// Result array list
		ArrayList<String> list = new ArrayList<String>();
		
    	// Query all individuals (DISTINCT because there can be multiple individual definitions)
		String query = prefixes + String.format(
				  "SELECT DISTINCT ?individualUri "
				+ "WHERE { "
				+ " GRAPH <%s> REVISION \"%s\" {"
				+ "	?individualUri a ?class . "
				+ "} }"
				+ "ORDER BY ?individualUri", graphName, revisionName);
		logger.debug(query);
		
		
		String result = Interface.sparqlSelectConstructAsk(query, "text/xml", true);
		logger.debug(result);
		
		// Iterate over all individuals
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		while (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			list.add(qs.getResource("?individualUri").toString());
		}
		logger.info("Individual List : " + list.toArray().toString());
		return list;
	}
	
	/**
	 * Get all corresponding triples of specified individual.
	 * 
	 * @param graphName the graph name
	 * @param revisionName the revision name
	 * @param individualUri the individual URI
	 * @param differenceModel the difference model
	 * @return the hash map of triples
	 * @throws IOException 
	 * @throws InternalErrorException 
	 */
	public static HashMap<String, TripleIndividualStructure> getAllTriplesOfIndividual(String graphName, String revisionName, String individualUri, DifferenceModel differenceModel) throws InternalErrorException {
		logger.info("Get all corresponding triples of specified individual.");
		
		// Result hash map
		HashMap<String, TripleIndividualStructure> list = new HashMap<String, TripleIndividualStructure>();
		
    	// Query all individuals
		String query = prefixes + String.format(
				  "SELECT ?predicate ?object %n"
				+ "FROM <%s> REVISION \"%s\" %n"
				+ "WHERE { %n"
				+ "	<%s> ?predicate ?object . %n"
				+ "}"
				+ "ORDER BY ?predicate ?object", graphName, revisionName, individualUri);
		logger.debug(query);
		
		//here difference with mergingClient
		String result = Interface.sparqlSelectConstructAsk(query, "text/xml", false);
		
		logger.debug(result);
		
		// Iterate over all individuals
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		while (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			
	    	String predicate = qs.getResource("?predicate").toString();
    	
	    	// Differ between literal and resource
			String object = "";
			TripleObjectTypeEnum objectType = null;
			if (qs.get("?object").isLiteral()) {
				object = qs.getLiteral("?object").toString();
				objectType = TripleObjectTypeEnum.LITERAL;
			} else {
				object = qs.getResource("?object").toString();
				objectType = TripleObjectTypeEnum.RESOURCE;
			}
	    	// Create the triple
			Triple triple = new Triple(individualUri, predicate, object, objectType);
			// Check if there is a corresponding difference
			Difference difference = getDifferenceByTriple(triple, differenceModel);			
			// Create the triple individual structure
			TripleIndividualStructure tripleIndividualStructure = new TripleIndividualStructure(triple, difference);
			// Put the triple individual structure
	    	list.put(tripleToString(triple), tripleIndividualStructure);
		}

		return list;
	}
	
	
	
	/**
	 * Get difference by triple. If the difference model does not contain the triple null will be returned.
	 * 
	 * @param triple the triple to look for
	 * @param differenceModel the difference model
	 * @return the difference or null if triple is not included in difference model
	 */
	public static Difference getDifferenceByTriple(Triple triple, DifferenceModel differenceModel) {
		String tripleIdentifier = tripleToString(triple);
		
		// Iterate over all difference groups
		Iterator<String> iteDifferenceGroupNames = differenceModel.getDifferenceGroups().keySet().iterator();
		while (iteDifferenceGroupNames.hasNext()) {
			String differenceGroupName = iteDifferenceGroupNames.next();
			DifferenceGroup differenceGroup = differenceModel.getDifferenceGroups().get(differenceGroupName);
			// Check if the hash map contains the triple
			if (differenceGroup.getDifferences().containsKey(tripleIdentifier)) {
				return differenceGroup.getDifferences().get(tripleIdentifier);
			};
		}
		return null;
	}
	
	
	
	/**
	 * Create the individual model of specified revision.
	 * 
	 * @param graphName the graph name
	 * @param revisionName the revision name
	 * @param differenceModel the difference model
	 * @return the individual model
	 * @throws IOException 
	 * @throws InternalErrorException 
	 */
	public static IndividualModel createIndividualModelOfRevision(String graphName, String revisionName, DifferenceModel differenceModel) throws InternalErrorException {
		IndividualModel individualModel = new IndividualModel();
		
		ArrayList<String> individualURIs = getAllIndividualsOfRevision(graphName, revisionName);
		
		Iterator<String> iteIndividualURIs = individualURIs.iterator();
		while (iteIndividualURIs.hasNext()) {
			String currentIndividualUri = iteIndividualURIs.next();
			HashMap<String, TripleIndividualStructure> currentTriples = getAllTriplesOfIndividual(graphName, revisionName, currentIndividualUri, differenceModel);
			IndividualStructure currentIndividualStructure = new IndividualStructure(currentIndividualUri);
			currentIndividualStructure.setTriples(currentTriples);		
			individualModel.addIndividualStructure(currentIndividualUri, currentIndividualStructure);
		}
		
		return individualModel;
	}
	
	
	
	/**
	 * Create the individual model in Triple Table.
	 * 
	 * @param individualA the individual of Branch A 
	 * @param individualA the individual of Branch A 
	 * @param individualModelBranchA the individual model of Branch A
	 * @param individualModelBranchB the individual model of Branch b
	 * @param tableModel read the information in individual table model
	 */
	public static List<TableRow> createIndividualTableList (String individualA, String individualB, IndividualModel individualModelBranchA, 
			IndividualModel individualModelBranchB, TableModel tableModel) {
		
		List<TableRow> TripleRowList = tableModel.getTripleRowList();
		List<TableRow> updatedTripleRowList = new ArrayList<TableRow>();
		
		String identiferUri;
		if(!individualB.isEmpty()){
			identiferUri = individualB;
			Iterator<Entry<String,TripleIndividualStructure>> indIter = individualModelBranchB
					.getIndividualStructures().get(identiferUri).getTriples().entrySet().iterator();
			while(indIter.hasNext()) {
				Entry<String,TripleIndividualStructure> indEnt = indIter.next();
				
				//get triple
				Triple triple = indEnt.getValue().getTriple();			
				
				String subject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getSubject(triple));
				String predicate = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(triple));
				String object = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(triple));
				
				Iterator<TableRow> itr = TripleRowList.iterator();
				boolean status = true;
				while(itr.hasNext()){
					TableRow tableRow = itr.next();
					if(tableRow.getSubject().equals(subject) && tableRow.getObject().equals(object) && tableRow.getPredicate().equals(predicate)) {
						updatedTripleRowList.add(tableRow);
						status = false;
					}				
				}
				if(status == true){
					updatedTripleRowList.add(new TableRow(triple, subject, predicate, object, "--", 
	                        "--", "--", "--", "--", "--","--"));
				}
			}
			return updatedTripleRowList;
			
		}else if (!individualA.isEmpty()) {
			identiferUri = individualA;
			Iterator<Entry<String,TripleIndividualStructure>> indIter = individualModelBranchA
					.getIndividualStructures().get(identiferUri).getTriples().entrySet().iterator();
			while(indIter.hasNext()) {
				Entry<String,TripleIndividualStructure> indEnt = indIter.next();
				
				//get triple
				Triple triple = indEnt.getValue().getTriple();			
				
				String subject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getSubject(triple));
				String predicate = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(triple));
				String object = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(triple));
				
				Iterator<TableRow> itr = TripleRowList.iterator();
				boolean status = true;
				while(itr.hasNext()){
					TableRow tableRow = itr.next();
					if(tableRow.getSubject().equals(subject) && tableRow.getObject().equals(object) && tableRow.getPredicate().equals(predicate)) {
						updatedTripleRowList.add(tableRow);
						status = false;
					}
					
				}
				
				if(status == true){
					updatedTripleRowList.add(new TableRow(triple, subject, predicate, object, "--", 
	                        "--", "--", "--", "--", "--","--"));
				}
				
			}
			return updatedTripleRowList;
			
		}else {
			return updatedTripleRowList;
		}	
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
	public static void createHighLevelChangeRenamingModel(HighLevelChangeModel highLevelChangeModel, DifferenceModel differenceModel) {
		// Clear high level change model
		highLevelChangeModel.clear();
		
		// Get all differences of state combination DELETED-ORIGINAL
		DifferenceGroup delOrig = differenceModel.getDifferenceGroups().get(SDDTripleStateEnum.DELETED + "-" + SDDTripleStateEnum.ORIGINAL);
		
		// Get all differences of state combination DELETED-ADDED
		DifferenceGroup delAdd = differenceModel.getDifferenceGroups().get(SDDTripleStateEnum.DELETED + "-" + SDDTripleStateEnum.ADDED);
		
		// Get all differences of state combination ADDED-NOTINCLUDED
		DifferenceGroup addNotInc = differenceModel.getDifferenceGroups().get(SDDTripleStateEnum.ADDED + "-" + SDDTripleStateEnum.NOTINCLUDED);

		if ((addNotInc != null) && ((delOrig != null) || (delAdd != null))) {
			// Get all possible prefixes
			HashMap<String, Difference> possiblePrefixes = getAllPrefixesOfDifferenceMap(addNotInc.getDifferences());
			// Iterate over all possible prefixes
			Iterator<String> itePossiblePrefixes = possiblePrefixes.keySet().iterator();
			while (itePossiblePrefixes.hasNext()) {
				String currentPrefix = itePossiblePrefixes.next();
				// Get possible mappings of DELETED-ORIGINAL map
				ArrayList<Difference> mappingsDelOrig = new ArrayList<Difference>();
				if (delOrig != null) {
					mappingsDelOrig = getAllDifferencesByPrefix(currentPrefix, delOrig.getDifferences());	
				}
				// Get possible mappings of DELETED-ADDED map
				ArrayList<Difference> mappingsDelAdd = new ArrayList<Difference>();
				if (delAdd != null) {
					mappingsDelAdd = getAllDifferencesByPrefix(currentPrefix, delAdd.getDifferences());
				}
				
				HighLevelChangeRenaming highLevelChangeRenaming = null;
				
				if ((mappingsDelOrig.size() == 1) && mappingsDelAdd.isEmpty()) {
					// Original found
					highLevelChangeRenaming = new HighLevelChangeRenaming(mappingsDelOrig.get(0), possiblePrefixes.get(currentPrefix));
				} else if (mappingsDelOrig.isEmpty() && (mappingsDelAdd.size() == 1)) {
					// Added found
					highLevelChangeRenaming = new HighLevelChangeRenaming(mappingsDelAdd.get(0), possiblePrefixes.get(currentPrefix));
				}	
	
				if (highLevelChangeRenaming != null) {
					highLevelChangeModel.addHighLevelChangeRenaming(tripleToString(highLevelChangeRenaming.getAdditionDifference().getTriple()), highLevelChangeRenaming);
				}		
			}
			
			Iterator<Entry<String, HighLevelChangeRenaming>> test = highLevelChangeModel.getHighLevelChangesRenaming().entrySet().iterator();
			while(test.hasNext()){
				Entry<String, HighLevelChangeRenaming> ent = test.next();
				
				logger.info("High Level Change Model Test: " + ent.getKey().toString());
				logger.info("High Level Additon Value : "+ ent.getValue().getAdditionDifference().getTriple().getObject().toString());
				logger.info("High Level Deletion Value : "+ ent.getValue().getDeletionDifference().getTriple().getObject().toString());
			}		
		}
	}
	
	
	/**
	 * Get all prefixes of difference map and corresponding difference. Prefix is equal to triple string which contains only subject and predicate.
	 * Object must be a literal and the difference should not be approved.
	 * 
	 * @param differenceMap the difference map
	 * @return return distinct map of prefix difference combinations
	 */
	public static HashMap<String, Difference> getAllPrefixesOfDifferenceMap(HashMap<String, Difference> differenceMap) {
		// Create the result array list
		HashMap<String, Difference> resultList = new HashMap<String, Difference>();
		
		// Iterate over all differences
		Iterator<String> iteDifferences = differenceMap.keySet().iterator();
		while (iteDifferences.hasNext()) {
			String currentKey = iteDifferences.next();
			Difference currentDifference = differenceMap.get(currentKey);
			Triple currentTriple = currentDifference.getTriple();
			String currentPrefix = "<" + currentTriple.getSubject() + "> <" + currentTriple.getPredicate() + "> ";
			if (!resultList.containsKey(currentPrefix) && currentTriple.getObjectType().equals(TripleObjectTypeEnum.LITERAL) && !currentDifference.getResolutionState().equals(ResolutionStateEnum.RESOLVED)) {
				resultList.put(currentPrefix, currentDifference);
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
	 * @param hightLevelChangeTableModel the Model to be created 
	 *  */
	public static void createHighLevelChangeTableModel (HighLevelChangeModel highLevelChangeModel, HighLevelChangeTableModel highLevelChangeTableModel) {
		highLevelChangeTableModel.clear();
		Iterator<Entry<String, HighLevelChangeRenaming>> iterHL = highLevelChangeModel.getHighLevelChangesRenaming().entrySet().iterator();
		while(iterHL.hasNext()){
			HighLevelChangeRenaming hlcr = iterHL.next().getValue();
			//get added Difference and deleted Difference
			Difference additionDifference = hlcr.getAdditionDifference();
			Difference deletionDifference = hlcr.getDeletionDifference();
			Triple additionTriple = additionDifference.getTriple();
			Triple deletionTriple = deletionDifference.getTriple();
					
			String subject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getSubject(additionTriple));
			String predicate = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(additionTriple));
			String altObject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(deletionTriple));
			String newObject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(additionTriple));
			
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
		
		Iterator<HighLevelChangeTableRow> iter = highLevelChangeTableModel.getTripleRowList().iterator();
		while(iter.hasNext()){
			HighLevelChangeTableRow row = iter.next();
			logger.info("HighLevelTable test: " + row.getSubject() +  row.getPredicate()+ row.getObjectAlt() + row.getObjectNew() + "--" + row.getTripleId());
		}
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
	
	public static void createDifferenceTree(DifferenceModel differenceModel, List<TreeNode> treeList) {
//		List<TreeNode> treeList = new ArrayList<TreeNode>();
		treeList.clear();	
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()){
			//get Triple List von jede DifferenceGroup
			List<String> tripleList = new ArrayList<String>();
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			
			//get Name von differenceGroup
			String groupName = (String) entryDG.getKey();
			
			logger.info("Tree test groupName: "+ groupName);
			//get jede differenceGroup
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			
			//get conflict status von differenceGroup
			boolean status = differ.isConflicting();
			
			logger.info("Tree test Status: "+ status);

			//get String name von jede Triples
			Iterator<Entry<String, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				Entry<String, Difference> entryDF = iterDIF.next();
				// get jede tripleName
				String tripleName = entryDF.getKey();
				logger.info("Tree test TripleName: "+ tripleName);
				
				Triple triple = entryDF.getValue().getTriple();
				
				String subject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getSubject(triple));
				String predicate = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(triple));
				String object = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(triple));
				
				StringBuilder TripleBuilder = new StringBuilder();
				String prefixTriple = TripleBuilder.append(subject).append(" ").append(predicate).append(" ").append(object).toString();
				
				logger.info("Tree test PrefixTriple: "+ prefixTriple);

				//get alle tripleNames in der DifferenceGroup
				tripleList.add(prefixTriple);
			}
			// create treeNode
			TreeNode treeNode = new TreeNode(groupName, tripleList, status);
			treeList.add(treeNode);
		}
		
		logger.info("Difference Tree successful created.");
	}
	/**read difference model and create table model
	 * */
	public static void createTableModel(DifferenceModel differenceModel, TableModel tableModel) {
		tableModel.clear();
		
		//get difference group
		Iterator<Entry<String, DifferenceGroup>> iterDM = differenceModel.getDifferenceGroups().entrySet().iterator();
		while(iterDM.hasNext()){
			Entry<String, DifferenceGroup> entryDG = (Entry<String, DifferenceGroup>) iterDM.next();
			DifferenceGroup differ = (DifferenceGroup) entryDG.getValue();
			boolean isconflicting = differ.isConflicting();
			SDDTripleStateEnum stateA = differ.getTripleStateA();
			SDDTripleStateEnum stateB = differ.getTripleStateB();
	//		SDDTripleStateEnum resolutionState = differ.getAutomaticResolutionState();
			String conflicting = (isconflicting ) ? "1" : "0";
			//get difference 
			Iterator<Entry<String, Difference>> iterDIF = differ.getDifferences().entrySet().iterator();
			while(iterDIF.hasNext()){
				
				Entry<String, Difference> entryDF = iterDIF.next();
				//get triple
				Triple triple = entryDF.getValue().getTriple();
				
				ResolutionStateEnum resolutionState = entryDF.getValue().getResolutionState();
				
				SDDTripleStateEnum autoResolutionState = entryDF.getValue().getTripleResolutionState();
				
				String subject = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getSubject(triple));
				String predicate = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(triple));
				String object = ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getObject(triple));
				//get revision number
				String revisionA = entryDF.getValue().getReferencedRevisionLabelA();
				String revisionB = entryDF.getValue().getReferencedRevisionLabelB();
				
				//read each TableRow
				tableModel.readTableRow(new TableRow(triple, subject, predicate, object, stateA.toString(), 
			                            stateB.toString(), revisionA, revisionB, conflicting, autoResolutionState.toString(),resolutionState.toString()));
				logger.info("State Test"+ autoResolutionState.toString());
													
			}			
			
		}
		Iterator<TableRow> idt = tableModel.getTripleRowList().iterator();
		while(idt.hasNext()){
			TableRow tr = idt.next();
			logger.info("TableModel ID Test:" + tr.getRevisionA() + tr.getResolutionState() + tr.getTripleId() );
		}
		
		
		logger.info("TableModel successful created.");
		
	}
	
	
	/**
	 * Converts a triple string to a string in which URIs are replaced by prefixes which were specified in the configuration.
	 * If no prefix was found or if input string is a literal the input string will be returned.
	 * 
	 * @param tripleString the triple string (subject or predicate or object) to convert
	 * @return the converted triple string or input string
	 * @throws ConfigurationException 
	 */
	public static String convertTripleStringToPrefixTripleString(String tripleString) {		
		if (tripleString.contains("<") && tripleString.contains(">")) {
			String tripleStringConverted = tripleString.trim().replaceAll("<", "").replaceAll(">", "");
			int lastIndexSlash = tripleStringConverted.lastIndexOf("/");
			int lastIndexHash = tripleStringConverted.lastIndexOf("#");
			if ((lastIndexSlash == -1) && (lastIndexHash == -1)) {
				return tripleString;
			} else {
				int index = 0;
				if (lastIndexSlash > lastIndexHash) {
					// Slash separator found
					index = lastIndexSlash + 1;
				} else {
					// Hash separator found
					index = lastIndexHash + 1;
				}
				String subString = tripleStringConverted.substring(0, index);
				
				logger.info("get substring: "+subString);
			}
		}
		return tripleString;
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
	public static ArrayList<String> getPropertiesOfRevision(String graphName, String branchNameA, String branchNameB) throws InternalErrorException {
		logger.info("Get all properties of branches.");
		
		// Result array list
		ArrayList<String> list = new ArrayList<String>();

    	// Query all properties (DISTINCT because there can be multiple property occurrences)
		String query = String.format(
				"SELECT DISTINCT ?propertyUri %n"
				+ "FROM <%s> REVISION \"%s\" %n"
				+ "FROM <%s> REVISION \"%s\" %n"
				+ "WHERE { %n"
				+ "	?subject ?propertyUri ?object . %n"
				+ "} %n"
				+ "ORDER BY ?propertyUri", graphName, branchNameA, graphName, branchNameB);
		logger.debug(query);
		
		//here difference with mergingClient
		
		String result = Interface.sparqlSelectConstructAsk(query, "text/xml", false);
		
		logger.debug(result);
		
		// Iterate over all properties
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		while (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			//get kurz property
//			ProcessManagement.convertTripleStringToPrefixTripleString(ProcessManagement.getPredicate(triple));
			String kqs = ProcessManagement.convertTripleStringToPrefixTripleString("<"+qs.getResource("?propertyUri").toString()+">");
			
			//list.add(qs.getResource("?propertyUri").toString());
			list.add(kqs);

		}
		return list;
	}
	
	
	/**
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 * ##                                                                                                                                                                      ##
	 * ## create push ergebnis and create merging query                                                                                                                                                     ##
	 * ##                                                                                                                                                                      ##
	 * ##########################################################################################################################################################################
	 * ##########################################################################################################################################################################
	 */	
	
	
	/**
	 * Get the whole content of a revision as jena model.
	 * 
	 * @param graphName the graph name
	 * @param revision the revision
	 * @return the whole content of the revision of the graph as jena model
	 * @throws InternalErrorException 
	 */
	public static Model getWholeContentOfRevision(String graphName, String revision) throws InternalErrorException {
		String query = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "FROM <%s> REVISION \"%s\" %n"
				+ "WHERE { %n"
				+ "	?s ?p ?o %n"
				+ "}", graphName, revision);
		String result = Interface.sparqlSelectConstructAsk(query, "text/turtle", false);
		
		logger.debug("WholeContent Result: "+result);		
		return JenaModelManagement.readStringToJenaModel(result,"text/html");
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
				String triple = tripleToString( difference.getTriple());					
				
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
