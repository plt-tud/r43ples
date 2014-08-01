package de.tud.plt.r43ples.management;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.tud.plt.r43ples.adminInterface.DiffResolveTool;
import de.tud.plt.r43ples.develop.examples.CreateExampleGraph;

/*
 * This class provides methods for merging branches.
 * *
 * @author Stephan Hensel
 *
 */
public class MergeManagement {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraph.class);
	/** The SPARQL prefixes. **/
	private final static String prefix_rmo = "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n";
	private final static String prefixes = "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ prefix_rmo
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
	
	
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
	private static String getCommonRevisionWithShortestPath(String revision1, String revision2) throws IOException, HttpException {
		
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
	private static void createRevisionProgress(LinkedList<String> list, String graphNameRevisionProgress, String uri) throws IOException, HttpException {
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
				fullGraphName = RevisionManagement.getFullGraphName(graphName, firstRevisionNumber);
			} catch (NoSuchElementException e) {
				// Create a temporary full graph
				RevisionManagement.generateFullGraphOfRevision(graphName, firstRevisionNumber, "RM-TEMP-REVISION-PROGRESS-FIRSTREVISION");
				fullGraphName = "RM-TEMP-REVISION-PROGRESS-FIRSTREVISION";
			}
			
			//TODO maybe replace rmo:revision with wasChangedByRevision (check PROV for possible solutions)
			// Create the initial content
			logger.info("Create the initial content.");
			String queryInitial = prefixes + String.format(	
				  "INSERT INTO <%s> { \n"
				+ "	<%s> a rmo:RevisionProgress; \n"
				+ "		rmo:original [ \n"
				+ "			rdf:subject ?s ; \n"
				+ "			rdf:predicate ?p ; \n"
				+ "			rdf:object ?o ; \n"
				+ "			rmo:revision \"%s\" \n"
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
						+ "	<%s> rmo:original ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:revision ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rmo:original ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:revision ?revision . \n"
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
						+ "	<%s> rmo:removed ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:revision ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rmo:removed ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:revision ?revision . \n"
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
						+ "	<%s> a rmo:RevisionProgress; \n"
						+ "		rmo:added [ \n"
						+ "			rdf:subject ?s ; \n"
						+ "			rdf:predicate ?p ; \n"
						+ "			rdf:object ?o ; \n"
						+ "			rmo:revision \"%s\" \n"
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
						+ "	<%s> rmo:original ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:revision ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rmo:original ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:revision ?revision . \n"
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
						+ "	<%s> rmo:added ?blank . \n"
						+ "	?blank rdf:subject ?s . \n"
						+ "	?blank rdf:predicate ?p . \n"
						+ "	?blank rdf:object ?o . \n"
						+ "	?blank rmo:revision ?revision . \n"
						+ "} \n"
						+ "WHERE { \n"
						+ "	SELECT ?blank ?s ?p ?o ?revision \n"
						+ "	WHERE { \n"
						+ "		{ \n"
						+ "			<%s> rmo:added ?blank . \n"
						+ "			?blank rdf:subject ?s . \n"
						+ "			?blank rdf:predicate ?p . \n"
						+ "			?blank rdf:object ?o . \n"
						+ "			?blank rmo:revision ?revision . \n"
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
						+ "	<%s> a rmo:RevisionProgress; \n"
						+ "		rmo:removed [ \n"
						+ "			rdf:subject ?s ; \n"
						+ "			rdf:predicate ?p ; \n"
						+ "			rdf:object ?o ; \n"
						+ "			rmo:revision \"%s\" \n"
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
	
	
	/**
	 * Creates the structural differences between two revision progresses.
	 * 
	 * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
	 * @param uriA the URI of the revision progress of branch A
	 * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
	 * @param uriB the URI of the revision progress of branch B
	 */
	private static void createStructuralDifferences(String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB) {

		// Tabelle in RDF darstellen, sodass es konfigurierbar ist, was Konflikte sind und was nicht
		// aus diesem Modell dann die unten stehenden Abfragen generieren
		// die Ergebnisse in einem neuen Graphen speichern
		
		// Example for added-added
//		PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#>
//			PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
//
//			SELECT ?s ?p ?o ?revisionA ?revisionB
//			WHERE {
//				GRAPH <RM-REVISION-PROGRESS-A-exampleGraph> {
//					<http://example/branch-A> rmo:removed ?blankA .
//						?blankA rdf:subject ?s .
//						?blankA rdf:predicate ?p .
//						?blankA rdf:object ?o .
//						?blankA rmo:revision ?revisionA .
//				}
//				GRAPH <RM-REVISION-PROGRESS-B-exampleGraph> {
//					<http://example/branch-B> rmo:added ?blankB .
//						?blankB rdf:subject ?s .
//						?blankB rdf:predicate ?p .
//						?blankB rdf:object ?o .
//						?blankB rmo:revision ?revisionB .
//				}
//			}
	}
	
	
	
	/**
	 * Create the conflicting triple model which contains all conflicting triples.
	 * 
	 * @param graphName the graph name
	 * @param graphNameRevisionProgressA the graph name of the revision progress of branch A
	 * @param uriA the URI of the revision progress of branch A
	 * @param graphNameRevisionProgressB the graph name of the revision progress of branch B
	 * @param uriB the URI of the revision progress of branch B
	 * @throws HttpException 
	 * @throws IOException 
	 */
	public static void createConflictingTripleModel(String graphName, String graphNameRevisionProgressA, String uriA, String graphNameRevisionProgressB, String uriB) throws IOException, HttpException {
		
		// Get all structural definitions which are generating conflicts
		String queryConflictingSD = String.format(
				  "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> %n"
				+ "PREFIX sdd:  <http://eatld.et.tu-dresden.de/sdd#> %n"
				+ "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#> %n"
				+ "SELECT ?combinationURI %n"
				+ "FROM <%s> %n"
				+ "WHERE { %n"
				+ "	?combinationURI a sddo:StructuralDefinition ; %n"
				+ "		sddo:isConflicting ?conflict . %n"
				+ "	FILTER (?conflict = \"true\"^^xsd:boolean) %n"
				+ "} %n", graphName + "-SDD");
		
		String result = TripleStoreInterface.executeQueryWithAuthorization(queryConflictingSD, "XML");
		
		// Get all conflicting combination URIs and save them in array list
		ArrayList<String> conflictingCombinationURIs = new ArrayList<String>();
		
		while (ResultSetFactory.fromXML(result).hasNext()) {
			QuerySolution qs = ResultSetFactory.fromXML(result).next();
			conflictingCombinationURIs.add(qs.getResource("?combinationURI").toString());
		}

		// Templates for revision A and B
		String sparqlTemplateRevisionA = String.format(
				  "	GRAPH <%s> { %n"
				+ "		<%s> %s ?blankA . %n"
				+ "			?blankA rdf:subject ?s . %n"
				+ "			?blankA rdf:predicate ?p . %n"
				+ "			?blankA rdf:object ?o . %n"
				+ "			?blankA rmo:revision ?revisionA . %n"
				+ "	} %n", graphNameRevisionProgressA, uriA);
		String sparqlTemplateRevisionB = String.format(
				  "	GRAPH <%s> { %n"
				+ "		<%s> %s ?blankB . %n"
				+ "			?blankB rdf:subject ?s . %n"
				+ "			?blankB rdf:predicate ?p . %n"
				+ "			?blankB rdf:object ?o . %n"
				+ "			?blankB rmo:revision ?revisionB . %n"
				+ "	} %n", graphNameRevisionProgressB, uriB);

		String sparqlTemplateNotExistsRevisionA = String.format(
				"FILTER NOT EXISTS {"
				+ "%s"
				+ "}", sparqlTemplateRevisionA);
		
		String sparqlTemplateNotExistsRevisionB = String.format(
				"FILTER NOT EXISTS {"
				+ "%s"
				+ "}", sparqlTemplateRevisionB);

		// Query for triple states of conflicting combination URI (A and B)
		
		
		
		
//		 "CONSTRUCT {?s ?p ?o} WHERE {" +
//			"  GRAPH <RM-MERGE-TEMP-MERGED> { ?s ?p ?o }" +
//			"  FILTER NOT EXISTS { GRAPH <RM-MERGE-TEMP-2> { ?s ?p ?o } }" +
//			" }";		
		
		
		
		
		// Create queries from template
		String sparqlQueryRevisionA = sparqlTemplateRevisionA;
		String sparqlQueryRevisionB = sparqlTemplateRevisionB;
		
		
		
		// Concatenated SPARQL query
		String query = String.format(
				prefixes
				+ "SELECT ?s ?p ?o ?revisionA ?revisionB %n"
				+ "WHERE { %n"
				+ "%s"
				+ "%s"
				+ "} %n", sparqlQueryRevisionA, sparqlQueryRevisionB);
		
		
				
				
//		PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#>
//			PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
//
//			SELECT ?s ?p ?o ?revisionB
//			WHERE {
//				FILTER NOT EXISTS {
//					GRAPH <RM-REVISION-PROGRESS-A-exampleGraph> {
//						<http://example/branch-A> rmo:removed ?blankA .
//							?blankA rdf:subject ?s .
//							?blankA rdf:predicate ?p .
//							?blankA rdf:object ?o .
//							?blankA rmo:revision ?revisionA .
//					}
//				}
//				GRAPH <RM-REVISION-PROGRESS-B-exampleGraph> {
//					<http://example/branch-B> rmo:added ?blankB .
//						?blankB rdf:subject ?s .
//						?blankB rdf:predicate ?p .
//						?blankB rdf:object ?o .
//						?blankB rmo:revision ?revisionB .
//				}
//			}
				
				
				

				
		
	}
	
	
	/**
	 * Read turtle file to jena model.
	 * 
	 * @param path the file path to the turtle file
	 * @return the model
	 * @throws IOException
	 */
	public static Model readTurtleFileToJenaModel(String path) throws IOException {
		Model model = null;
		model = ModelFactory.createDefaultModel();
		InputStream is = new BufferedInputStream(new FileInputStream(path));
		model.read(is, null, "TURTLE");
		is.close();
		
		return model;
	}
	
	
	/**
	 * Converts a jena model to N-Triple syntax. 
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
	 * Create the initial SDD for the specified graph under revision control.
	 * 
	 * @param graphName the graph name
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 * @throws HttpException 
	 */
	public static void createInitialSDD(String graphName) throws UnsupportedEncodingException, IOException, HttpException {
		// Load initial SDD into virtuoso
		logger.info("Create the initial SDD graph.");
		createNewGraph(graphName + "-SDD");		
		RevisionManagement.executeINSERT(graphName + "-SDD", convertJenaModelToNTriple(readTurtleFileToJenaModel("dataset/sdd.ttl")));

		// Create the reference
		logger.info("Insert reference to SDD graph into revision graph.");	
		String queryContent = String.format(
				  "<%s> rmo:referencedSDD <%s> .%n"
				  , graphName, graphName + "-SDD");
		
		String querySDD = String.format(
				prefix_rmo
				+ "INSERT IN GRAPH <%s> {%s}", Config.revision_graph, queryContent);
		TripleStoreInterface.executeQueryWithAuthorization(querySDD, "HTML");
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
