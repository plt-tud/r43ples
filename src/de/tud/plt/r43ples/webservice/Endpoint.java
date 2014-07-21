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
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;


/**
 * Provides SPARQL endpoint via [host]:[port]/endpoint/
 * Supplies update and query.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * 
 */
@Path("r43ples")
public class Endpoint {

	@Context UriInfo uriInfo;
	static Logger logger = Logger.getLogger(Endpoint.class);


	/**
	 * @return service version
	 */
	@Path("version")
	@GET
	public String version() {
		logger.info("Version queried.");
		return "Version 0.8";
	}
	
	/**
	 * Provide revision information about R43ples system
	 * @param graph Provide only information about this graph (if not null)
	 * @return RDF model of revision information
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	@Path("revisiongraph")
	@GET
	@Produces({"text/turtle", "application/rdf+xml", MediaType.APPLICATION_JSON})
	public String getRevisionGraph(
			@HeaderParam("Accept") String format_header, 
			@QueryParam("format") @DefaultValue("text/turtle") String format_query,
			@QueryParam("graph") @DefaultValue("") String graph) {
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
	 * HTTP GET interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Provides HTML form if no query is specified and HTML is requested
	 * Provides Service Description if no query is specified and RDF representation is requested
	 * 
	 * @param format the format
	 * @param sparqlQuery the SPARQL query
	 * @return the response
	 */
	@Path("sparql")
	@GET
	@Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle"})
	public Response sparql(@HeaderParam("Accept") String format_header, 
			@QueryParam("format") String format_query, 
			@QueryParam("query") @DefaultValue("") String sparqlQuery) {
		String format = (format_query!=null) ? format_query : format_header;
		logger.info("SPARQL requested with format: " +  format);
		if (sparqlQuery.equals(""))
		{
			if (format.contains("text/html")){
				logger.info("SPARQL form requested");
				File fileToSend = new File("resources/index.html");
				return Response.ok(fileToSend, "text/html").build();
			} else {
				return getServiceDescription(format);
			}
		}
		else {
			logger.info("SPARQL query was requested. Query: " + sparqlQuery);
			Response response = null;
			try {
				sparqlQuery = URLDecoder.decode(sparqlQuery, "UTF-8");
				if (sparqlQuery.toUpperCase().contains("SELECT") || sparqlQuery.toUpperCase().contains("ASK")) {
					response = produceSelectResponse(sparqlQuery, format);
				} else if (sparqlQuery.toUpperCase().contains("INSERT") || sparqlQuery.toUpperCase().contains("DELETE")) {
					response = produceInsertDeleteResponse(sparqlQuery, format);
				} else if (sparqlQuery.toUpperCase().contains("CREATE")) {
					response = produceCreateGraphResponse(sparqlQuery, format);
				} else if (sparqlQuery.toUpperCase().contains("DROP")) {
					response = produceDropGraphResponse(sparqlQuery, format);
				} else if (sparqlQuery.toUpperCase().contains("TAG") || sparqlQuery.toUpperCase().contains("BRANCH")) {
					response = produceBranchOrTagResponse(sparqlQuery, format);
				}
			} catch (HttpException | IOException e) {
				e.printStackTrace();
				throw new InternalServerErrorException(e.getMessage());
			}	
			return response;
		}
	}
	

	/**
	 * Provides the SPARQL Endpoint description of the original sparql endpoint with the additional R43ples feature (sd:feature)
	 * and replaces URIs.
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private Response getServiceDescription(String format) {
		logger.info("Service Description requested");
		DefaultHttpClient client =new DefaultHttpClient();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(Config.sparql_user, Config.sparql_password);
	    client.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null), credentials);
	    HttpGet request = new HttpGet(Config.sparql_endpoint);
		request.setHeader("Accept", "text/turtle");
		try{
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
	private Response produceSelectResponse(String query, String format) throws HttpException, IOException {

		ResponseBuilder responseBuilder = Response.ok();
		
		// create pattern for FROM clause
		Pattern pattern = Pattern.compile("FROM\\s*<(?<graph>.*)>\\s*#REVISION\\s*\"(?<revision>.*)\"");
		
		Matcher m = pattern.matcher(query);
		boolean found = false;
		while (m.find()){
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
				if (RevisionManagement.isBranch(graphName, revisionNumber))
					newGraphName = RevisionManagement.getFullGraphName(graphName, revisionNumber);
				else {
					// Respond with specified revision, therefore the revision must be generated - saved in graph <RM-TEMP-graphName>
					newGraphName = "RM-TEMP-" + graphName;
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
				query = query.replace("<" + graphName + ">", "<" + newGraphName + ">");
				headerRevisionNumber = revisionNumber;
			}
		    // Respond with specified revision
			responseBuilder.header(graphName + "-revision-number", headerRevisionNumber);
		    responseBuilder.header(graphName + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		}
		if (!found) {
			throw new InternalServerErrorException("Query contain errors:\n"+query);
		}
		return responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(query, format)).type(format).build();
	}
	
	
	/**
	 * Produce the response for a INSERT or DELETE SPARQL query.
	 * 
	 * @param query the SPARQL query
	 * @param format the result format
	 * @return the response with HTTP header for every graph (revision number and MASTER revision number)
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	private Response produceInsertDeleteResponse(String query, String format) throws HttpException, IOException {
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));		
		logger.info("Update detected");
		
		// General variables
		String user = extractUser(query);
		String commitMessage = extractCommitMessage(query);
		// if no commit message is declared use default message
		if (commitMessage == null) {
			commitMessage = "No commit message specified.";
		}
		
		// create pattern for FROM and INTO clause
		String query_replaced = query;
		Pattern pattern =  Pattern.compile("(FROM|INTO|GRAPH)\\s*<(?<graph>.*)>\\s*#REVISION\\s*\"(?<revision>.*)\"");
		Matcher m = pattern.matcher(query);
		boolean found = m.find();
		
		if (!found) {
			throw new InternalServerErrorException("Query contain errors:\n"+query);
		}
		
	    String graphName = m.group("graph");
	    String revisionName = m.group("revision"); //can contain revision numbers or reference names
	    String revisionNumber = RevisionManagement.getRevisionNumber(graphName, revisionName); //contains only revision numbers
	    
	    if (!RevisionManagement.isBranch(graphName, revisionNumber))
			throw new InternalServerErrorException("Revision is not referenced by branch");
	    
	    String referenceFullGraph = RevisionManagement.getFullGraphName(graphName, revisionNumber);
	    String newRevisionNumber = RevisionManagement.getNextRevisionNumberForLastRevisionNumber(graphName, revisionNumber);

	    // Create the temporary graph and fill with reference full graph
	    TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <RM-UPDATE-TEMP-" + graphName + ">", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <RM-UPDATE-TEMP-" + graphName + ">", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("COPY <" + referenceFullGraph + "> TO <RM-UPDATE-TEMP-" + graphName + ">", "HTML");
		
		// Replace graph name in SPARQL query 
		query_replaced = query_replaced.replace("FROM <" + graphName + ">", "FROM <RM-UPDATE-TEMP-" + graphName + ">");
		query_replaced = query_replaced.replace("INTO <" + graphName + ">", "INTO <RM-UPDATE-TEMP-" + graphName + ">");
		query_replaced = query_replaced.replace("GRAPH <" + graphName + ">", "GRAPH <RM-UPDATE-TEMP-" + graphName + ">");
		
		logger.info("query replaced: " + query_replaced);

		
		// Execute SPARQL query
		responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(query_replaced, format)); 
			
		// Create deltas for new revision
		// Get all added triples
		String queryAddedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
									"  GRAPH <RM-UPDATE-TEMP-" + graphName + "> { ?s ?p ?o }" +
									"  FILTER NOT EXISTS { GRAPH <" + referenceFullGraph + "> { ?s ?p ?o } }" +
									" }";
		String addedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");
		
		// Get all removed triples
		String queryRemovedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
										"  GRAPH <" + referenceFullGraph + "> { ?s ?p ?o }" +
										"  FILTER NOT EXISTS { GRAPH <RM-UPDATE-TEMP-" + graphName + "> { ?s ?p ?o } }" +
										" }";
		String removedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(revisionNumber);
					
		// Create new revision
		RevisionManagement.createNewRevision(graphName, addedTriples, removedTriples, user, newRevisionNumber, commitMessage, list);
		
		// Respond with next revision number
    	responseBuilder.header(graphName + "-revision-number", newRevisionNumber);
		responseBuilder.header(graphName + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		logger.info("Respond with new revision number " + newRevisionNumber + ".");
	
		Response response = responseBuilder.build();
		
		return response;
	}

	
		
	/** Creates a graph under version control for command "CREATE GRAPH <?>"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @return
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private Response produceCreateGraphResponse(String query,
			String format) throws IOException, HttpException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Graph creation detected");
		
		// Execute SPARQL query
		responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(query, format)); 
		
		// Add R43ples information
		Pattern pattern =  Pattern.compile("CREATE(?<silent> SILENT)? GRAPH <(?<graph>.*)>");
		Matcher m = pattern.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
		    String graphName = m.group("graph");
		    RevisionManagement.putGraphUnderVersionControl(graphName);
		}
		if (!found) {
			throw new InternalServerErrorException("Query contain errors:\n"+query);
		}
		
		return responseBuilder.build();
	}
	
	
	/** Drops a graph under version control for command "DROP (SILENT) GRAPH <?>"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @return
	 * @throws IOException 
	 * @throws HttpException 
	 */
	private Response produceDropGraphResponse(String query,
			String format) throws IOException, HttpException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		
		// Clear R43ples information for specified graphs
		Pattern pattern =  Pattern.compile("DROP(?<silent> SILENT)? GRAPH <(?<graph>.*)>");
		Matcher m = pattern.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
		    String graphName = m.group("graph"); 
		    RevisionManagement.purgeGraph(graphName);
		}
		if (!found) {
			throw new InternalServerErrorException("Query contain errors:\n"+query);
		}
		else {
			// Execute SPARQL query
			responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(query, format));
		}
		
		return responseBuilder.build();
	}
	
	/** Creates a tag or a branch for a specific graph and revision.
	 * Using command "TAG GRAPH <?> #REVISION "rev" TO "tag"
	 * Using command "BRANCH GRAPH <?> #REVISION "rev" TO "tag"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @return
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	private Response produceBranchOrTagResponse(String sparqlQuery,
			String format) throws HttpException, IOException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Tag or branch creation detected");
		String user = extractUser(sparqlQuery);
		String commitMessage = extractCommitMessage(sparqlQuery);
		
		// Add R43ples information
		Pattern pattern =  Pattern.compile("(?<action>TAG|BRANCH) GRAPH <(?<graph>.*)> #REVISION \"(?<revision>.*)\" TO \"(?<name>.*)\"");
		Matcher m = pattern.matcher(sparqlQuery);
		
		boolean foundEntry = false;
		while (m.find()) {
			foundEntry = true;
			String action = m.group("action");
		    String graphName = m.group("graph");
		    String revisionNumber = m.group("revision");
		    String name = m.group("name");
		    if (action.equals("TAG"))
		    	RevisionManagement.createTag(graphName, revisionNumber, name, user, commitMessage);
		    else if (action.equals("BRANCH"))
		    	RevisionManagement.createBranch(graphName, revisionNumber, name, user, commitMessage);
		    else
		    	throw new InternalServerErrorException("Error in query: " + sparqlQuery);
		}
		if (!foundEntry)
			throw new InternalServerErrorException("Error in query: " + sparqlQuery);
		
		return responseBuilder.build();
	}


	/** Extracts user out of query
	 * @param query
	 * @return
	 * @throws InternalServerErrorException
	 */
	private String extractUser(String query) {
		Pattern userPattern = Pattern.compile("#USER\\s*\"(?<user>.*)\"");
		Matcher userMatcher = userPattern.matcher(query);
		if (userMatcher.find()) {
			return userMatcher.group("user");
		} else {
			throw new InternalServerErrorException("No user specified");
		}
	}
	
	/** Extracts commit message out of query
	 * @param query
	 * @return
	 * @throws InternalServerErrorException
	 */
	private String extractCommitMessage(String query) {
		Pattern pattern = Pattern.compile("#MESSAGE\\s*\"(?<message>.*)\"");
		Matcher matcher = pattern.matcher(query);
		if (matcher.find()) {
			return matcher.group("message");
		} else {
			throw new InternalServerErrorException("No commit message specified");
		}
	}
}