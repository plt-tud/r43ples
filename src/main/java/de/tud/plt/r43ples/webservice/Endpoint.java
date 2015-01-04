package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.configuration.ConfigurationException;
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
import org.glassfish.jersey.server.mvc.Template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.exception.InternalServerErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.MergeQueryTypeEnum;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.visualisation.GraphVizVisualisation;

/**
 * Provides SPARQL endpoint via [host]:[port]/r43ples/.
 * Supplies version information, service description as well as SPARQL queries.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * 
 */
@Path("/")
public class Endpoint {

	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	
	private final Pattern patternSelectQuery = Pattern.compile(
			"(?<type>SELECT|ASK|CONSTRUCT).*WHERE\\s*\\{(?<where>.*)\\}", 
			patternModifier);
	private final Pattern patternSelectFromPart = Pattern.compile(
			"FROM\\s*<(?<graph>.*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternUpdateRevision = Pattern.compile(
			"(?<action>INSERT|DELETE|WHERE)\\s*\\{\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	private final Pattern patternEmptyGraphPattern = Pattern.compile(
			"GRAPH\\s*<(?<graph>[^>]*)>\\s*\\{\\s*\\}",
			patternModifier);
	private final Pattern patternGraphWithRevision = Pattern.compile(
			"GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCreateGraph = Pattern.compile(
			"CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternDropGraph = Pattern.compile(
			"DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternBranchOrTagQuery = Pattern.compile(
			"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
			patternModifier);
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
			patternModifier);
	private final Pattern patternMergeQuery =  Pattern.compile(
			"MERGE\\s*(?<action>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
			patternModifier);
	
	
	@Context
	private UriInfo uriInfo;
	
	
	private Map<String, Object> htmlMap;
	 {
		Map<String, Object> aMap = new HashMap<String, Object>();
		aMap.put("git", GitRepositoryState.getGitRepositoryState());
	    aMap.put("uriInfo", uriInfo);
		htmlMap= aMap;
	}
		
	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);

	
	@GET
	@Path("test")
	@Template(name="/test.mustache")
	public Map<String, Object> test() throws ConfigurationException {
	    htmlMap.put("feature", 12);
	    htmlMap.put("uriInfo", uriInfo);
	    htmlMap.put("name", "Mustache");
		return htmlMap;
	}
	
	
	/**
	 * Creates sample datasets
	 * @return information provided as HTML response
	 */
	@Path("createSampleDataset")
	@GET
	@Template(name = "/exampleDatasetGeneration.mustache")
	public final List<String> createSampleDataset() {
		final String graphName1 = "http://test.com/r43ples-dataset-1";
		final String graphName2 = "http://test.com/r43ples-dataset-2";
		final String graphName3 = "http://test.com/r43ples-dataset-merging";
		final String graphName4 = "http://test.com/r43ples-dataset-merging-classes";
		final String graphName5 = "http://test.com/r43ples-dataset-renaming";
		try {
			SampleDataSet.createSampleDataset1(graphName1);
			SampleDataSet.createSampleDataset2(graphName2);
			SampleDataSet.createSampleDataSetMerging(graphName3);
			SampleDataSet.createSampleDataSetMergingClasses(graphName4);
			SampleDataSet.createSampleDataSetRenaming(graphName5);
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
		List<String> graphs = new ArrayList<>();
		graphs.add(graphName1);
		graphs.add(graphName2);
		graphs.add(graphName3);
		graphs.add(graphName4);
		graphs.add(graphName5);

	    htmlMap.put("graphs", graphs);
		return graphs;
	}
	
	
	/**
	 * Provide revision information about R43ples system.
	 * 
	 * @param graph
	 *            Provide only information about this graph (if not null)
	 * @return RDF model of revision information
	 */
	@Path("revisiongraph")
	@GET
	@Produces({ "text/turtle", "application/rdf+xml", MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,
			MediaType.APPLICATION_SVG_XML })
	public final Object getRevisionGraph(@HeaderParam("Accept") final String format_header,
			@QueryParam("format") final String format_query, @QueryParam("graph") @DefaultValue("") final String graph) {
		logger.info("Get Revision Graph");
		String format = (format_query != null) ? format_query : format_header;
		logger.info("format: " + format);

		try {
			ResponseBuilder response = Response.ok();
			if (format.contains(MediaType.TEXT_HTML)) {
				response.type(MediaType.TEXT_HTML);
				response.entity(GraphVizVisualisation.getGraphVizHtmlOutput(graph));
			}
			else {
				response.type(format);
				response.entity(RevisionManagement.getRevisionInformation(graph, format));
			}
			return response.build();
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Provide information about revised graphs
	 * 
	 * @return list of graphs which are under revision control
	 */
	@Path("getRevisedGraphs")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public final String getRevisedGraphs(@HeaderParam("Accept") final String format_header,
			@QueryParam("format") @DefaultValue("application/json") final String format_query) {
		logger.info("Get Revised Graphs");
		String format = (format_query != null) ? format_query : format_header;
		logger.info("format: " + format);
		try {
			return RevisionManagement.getRevisedGraphsSparql(format);
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * HTTP POST interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * 
	 * @param formatHeader
	 *            format specified in the HTTP header
	 * @param formatQuery
	 *            format specified in the HTTP parameters
	 * @param sparqlQuery
	 *            the SPARQL query
	 * @return the response
	 * @throws IOException
	 * @throws HttpException 
	 */
	@Path("sparql")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle" })
	public final Response sparqlPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("format") final String formatQuery, @FormParam("query") @DefaultValue("") final String sparqlQuery)
			throws IOException, HttpException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		return sparql(format, sparqlQuery);
	}
		
	
	/**
	 * HTTP GET interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Provides HTML form if no query is specified and HTML is requested
	 * Provides Service Description if no query is specified and RDF
	 * representation is requested
	 * 
	 * @param formatHeader
	 *            format specified in the HTTP header
	 * @param formatQuery
	 *            format specified in the HTTP parameters
	 * @param sparqlQuery
	 *            the SPARQL query
	 * @return the response
	 * @throws IOException
	 * @throws HttpException 
	 */
	@Path("sparql")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle" })
	public final Response sparqlGET(@HeaderParam("Accept") final String formatHeader,
			@QueryParam("format") final String formatQuery, @QueryParam("query") @DefaultValue("") final String sparqlQuery)
			throws IOException, HttpException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		String sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");
		return sparql(format, sparqlQueryDecoded);
	}
	
	
	/**
	 * HTTP GET merging interface.
	 * This is the HTML front end  for the merging functionalities of R43ples
	 *
	 */
	@Path("merging")
	@GET
	@Template(name = "/merging.mustache")
	public final Map<String, Object> getMerging() {
		logger.info("Get Merging interface");
		return htmlMap;
	}
		
	
	/**
	 * Interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Provides HTML form if no query is specified and HTML is requested
	 * Provides Service Description if no query is specified and RDF
	 * representation is requested
	 * 
	 * @param format
	 *            mime type for response format
	 * @param sparqlQuery
	 *            decoded SPARQL query
	 * @return the response
	 * @throws IOException
	 * @throws HttpException 
	 */
	public final Response sparql(final String format, final String sparqlQuery)
			throws IOException, HttpException {
		if (sparqlQuery.equals("")) {
			if (format.contains(MediaType.TEXT_HTML)) {
				return getHTMLResponse();
			} else {
				return getServiceDescriptionResponse(format);
			}
		} else {
			return getSparqlResponse(format, sparqlQuery);
		}
	}

	/**
	 * @param format
	 * 			requested mime type 
	 * @param sparqlQuery
	 * 			string containing the SPARQL query
	 * @return HTTP response of evaluating the sparql query 
	 * @throws InternalServerErrorException
	 */
	private Response getSparqlResponse(final String format, String sparqlQuery) throws InternalServerErrorException {
		logger.info("SPARQL query was requested. Query: " + sparqlQuery);
		try {
			String user = null;
			Matcher userMatcher = patternUser.matcher(sparqlQuery);
			if (userMatcher.find()) {
				user = userMatcher.group("user");
				sparqlQuery = userMatcher.replaceAll("");
			}
			String message = null;
			Matcher messageMatcher = patternCommitMessage.matcher(sparqlQuery);
			if (messageMatcher.find()) {
				message = messageMatcher.group("message");
				sparqlQuery = messageMatcher.replaceAll("");
			}

			if (patternSelectQuery.matcher(sparqlQuery).find()) {
				return getSelectResponse(sparqlQuery, format);
			}
			if (patternUpdateQuery.matcher(sparqlQuery).find()) {
				return getUpdateResponse(sparqlQuery, user, message, format);
			}
			if (patternCreateGraph.matcher(sparqlQuery).find()) {
				return getCreateGraphResponse(sparqlQuery, format);
			}
			if (patternMergeQuery.matcher(sparqlQuery).find()) {
				return getMergeResponse(sparqlQuery, user, message, format);
			}
			if (patternDropGraph.matcher(sparqlQuery).find()) {
				return getDropGraphResponse(sparqlQuery, format);
			}
			if (patternBranchOrTagQuery.matcher(sparqlQuery).find()) {
				return getBranchOrTagResponse(sparqlQuery, user, message, format);
			}
			throw new InternalServerErrorException("No R43ples query detected");
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Get HTML response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return HTML response for SPARQL form
	 * @throws HttpException
	 * @throws IOException
	 */
	private Response getHTMLResponse() throws HttpException, IOException {
		logger.info("SPARQL form requested");
		List<String> graphList = RevisionManagement.getRevisedGraphs();
		
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/endpoint.mustache");
	    StringWriter sw = new StringWriter();
	    
	    htmlMap.put("graphList", graphList);
	    mustache.execute(sw, htmlMap);		
		
		String content = sw.toString();
		
		return Response.ok().entity(content).type(MediaType.TEXT_HTML).build();
	}

	
	/**
	 * Provides the SPARQL Endpoint description of the original sparql endpoint
	 * with the additional R43ples feature (sd:feature) and replaced URIs.
	 * 
	 * @param format
	 *            serialisation format of the service description
	 * @return Extended Service Description
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private Response getServiceDescriptionResponse(final String format) {
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

			UpdateRequest srequest = UpdateFactory.create(""
					+ "PREFIX sd:    <http://www.w3.org/ns/sparql-service-description#>"
					+ "DELETE { ?s ?p ?o.} " 
					+ "INSERT { <" + uriInfo.getAbsolutePath() + "> ?p ?o.} "
					+ "WHERE { ?s a sd:Service; ?p ?o.} ");
			UpdateAction.execute(srequest, model);
			srequest = UpdateFactory.create(""
					+ "PREFIX sd:    <http://www.w3.org/ns/sparql-service-description#>"
					+ "DELETE { ?s sd:url ?url.} " 
					+ "INSERT { ?s sd:url <" + uriInfo.getAbsolutePath() + ">.} "
					+ "WHERE { ?s sd:url ?url.} ");
			UpdateAction.execute(srequest, model);
			srequest = UpdateFactory.create(""
					+ "PREFIX sd:    <http://www.w3.org/ns/sparql-service-description#>"
					+ "INSERT { ?s sd:feature sd:r43ples.} " 
					+ "WHERE { ?s a sd:Service.} ");
			UpdateAction.execute(srequest, model);

			StringWriter sw = new StringWriter();
			model.write(sw, RDFLanguages.nameToLang(format).getName());
			return Response.ok().entity(sw.toString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Produce the response for a SELECT SPARQL query. can handle multiple
	 * graphs
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @return the response with HTTP header for every graph (revision number
	 *         and MASTER revision number)
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	private Response getSelectResponse(final String query, final String format) throws HttpException, IOException {
		if (query.contains("OPTION r43ples:SPARQL_JOIN")) {
			ResponseBuilder responseBuilder = Response.ok();
			String query_rewritten = query.replace("OPTION r43ples:SPARQL_JOIN", "");
			query_rewritten = SparqlRewriter.rewriteQuery(query_rewritten);
			String result = TripleStoreInterface.executeQueryWithAuthorization(query_rewritten, format);
			responseBuilder.entity(result);
			responseBuilder.type(format);
			
			responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(query));
			return responseBuilder.build();
		}
		else {
			return getSelectResponseClassic(query, format);
		}
	}



	/**
	 * @param query
	 * @param format
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private Response getSelectResponseClassic(final String query, final String format) throws HttpException, IOException, UnsupportedEncodingException {
		ResponseBuilder responseBuilder = Response.ok();
		String queryM = query;

		Matcher m = patternSelectFromPart.matcher(queryM);
		boolean found = false;
		while (m.find()) {
			found = true;
			String graphName = m.group("graph");
			String revisionNumber = m.group("revision");
			String newGraphName;

			// if no revision number is declared use the MASTER as default
			if (revisionNumber == null) {
				revisionNumber = "master";
			}
			String headerRevisionNumber;
			if (revisionNumber.equalsIgnoreCase("master")) {
				// Respond with MASTER revision - nothing to be done - MASTER revisions are already created in the named graphs
				headerRevisionNumber = "master";
				newGraphName = graphName;
			} else {
				if (RevisionManagement.isBranch(graphName, revisionNumber)) {
					newGraphName = RevisionManagement.getReferenceGraph(graphName, revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision must be generated - saved in graph <RM-TEMP-graphName>
					newGraphName = graphName + "-temp";
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
				headerRevisionNumber = revisionNumber;
			}

			queryM = m.replaceFirst("FROM <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);

			
		    // Remove the http:// of the graph name because it is not permitted that a header parameter contains a colon
			String 	graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
		    
		    // Respond with specified revision
			responseBuilder.header(graphNameHeader + "-revision-number", headerRevisionNumber);
		    responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		}
		if (!found) {
			logger.info("No R43ples SELECT query: " + queryM);
		}
		String response = TripleStoreInterface.executeQueryWithAuthorization(queryM, format);
		return responseBuilder.entity(response).type(format).build();
	}

	
	/**
	 * Produce the response for a INSERT or DELETE SPARQL query.
	 * 
	 * @param query
	 *            the SPARQL query containing the update request
	 * @param user
	 * 			  user committing the update
	 * @param commitMessage
	 * 			  message describing the commit
	 * @param format
	 *            the result format
	 * @return the response with HTTP header for every graph (revision number
	 *         and MASTER revision number)
	 * @throws IOException
	 * @throws HttpException
	 */
	private Response getUpdateResponse(final String query, final String user, final String commitMessage,
			final String format) throws HttpException, IOException {

		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Update detected");
		
		// write to add and delete sets
		// (replace graph names in query)
		String queryM = query;
		Matcher m = patternUpdateRevision.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String revisionName = m.group("revision"); // can contain revision
														// numbers or reference
														// names
			String action = m.group("action");
			String newRevisionNumber = RevisionManagement.getNextRevisionNumber(graphName, revisionName);
			String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
			String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;
			if (!RevisionManagement.isBranch(graphName, revisionName)) {
				throw new InternalServerErrorException("Revision is not referenced by branch");
			}
			if (action.equalsIgnoreCase("INSERT")) {
				queryM = m.replaceFirst(String.format("INSERT { GRAPH <%s>", addSetGraphUri));
			} else if (action.equalsIgnoreCase("DELETE")) {
				queryM = m.replaceFirst(String.format("INSERT { GRAPH <%s>", removeSetGraphUri));
			} else if (action.equalsIgnoreCase("WHERE")) {
				// TODO ersetze mit SPARQL JOIN
				String tempGraphName = graphName + "-temp";
				RevisionManagement.generateFullGraphOfRevision(graphName, revisionName, tempGraphName);
				queryM = m.replaceFirst(String.format("WHERE { GRAPH <%s>", tempGraphName));
			}
			m = patternUpdateRevision.matcher(queryM);
		}
		
		// Remove empty insert clauses which otherwise will lead to errors
		m= patternEmptyGraphPattern.matcher(queryM);
		queryM = m.replaceAll("");

		TripleStoreInterface.executeQueryWithAuthorization(queryM);

		queryM = query;
		m = patternGraphWithRevision.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String revisionName = m.group("revision"); // can contain revision
														// numbers or reference
														// names
			// General variables
			String newRevisionNumber = RevisionManagement.getNextRevisionNumber(graphName, revisionName);
			String referenceFullGraph = RevisionManagement.getReferenceGraph(graphName, revisionName);
			String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
			String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;

			// remove doubled data
			// (already existing triples in add set; not existing triples in
			// delete set)
			TripleStoreInterface
					.executeQueryWithAuthorization(String.format(
							"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }", addSetGraphUri,
							referenceFullGraph));
			TripleStoreInterface.executeQueryWithAuthorization(String.format(
					"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
					removeSetGraphUri, removeSetGraphUri, referenceFullGraph));

			// merge change sets into reference graph
			// (copy add set to reference graph; remove delete set from reference graph)
			TripleStoreInterface.executeQueryWithAuthorization(String.format(
						"INSERT { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
						referenceFullGraph,	addSetGraphUri));
			TripleStoreInterface.executeQueryWithAuthorization(String.format(
					"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }", 
					referenceFullGraph,	removeSetGraphUri));

			// add meta information to R43ples
			ArrayList<String> usedRevisionNumber = new ArrayList<String>();
			usedRevisionNumber.add(revisionName);
			RevisionManagement.addMetaInformationForNewRevision(graphName, user, commitMessage, usedRevisionNumber,
					newRevisionNumber, addSetGraphUri, removeSetGraphUri);

			String 	graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
			
			// Respond with next revision number
	    	responseBuilder.header(graphNameHeader + "-revision-number", newRevisionNumber);
			responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
			logger.info("Respond with new revision number " + newRevisionNumber + ".");
			logger.info("Respond with new revision number " + newRevisionNumber);
			queryM = m.replaceAll(String.format("GRAPH <%s> ", graphName));
			m = patternGraphWithRevision.matcher(queryM);
		}

		Response response = responseBuilder.build();
		return response;
	}


	/**
	 * Creates a graph under version control for command "CREATE GRAPH <?>"
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @throws IOException
	 * @throws HttpException
	 */
	private Response getCreateGraphResponse(final String query, final String format) throws IOException, HttpException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Graph creation detected");

		Matcher m = patternCreateGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
			String graphName = m.group("graph");
//			String silent = m.group("silent");
			String querySparql = m.group();
			
			// Create graph
			String result = TripleStoreInterface.executeQueryWithAuthorization(querySparql, format);
		    responseBuilder.entity(result);
		    
		    if (RevisionManagement.getMasterRevisionNumber(graphName) == null)
		    {
			    // Add R43ples information
			    RevisionManagement.putGraphUnderVersionControl(graphName);
		    	
			    String 	graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
			    responseBuilder.header(graphNameHeader + "-revision-number", 0);
				responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", 0);
			}

		}
		if (!found) {
			throw new InternalServerErrorException("Query doesn't contain a correct CREATE query:\n" + query);
		}
		return responseBuilder.build();
	}

	/**
	 * Drops a graph under version control for command "DROP (SILENT) GRAPH <?>"
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @throws IOException
	 * @throws HttpException
	 */
	private Response getDropGraphResponse(final String query, final String format) throws IOException, HttpException {
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
			throw new InternalServerErrorException("Query contain errors:\n" + query);
		}
		responseBuilder.status(Response.Status.OK);
		responseBuilder.entity("Successful: " + query);
		return responseBuilder.build();
	}

	/**
	 * Creates a tag or a branch for a specific graph and revision. Using
	 * command "TAG GRAPH <?> REVISION "rev" TO "tag" Using command
	 * "BRANCH GRAPH <?> REVISION "rev" TO "tag"
	 * 
	 * @param sparqlQuery
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	private Response getBranchOrTagResponse(final String sparqlQuery, final String user, final String commitMessage,
			final String format) throws HttpException, IOException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Tag or branch creation detected");

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

			String 	graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
		    	
	    	// Respond with next revision number
		    responseBuilder.header(graphNameHeader + "-revision-number", RevisionManagement.getRevisionNumber(graphName, referenceName));
	    	responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
		    
		}
		if (!foundEntry) {
			throw new InternalServerErrorException("Error in query: " + sparqlQuery);
		}

		return responseBuilder.build();
	}
	
	
	/** 
	 * Creates a merge between the specified branches.
	 * 
	 * Using command: MERGE GRAPH <graphURI> BRANCH "branchNameA" INTO "branchNameB"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	private Response getMergeResponse(final String sparqlQuery, final String user, final String commitMessage, final String format) throws HttpException, IOException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Merge creation detected");

		// Add R43ples information
		Matcher m = patternMergeQuery.matcher(sparqlQuery);
		
		boolean foundEntry = false;
		while (m.find()) {
			foundEntry = true;
			String newRevisionNumber = null;
			
			String action = m.group("action");
			String graphName = m.group("graph");
			String sdd = m.group("sdd");
			String sddURI = m.group("sddURI");
			String branchNameA = m.group("branchNameA");
			String branchNameB = m.group("branchNameB");
			String with = m.group("with");
			String triples = m.group("triples");
			
			String revisionUriA = RevisionManagement.getRevisionUri(graphName, branchNameA);
			String revisionUriB = RevisionManagement.getRevisionUri(graphName, branchNameB);
			
			logger.debug("action: " + action);
			logger.debug("graph: " + graphName);
			logger.debug("sdd: " + sdd);
			logger.debug("sddURI: " + sddURI);
			logger.debug("branchNameA: " + branchNameA);
			logger.debug("branchNameB: " + branchNameB);
			logger.debug("with: " + with);
			logger.debug("triples: " + triples);
			
			// TODO check graph existence
			
			// Check if A and B are different valid branches (it is only possible to merge terminal nodes)
			if (!RevisionManagement.getRevisionNumber(graphName, branchNameA).equals(RevisionManagement.getRevisionNumber(graphName, branchNameB))) {
				// Different branches specified
				// Check if both are terminal nodes
				if (!(RevisionManagement.isBranch(graphName, branchNameA) && RevisionManagement.isBranch(graphName, branchNameB))) {
					// There are non terminal nodes used
					throw new InternalServerErrorException("Non terminal nodes were used: " + sparqlQuery);
				}
			} else {
				// Branches are equal - throw error
				throw new InternalServerErrorException("Specified branches are equal: " + sparqlQuery);
			}

			
			// Differ between MERGE query with specified SDD and without SDD			
			String usedSDDURI = null;
			if (sdd != null) {
				// Specified SDD
				usedSDDURI = sddURI;
			} else {
				// Default SDD
				// Query the referenced SDD
				String querySDD = String.format(
						  "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> %n"
						+ "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> %n"
						+ "SELECT ?defaultSDD %n"
						+ "FROM <%s> %n"
						+ "WHERE { %n"
						+ "	<%s> a rmo:Graph ;%n"
						+ "		sddo:hasDefaultSDD ?defaultSDD . %n"
						+ "}", Config.revision_graph, graphName);
				
				String resultSDD = TripleStoreInterface.executeQueryWithAuthorization(querySDD, "XML");
				if (ResultSetFactory.fromXML(resultSDD).hasNext()) {
					QuerySolution qs = ResultSetFactory.fromXML(resultSDD).next();
					usedSDDURI = qs.getResource("?defaultSDD").toString();
				} else {
					throw new InternalServerErrorException("Error in revision graph! Selected graph <" + graphName + "> has no default SDD referenced.");
				}
			}

			// Get the common revision with shortest path
			String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionUriA, revisionUriB);
			
			// Create the revision progress for A and B
			String graphNameA = "RM-REVISION-PROGRESS-A-" + graphName;
			String graphNameB = "RM-REVISION-PROGRESS-B-" + graphName;
			String uriA = "http://eatld.et.tu-dresden.de/branch-A";
			String uriB = "http://eatld.et.tu-dresden.de/branch-B";
			
			MergeManagement.createRevisionProgress(MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriA), graphNameA, uriA);
			MergeManagement.createRevisionProgress(MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriB), graphNameB, uriB);
			
			// Create difference model
			MergeManagement.createDifferenceTripleModel(graphName, "RM-DIFFERENCE-MODEL-" + graphName, graphNameA, uriA, graphNameB, uriB, usedSDDURI);
			
			// Differ between the different merge queries
			if ((action != null) && (action.equalsIgnoreCase("AUTO")) && (with == null) && (triples == null)) {
				logger.info("AUTO MERGE query detected");
				// Create the merged revision
				newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, "RM-DIFFERENCE-MODEL-" + graphName, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.AUTO, "");
			} else if ((action != null) && (action.equalsIgnoreCase("MANUAL")) && (with != null) && (triples != null)) {
				logger.info("MANUAL MERGE query detected");
				// Create the merged revision
				newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, "RM-DIFFERENCE-MODEL-" + graphName, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.MANUAL, triples);
			} else if ((action == null) && (with != null) && (triples != null)) {
				logger.info("MERGE WITH query detected");
				// Create the merged revision
				newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, "RM-DIFFERENCE-MODEL-" + graphName, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.WITH, triples);
			} else if ((action == null) && (with == null) && (triples == null)) {
				logger.info("MERGE query detected");
				// Check if difference model contains conflicts
				String queryASK = String.format(
						  "ASK { %n"
						+ "	GRAPH <%s> { %n"
						+ " 	?ref <http://eatld.et.tu-dresden.de/sddo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
						+ "	} %n"
						+ "}", "RM-DIFFERENCE-MODEL-" + graphName);
				String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
				if (resultASK.equals("true")) {
					// Difference model contains conflicts
					// Return the conflict model to the client
					responseBuilder = Response.status(Response.Status.CONFLICT);
					responseBuilder.entity(RevisionManagement.getContentOfGraphByConstruct("RM-DIFFERENCE-MODEL-" + graphName));
				} else {
					// Difference model contains no conflicts
					// Create the merged revision
					newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, "RM-DIFFERENCE-MODEL-" + graphName, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.COMMON, "");
				}
			} else {
				throw new InternalServerErrorException("This is not a valid MERGE query: " + sparqlQuery);
			}
			
			String 	graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
			
			// Return the revision number which were used (convert tag or branch identifier to revision number)
			responseBuilder.header(graphNameHeader + "-revision-number-of-branch-A", RevisionManagement.getRevisionNumber(graphName, branchNameA));
			responseBuilder.header(graphNameHeader + "-revision-number-of-branch-B", RevisionManagement.getRevisionNumber(graphName, branchNameB));			
			
			if (newRevisionNumber != null) {
				// Respond with next revision number
		    	responseBuilder.header(graphNameHeader + "-revision-number", newRevisionNumber);
				responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
				logger.info("Respond with new revision number " + newRevisionNumber + ".");
			}
		}
		if (!foundEntry)
			throw new InternalServerErrorException("Error in query: " + sparqlQuery);
		
		return responseBuilder.build();	
	}
	
}
