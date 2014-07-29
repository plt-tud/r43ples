package de.tud.plt.r43ples.develop.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

import de.tud.plt.r43ples.develop.examples.CreateExampleGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;

/**
 * Contains SPARQL queries which will be later used. 
 * 
 * @author Stephan Hensel
 *
 */
public class SparqlQueryTests {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraph.class);
	/** The endpoint. **/
	private static String endpoint = "http://localhost:8890/sparql-auth";
	/** The revision graph. **/
	private static String revisionGraph = "r43ples-revisions";
	/** The user credentials. **/
	private static UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("dba", "dba");
	/** The SPARQL prefixes. **/
	private final static String prefix_rmo = "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n";
	private final static String prefixes = "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ prefix_rmo
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
	
	
	/**
	 * Main entry point. Execute some tests.
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws IOException, HttpException, ConfigurationException {
		
		// Start service
		//Service.main(null);
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		
		logger.info("Common revision: \n" + getCommonRevisionWithShortestPath("exampleGraph-revision-1.0-1", "exampleGraph-revision-1.1-1"));
		
		logger.info("Common revision: \n" + getCommonRevisionWithShortestPath("exampleGraph-revision-1", "exampleGraph-revision-1.1-1"));
		
		
		getPathBetweenStartAndTargetRevision("exampleGraph-revision-1", "exampleGraph-revision-1.1-1");
		
		createRevisionProgress(getPathBetweenStartAndTargetRevision("exampleGraph-revision-1", "exampleGraph-revision-1.0-1"), "RM-REVISION-PROGRESS-0-exampleGraph", "http://example/branch-0");
		createRevisionProgress(getPathBetweenStartAndTargetRevision("exampleGraph-revision-1", "exampleGraph-revision-1.1-1"), "RM-REVISION-PROGRESS-1-exampleGraph", "http://example/branch-1");
	}
	
	
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
			+ "LIMIT 1", revisionGraph, revision1, revisionGraph, revision2, revisionGraph, revision1, revisionGraph, revision2);
		
		String result = executeQueryWithAuthorization(query, "XML");
		
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
			+ "}  ORDER BY ?step", revisionGraph, targetRevision, startRevision);
		
		String result = executeQueryWithAuthorization(query, "XML");
		
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
	 * Create a new graph.
	 * 
	 * @param graphname the graph name
	 * @throws IOException
	 * @throws HttpException
	 */
	private static void createNewGraph(String graphName) throws IOException, HttpException {
		logger.info("Create new graph with the name: " + graphName + ".");
		executeQueryWithAuthorization(String.format("DROP SILENT GRAPH <%s>", graphName), "HTML");
		executeQueryWithAuthorization(String.format("CREATE GRAPH  <%s>", graphName), "HTML");
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
		executeQueryWithAuthorization(String.format("DROP SILENT GRAPH <%s>", graphNameRevisionProgress), "HTML");
		executeQueryWithAuthorization(String.format("CREATE GRAPH  <%s>", graphNameRevisionProgress), "HTML");
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
				+ "}", revisionGraph, firstRevision, firstRevision);
			
			String result = executeQueryWithAuthorization(query, "XML");
			
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
			executeQueryWithAuthorization(queryInitial, "HTML");
			
			// Drop the temporary full graph
			logger.info("Drop the temporary full graph.");
			executeQueryWithAuthorization("DROP SILENT GRAPH <RM-TEMP-REVISION-PROGRESS-FIRSTREVISION>", "HTML");
			
			// Update content by current add and delete set - remove old entries
			while (iteList.hasNext()) {
				String revision = iteList.next();
				logger.info("Update content by current add and delete set of revision " + revision + " - remove old entries.");
				// Get the ADD and DELETE set URIs
				String addSetURI = getAddSetURI(revision, Config.revision_graph);
				String deleteSetURI = getDeleteSetURI(revision, Config.revision_graph);
				
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
					executeQueryWithAuthorization(queryRevision, "HTML");

				} else {
					//TODO Error management - is needed when a ADD or DELETE set is not referenced in the current implementation this error should not occur
					logger.error("ADD or DELETE set of " + revision + "does not exists.");
				}
				logger.info("Revision progress was created.");
				
			}
			
			
		}
		
	}
		
	
	/**
	 * Executes a SPARQL-query against an endpoint without authorization.
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeQueryWithoutAuthorization(String query, String format) throws IOException {
		URL url = null;
		
		url = new URL(endpoint+ "?query=" + URLEncoder.encode(query, "UTF-8") + "&format=" + URLEncoder.encode(format, "UTF-8") + "&timeout=0");
		logger.debug(url.toString());

		URLConnection con = null;
		InputStream in = null;
		con = url.openConnection();
		in = con.getInputStream();
	
		String encoding = con.getContentEncoding();
		encoding = (encoding == null) ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		return body;
		
	}
	
	
	/**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String executeQueryWithAuthorization(String query, String format) throws IOException, HttpException {
		String result = null;
		
		logger.info("Execute query on SPARQL endpoint:\n"+ query);
		DefaultHttpClient httpClient = new DefaultHttpClient();
	    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
			
	    HttpPost request = new HttpPost(endpoint);
		
		//set up HTTP Post Request (look at http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSSparqlProtocol for Protocol)
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("format",format));
		nameValuePairs.add(new BasicNameValuePair("query", query));
		
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		//Execute Query
		HttpResponse response = httpClient.execute(request);
		logger.debug("Statuscode: " + response.getStatusLine().getStatusCode());
		InputStreamReader in = new InputStreamReader(response.getEntity().getContent());
		result = IOUtils.toString(in);
		if (response.getStatusLine().getStatusCode() != Status.OK.getStatusCode()) {
			throw new HttpException(response.getStatusLine().toString()+"\n"+result);
		}
		
		return result;
	}
	
	
	/**
	 * Get the ADD set URI of a given revision URI.
	 * 
	 * @param revisionURI the revision URI
	 * @param revisionGraph the revision graph
	 * @return the ADD set URI, returns null when the revision URI does not exists or no ADD set is referenced by the revision URI
	 * @throws HttpException 
	 * @throws IOException 
	 */
	private static String getAddSetURI(String revisionURI, String revisionGraph) throws IOException, HttpException {
		String query = String.format(
			  "SELECT ?addSetURI \n"
			+ "FROM <%s> \n"
			+ "WHERE { \n"
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#deltaAdded> ?addSetURI . \n"
			+ "}", revisionGraph, revisionURI);
		
		String result = executeQueryWithAuthorization(query, "XML");
		
		if (ResultSetFactory.fromXML(result).hasNext()) {
			QuerySolution qs = ResultSetFactory.fromXML(result).next();
			return qs.getResource("?addSetURI").toString();
		} else {
			return null;
		}
	}
	
	
	/**
	 * Get the DELETE set URI of a given revision URI.
	 * 
	 * @param revisionURI the revision URI
	 * @param revisionGraph the revision graph
	 * @return the DELETE set URI, returns null when the revision URI does not exists or no DELETE set is referenced by the revision URI
	 * @throws HttpException 
	 * @throws IOException 
	 */
	private static String getDeleteSetURI(String revisionURI, String revisionGraph) throws IOException, HttpException {
		String query = String.format(
			  "SELECT ?deleteSetURI \n"
			+ "FROM <%s> \n"
			+ "WHERE { \n"
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#deltaRemoved> ?deleteSetURI . \n"
			+ "}", revisionGraph, revisionURI);
		
		String result = executeQueryWithAuthorization(query, "XML");
		
		if (ResultSetFactory.fromXML(result).hasNext()) {
			QuerySolution qs = ResultSetFactory.fromXML(result).next();
			return qs.getResource("?deleteSetURI").toString();
		} else {
			return null;
		}
	}	
}
