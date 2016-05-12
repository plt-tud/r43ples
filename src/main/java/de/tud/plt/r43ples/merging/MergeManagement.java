package de.tud.plt.r43ples.merging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileUtils;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merge.SDDTripleStateEnum;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

/**
 * This class provides methods for merging branches.
 * 
 * @author Stephan Hensel
 *
 */
public class MergeManagement {

	/** The logger. */
	private static Logger logger = Logger.getLogger(MergeManagement.class);
	/** The SPARQL prefixes. **/
	private final static String prefix_rmo = "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n";
	private final static String prefixes = "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ prefix_rmo
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX rpo: <http://eatld.et.tu-dresden.de/rpo#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd: <http://eatld.et.tu-dresden.de/sdd#> \n";
	
	
	/**
	 * Get the common revision of the specified revisions which has the shortest path to the two.
	 * To ensure wise results the revisions should be terminal branch nodes.
	 * 
	 * @param revision1 the first revision should be a terminal branch node
	 * @param revision2 the second revision should be a terminal branch node
	 * @return the nearest common revision
	 */
	public static String getCommonRevisionWithShortestPath(final String revisionGraph, final String revision1, final String revision2) {
		
		logger.info("Get the common revision of <" + revision1 + "> and <" + revision2 + "> which has the shortest path.");
		
		String query = String.format(
			  "PREFIX prov: <http://www.w3.org/ns/prov#> "
			  + "SELECT DISTINCT ?revision "
			  + "WHERE { "
			  + "    GRAPH <%s> {"
			  + "        <%2$s> prov:wasDerivedFrom+ ?revision ."
			  + "        <%3$s> prov:wasDerivedFrom+ ?revision ."
			  + "        ?next prov:wasDerivedFrom ?revision."
			  + "        FILTER NOT EXISTS {"
			  + "            <%2$s> prov:wasDerivedFrom+ ?next ."
			  + "            <%3$s> prov:wasDerivedFrom+ ?next ."
			  + "        }"
			  + "    }"
			  + "}"
			  + "LIMIT 1",
			  revisionGraph, revision1, revision2);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			logger.info("Common revision found.");
			return qs.getResource("?revision").toString();
		}
		
		logger.info("No common revision could be found.");
		return null;		
	}
	
	
	/**
	 * Calculate the path from start revision to target revision.
	 * 
	 * @param startRevision the start revision
	 * @param targetRevision the target revision
	 * @return linked list with all revisions from start revision to target revision
	 */
	public static LinkedList<String> getPathBetweenStartAndTargetRevision(
			final String revisionGraph,	final String startRevision, final String targetRevision) {
		
		logger.info("Calculate the shortest path from revision <" + startRevision + "> to <" + targetRevision + "> .");
		String query = String.format(
			  "PREFIX prov: <http://www.w3.org/ns/prov#> %n"
			+ "SELECT DISTINCT ?revision ?previousRevision %n"
			+ "WHERE { %n"
			+ "	GRAPH <%s> { %n"
			+ "		<%s> prov:wasDerivedFrom* ?revision."
			+ "		?revision prov:wasDerivedFrom* <%s>."
			+ "		OPTIONAL{?revision prov:wasDerivedFrom ?previousRevision}"
			+ " }"
			+ "}", revisionGraph, targetRevision, startRevision);
		
		HashMap<String, ArrayList<String>> resultMap = new HashMap<String, ArrayList<String>>();
		LinkedList<String> list = new LinkedList<String>();
		
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);

		// Path element counter
		int counterLength = 0;
		
		while (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			String resource = qs.getResource("?revision").toString();
			String previousResource = null;
			if (qs.getResource("?previousRevision") != null) {
				previousResource = qs.getResource("?previousRevision").toString();
			}
			if (resultMap.containsKey(resource)) {
				resultMap.get(resource).add(previousResource);
			} else {
				ArrayList<String> arrayList = new ArrayList<String>();
				counterLength++;
				arrayList.add(previousResource);
				resultMap.put(resource, arrayList);
			}
		}
		
		// Sort the result map -> sorted list of path elements
		// A merged revision can have two predecessors -> it is important to choose the right predecessor revision according to the selected path
		String currentPathElement = targetRevision;
		for (int i = 0; i < counterLength; i++) {
			list.addFirst(currentPathElement);
			
			// Check if start revision was already reached
			if (currentPathElement.equals(startRevision)) {
				return list;
			}
			
			if (resultMap.get(currentPathElement).size() > 1) {
				if (resultMap.containsKey(resultMap.get(currentPathElement).get(0))) {
					currentPathElement = resultMap.get(currentPathElement).get(0);
				} else {
					currentPathElement = resultMap.get(currentPathElement).get(1);
				}
			} else {
				currentPathElement = resultMap.get(currentPathElement).get(0);
			}
		}

		return list;
	}
	
	
	/**
	 * Create the revision progresses for both branches.
	 * 
	 * @param listA the linked list with all revisions from start revision to target revision of branch A
	 * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
	 * @param uriA the URI of the revision progress of branch A
	 * @param listB the linked list with all revisions from start revision to target revision branch B
	 * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
	 * @param uriB the URI of the revision progress of branch B
	 * @throws InternalErrorException 
	 */
	public static void createRevisionProgresses(final String revisionGraph, final String graphName,
			LinkedList<String> listA, String graphNameRevisionProgressA, String uriA, 
			LinkedList<String> listB, String graphNameRevisionProgressB, String uriB) throws InternalErrorException {
		logger.info("Create the revision progress of branch A and B.");
		
		// Get the common revision
		String commonRevision = null;
		if ((listA.size() > 0) && (listB.size() > 0)) {
			commonRevision = listA.getFirst();
		} else {
			throw new InternalErrorException("Revision path contains no revisions.");
		}

		// Get the revision number of first revision
		logger.info("Get the revision number of first revision.");
		String firstRevisionNumber = "";

		String query = String.format(
			  "SELECT ?number %n"
			+ "WHERE { %n"
			+ "	GRAPH <%s> {"
			+ "		<%s> <http://eatld.et.tu-dresden.de/rmo#revisionNumber> ?number ."
			+ "} }", revisionGraph, commonRevision, commonRevision);
		
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			firstRevisionNumber = qs.getLiteral("?number").toString();
		}
		
		// Get the full graph name of first revision or create full revision graph of first revision
		String fullGraphNameCommonRevision = "";
		Boolean tempGraphWasCreated = false;
		try {
			fullGraphNameCommonRevision = RevisionManagement.getReferenceGraph(graphName, firstRevisionNumber);
		} catch (InternalErrorException e) {
			// Create a temporary full graph
			fullGraphNameCommonRevision = graphName + "RM-TEMP-REVISION-PROGRESS-FULLGRAPH";
			RevisionManagement.generateFullGraphOfRevision(graphName, firstRevisionNumber, fullGraphNameCommonRevision);
			tempGraphWasCreated = true;
		}
		
		// Create revision progress of branch A
		createRevisionProgress(revisionGraph, listA, fullGraphNameCommonRevision, graphNameRevisionProgressA, uriA);
		
		// Create revision progress of branch A
		createRevisionProgress(revisionGraph, listB, fullGraphNameCommonRevision, graphNameRevisionProgressB, uriB);
		
		// Drop the temporary full graph
		if (tempGraphWasCreated) {
			logger.info("Drop the temporary full graph.");
			TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + fullGraphNameCommonRevision + ">");
		}
		
	}
	
	
	/**
	 * Create the revision progress.
	 * 
	 * @param list the linked list with all revisions from start revision to target revision
	 * @param fullGraphNameCommonRevision the full graph name of the common revision (first revision of path)
	 * @param graphNameRevisionProgress the graph name of the revision progress
	 * @param uri the URI of the revision progress
	 * @throws InternalErrorException 
	 */
	public static void createRevisionProgress(final String revisionGraph, LinkedList<String> list, String fullGraphNameCommonRevision, String graphNameRevisionProgress, String uri) throws InternalErrorException {
		logger.info("Create the revision progress of " + uri + " in graph " + graphNameRevisionProgress + ".");
		
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameRevisionProgress));
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE GRAPH  <%s>", graphNameRevisionProgress));
		Iterator<String> iteList = list.iterator();
		
		if (iteList.hasNext()) {
			String firstRevision = iteList.next();
			
			// Create the initial content
			logger.info("Create the initial content.");
			String queryInitial = prefixes + String.format(	
				  "INSERT { GRAPH <%s> { %n"
				+ "	<%s> a rpo:RevisionProgress; %n"
				+ "		rpo:original [ %n"
				+ "			rdf:subject ?s ; %n"
				+ "			rdf:predicate ?p ; %n"
				+ "			rdf:object ?o ; %n"
				+ "			rmo:references <%s> %n"
				+ "		] %n"
				+ "} } WHERE { %n"
				+ "	GRAPH <%s> %n"
				+ "		{ ?s ?p ?o . } %n"
				+ "}",graphNameRevisionProgress, uri, firstRevision, fullGraphNameCommonRevision);
		
			// Execute the query which generates the initial content
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryInitial);
						
			// Update content by current add and delete set - remove old entries
			while (iteList.hasNext()) {
				String revision = iteList.next();
				logger.info("Update content by current add and delete set of revision " + revision + " - remove old entries.");
				// Get the ADD and DELETE set URIs
				String addSetURI = RevisionManagement.getAddSetURI(revision, revisionGraph);
				String deleteSetURI = RevisionManagement.getDeleteSetURI(revision, revisionGraph);
				
				if ((addSetURI != null) && (deleteSetURI != null)) {
					
					// Update the revision progress with the data of the current revision ADD set
					
					// Delete old entries (original)
					String queryRevision = prefixes + String.format(
						  "DELETE { GRAPH <%s> { %n"
						+ "	<%s> rpo:original ?blank . %n"
						+ "	?blank rdf:subject ?s . %n"
						+ "	?blank rdf:predicate ?p . %n"
						+ "	?blank rdf:object ?o . %n"
						+ "	?blank rmo:references ?revision . %n"
						+ "} } %n"
						+ "WHERE { "
						+ "		GRAPH <%s> { %n"
						+ "			<%s> rpo:original ?blank . %n"
						+ "			?blank rdf:subject ?s . %n"
						+ "			?blank rdf:predicate ?p . %n"
						+ "			?blank rdf:object ?o . %n"
						+ "			?blank rmo:references ?revision . %n"
						+ "		} %n"
						+ "		GRAPH <%s> { %n"
						+ "			?s ?p ?o %n"
						+ "		} %n"
						+ "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, addSetURI);
					
					queryRevision += "\n";
					
					// Delete old entries (added)
					queryRevision += String.format(
						  "DELETE { GRAPH <%s> { %n"
						+ "	<%s> rpo:added ?blank . %n"
						+ "	?blank rdf:subject ?s . %n"
						+ "	?blank rdf:predicate ?p . %n"
						+ "	?blank rdf:object ?o . %n"
						+ "	?blank rmo:references ?revision . %n"
						+ "} } %n"
						+ "WHERE { "
						+ "		GRAPH <%s> { %n"
						+ "			<%s> rpo:added ?blank . %n"
						+ "			?blank rdf:subject ?s . %n"
						+ "			?blank rdf:predicate ?p . %n"
						+ "			?blank rdf:object ?o . %n"
						+ "			?blank rmo:references ?revision . %n"
						+ "		} %n"
						+ "		GRAPH <%s> { %n"
						+ "			?s ?p ?o %n"
						+ "		} %n"
						+ "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, addSetURI);
					
					queryRevision += "\n";
					
					// Delete old entries (removed)
					queryRevision += String.format(
						  "DELETE { GRAPH <%s> { %n"
						+ "	<%s> rpo:removed ?blank . %n"
						+ "	?blank rdf:subject ?s . %n"
						+ "	?blank rdf:predicate ?p . %n"
						+ "	?blank rdf:object ?o . %n"
						+ "	?blank rmo:references ?revision . %n"
						+ "} } %n"
						+ "WHERE { "
						+ "		GRAPH <%s> { %n"
						+ "			<%s> rpo:removed ?blank . %n"
						+ "			?blank rdf:subject ?s . %n"
						+ "			?blank rdf:predicate ?p . %n"
						+ "			?blank rdf:object ?o . %n"
						+ "			?blank rmo:references ?revision . %n"
						+ "		} %n"
						+ "		GRAPH <%s> { %n"
						+ "			?s ?p ?o %n"
						+ "		} %n"
						+ "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, addSetURI);
					
					queryRevision += "\n";
					
					// Insert new entries (added)
					queryRevision += String.format(	
						  "INSERT { GRAPH <%s> {%n"
						+ "	<%s> a rpo:RevisionProgress; %n"
						+ "		rpo:added [ %n"
						+ "			rdf:subject ?s ; %n"
						+ "			rdf:predicate ?p ; %n"
						+ "			rdf:object ?o ; %n"
						+ "			rmo:references <%s> %n"
						+ "		] %n"
						+ "} } WHERE { %n"
						+ "	GRAPH <%s> %n"
						+ "		{ ?s ?p ?o . } %n"
						+ "};", graphNameRevisionProgress, uri, revision, addSetURI);
					
					queryRevision += "\n \n";
					
					// Update the revision progress with the data of the current revision DELETE set
					
					// Delete old entries (original)
					queryRevision += String.format(
						  "DELETE { GRAPH <%s> { %n"
						+ "	<%s> rpo:original ?blank . %n"
						+ "	?blank rdf:subject ?s . %n"
						+ "	?blank rdf:predicate ?p . %n"
						+ "	?blank rdf:object ?o . %n"
						+ "	?blank rmo:references ?revision . %n"
						+ "} } %n"
						+ "WHERE { "
						+ "		GRAPH <%s> { %n"
						+ "			<%s> rpo:original ?blank . %n"
						+ "			?blank rdf:subject ?s . %n"
						+ "			?blank rdf:predicate ?p . %n"
						+ "			?blank rdf:object ?o . %n"
						+ "			?blank rmo:references ?revision . %n"
						+ "		} %n"
						+ "		GRAPH <%s> { %n"
						+ "			?s ?p ?o %n"
						+ "		} %n"
						+ "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, deleteSetURI);
					
					queryRevision += "\n";
					
					// Delete old entries (added)
					queryRevision += String.format(
						  "DELETE { GRAPH <%s> { %n"
						+ "	<%s> rpo:added ?blank . %n"
						+ "	?blank rdf:subject ?s . %n"
						+ "	?blank rdf:predicate ?p . %n"
						+ "	?blank rdf:object ?o . %n"
						+ "	?blank rmo:references ?revision . %n"
						+ "} } %n"
						+ "WHERE { "
						+ "		GRAPH <%s> { %n"
						+ "			<%s> rpo:added ?blank . %n"
						+ "			?blank rdf:subject ?s . %n"
						+ "			?blank rdf:predicate ?p . %n"
						+ "			?blank rdf:object ?o . %n"
						+ "			?blank rmo:references ?revision . %n"
						+ "		} %n"
						+ "		GRAPH <%s> { %n"
						+ "			?s ?p ?o %n"
						+ "		} %n"
						+ "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, deleteSetURI);
					
					queryRevision += "\n";
					
					// Delete old entries (removed)
					queryRevision += String.format(
						  "DELETE { GRAPH <%s> { %n"
						+ "	<%s> rpo:removed ?blank . %n"
						+ "	?blank rdf:subject ?s . %n"
						+ "	?blank rdf:predicate ?p . %n"
						+ "	?blank rdf:object ?o . %n"
						+ "	?blank rmo:references ?revision . %n"
						+ "} } %n"
						+ "WHERE { "
						+ "		GRAPH <%s> { %n"
						+ "			<%s> rpo:removed ?blank . %n"
						+ "			?blank rdf:subject ?s . %n"
						+ "			?blank rdf:predicate ?p . %n"
						+ "			?blank rdf:object ?o . %n"
						+ "			?blank rmo:references ?revision . %n"
						+ "		} %n"
						+ "		GRAPH <%s> { %n"
						+ "			?s ?p ?o %n"
						+ "		} %n"
						+ "};", graphNameRevisionProgress, uri, graphNameRevisionProgress, uri, deleteSetURI);
					
					queryRevision += "\n";
					
					// Insert new entries (removed)
					queryRevision += String.format(	
						  "INSERT { GRAPH <%s> { %n"
						+ "	<%s> a rpo:RevisionProgress; %n"
						+ "		rpo:removed [ %n"
						+ "			rdf:subject ?s ; %n"
						+ "			rdf:predicate ?p ; %n"
						+ "			rdf:object ?o ; %n"
						+ "			rmo:references <%s> %n"
						+ "		] %n"
						+ "} } WHERE { %n"
						+ "	GRAPH <%s> %n"
						+ "		{ ?s ?p ?o . } %n"
						+ "}", graphNameRevisionProgress, uri, revision, deleteSetURI);
				
					// Execute the query which updates the revision progress by the current revision
					TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRevision);

				} else {
					//TODO Error management - is needed when a ADD or DELETE set is not referenced in the current implementation this error should not occur
					logger.error("ADD or DELETE set of " + revision + "does not exists.");
				}
				logger.info("Revision progress was created.");
			}
		}
	}

	
	/**
	 * Create the difference triple model which contains all differing triples.
	 * 
	 * @param graphName the graph name
	 * @param graphNameDifferenceTripleModel the graph name of the difference triple model
	 * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
	 * @param uriA the URI of the revision progress of branch A
	 * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
	 * @param uriB the URI of the revision progress of branch B
	 * @param uriSDD the URI of the SDD to use
	 */
	public static void createDifferenceTripleModel(String graphName, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD){
		
		logger.info("Create the difference triple model");
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameDifferenceTripleModel));
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE GRAPH  <%s>", graphNameDifferenceTripleModel));
		
		// Templates for revision A and B
		String sparqlTemplateRevisionA = String.format(
				  "	GRAPH <%s> { %n"
				+ "		<%s> <%s> ?blankA . %n"
				+ "			?blankA rdf:subject ?s . %n"
				+ "			?blankA rdf:predicate ?p . %n"
				+ "			?blankA rdf:object ?o . %n"
				+ "			?blankA rmo:references ?revisionA . %n"
				+ "	} %n", graphNameRevisionProgressA, uriA, "%s");
		String sparqlTemplateRevisionB = String.format(
				  "	GRAPH <%s> { %n"
				+ "		<%s> <%s> ?blankB . %n"
				+ "			?blankB rdf:subject ?s . %n"
				+ "			?blankB rdf:predicate ?p . %n"
				+ "			?blankB rdf:object ?o . %n"
				+ "			?blankB rmo:references ?revisionB . %n"
				+ "	} %n", graphNameRevisionProgressB, uriB, "%s");

		String sparqlTemplateNotExistsRevisionA = String.format(
				  "FILTER NOT EXISTS { %n"
				+ "	GRAPH <%s> { %n"
				+ "		<%s> ?everything ?blankA . %n"
				+ "			?blankA rdf:subject ?s . %n"
				+ "			?blankA rdf:predicate ?p . %n"
				+ "			?blankA rdf:object ?o . %n"
				+ "			?blankA rmo:references ?revisionA . %n"
				+ "	} %n"
				+ "}", graphNameRevisionProgressA, uriA);
		
		String sparqlTemplateNotExistsRevisionB = String.format(
				  "FILTER NOT EXISTS { %n"
				+ "	GRAPH <%s> { %n"
				+ "		<%s> ?everything ?blankB . %n"
				+ "			?blankB rdf:subject ?s . %n"
				+ "			?blankB rdf:predicate ?p . %n"
				+ "			?blankB rdf:object ?o . %n"
				+ "			?blankB rmo:references ?revisionB . %n"
				+ "	} %n"
				+ "}", graphNameRevisionProgressB, uriB);
		
		// Get all structural definitions which are generating differences
		String queryDifferingSD = String.format(
				  "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> %n"
				+ "PREFIX sdd:  <http://eatld.et.tu-dresden.de/sdd#> %n"
				+ "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> %n"
				+ "SELECT ?combinationURI ?tripleStateA ?tripleStateB ?conflict ?automaticResolutionState %n"
				+ "WHERE { GRAPH <%s> { %n"
				+ "	<%s> a sddo:StructuralDefinitionGroup ;"
				+ "		sddo:hasStructuralDefinition ?combinationURI ."
				+ "	?combinationURI a sddo:StructuralDefinition ; %n"
				+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
				+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
				+ "		sddo:isConflicting ?conflict ; %n"
				+ "		sddo:automaticResolutionState ?automaticResolutionState . %n"
				+ "} } %n", Config.sdd_graph, uriSDD);
				
		// Iterate over all differing combination URIs
		ResultSet resultSetDifferences = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifferingSD);
		while (resultSetDifferences.hasNext()) {
			QuerySolution qs = resultSetDifferences.next();

			String currentDifferenceCombinationURI = qs.getResource("?combinationURI").toString();
			String currentTripleStateA = qs.getResource("?tripleStateA").toString();
			String currentTripleStateB = qs.getResource("?tripleStateB").toString();
			// Will return an integer value because virtuoso stores boolean internal as integer
			String currentConflictState = qs.getLiteral("?conflict").toString();
			// TDB returns boolean value without "" -> add it to use it in the next query correctly
			if (currentConflictState.equals("true^^http://www.w3.org/2001/XMLSchema#boolean")) {
				currentConflictState = "\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
			} else {
				currentConflictState = "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
			}
			String currentAutomaticResolutionState = qs.getResource("?automaticResolutionState").toString();
			
			String querySelectPart = "SELECT ?s ?p ?o %s %s %n";
			String sparqlQueryRevisionA = null;
			String sparqlQueryRevisionB = null;			
			
			// A
			if (currentTripleStateA.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
				// In revision A the triple was added
				querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
				sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleStateEnum.ADDED.getRpoRepresentation());
			} else if (currentTripleStateA.equals(SDDTripleStateEnum.DELETED.getSddRepresentation())) {
				// In revision A the triple was deleted
				querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
				sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleStateEnum.DELETED.getRpoRepresentation());
			} else if (currentTripleStateA.equals(SDDTripleStateEnum.ORIGINAL.getSddRepresentation())) {
				// In revision A the triple is original
				querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
				sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleStateEnum.ORIGINAL.getRpoRepresentation());
			} else if (currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
				// In revision A the triple is not included
				querySelectPart = String.format(querySelectPart, "", "%s");
				sparqlQueryRevisionA = sparqlTemplateNotExistsRevisionA;
			}
			
			// B
			if (currentTripleStateB.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
				// In revision B the triple was added
				querySelectPart = String.format(querySelectPart, "?revisionB");
				sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleStateEnum.ADDED.getRpoRepresentation());
			} else if (currentTripleStateB.equals(SDDTripleStateEnum.DELETED.getSddRepresentation())) {
				// In revision B the triple was deleted
				querySelectPart = String.format(querySelectPart, "?revisionB");
				sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleStateEnum.DELETED.getRpoRepresentation());
			} else if (currentTripleStateB.equals(SDDTripleStateEnum.ORIGINAL.getSddRepresentation())) {
				// In revision B the triple is original
				querySelectPart = String.format(querySelectPart, "?revisionB");
				sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleStateEnum.ORIGINAL.getRpoRepresentation());
			} else if (currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
				// In revision B the triple is not included
				querySelectPart = String.format(querySelectPart, "");
				sparqlQueryRevisionB = sparqlTemplateNotExistsRevisionB;
			}
		
			// Concatenated SPARQL query
			String query = String.format(
					prefixes
					+ "%s"
					+ "WHERE { %n"
					+ "%s"
					+ "%s"
					+ "} %n", querySelectPart, sparqlQueryRevisionA, sparqlQueryRevisionB);
					
			// Iterate over all triples
			ResultSet resultSetTriples = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
			while (resultSetTriples.hasNext()) {
				QuerySolution qsQuery = resultSetTriples.next();
				
				String subject = qsQuery.getResource("?s").toString();
				String predicate = qsQuery.getResource("?p").toString();

				// Differ between literal and resource
				String object = "";
				if (qsQuery.get("?o").isLiteral()) {
					object = "\"" + qsQuery.getLiteral("?o").toString() + "\"";
				} else {
					object = "<" + qsQuery.getResource("?o").toString() + ">";
				}
				
				// Create the references A and B part of the query
				String referencesAB = ". %n";
				if (!currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation()) && !currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
					referencesAB = String.format(
							  "			rpo:referencesA <%s> ; %n"
							+ "			rpo:referencesB <%s> %n", qsQuery.getResource("?revisionA").toString(), 
																qsQuery.getResource("?revisionB").toString());
				} else if (currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation()) && !currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
					referencesAB = String.format(
							  "			rpo:referencesB <%s> %n", qsQuery.getResource("?revisionB").toString());
				} else if (!currentTripleStateA.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation()) && currentTripleStateB.equals(SDDTripleStateEnum.NOTINCLUDED.getSddRepresentation())) {
					referencesAB = String.format(
							  "			rpo:referencesA <%s> %n", qsQuery.getResource("?revisionA").toString());
				}
				
				String queryTriple = prefixes + String.format(
						  "INSERT DATA { GRAPH <%s> {%n"
						+ "	<%s> a rpo:DifferenceGroup ; %n"
						+ "	sddo:hasTripleStateA <%s> ; %n"
						+ "	sddo:hasTripleStateB <%s> ; %n"
						+ "	sddo:isConflicting %s ; %n"
						+ "	sddo:automaticResolutionState <%s> ; %n"
						+ "	rpo:hasDifference [ %n"
						+ "		a rpo:Difference ; %n"
						+ "			rpo:hasTriple [ %n"
						+ "				rdf:subject <%s> ; %n"
						+ "				rdf:predicate <%s> ; %n"
						+ "				rdf:object %s %n"
						+ "			] ; %n"
						+ "%s"
						+ "	] . %n"
						+ "} }", graphNameDifferenceTripleModel, 
									currentDifferenceCombinationURI, 
									currentTripleStateA, 
									currentTripleStateB,
									currentConflictState,
									currentAutomaticResolutionState,
									subject, 
									predicate,
									object,
									referencesAB);
				
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryTriple);
			}
		}
	}
	
	
	/**
	 * Create a merged revision.
	 * 
	 * @param graphName the graph name
	 * @param branchNameA the name of branch A
	 * @param branchNameB the name of branch B
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param graphNameDifferenceTripleModel the graph name of the difference triple model
	 * @param graphNameRevisionProgressA the graph name of the revisions progress A
	 * @param uriA the URI A
	 * @param graphNameRevisionProgressB the graph name of the revisions progress B
	 * @param uriB the URI B
	 * @param uriSDD the URI of the SDD
	 * @param type the merge query type
	 * @param triples the triples which are belonging to the current merge query in N-Triple serialization
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String createMergedRevision(String graphName, String branchNameA, String branchNameB, String user, String commitMessage, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD, MergeQueryTypeEnum type, String triples) throws InternalErrorException {
		 
		// Create an empty temporary graph which will contain the merged full content
		String graphNameOfMerged = graphName + "-RM-MERGED-TEMP";
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", graphNameOfMerged));
		TripleStoreInterfaceSingleton.get().executeCreateGraph(graphNameOfMerged);
		
		// Get the full graph name of branch A
		String graphNameOfBranchA = RevisionManagement.getReferenceGraph(graphName, branchNameA);
		// Get the full graph name of branch B
		String graphNameOfBranchB = RevisionManagement.getReferenceGraph(graphName, branchNameB);
		
		if (type.equals(MergeQueryTypeEnum.MANUAL)) {
			// Manual merge query
			RevisionManagement.executeINSERT(graphNameOfMerged, triples);
		} else {	
			// Copy graph B to temporary merged graph
			String queryCopy = String.format("COPY <%s> TO <%s>", graphNameOfBranchB, graphNameOfMerged);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryCopy);
			
			// Get the triples from branch A which should be added to/removed from the merged revision
			String triplesToAdd = "";
			String triplesToDelete = "";
			
			// Get all difference groups
			String queryDifferenceGroup = prefixes + String.format(
					  "SELECT ?differenceCombinationURI ?automaticResolutionState ?tripleStateA ?tripleStateB ?conflict %n"
					+ "WHERE { GRAPH <%s> { %n"
					+ "	?differenceCombinationURI a rpo:DifferenceGroup ; %n"
					+ "		sddo:automaticResolutionState ?automaticResolutionState ; %n"
					+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
					+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
					+ "		sddo:isConflicting ?conflict . %n"
					+ "} }", graphNameDifferenceTripleModel);
	
			// Iterate over all difference groups
			ResultSet resultSetDifferenceGroups = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifferenceGroup);
			while (resultSetDifferenceGroups.hasNext()) {
				QuerySolution qsCurrentDifferenceGroup = resultSetDifferenceGroups.next();
	
				String currentDifferencGroupURI = qsCurrentDifferenceGroup.getResource("?differenceCombinationURI").toString();
				String currentDifferencGroupAutomaticResolutionState = qsCurrentDifferenceGroup.getResource("?automaticResolutionState").toString();
//				Currently not needed
//				String currentDifferencGroupTripleStateA = qsCurrentDifferenceGroup.getResource("?tripleStateA").toString();
//				String currentDifferencGroupTripleStateB = qsCurrentDifferenceGroup.getResource("?tripleStateB").toString();
				boolean currentDifferencGroupConflict = qsCurrentDifferenceGroup.getLiteral("?conflict").getBoolean();
				
				// Get all differences (triples) of current difference group
				String queryDifference = prefixes + String.format(
						  "SELECT ?s ?p ?o %n"
						+ "WHERE { GRAPH <%s> { %n"
						+ "	<%s> a rpo:DifferenceGroup ; %n"
						+ "		rpo:hasDifference ?blankDifference . %n"
						+ "	?blankDifference a rpo:Difference ; %n"
						+ "		rpo:hasTriple ?triple . %n"
						+ "	?triple rdf:subject ?s . %n"
						+ "	?triple rdf:predicate ?p . %n"
						+ "	?triple rdf:object ?o . %n"
						+ "} }", graphNameDifferenceTripleModel, currentDifferencGroupURI);
				
				// Iterate over all differences (triples)
				ResultSet resultSetDifferences = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifference);
				while (resultSetDifferences.hasNext()) {
					QuerySolution qsCurrentDifference = resultSetDifferences.next();
					
					String subject = "<" + qsCurrentDifference.getResource("?s").toString() + ">";
					String predicate = "<" + qsCurrentDifference.getResource("?p").toString() + ">";
	
					// Differ between literal and resource
					String object = "";
					if (qsCurrentDifference.get("?o").isLiteral()) {
						object = "\"" + qsCurrentDifference.getLiteral("?o").toString() + "\"";
					} else {
						object = "<" + qsCurrentDifference.getResource("?o").toString() + ">";
					}
					
					if (	type.equals(MergeQueryTypeEnum.AUTO) || 
							type.equals(MergeQueryTypeEnum.COMMON) || 
							(type.equals(MergeQueryTypeEnum.WITH) && !currentDifferencGroupConflict) ) {
						// MERGE AUTO or common MERGE query
						if (currentDifferencGroupAutomaticResolutionState.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
							// Triple should be added
							triplesToAdd += subject + " " + predicate + " " + object + " . \n";
						} else {
							// Triple should be deleted
							triplesToDelete += subject + " " + predicate + " " + object + " . \n";
						}
					} else {
						// MERGE WITH query - conflicting triple
						Model model = JenaModelManagement.readNTripleStringToJenaModel(triples);
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
							triplesToAdd += subject + " " + predicate + " " + object + " . \n";
						} else {
							// Triple should be deleted
							triplesToDelete += subject + " " + predicate + " " + object + " . \n";
						}
					}
				}
				// Update the merged graph
				// Insert triplesToAdd
				RevisionManagement.executeINSERT(graphNameOfMerged, triplesToAdd);
				// Delete triplesToDelete
				RevisionManagement.executeDELETE(graphNameOfMerged, triplesToDelete);
			}
		}
		
		// Calculate the add and delete sets
		
		// Get all added triples (concatenate all triples which are in MERGED but not in A and all triples which are in MERGED but not in B)
		String queryAddedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { "
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfMerged, graphNameOfBranchA);
		
		String addedTriples = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryAddedTriples, FileUtils.langNTriple);
		
		queryAddedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfMerged, graphNameOfBranchB);

		addedTriples += TripleStoreInterfaceSingleton.get().executeConstructQuery(queryAddedTriples, FileUtils.langNTriple);
		
		// Get all removed triples (concatenate all triples which are in A but not in MERGED and all triples which are in B but not in MERGED)
		String queryRemovedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfBranchA, graphNameOfMerged);
		
		String removedTriples = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryRemovedTriples, FileUtils.langNTriple);
		
		queryRemovedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfBranchB, graphNameOfMerged);
		
		removedTriples += TripleStoreInterfaceSingleton.get().executeConstructQuery(queryRemovedTriples, FileUtils.langNTriple);

		// Create list with the 2 predecessors - the order is important - fist item will specify the branch were the new merged revision will be created
		ArrayList<String> usedRevisionNumbers = new ArrayList<String>();
		usedRevisionNumbers.add(branchNameB);
		usedRevisionNumbers.add(branchNameA);
		return RevisionManagement.createNewRevision(graphName, addedTriples, removedTriples, user, commitMessage, usedRevisionNumbers);
	}
	
	/**
	 * Create a rebase merged revision.
	 * 
	 * @param graphName the graph name
	 * @param branchNameA the name of branch A
	 * @param branchNameB the name of branch B
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param graphNameDifferenceTripleModel the graph name of the difference triple model
	 * @param graphNameRevisionProgressA the graph name of the revisions progress A
	 * @param uriA the URI A
	 * @param graphNameRevisionProgressB the graph name of the revisions progress B
	 * @param uriB the URI B
	 * @param uriSDD the URI of the SDD
	 * @param type the merge query type
	 * @param triples the triples which are belonging to the current merge query in N-Triple serialization
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static ArrayList<String> createRebaseMergedTripleList(String graphName, String branchNameA, String branchNameB, String user, String commitMessage, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD, MergeQueryTypeEnum type, String triples) throws InternalErrorException {
		//set the triple list
		ArrayList<String> list = new ArrayList<String>();
		
		// Create an empty temporary graph which will contain the merged full content
		String graphNameOfMerged = graphName + "-RM-MERGED-TEMP";
		TripleStoreInterfaceSingleton.get().executeCreateGraph(graphNameOfMerged);
		
		// Get the full graph name of branch A
		String graphNameOfBranchA = RevisionManagement.getReferenceGraph(graphName, branchNameA);
		// Get the full graph name of branch B
		String graphNameOfBranchB = RevisionManagement.getReferenceGraph(graphName, branchNameB);
		
		logger.info("the triples: "+ triples);
		if (type.equals(MergeQueryTypeEnum.MANUAL)) {
			// Manual merge query
			RevisionManagement.executeINSERT(graphNameOfMerged, triples);
		} else {	
			// Copy graph B to temporary merged graph
			String queryCopy = String.format("COPY <%s> TO <%s>", graphNameOfBranchB, graphNameOfMerged);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryCopy);
			
			// Get the triples from branch A which should be added to/removed from the merged revision
			String triplesToAdd = "";
			String triplesToDelete = "";
			
			// Get all difference groups
			String queryDifferenceGroup = prefixes + String.format(
					  "SELECT ?differenceCombinationURI ?automaticResolutionState ?tripleStateA ?tripleStateB ?conflict %n"
					+ "WHERE { GRAPH <%s> { %n"
					+ "	?differenceCombinationURI a rpo:DifferenceGroup ; %n"
					+ "		sddo:automaticResolutionState ?automaticResolutionState ; %n"
					+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
					+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
					+ "		sddo:isConflicting ?conflict . %n"
					+ "} }", graphNameDifferenceTripleModel);
	
			// Iterate over all difference groups
			ResultSet resultSetDifferenceGroups = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifferenceGroup);
			while (resultSetDifferenceGroups.hasNext()) {
				QuerySolution qsCurrentDifferenceGroup = resultSetDifferenceGroups.next();
	
				String currentDifferencGroupURI = qsCurrentDifferenceGroup.getResource("?differenceCombinationURI").toString();
				String currentDifferencGroupAutomaticResolutionState = qsCurrentDifferenceGroup.getResource("?automaticResolutionState").toString();
//				Currently not needed
//				String currentDifferencGroupTripleStateA = qsCurrentDifferenceGroup.getResource("?tripleStateA").toString();
//				String currentDifferencGroupTripleStateB = qsCurrentDifferenceGroup.getResource("?tripleStateB").toString();
				boolean currentDifferencGroupConflict = qsCurrentDifferenceGroup.getLiteral("?conflict").getBoolean();
				
				// Get all differences (triples) of current difference group
				String queryDifference = prefixes + String.format(
						  "SELECT ?s ?p ?o %n"
						+ "WHERE { GRAPH <%s> { %n"
						+ "	<%s> a rpo:DifferenceGroup ; %n"
						+ "		rpo:hasDifference ?blankDifference . %n"
						+ "	?blankDifference a rpo:Difference ; %n"
						+ "		rpo:hasTriple ?triple . %n"
						+ "	?triple rdf:subject ?s . %n"
						+ "	?triple rdf:predicate ?p . %n"
						+ "	?triple rdf:object ?o . %n"
						+ "} }", graphNameDifferenceTripleModel, currentDifferencGroupURI);
				
				// Iterate over all differences (triples)
				ResultSet resultSetDifferences = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryDifference);
				while (resultSetDifferences.hasNext()) {
					QuerySolution qsCurrentDifference = resultSetDifferences.next();
					
					String subject = "<" + qsCurrentDifference.getResource("?s").toString() + ">";
					String predicate = "<" + qsCurrentDifference.getResource("?p").toString() + ">";
	
					// Differ between literal and resource
					String object = "";
					if (qsCurrentDifference.get("?o").isLiteral()) {
						object = "\"" + qsCurrentDifference.getLiteral("?o").toString() + "\"";
					} else {
						object = "<" + qsCurrentDifference.getResource("?o").toString() + ">";
					}
					
					if (	type.equals(MergeQueryTypeEnum.AUTO) || 
							type.equals(MergeQueryTypeEnum.COMMON) || 
							(type.equals(MergeQueryTypeEnum.WITH) && !currentDifferencGroupConflict) ) {
						
						// MERGE AUTO or common MERGE query
						if (currentDifferencGroupAutomaticResolutionState.equals(SDDTripleStateEnum.ADDED.getSddRepresentation())) {
							// Triple should be added
							triplesToAdd += subject + " " + predicate + " " + object + " . \n";
						} else {
							// Triple should be deleted
							triplesToDelete += subject + " " + predicate + " " + object + " . \n";
						}
					}else {
						
						// MERGE WITH query - conflicting triple
						Model model = JenaModelManagement.readNTripleStringToJenaModel(triples);
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
							triplesToAdd += subject + " " + predicate + " " + object + " . \n";
						} else {
							// Triple should be deleted
							triplesToDelete += subject + " " + predicate + " " + object + " . \n";
						}
					}
				}
				// Update the merged graph
				// Insert triplesToAdd
				RevisionManagement.executeINSERT(graphNameOfMerged, triplesToAdd);
				// Delete triplesToDelete
				RevisionManagement.executeDELETE(graphNameOfMerged, triplesToDelete);
			}
		}
		
		// Calculate the add and delete sets
		
		// Get all added triples (concatenate all triples which are in MERGED but not in A and all triples which are in MERGED but not in B)
		String queryAddedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfMerged, graphNameOfBranchA);
		
		String addedTriples = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryAddedTriples, FileUtils.langNTriple);
		
		queryAddedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfMerged, graphNameOfBranchB);

		addedTriples += TripleStoreInterfaceSingleton.get().executeConstructQuery(queryAddedTriples, FileUtils.langNTriple);
		
		// Get all removed triples (concatenate all triples which are in A but not in MERGED and all triples which are in B but not in MERGED)
		String queryRemovedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfBranchA, graphNameOfMerged);
		
		String removedTriples = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryRemovedTriples, FileUtils.langNTriple);
		
		queryRemovedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfBranchB, graphNameOfMerged);
		
		removedTriples += TripleStoreInterfaceSingleton.get().executeConstructQuery(queryRemovedTriples, FileUtils.langNTriple);
		
		// Add the string to the result list
		list.add(String.format(addedTriples));
		list.add(String.format(removedTriples));
				
		return list;
	}
	
}
