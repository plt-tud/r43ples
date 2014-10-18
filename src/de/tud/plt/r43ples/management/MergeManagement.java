package de.tud.plt.r43ples.management;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/*
 * This class provides methods for merging branches.
 * *
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
	 * @throws IOException
	 * @throws HttpException 
	 */
	public static String getCommonRevisionWithShortestPath(String revision1, String revision2) throws IOException, HttpException {
		
		logger.info("Get the common revision of <" + revision1 + "> and <" + revision2 + "> which has the shortest path.");
		String query = String.format(
			  "# Query selects the revision which is on both paths (branch 1 and branch 2) and has the minimal path element count \n"
			+ "SELECT ?link MIN(xsd:decimal(?pathElements1) + xsd:decimal(?pathElements2)) AS ?pathElementCountBothBranches \n"
			+ "WHERE { \n"
			+ "	{ \n"
			+ "		# Query creates for each start revision of branch 1 the path element count \n"
			+ "		SELECT ?startRevision1 COUNT(?path1) as ?pathElements1 \n"
			+ "		WHERE { \n"
			+ "			{ \n"
			+ "				SELECT ?s ?startRevision1 \n"
			+ "				WHERE { \n"
			+ "					graph <%s> { \n"
			+ "						?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?startRevision1 . \n"
			+ "					} \n"
			+ "				} \n"
			+ "			} \n"
			+ "			OPTION ( TRANSITIVE, \n"
			+ "					 t_distinct, \n"
			+ "					 t_in(?s), \n"
			+ "					 t_out(?startRevision1), \n"
			+ "					 t_step (?s) as ?link1, \n"
			+ "					 t_step ('path_id') as ?path1, \n"
			+ "					 t_step ('step_no') as ?step1 \n"
			+ "					) . \n"
			+ "			FILTER ( ?s = <%s> ) \n"
			+ "		} GROUP BY ?startRevision1 \n"
			+ "	} \n"
			+ "	OPTIONAL \n"
			+ "	{ \n"
			+ "		# Query creates for each start revision of branch 2 the path element count \n"
			+ "		SELECT ?startRevision2 COUNT(?path2) as ?pathElements2 \n"
			+ "		WHERE { \n"
			+ "			{ \n"
			+ "				SELECT ?s ?startRevision2 \n"
			+ "				WHERE { \n"
			+ "					graph <%s> { \n"
			+ "						?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?startRevision2 . \n"
			+ "					} \n"
			+ "				} \n"
			+ "			} \n"
			+ "			OPTION ( TRANSITIVE, \n"
			+ "					 t_distinct, \n"
			+ "					 t_in(?s), \n"
			+ "					 t_out(?startRevision2), \n"
			+ "					 t_step (?s) as ?link2, \n"
			+ "					 t_step ('path_id') as ?path2, \n"
			+ "					 t_step ('step_no') as ?step1 \n"
			+ "					) . \n"
			+ "			FILTER ( ?s = <%s> ) \n"
			+ "		  } GROUP BY ?startRevision2 \n"
			+ "	} \n"
			+ "	OPTIONAL \n"
			+ "	{ \n"
			+ "		# Query response contains all revisions which are on both paths (branch 1 and branch 2) \n"
			+ "		SELECT DISTINCT ?link1 AS ?link \n"
			+ "		WHERE { \n"
			+ "			{ \n"
			+ "				# Query creates all possible paths for branch 1 \n"
			+ "				SELECT ?link1 ?step1 ?path1 \n"
			+ "				WHERE { \n"
			+ "					{ \n"
			+ "						SELECT ?s ?o \n"
			+ "						WHERE { \n"
			+ "							graph <%s> { \n"
			+ "								?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?o . \n"
			+ "							} \n"
			+ "						} \n"
			+ "					} \n"
			+ "					OPTION ( TRANSITIVE, \n"
			+ "							 t_distinct, \n"
			+ "							 t_in(?s), \n"
			+ "							 t_out(?o), \n"
			+ "							 t_step (?s) as ?link1, \n"
			+ "							 t_step ('path_id') as ?path1, \n"
			+ "							 t_step ('step_no') as ?step1 \n"
			+ "							) . \n"
			+ "					FILTER ( ?s = <%s> ) \n"
			+ "				} \n"
			+ "			} \n"
			+ "			OPTIONAL \n"
			+ "			{ \n"
			+ "				# Query creates all possible paths for branch 2 \n"
			+ "				SELECT ?link2 ?step2 ?path2 \n"
			+ "				WHERE { \n"
			+ "					{ \n"
			+ "						SELECT ?s ?o \n"
			+ "						WHERE { \n"
			+ "							graph <%s> { \n"
			+ "								?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?o . \n"
			+ "							} \n"
			+ "						} \n"
			+ "					} \n"
			+ "					OPTION ( TRANSITIVE, \n"
			+ "							 t_distinct, \n"
			+ "							 t_in(?s), \n"
			+ "							 t_out(?o), \n"
			+ "							 t_step (?s) as ?link2, \n"
			+ "							 t_step ('path_id') as ?path2, \n"
			+ "							 t_step ('step_no') as ?step2 \n"
			+ "							) . \n"
			+ "					FILTER ( ?s = <%s> ) \n"
			+ "				} \n"
			+ "			} \n"
			+ "			FILTER ( ?link1 = ?link2 ) \n"
			+ "		} \n"
			+ "	} \n"
			+ "	FILTER ( ?startRevision1 = ?startRevision2 && ?startRevision1 = ?link ) \n"
			+ "} ORDER BY ?pathElementCountBothBranches \n"
			+ "LIMIT 1", Config.revision_graph, revision1, Config.revision_graph, revision2, Config.revision_graph, revision1, Config.revision_graph, revision2);
		
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		
		if (ResultSetFactory.fromXML(result).hasNext()) {
			QuerySolution qs = ResultSetFactory.fromXML(result).next();
			logger.info("Common revision found.");
			return qs.getResource("?link").toString();
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
	 * @throws HttpException 
	 * @throws IOException 
	 */
	public static LinkedList<String> getPathBetweenStartAndTargetRevision(String startRevision, String targetRevision) throws IOException, HttpException {
		
		logger.info("Calculate the shortest path from revision <" + startRevision + "> to <" + targetRevision + "> .");
		String query = String.format(
			  "# Query creates shortest path between start and target revision \n"
			+ "SELECT ?link ?step \n"
			+ "WHERE { \n"
			+ "	{ \n"
			+ "		SELECT ?s ?o \n"
			+ "		WHERE { \n"
			+ "			graph <%s> { \n"
			+ "				?s <http://www.w3.org/ns/prov#wasDerivedFrom> ?o . \n"
			+ "			} \n"
			+ "		} \n"
			+ "	} \n"
			+ "	OPTION ( TRANSITIVE, \n"
			+ "			 t_distinct, \n"
			+ "			 t_in(?s), \n"
			+ "			 t_out(?o), \n"
			+ "			 t_shortest_only, \n"
			+ "			 t_step (?s) as ?link, \n"
			+ "			 t_step ('path_id') as ?path, \n"
			+ "			 t_step ('step_no') as ?step \n"
			+ "			) . \n"
			+ "	FILTER ( ?s = <%s> && ?o = <%s> ) \n"
			+ "}  ORDER BY ?step", Config.revision_graph, targetRevision, startRevision);
		
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		
		LinkedList<String> list = new LinkedList<String>();
		
		ResultSet resultSet = ResultSetFactory.fromXML(result);

		while (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			String resource = qs.getResource("?link").toString();
			logger.info("Path element: \n" + resource);
			list.addFirst(resource);
		}

		return list;
	}
	
	
	/**
	 * Create the revision progress.
	 * 
	 * @param list the linked list with all revisions from start revision to target revision
	 * @param graphNameRevisionProgress the graph name of the revision progress
	 * @param uri the URI of the revision progress
	 * @throws IOException
	 * @throws HttpException
	 */
	public static void createRevisionProgress(LinkedList<String> list, String graphNameRevisionProgress, String uri) throws IOException, HttpException {
		logger.info("Create the revision progress of " + uri + " in graph " + graphNameRevisionProgress + ".");
		
		logger.info("Create the revision progress graph with the name: \n" + graphNameRevisionProgress);
		TripleStoreInterface.executeQueryWithAuthorization(String.format("DROP SILENT GRAPH <%s>", graphNameRevisionProgress), "HTML");
		TripleStoreInterface.executeQueryWithAuthorization(String.format("CREATE GRAPH  <%s>", graphNameRevisionProgress), "HTML");
		Iterator<String> iteList = list.iterator();
		
		if (iteList.hasNext()) {
			String firstRevision = iteList.next();
			
			// Get the revision number of first revision
			logger.info("Get the revision number of first revision.");
			String firstRevisionNumber = "";
			String graphName = "";

			String query = String.format(
				  "SELECT ?number ?graph \n"
				+ "FROM <%s> \n"
				+ "WHERE { \n"
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#revisionNumber> ?number . \n"
				+ " <%s> <http://eatld.et.tu-dresden.de/rmo#revisionOf> ?graph . \n"
				+ "}", Config.revision_graph, firstRevision, firstRevision);
			
			String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
			
			if (ResultSetFactory.fromXML(result).hasNext()) {
				QuerySolution qs = ResultSetFactory.fromXML(result).next();
				firstRevisionNumber = qs.getLiteral("?number").toString();
				graphName = qs.getResource("?graph").toString();
			}
			
			// Get the full graph name of first revision or create full revision graph of first revision
			String fullGraphName = "";
			try {
				fullGraphName = RevisionManagement.getReferenceGraph(graphName, firstRevisionNumber);
			} catch (NoSuchElementException e) {
				// Create a temporary full graph
				RevisionManagement.generateFullGraphOfRevision(graphName, firstRevisionNumber, "RM-TEMP-REVISION-PROGRESS-FIRSTREVISION");
				fullGraphName = "RM-TEMP-REVISION-PROGRESS-FIRSTREVISION";
			}
			
			// Create the initial content
			logger.info("Create the initial content.");
			String queryInitial = prefixes + String.format(	
				  "INSERT INTO <%s> { \n"
				+ "	<%s> a rpo:RevisionProgress; \n"
				+ "		rpo:original [ \n"
				+ "			rdf:subject ?s ; \n"
				+ "			rdf:predicate ?p ; \n"
				+ "			rdf:object ?o ; \n"
				+ "			rmo:references <%s> \n"
				+ "		] \n"
				+ "} WHERE { \n"
				+ "	GRAPH <%s> \n"
				+ "		{ ?s ?p ?o . } \n"
				+ "}",graphNameRevisionProgress,uri, firstRevision, fullGraphName);
		
			// Execute the query which generates the initial content
			TripleStoreInterface.executeQueryWithAuthorization(queryInitial, "HTML");
			
			// Drop the temporary full graph
			logger.info("Drop the temporary full graph.");
			TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <RM-TEMP-REVISION-PROGRESS-FIRSTREVISION>", "HTML");
			
			// Update content by current add and delete set - remove old entries
			while (iteList.hasNext()) {
				String revision = iteList.next();
				logger.info("Update content by current add and delete set of revision " + revision + " - remove old entries.");
				// Get the ADD and DELETE set URIs
				String addSetURI = RevisionManagement.getAddSetURI(revision, Config.revision_graph);
				String deleteSetURI = RevisionManagement.getDeleteSetURI(revision, Config.revision_graph);
				
				if ((addSetURI != null) && (deleteSetURI != null)) {
					
					// Update the revision progress with the data of the current revision ADD set
					
					// Delete old entries (original)
					String queryRevision = prefixes + String.format(
						  "DELETE FROM GRAPH <%s> { \n"
						+ "	<%s> rpo:original ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:references ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rpo:original ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:references ?revision . \n"
						+ "		} \n"
						+ "		GRAPH <%s> { \n"
						+ "			?s ?p ?o \n"
						+ "		} \n"
						+ "	} \n"
						+ "}",graphNameRevisionProgress, uri, uri, addSetURI);
					
					queryRevision += "\n";
					
					// Delete old entries (removed)
					queryRevision += String.format(
						  "DELETE FROM GRAPH <%s> { \n"
						+ "	<%s> rpo:removed ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:references ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rpo:removed ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:references ?revision . \n"
						+ "		} \n"
						+ "		GRAPH <%s> { \n"
						+ "			?s ?p ?o \n"
						+ "		} \n"
						+ "	} \n"
						+ "}",graphNameRevisionProgress, uri, uri, addSetURI);
					
					queryRevision += "\n";
					
					// Insert new entries (added)
					queryRevision += String.format(	
						  "INSERT INTO <%s> { \n"
						+ "	<%s> a rpo:RevisionProgress; \n"
						+ "		rpo:added [ \n"
						+ "			rdf:subject ?s ; \n"
						+ "			rdf:predicate ?p ; \n"
						+ "			rdf:object ?o ; \n"
						+ "			rmo:references <%s> \n"
						+ "		] \n"
						+ "} WHERE { \n"
						+ "	GRAPH <%s> \n"
						+ "		{ ?s ?p ?o . } \n"
						+ "}",graphNameRevisionProgress, uri, revision, addSetURI);
					
					queryRevision += "\n \n";
					
					// Update the revision progress with the data of the current revision DELETE set
					
					// Delete old entries (original)
					queryRevision += String.format(
						  "DELETE FROM GRAPH <%s> { \n"
						+ "	<%s> rpo:original ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:references ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rpo:original ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:references ?revision . \n"
						+ "		} \n"
						+ "		GRAPH <%s> { \n"
						+ "			?s ?p ?o \n"
						+ "		} \n"
						+ "	} \n"
						+ "}",graphNameRevisionProgress, uri, uri, deleteSetURI);
					
					queryRevision += "\n";
					
					// Delete old entries (added)
					queryRevision += String.format(
						  "DELETE FROM GRAPH <%s> { \n"
						+ "	<%s> rpo:added ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:references ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rpo:added ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:references ?revision . \n"
						+ "		} \n"
						+ "		GRAPH <%s> { \n"
						+ "			?s ?p ?o \n"
						+ "		} \n"
						+ "	} \n"
						+ "}",graphNameRevisionProgress, uri, uri, deleteSetURI);
					
					queryRevision += "\n";
					
					// Insert new entries (removed)
					queryRevision += String.format(	
						  "INSERT INTO <%s> { \n"
						+ "	<%s> a rpo:RevisionProgress; \n"
						+ "		rpo:removed [ \n"
						+ "			rdf:subject ?s ; \n"
						+ "			rdf:predicate ?p ; \n"
						+ "			rdf:object ?o ; \n"
						+ "			rmo:references <%s> \n"
						+ "		] \n"
						+ "} WHERE { \n"
						+ "	GRAPH <%s> \n"
						+ "		{ ?s ?p ?o . } \n"
						+ "}",graphNameRevisionProgress, uri, revision, deleteSetURI);
				
					// Execute the query which updates the revision progress by the current revision
					TripleStoreInterface.executeQueryWithAuthorization(queryRevision, "HTML");

				} else {
					//TODO Error management - is needed when a ADD or DELETE set is not referenced in the current implementation this error should not occur
					logger.error("ADD or DELETE set of " + revision + "does not exists.");
				}
				logger.info("Revision progress was created.");
				
			}
			
			
		}
		
	}

	
	/*
	 * Create the difference triple model which contains all differing triples.
	 * 
	 * @param graphName the graph name
	 * @param graphNameDifferenceTripleModel the graph name of the difference triple model
	 * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
	 * @param uriA the URI of the revision progress of branch A
	 * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
	 * @param uriB the URI of the revision progress of branch B
	 * @param uriSDD the URI of the SDD to use
	 * @throws HttpException 
	 * @throws IOException 
	 */
	public static void createDifferenceTripleModel(String graphName, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD) throws IOException, HttpException {
		
		logger.info("Create the difference triple model");
		TripleStoreInterface.executeQueryWithAuthorization(String.format("DROP SILENT GRAPH <%s>", graphNameDifferenceTripleModel), "HTML");
		TripleStoreInterface.executeQueryWithAuthorization(String.format("CREATE GRAPH  <%s>", graphNameDifferenceTripleModel), "HTML");
		
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
				+ "FROM <%s> %n"
				+ "WHERE { %n"
				+ "	<%s> a sddo:StructuralDefinitionGroup ;"
				+ "		sddo:hasStructuralDefinition ?combinationURI ."
				+ "	?combinationURI a sddo:StructuralDefinition ; %n"
				+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
				+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
				+ "		sddo:isConflicting ?conflict ; %n"
				+ "		sddo:automaticResolutionState ?automaticResolutionState . %n"
				+ "} %n", Config.sdd_graph, uriSDD);
				
		String result = TripleStoreInterface.executeQueryWithAuthorization(queryDifferingSD, "XML");
		
		// Iterate over all differing combination URIs
		ResultSet resultSetDifferences = ResultSetFactory.fromXML(result);
		while (resultSetDifferences.hasNext()) {
			QuerySolution qs = resultSetDifferences.next();

			String currentDifferenceCombinationURI = qs.getResource("?combinationURI").toString();
			String currentTripleStateA = qs.getResource("?tripleStateA").toString();
			String currentTripleStateB = qs.getResource("?tripleStateB").toString();
			// Will return an integer value because virtuoso stores boolean internal as integer
			String currentConflictState = qs.getLiteral("?conflict").toString();
			// Convert integer to boolean to use it in the next query correctly
			if (currentConflictState.equals("1^^http://www.w3.org/2001/XMLSchema#integer")) {
				currentConflictState = "\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
			} else {
				currentConflictState = "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
			}
			String currentAutomaticResolutionState = qs.getResource("?automaticResolutionState").toString();
			
			String querySelectPart = "SELECT ?s ?p ?o %s %s %n";
			String sparqlQueryRevisionA = null;
			String sparqlQueryRevisionB = null;			
			
			// A
			if (currentTripleStateA.equals(SDDTripleState.ADDED.getSddRepresentation())) {
				// In revision A the triple was added
				querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
				sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleState.ADDED.getRpoRepresentation());
			} else if (currentTripleStateA.equals(SDDTripleState.DELETED.getSddRepresentation())) {
				// In revision A the triple was deleted
				querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
				sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleState.DELETED.getRpoRepresentation());
			} else if (currentTripleStateA.equals(SDDTripleState.ORIGINAL.getSddRepresentation())) {
				// In revision A the triple is original
				querySelectPart = String.format(querySelectPart, "?revisionA", "%s");
				sparqlQueryRevisionA = String.format(sparqlTemplateRevisionA, SDDTripleState.ORIGINAL.getRpoRepresentation());
			} else if (currentTripleStateA.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation())) {
				// In revision A the triple is not included
				querySelectPart = String.format(querySelectPart, "", "%s");
				sparqlQueryRevisionA = sparqlTemplateNotExistsRevisionA;
			}
			
			// B
			if (currentTripleStateB.equals(SDDTripleState.ADDED.getSddRepresentation())) {
				// In revision B the triple was added
				querySelectPart = String.format(querySelectPart, "?revisionB");
				sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleState.ADDED.getRpoRepresentation());
			} else if (currentTripleStateB.equals(SDDTripleState.DELETED.getSddRepresentation())) {
				// In revision B the triple was deleted
				querySelectPart = String.format(querySelectPart, "?revisionB");
				sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleState.DELETED.getRpoRepresentation());
			} else if (currentTripleStateB.equals(SDDTripleState.ORIGINAL.getSddRepresentation())) {
				// In revision B the triple is original
				querySelectPart = String.format(querySelectPart, "?revisionB");
				sparqlQueryRevisionB = String.format(sparqlTemplateRevisionB, SDDTripleState.ORIGINAL.getRpoRepresentation());
			} else if (currentTripleStateB.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation())) {
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
					
			String queryResult = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		
			// Iterate over all triples
			ResultSet resultSetTriples = ResultSetFactory.fromXML(queryResult);
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
				if (!currentTripleStateA.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation()) && !currentTripleStateB.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation())) {
					referencesAB = String.format(
							  "			rpo:referencesA <%s> ; %n"
							+ "			rpo:referencesB <%s> %n", qsQuery.getResource("?revisionA").toString(), 
																qsQuery.getResource("?revisionB").toString());
				} else if (currentTripleStateA.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation()) && !currentTripleStateB.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation())) {
					referencesAB = String.format(
							  "			rpo:referencesB <%s> %n", qsQuery.getResource("?revisionB").toString());
				} else if (!currentTripleStateA.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation()) && currentTripleStateB.equals(SDDTripleState.NOTINCLUDED.getSddRepresentation())) {
					referencesAB = String.format(
							  "			rpo:referencesA <%s> %n", qsQuery.getResource("?revisionA").toString());
				}
				
				String queryTriple = prefixes + String.format(
						  "INSERT INTO <%s> {%n"
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
						+ "}", graphNameDifferenceTripleModel, 
									currentDifferenceCombinationURI, 
									currentTripleStateA, 
									currentTripleStateB,
									currentConflictState,
									currentAutomaticResolutionState,
									subject, 
									predicate,
									object,
									referencesAB);
				
				TripleStoreInterface.executeQueryWithAuthorization(queryTriple, "XML");
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
	 * @return 
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String createMergedRevision(String graphName, String branchNameA, String branchNameB, String user, String commitMessage, String graphNameDifferenceTripleModel, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB, String uriSDD, MergeQueryTypeEnum type, String triples) throws HttpException, IOException {
		 
		// Create an empty temporary graph which will contain the merged full content
		String graphNameOfMerged = "RM-MERGED-TEMP-" + graphName;
		createNewGraph(graphNameOfMerged);
		
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
			TripleStoreInterface.executeQueryWithAuthorization(queryCopy, "HTML");
			
			// Get the triples from branch A which should be added to/removed from the merged revision
			String triplesToAdd = "";
			String triplesToDelete = "";
			
			// Get all difference groups
			String queryDifferenceGroup = prefixes + String.format(
					  "SELECT ?differenceCombinationURI ?automaticResolutionState ?tripleStateA ?tripleStateB ?conflict %n"
					+ "FROM <%s> %n"
					+ "WHERE { %n"
					+ "	?differenceCombinationURI a rpo:DifferenceGroup ; %n"
					+ "		sddo:automaticResolutionState ?automaticResolutionState ; %n"
					+ "		sddo:hasTripleStateA ?tripleStateA ; %n"
					+ "		sddo:hasTripleStateB ?tripleStateB ; %n"
					+ "		sddo:isConflicting ?conflict . %n"
					+ "}", graphNameDifferenceTripleModel);
	
			String resultDifferenceGroup = TripleStoreInterface.executeQueryWithAuthorization(queryDifferenceGroup, "XML");
			// Iterate over all difference groups
			ResultSet resultSetDifferenceGroups = ResultSetFactory.fromXML(resultDifferenceGroup);
			while (resultSetDifferenceGroups.hasNext()) {
				QuerySolution qsCurrentDifferenceGroup = resultSetDifferenceGroups.next();
	
				String currentDifferencGroupURI = qsCurrentDifferenceGroup.getResource("?differenceCombinationURI").toString();
				String currentDifferencGroupAutomaticResolutionState = qsCurrentDifferenceGroup.getResource("?automaticResolutionState").toString();
//				Currently not needed
//				String currentDifferencGroupTripleStateA = qsCurrentDifferenceGroup.getResource("?tripleStateA").toString();
//				String currentDifferencGroupTripleStateB = qsCurrentDifferenceGroup.getResource("?tripleStateB").toString();
				int currentDifferencGroupConflict = qsCurrentDifferenceGroup.getLiteral("?conflict").getInt();
				
				// Get all differences (triples) of current difference group
				String queryDifference = prefixes + String.format(
						  "SELECT ?s ?p ?o %n"
						+ "FROM <%s> %n"
						+ "WHERE { %n"
						+ "	<%s> a rpo:DifferenceGroup ; %n"
						+ "		rpo:hasDifference ?blankDifference . %n"
						+ "	?blankDifference a rpo:Difference ; %n"
						+ "		rpo:hasTriple ?triple . %n"
						+ "	?triple rdf:subject ?s . %n"
						+ "	?triple rdf:predicate ?p . %n"
						+ "	?triple rdf:object ?o . %n"
						+ "}", graphNameDifferenceTripleModel, currentDifferencGroupURI);
				
				String resultDifference = TripleStoreInterface.executeQueryWithAuthorization(queryDifference, "XML");
				// Iterate over all differences (triples)
				ResultSet resultSetDifferences = ResultSetFactory.fromXML(resultDifference);
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
					
					if (type.equals(MergeQueryTypeEnum.AUTO) || type.equals(MergeQueryTypeEnum.COMMON) || (type.equals(MergeQueryTypeEnum.WITH) && (currentDifferencGroupConflict == 0))) {
						// MERGE AUTO or common MERGE query
						if (currentDifferencGroupAutomaticResolutionState.equals(SDDTripleState.ADDED.getSddRepresentation())) {
							// Triple should be added
							triplesToAdd += subject + " " + predicate + " " + object + " . \n";
						} else {
							// Triple should be deleted
							triplesToDelete += subject + " " + predicate + " " + object + " . \n";
						}
					} else {
						// MERGE WITH query - conflicting triple
						Model model = readNTripleStringToJenaModel(triples);
						// Create ASK query which will check if the model contains the specified triple
						String queryAsk = String.format(
								  "ASK { %n"
								+ " %s %s %s %n"
								+ "}", subject, predicate, object);
						Query query = QueryFactory.create(queryAsk);
						QueryExecution qe = QueryExecutionFactory.create(query, model);
						boolean resultAsk = qe.execAsk();
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
		
		String addedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");
		
		queryAddedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfMerged, graphNameOfBranchB);

		addedTriples += TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");
		
		// Get all removed triples (concatenate all triples which are in A but not in MERGED and all triples which are in B but not in MERGED)
		String queryRemovedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfBranchA, graphNameOfMerged);
		
		String removedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");
		
		queryRemovedTriples = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { %n"
				+ "	GRAPH <%s> { ?s ?p ?o } %n"
				+ "	FILTER NOT EXISTS { %n"
				+ "		GRAPH <%s> { ?s ?p ?o } %n"
				+ "	} %n"
				+ "}", graphNameOfBranchB, graphNameOfMerged);
		
		removedTriples += TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");

		// Create list with the 2 predecessors - the order is important - fist item will specify the branch were the new merged revision will be created
		ArrayList<String> usedRevisionNumbers = new ArrayList<String>();
		usedRevisionNumbers.add(branchNameB);
		usedRevisionNumbers.add(branchNameA);
		return RevisionManagement.createNewRevision(graphName, addedTriples, removedTriples, user, commitMessage, usedRevisionNumbers);
	}

	
	/**
	 * Read turtle file to jena model.
	 * 
	 * @param path the file path to the turtle file
	 * @return the jena model
	 * @throws IOException
	 */
	public static Model readTurtleFileToJenaModel(String path) throws IOException {
		Model model = ModelFactory.createDefaultModel();
		InputStream is = new BufferedInputStream(new FileInputStream(path));
		model.read(is, null, "TURTLE");
		is.close();
		
		return model;
	}
	
	
	/**
	 * Read N-Triple string to jena model.
	 * 
	 * @param triples the triples in N-Triple serialization
	 * @return the model
	 * @throws IOException
	 */
	public static Model readNTripleStringToJenaModel(String triples) throws IOException {
		Model model = null;
		model = ModelFactory.createDefaultModel();
		InputStream is = new ByteArrayInputStream(triples.getBytes());
		model.read(is, null, "N-TRIPLE");
		is.close();
		
		return model;
	}
	
	
	/**
	 * Converts a jena model to N-Triple serialization. 
	 * 
	 * @param model the jena model
	 * @return the string which contains the N-Triples
	 * @throws UnsupportedEncodingException
	 */
	public static String convertJenaModelToNTriple(Model model) throws UnsupportedEncodingException {
			
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		model.write(os, "N-TRIPLES");
		
		return new String(os.toByteArray(), "UTF-8");
	}
	
	
	/**
	 * Create a new graph. When graph already exists it will be dropped.
	 * 
	 * @param graphname the graph name
	 * @throws IOException
	 * @throws HttpException
	 */
	private static void createNewGraph(String graphName) throws IOException, HttpException {
		logger.info("Create new graph with the name: " + graphName + ".");
		TripleStoreInterface.executeQueryWithAuthorization(String.format("DROP SILENT GRAPH <%s>", graphName), "HTML");
		TripleStoreInterface.executeQueryWithAuthorization(String.format("CREATE GRAPH  <%s>", graphName), "HTML");
	}
	
	
	// TODO upload on initialization RMO and SDDO so the client has the possibility to use the ontology data
	// TODO Add SPIN file to graph and also reference in RMO
	
}
