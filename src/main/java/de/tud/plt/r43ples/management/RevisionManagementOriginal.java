
package de.tud.plt.r43ples.management;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.FileUtils;

import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.merging.MergeManagement;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

/**
 * This class provides methods for interaction with graphs.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * 
 */
public class RevisionManagementOriginal {

	/** The logger. **/
	private static Logger logger = Logger.getLogger(RevisionManagementOriginal.class);
	

	/**
	 * create new revision with patch with addedUri and removedUri
	 *
	 * @param graphName
	 *            the graph name
	 * @param addSetGraphUri
	 *           uri of the data set of added triples as N-Triples
	 * @param deleteSetGraphUri
	 *           uri of the data set of removed triples as N-Triples
	 * @param user
	 *            the user name who creates the revision
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String createNewRevisionWithPatch(final String graphName, final String addSetGraphUri, final String deleteSetGraphUri,
			final String user, final String commitMessage, final String usedRevisionNumber) throws InternalErrorException {

		// TODO currently not working
//		RevisionDraft d = new RevisionDraft(graphName, usedRevisionNumber);
//		d.addSetURI = addSetGraphUri;
//		d.deleteSetURI = deleteSetGraphUri;
//		addNewRevisionFromChangeSet(user, commitMessage, d);
//		return d.newRevisionNumber;
		return null;
	}






	/** move the reference in the specified revision graph from the old revision to the new one
	 * 
	 * @param revisionGraph revision graph in the triplestore
	 * @param branchName name of branch
	 * @param revisionOld uri of the old revision
	 * @param revisionNew uri of the new revision
	 *  */
	public static void moveBranchReference(final String revisionGraph, final String branchName, final String revisionOld, final String revisionNew){
		// delete old reference
		String query = Config.prefixes	+ String.format(""
				+ "DELETE DATA { GRAPH <%1$s> { <%2$s> rmo:references <%3$s>. } };" 
				+ "INSERT DATA { GRAPH <%1$s> { <%2$s> rmo:references <%4$s>. } }",
				revisionGraph, branchName, revisionOld, revisionNew);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
	}
	
	
	/**
	 * updates all revisions between A and B and let them belong to the specified branch
	 * 
	 * @param revisionGraph 
	 * @param branch URI of the branch
	 * @param revisionStart uri of start revision
	 * @param revisionStop uri of last revision
	 * */
	public static void updateBelongsTo(final String revisionGraph, String branch, String revisionStart, String revisionStop ){
		LinkedList<String> revisionList =  MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, revisionStart, revisionStop);
		
		Iterator<String> riter = revisionList.iterator();
		while(riter.hasNext()) {
			String revision = riter.next();

			String query = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> { <%s> rmo:belongsTo <%s>. } };%n",
					revisionGraph, revision, branch);
			
			logger.debug("revisionlist info" + revision);
			logger.debug("updated info" + query);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);			
		}
	}
	
	/** copy graph of branchA to fullgraph of branchB
	 * @param sourceGraph uri of source graph
	 * @param targetGraph uri of target graph
	 * */
	public static void fullGraphCopy(String sourceGraph, String targetGraph) {	
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(
				"COPY GRAPH <" + sourceGraph + "> TO GRAPH <"+ targetGraph + ">");
	}

	/**
	 * Checks if graph exists in triple store. Works only when the graph is not
	 * empty.
	 * 
	 * @param graphName
	 *            the graph name
	 * @return boolean value if specified graph exists and contains at least one
	 *         triple elsewhere it will return false
	 */
	public static boolean checkGraphExistence(final String graphName){
		String query = "ASK { GRAPH <" + graphName + "> {?s ?p ?o} }";
		return TripleStoreInterfaceSingleton.get().executeAskQuery(query);
	}

	/**
	 * Creates the whole revision from the add and delete sets of the
	 * predecessors. Saved in graph tempGraphName.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionName
	 *            revision number or revision name to build content for
	 * @param tempGraphName
	 *            the graph where the temporary graph is stored
	 * @throws InternalErrorException 
	 */
	public static void generateFullGraphOfRevision(final String graphName, final String revisionName,
			final String tempGraphName) throws InternalErrorException {
		logger.info("Rebuild whole content of revision " + revisionName + " of graph <" + graphName
				+ "> into temporary graph <" + tempGraphName + ">");
		RevisionGraph graph = new RevisionGraph(graphName);
		Revision revision = graph.getRevision(revisionName);

		// Create temporary graph
		TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + tempGraphName + ">");
		TripleStoreInterfaceSingleton.get().executeUpdateQuery("CREATE GRAPH <" + tempGraphName + ">");

		// Create path to revision

		LinkedList<Revision> list = PathCalculationSingleton.getInstance().getPathToRevisionWithFullGraph(graph, revision)
				.getRevisionPath();

		// Copy branch to temporary graph
		String number = list.removeLast().getRevisionIdentifier();
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(
				"COPY GRAPH <" + graph.getReferenceGraph(number) + "> TO GRAPH <"
						+ tempGraphName + ">");

		while (!list.isEmpty()) {
			// add- und delete-sets could be extracted from revision tree information
			// hard coded variant is faster
			String graph_removed = graphName + "-deleteSet-"+ number;
			String graph_added   = graphName + "-addSet-"+ number;
			// Add data to temporary graph
			if (RevisionManagementOriginal.checkGraphExistence(graph_removed))
				TripleStoreInterfaceSingleton.get().executeUpdateQuery("ADD GRAPH <" + graph_removed + "> TO GRAPH <" + tempGraphName + ">");
			// Remove data from temporary graph (no opposite of SPARQL ADD available)
			if (RevisionManagementOriginal.checkGraphExistence(graph_added))
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(  "DELETE { GRAPH <" + tempGraphName+ "> { ?s ?p ?o.} }"
														+ "WHERE  { GRAPH <" + graph_added	+ "> { ?s ?p ?o.} }");
			Revision first = list.removeLast();
			if (first!=null)
				number = first.getRevisionIdentifier();
		}

	}




	/**
	 * Split huge INSERT statements into separate queries of up to 500 triple
	 * statements.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param dataSetAsNTriples
	 *            the data to insert as N-Triples
	 */
	public static void executeINSERT(final String graphName, final String dataSetAsNTriples) {

		String insertQueryTemplate = "INSERT DATA { GRAPH <%s> { %s } }";
		
		splitAndExecuteBigQuery(graphName, dataSetAsNTriples, insertQueryTemplate);
	}
	
	/**
	 * Split huge DELETE statements into separate queries of up to fifty triple statements.
	 * 
	 * @param graphName the graph name
	 * @param dataSetAsNTriples the data to insert as N-Triples 
	 */
	public static void executeDELETE(final String graphName, final String dataSetAsNTriples) {

		String deleteQueryTemplate = "DELETE DATA { GRAPH <%s> { %s } }";
		
		splitAndExecuteBigQuery(graphName, dataSetAsNTriples, deleteQueryTemplate);
	}
	
	
	
	public static void splitAndExecuteBigQuery(final String graphName, final String dataSetAsNTriples, final String template){
		final int MAX_STATEMENTS = 500;
		String[] lines = dataSetAsNTriples.split("\n");
		int counter = 0;
		StringBuilder insert = new StringBuilder();
		
		for (int i=0; i < lines.length; i++) {
			insert.append(lines[i]);
			insert.append("\n");
			counter++;
			if (counter == MAX_STATEMENTS-1) {
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(template, graphName, insert));
				counter = 0;
				insert = new StringBuilder();
			}
		}

		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(template, graphName, insert));
	}
	

	

	/**
	 * Download complete revision information of R43ples from SPARQL endpoint.
	 * Provide only information from specified graph if not null
	 * 
	 * @param graphName
	 *            provide only information from specified graph (if not NULL)
	 * @param format
	 *            serialization of the RDF model
	 * @return String containing the RDF model in the specified serialization
	 */
	public static String getRevisionInformation(final String graphName, final String format) {
		String sparqlQuery;
		
		if (graphName.equals("")) {
			sparqlQuery = Config.prefixes + String.format(""
					+ "CONSTRUCT { ?s ?p ?o. }"
					+ "WHERE { "
					+ "	GRAPH <%s> { ?graph a rmo:Graph; rmo:hasRevisionGraph ?revisiongraph.}"
					+ "	GRAPH ?revisionGraph {?s ?p ?o.}"
					+ "}", Config.revision_graph);
			return TripleStoreInterfaceSingleton.get().executeConstructQuery(sparqlQuery, format);
		} else {
			RevisionGraph graph = new RevisionGraph(graphName);
			return graph.getContentOfRevisionGraph(format);
		}
		
	}
	
	/**
	 * Get the content of this revision graph by execution of CONSTRUCT.
	 * 
	 * @param graphName the graphName
	 * @param format RDF serialization format which should be returned
	 * @return the constructed graph content as specified RDF serialization format
	 */
	public static String getContentOfGraph(final String graphName, final String format) {
		String query = Config.prefixes + String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { GRAPH <%s> {?s ?p ?o} }", graphName);
		return TripleStoreInterfaceSingleton.get().executeConstructQuery(query, format);		
	}


	/**
	 * Get revised graphs in R43ples.
	 * 
	 * @param format
	 *            serialization of the response
	 * @return String containing the SPARQL response in specified format
	 */
	public static String getRevisedGraphsSparql(final String format) {
		String sparqlQuery = Config.prefixes
				+ String.format("" 
						+ "SELECT DISTINCT ?graph " 
						+ "WHERE {"
						+ " GRAPH <%s> { ?graph a rmo:Graph. }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery, format);
	}
	
	
	/**
	 * Get revised graphs in R43ples.
	 * 
	 * @return result set
	 */
	public static ResultSet getRevisedGraphs() {
		String sparqlQuery = Config.prefixes
				+ String.format("" 
						+ "SELECT DISTINCT ?graph " 
						+ "WHERE {"
						+ " GRAPH <%s> {  ?graph a rmo:Graph }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
	}
	

	/**
	 * Get revised graphs in R43ples as list of string.
	 * 
	 * @return list of strings containing the revised graphs of R43ples
	 */
	public static ArrayList<String> getRevisedGraphsList() {
		ArrayList<String> list = new ArrayList<String>();
		ResultSet results = getRevisedGraphs();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			list.add(qs.getResource("graph").toString());
		}
		return list;
	}
	
	

	/**
	 * @param user
	 *            name as string
	 * @return URI of person
	 */
	public static String getUserName(final String user) {
		// When user does not already exists - create new

		String query = Config.prefixes
				+ String.format("SELECT ?personUri { GRAPH <%s>  { " + "?personUri a prov:Person;"
						+ "  rdfs:label \"%s\"." + "} }", Config.revision_graph, user);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (results.hasNext()) {
			logger.debug("User " + user + " already exists.");
			QuerySolution qs = results.next();
			return qs.getResource("?personUri").toString();
		} else {
			String personUri = null;
			try {
				personUri = "http://eatld.et.tu-dresden.de/persons/" + URLEncoder.encode(user, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			logger.debug("User does not exists. Create user " + personUri + ".");
			query = Config.prefixes
					+ String.format("INSERT DATA { GRAPH <%s> { <%s> a prov:Person; rdfs:label \"%s\". } }",
							Config.revision_graph, personUri, user);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
			return personUri;
		}
	}
	



	

	/**
	 * Get the ADD set URI of a given revision URI.
	 * 
	 * @param revisionURI the revision URI
	 * @param revisionGraph the revision graph
	 * @return the ADD set URI, returns null when the revision URI does not exists or no ADD set is referenced by the revision URI
	 */
	public static String getAddSetURI(String revisionURI, String revisionGraph) {
		String query = String.format(
			  "SELECT ?addSetURI %n"
			+ "WHERE { GRAPH <%s> {%n"
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#addSet> ?addSetURI . %n"
			+ "} }", revisionGraph, revisionURI);
		
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
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
	 */
	public static String getDeleteSetURI(String revisionURI, String revisionGraph) {
		String query = String.format(
			  "SELECT ?deleteSetURI %n"
		    + "WHERE { GRAPH <%s> {%n"
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#deleteSet> ?deleteSetURI . %n"
			+ "} }", revisionGraph, revisionURI);
		
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			return qs.getResource("?deleteSetURI").toString();
		} else {
			return null;
		}
	}
	
	
	
	/** Creates an RDF description for the revision tree of the graphs specified in the given SPARQL query
	 * @param query SPARQL query
	 * @return RDF string containing information for graphs specified in query 
	 */
	public static String getResponseHeaderFromQuery(String query) {
		final Pattern patternGraph = Pattern.compile(
				"(GRAPH|FROM|INTO)\\s*<(?<graph>[^>]*)>\\s*",
				Pattern.CASE_INSENSITIVE);
		
		StringBuilder graphNames = new StringBuilder();
		Matcher m = patternGraph.matcher(query);
		m.find();
		while (!m.hitEnd()) {
			String graphName = m.group("graph");
			graphNames.append("<"+graphName+">");
			m.find();
			if (!m.hitEnd())
				graphNames.append(", ");			
		}
		String names = graphNames.toString();
		String result = getResponseHeader(names);
		return result;
		
	}
	
	public static String getResponseHeader(String graphList) {
		String queryConstruct = Config.prefixes + String.format(
				  "CONSTRUCT {"
				+ " ?ref a ?type;"
				+ "		rdfs:label ?label;"
				+ "		rmo:references ?rev."
				+ " ?rev rmo:revisionNumber ?number . %n"
				+ "} %n"
				+ "WHERE {"
				+ " GRAPH <%s> {"
				+ "   ?graph a rmo:Graph; rmo:hasRevisionGraph ?revisionGraph."
				+ "   FILTER (?graph IN (%s))"
				+ " }"
				+ " GRAPH ?revisionGraph { "
				+ " ?ref a ?type;"
				+ "		rdfs:label ?label;%n"
				+ "		rmo:references ?rev."
				+ " ?rev rmo:revisionNumber ?number . %n"
				+ "FILTER (?type IN (rmo:Tag, rmo:Master, rmo:Branch)) %n"
				+ "} }", Config.revision_graph, graphList);
		String header = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryConstruct, FileUtils.langTurtle);
		return header;
	}
		
}

