package de.tud.plt.r43ples.webservice;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;


/**
 * Provides SPARQL endpoint via [host]:[port]/r43ples/
 * Supplies version information, service description as well as SPARQL queries.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * 
 */
@Path("r43ples")
public class Endpoint {

	@Context private UriInfo uriInfo;
	private final static Logger logger = Logger.getLogger(Endpoint.class);

	
	/**
	 * Provide revision information about R43ples system.
	 * @param graph Provide only information about this graph (if not null)
	 * @return RDF model of revision information
	 */
	@Path("revisiongraph")
	@GET
	@Produces({"text/turtle", "application/rdf+xml", MediaType.APPLICATION_JSON})
	public final String getRevisionGraph(
			@HeaderParam("Accept") final String format_header, 
			@QueryParam("format") @DefaultValue("text/turtle") final String format_query,
			@QueryParam("graph") @DefaultValue("") final String graph) {
		logger.info("Get Revision Graph");
		String format = (format_query!=null) ? format_query : format_header;
		logger.info("format: " +  format);
		
		try {
			return RevisionManagement.getRevisionInformation(graph, format);
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	/**
	 * Provide information about revised graphs
	 * @param graph Provide only information about this graph (if not null)
	 * @return RDF model of revision information
	 */
	@Path("getRevisedGraphs")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public final String getRevisedGraphs(
			@HeaderParam("Accept") final String format_header, 
			@QueryParam("format") @DefaultValue("application/json") final String format_query) {
		logger.info("Get Revised Graphs");
		String format = (format_query!=null) ? format_query : format_header;
		logger.info("format: " +  format);
		try {
			return RevisionManagement.getRevisedGraphs(format);
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	
	private final Pattern patternSelectQuery = Pattern.compile(
			"(?<type>SELECT|ASK).*WHERE\\s*\\{(?<where>.*)\\}", 
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternSelectFromPart = Pattern.compile(
			"FROM\\s*<(?<graph>.*)>\\s*REVISION\\s*\"(?<revision>.*)\"",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>.*)>", 
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternUpdateRevisionQuery =  Pattern.compile(
			"(?<action>FROM|INTO|GRAPH)\\s*<(?<graph>.*)>\\s*REVISION\\s*\"(?<revision>.*)\"",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternCreateGraph =  Pattern.compile(
			"CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>.*)>",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternDropGraph =  Pattern.compile(
			"DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>.*)>",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternBranchOrTagQuery =  Pattern.compile(
			"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>.*)>\\s*REVISION\\s*\"(?<revision>.*)\"\\s*TO\\s*\"(?<name>.*)\"",
			Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>.*)\"",
			Pattern.CASE_INSENSITIVE);
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>.*)\"",
			Pattern.CASE_INSENSITIVE);
	
	/**
	 * HTTP GET interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Provides HTML form if no query is specified and HTML is requested
	 * Provides Service Description if no query is specified and RDF representation is requested
	 * 
	 * @param formatHeader format specified in the HTTP header
	 * @param formatQuery format specified in the HTTP parameters
	 * @param sparqlQuery the SPARQL query
	 * @return the response
	 */
	@Path("sparql")
	@GET
	@Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle"})
	public final Response sparql(@HeaderParam("Accept") final String formatHeader, 
			@QueryParam("format") final String formatQuery, 
			@QueryParam("query") @DefaultValue("") final String sparqlQuery) {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		logger.info("SPARQL requested with format: " +  format);
		if (sparqlQuery.equals(""))	{
			if (format.contains("text/html")) {
				logger.info("SPARQL form requested");
				File fileToSend = new File("resources/webapp/index.html");
				return Response.ok(fileToSend, "text/html").build();
			} else {
				return getServiceDescription(format);
			}
		} else {
			logger.info("SPARQL query was requested. Query: " + sparqlQuery);
			Response response = null;
			try {
				String sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");
				if (patternSelectQuery.matcher(sparqlQueryDecoded).find()) {
					return produceSelectResponse(sparqlQueryDecoded, format);
				}
				if (patternUpdateQuery.matcher(sparqlQueryDecoded).find()) {
					return produceInsertDeleteResponse(sparqlQueryDecoded, format);
				}
				if (patternCreateGraph.matcher(sparqlQueryDecoded).find()) {
					return produceCreateGraphResponse(sparqlQueryDecoded, format);
				}
				if (patternDropGraph.matcher(sparqlQueryDecoded).find()) {
					return produceDropGraphResponse(sparqlQueryDecoded, format);
				}
				if (patternBranchOrTagQuery.matcher(sparqlQueryDecoded).find()) {
					return produceBranchOrTagResponse(sparqlQueryDecoded, format);
				}
			} catch (HttpException | IOException e) {
				e.printStackTrace();
				throw new InternalServerErrorException(e.getMessage());
			}	
			return response;
		}
	}
	
	@Path("createTestDataset")
	@GET
	public final String createTestDataset() {
		ArrayList<String> list = new ArrayList<String>();		
		String graphName = "http://test.com/r43ples-dataset";
		try {
			RevisionManagement.putGraphUnderVersionControl(graphName);
			
			list.add("0");
			RevisionManagement.createNewRevision(graphName, 
					ResourceManagement.getContentFromResource("samples/test-delta-added-1.nt"), 
					ResourceManagement.getContentFromResource("samples/test-delta-removed-1.nt"),
					"test_user", "test commit message 1", list);			
			
			list.remove("0");
			list.add("1");
			RevisionManagement.createNewRevision(graphName, 
					ResourceManagement.getContentFromResource("samples/test-delta-added-2.nt"), 
					ResourceManagement.getContentFromResource("samples/test-delta-removed-2.nt"),
					"test_user", "test commit message 2", list);		
			
			list.remove("1");
			list.add("2");
			RevisionManagement.createNewRevision(graphName, 
					ResourceManagement.getContentFromResource("samples/test-delta-added-3.nt"), 
					ResourceManagement.getContentFromResource("samples/test-delta-removed-3.nt"),
					"test_user", "test commit message 3", list);		
			
			list.remove("2");
			list.add("3");
			RevisionManagement.createNewRevision(graphName, 
					ResourceManagement.getContentFromResource("samples/test-delta-added-4.nt"), 
					ResourceManagement.getContentFromResource("samples/test-delta-removed-4.nt"),
					"test_user", "test commit message 4", list);
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}	
		
		return "Test dataset successfully created in graph <"+graphName+">";
	}
	

	/**
	 * Provides the SPARQL Endpoint description of the original sparql endpoint with the additional R43ples feature (sd:feature)
	 * and replaces URIs.
	 * @param format serialisation format of the service description
	 * @return Extended Service Description
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private Response getServiceDescription(final String format) {
		logger.info("Service Description requested");
		DefaultHttpClient client = new DefaultHttpClient();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(Config.sparql_user, Config.sparql_password);
	    client.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null), credentials);
	    HttpGet request = new HttpGet(Config.sparql_endpoint);
		request.setHeader("Accept", "text/turtle");
		try {
			HttpResponse response = client.execute(request);
	
			Model model = ModelFactory.createDefaultModel();
			model.read(response.getEntity().getContent(), null, Lang.TURTLE.getName());
			
			UpdateRequest srequest = UpdateFactory.create("PREFIX sd:    <http://www.w3.org/ns/sparql-service-description#>"
					+ "DELETE { ?s ?p ?o.} "
					+ "INSERT { <"+uriInfo.getAbsolutePath()+"> ?p ?o.} "
					+ "WHERE { ?s a sd:Service; ?p ?o.} ");
			UpdateAction.execute(srequest, model);
			srequest = UpdateFactory.create("PREFIX sd:    <http://www.w3.org/ns/sparql-service-description#>"
					+ "DELETE { ?s sd:url ?url.} "
					+ "INSERT { ?s sd:url <"+uriInfo.getAbsolutePath()+">.} "
					+ "WHERE { ?s sd:url ?url.} ");
			UpdateAction.execute(srequest, model); 
			srequest = UpdateFactory.create("PREFIX sd:    <http://www.w3.org/ns/sparql-service-description#>"
					+ "INSERT { ?s sd:feature sd:r43ples.} "
					+ "WHERE { ?s a sd:Service.} ");
			UpdateAction.execute(srequest, model); 
			
			StringWriter sw = new StringWriter();
			model.write(sw,  RDFLanguages.nameToLang(format).getName());
			return Response.ok().entity(sw.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	
	/**
	 * Produce the response for a SELECT SPARQL query.
	 * can handle multiple graphs
	 * 
	 * @param query the SPARQL query
	 * @param format the result format
	 * @return the response with HTTP header for every graph (revision number and MASTER revision number)
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	private Response produceSelectResponse(final String query, final String format) throws HttpException, IOException {

		String queryM = query;
		ResponseBuilder responseBuilder = Response.ok();
			
		Matcher m = patternSelectFromPart.matcher(queryM);
		boolean found = false;
		while (m.find()) {
			found = true;
		    String graphName = m.group("graph");
		    String revisionNumber = m.group("revision");
		    
		    // if no revision number is declared use the MASTER as default
		    if (revisionNumber == null) {
		    	revisionNumber = "MASTER";
		    }
		    String headerRevisionNumber;
		    if (revisionNumber.equalsIgnoreCase("MASTER")) {
				// Respond with MASTER revision - nothing to be done - MASTER revisions are already created in the named graphs				
		    	headerRevisionNumber = "MASTER";
			} else {
				String newGraphName;
				if (RevisionManagement.isBranch(graphName, revisionNumber)) {
					newGraphName = RevisionManagement.getFullGraphName(graphName, revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision must be generated - saved in graph <RM-TEMP-graphName>
					newGraphName = "RM-TEMP-" + graphName;
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
				queryM = m.replaceFirst("FROM <" + newGraphName + ">");
				m = patternSelectFromPart.matcher(queryM);
				headerRevisionNumber = revisionNumber;
			}
		    // Respond with specified revision
			responseBuilder.header(graphName + "-revision-number", headerRevisionNumber);
		    responseBuilder.header(graphName + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		}
		if (!found) {
			logger.info("No R43ples SELECT query: "+queryM);
		}
		String response = TripleStoreInterface.executeQueryWithAuthorization(queryM, format);
		return responseBuilder.entity(response).type(format).build();
	}
	
	
	/**
	 * Produce the response for a INSERT or DELETE SPARQL query.
	 * 
	 * @param query the SPARQL query
	 * @param format the result format
	 * @return the response with HTTP header for every graph (revision number and MASTER revision number)
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private Response produceInsertDeleteResponse(final String query, final String format) throws HttpException, IOException {
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));		
		logger.info("Update detected");
		
		// General variables
		String user = extractUser(query);
		String commitMessage = extractCommitMessage(query);
		// if no commit message is declared use default message
		if (commitMessage == null) {
			commitMessage = "No commit message specified.";
		}
		Matcher m = patternUpdateRevisionQuery.matcher(query);
		boolean found = m.find();
		
		if (!found) {
			throw new InternalServerErrorException("Query contain errors:\n" + query);
		}
		
	    String graphName = m.group("graph");
	    String revisionName = m.group("revision"); //can contain revision numbers or reference names
	    
	    if (!RevisionManagement.isBranch(graphName, revisionName)) {
			throw new InternalServerErrorException("Revision is not referenced by branch");
		}
	    
	    String referenceFullGraph = RevisionManagement.getFullGraphName(graphName, revisionName);

	    // Create the temporary graph and fill with reference full graph
	    String graphUpdateTemp = "RM-UPDATE-TEMP-" + graphName;
	    TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <" + graphUpdateTemp + ">", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <" + graphUpdateTemp + ">", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("COPY <" + referenceFullGraph + "> TO <" + graphUpdateTemp + ">", "HTML");
		
		// Replace graph name in SPARQL query 
		// TODO: should also work with different graph names and revisions in one query
		String query_replaced = m.replaceAll("${action} <" + graphUpdateTemp + ">");
		logger.info("query replaced: " + query_replaced);

		
		// Execute SPARQL query
		responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(query_replaced, format)); 
			
		// Create deltas for new revision
		// Get all added triples
		String queryAddedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
									"  GRAPH <" + graphUpdateTemp + "> { ?s ?p ?o }" +
									"  FILTER NOT EXISTS { GRAPH <" + referenceFullGraph + "> { ?s ?p ?o } }" +
									" }";
		String addedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");
		
		// Get all removed triples
		String queryRemovedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
										"  GRAPH <" + referenceFullGraph + "> { ?s ?p ?o }" +
										"  FILTER NOT EXISTS { GRAPH <" + graphUpdateTemp + "> { ?s ?p ?o } }" +
										" }";
		String removedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(revisionName);
					
		// Create new revision
		String newRevisionNumber = RevisionManagement.createNewRevision(graphName, addedTriples, removedTriples, user, commitMessage, list);
		
		// Respond with next revision number
    	responseBuilder.header(graphName + "-revision-number", newRevisionNumber);
		responseBuilder.header(graphName + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		logger.info("Respond with new revision number " + newRevisionNumber + ".");
	
		Response response = responseBuilder.build();
		
		return response;
	}

	
		
	/** Creates a graph under version control for command "CREATE GRAPH <?>"
	 * 
	 * @param query the SPARQL query
	 * @param format the result format
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private Response produceCreateGraphResponse(final String query,
			final String format) throws IOException, HttpException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Graph creation detected");
		
		Matcher m = patternCreateGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
		    String graphName = m.group("graph");
		    // Execute SPARQL query
		    String querySparql = m.group();
			responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(querySparql, format));
		    // Add R43ples information
		    RevisionManagement.putGraphUnderVersionControl(graphName);
	    	responseBuilder.header(graphName + "-revision-number", 0);
			responseBuilder.header(graphName + "-revision-number-of-MASTER", 0);
		}
		if (!found) {
			throw new InternalServerErrorException("Query doesn't contain a correct CREATE query:\n"+query);
		}
		return responseBuilder.build();
	}
	
	
	/** Drops a graph under version control for command "DROP (SILENT) GRAPH <?>"
	 * 
	 * @param query the SPARQL query
	 * @param format the result format
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private Response produceDropGraphResponse(final String query,
			final String format) throws IOException, HttpException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		
		// Clear R43ples information for specified graphs
		Matcher m = patternDropGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
		    String graphName = m.group("graph"); 
		    RevisionManagement.purgeGraph(graphName);
		}
		if (!found) {
			throw new InternalServerErrorException("Query contain errors:\n"+query);
		}
		responseBuilder.status(Response.Status.OK);
		responseBuilder.entity("Successful: "+ query);
		return responseBuilder.build();
	}
	
	/** Creates a tag or a branch for a specific graph and revision.
	 * Using command "TAG GRAPH <?> REVISION "rev" TO "tag"
	 * Using command "BRANCH GRAPH <?> REVISION "rev" TO "tag"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	private Response produceBranchOrTagResponse(final String sparqlQuery,
			final String format) throws HttpException, IOException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Tag or branch creation detected");
		String user = extractUser(sparqlQuery);
		String commitMessage = extractCommitMessage(sparqlQuery);
		
		// Add R43ples information
		Matcher m = patternBranchOrTagQuery.matcher(sparqlQuery);
		
		boolean foundEntry = false;
		while (m.find()) {
			foundEntry = true;
			String action = m.group("action");
		    String graphName = m.group("graph");
		    String revisionNumber = m.group("revision");
		    String referenceName = m.group("name");
		    try {
			    if (action.equals("TAG")) {
					RevisionManagement.createReference("tag", graphName, revisionNumber, referenceName, user, commitMessage);
				} else if (action.equals("BRANCH")) {
					RevisionManagement.createReference("branch", graphName, revisionNumber, referenceName, user, commitMessage);
				} else {
					throw new InternalServerErrorException("Error in query: " + sparqlQuery);
				}
			} catch (IdentifierAlreadyExistsException e) {
				responseBuilder = Response.status(Response.Status.CONFLICT);
			}
		    	
	    	// Respond with next revision number
		    responseBuilder.header(graphName + "-revision-number", RevisionManagement.getRevisionNumber(graphName, referenceName));
	    	responseBuilder.header(graphName + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		    
		}
		if (!foundEntry) {
			throw new InternalServerErrorException("Error in query: " + sparqlQuery);
		}
		
		return responseBuilder.build();
	}


	/** Extracts user out of query
	 * @param query
	 * @return user mentioned in a query
	 * @throws InternalServerErrorException
	 */
	private String extractUser(final String query) {
		Matcher userMatcher = patternUser.matcher(query);
		if (userMatcher.find()) {
			return userMatcher.group("user");
		} else {
			throw new InternalServerErrorException("No user specified");
		}
	}
	
	/** Extracts commit message out of query
	 * @param query
	 * @throws InternalServerErrorException
	 */
	private String extractCommitMessage(final String query) {
		Matcher matcher = patternCommitMessage.matcher(query);
		if (matcher.find()) {
			return matcher.group("message");
		} else {
			throw new InternalServerErrorException("No commit message specified");
		}
	}
}
