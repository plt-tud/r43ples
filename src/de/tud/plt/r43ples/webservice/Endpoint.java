package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.exception.InternalServerErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
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
@Path("r43ples")
public class Endpoint {

	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	
	private final Pattern patternSelectQuery = Pattern.compile(
			"(?<type>SELECT|ASK).*WHERE\\s*\\{(?<where>.*)\\}", 
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
	private final Pattern patternGraph = Pattern.compile(
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
	
	@Context
	private UriInfo uriInfo;
	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);

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
	public final Response getRevisionGraph(@HeaderParam("Accept") final String format_header,
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
		return sparql(format, sparqlQuery);
	}
		
	public final Response sparql(final String format, final String sparqlQuery)
			throws IOException, HttpException {
		logger.info("SPARQL requested with format: " + format);
		if (sparqlQuery.equals("")) {
			if (format.contains("text/html")) {
				logger.info("SPARQL form requested");
				List<String> graphList = RevisionManagement.getRevisedGraphs();
				StringBuilder sb1 = new StringBuilder("<option value=\"\">(All)</option>\n");
				StringBuilder sb2 = new StringBuilder("<option value=\"\">(None)</option>\n");
				for (Iterator<String> opt = graphList.iterator(); opt.hasNext();) {
					String graph = opt.next();
					sb1.append(String.format("<option value=\"%1$s\">%1$s</option>%n", graph));
					sb2.append(String.format("<option value=\"DROP GRAPH <%1$s>\">%1$s</option>%n", graph));
				}
				String content = String.format(ResourceManagement.getContentFromResource("webapp/index.html"), 
						Endpoint.class.getPackage().getImplementationVersion(), sb1.toString(), sb2.toString());
				return Response.ok().entity(content).type(MediaType.TEXT_HTML).build();
			} else {
				return getServiceDescriptionResponse(format);
			}
		} else {
			logger.info("SPARQL query was requested. Query: " + sparqlQuery);
			try {
				String sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");

				String user = null;
				Matcher userMatcher = patternUser.matcher(sparqlQueryDecoded);
				if (userMatcher.find()) {
					user = userMatcher.group("user");
					sparqlQueryDecoded = userMatcher.replaceAll("");
				}
				String message = null;
				Matcher messageMatcher = patternCommitMessage.matcher(sparqlQueryDecoded);
				if (messageMatcher.find()) {
					message = messageMatcher.group("message");
					sparqlQueryDecoded = messageMatcher.replaceAll("");
				}

				if (patternSelectQuery.matcher(sparqlQueryDecoded).find()) {
					return getSelectResponse(sparqlQueryDecoded, format);
				}
				if (patternUpdateQuery.matcher(sparqlQueryDecoded).find()) {
					return getUpdateResponse(sparqlQueryDecoded, user, message, format);
				}
				if (patternCreateGraph.matcher(sparqlQueryDecoded).find()) {
					return getCreateGraphResponse(sparqlQueryDecoded, format);
				}
				if (patternDropGraph.matcher(sparqlQueryDecoded).find()) {
					return getDropGraphResponse(sparqlQueryDecoded, format);
				}
				if (patternBranchOrTagQuery.matcher(sparqlQueryDecoded).find()) {
					return getBranchOrTagResponse(sparqlQueryDecoded, user, message, format);
				}
				throw new InternalServerErrorException("No R43ples query detected");
			} catch (HttpException | IOException e) {
				e.printStackTrace();
				throw new InternalServerErrorException(e.getMessage());
			}
		}
	}

	/**
	 * Creates sample datasets
	 * @return information provided as HTML response
	 */
	@Path("createSampleDataset")
	@GET
	public final Response createSampleDataset() {
		final String graphName1 = "http://test.com/r43ples-dataset";
		final String graphName2 = "http://test.com/r43ples-dataset-2";
		try {
			SampleDataSet.createSampleDataset1(graphName1);
			SampleDataSet.createSampleDataset2(graphName2);
		} catch (HttpException | IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
		String result = String.format(
				"Test dataset 1 successfully created in graph: %s %n"
				+ "Test dataset 2 successfully created in graph: %s %n", graphName1, graphName2);
		return Response.ok().entity(result).build();
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
		ResponseBuilder responseBuilder = Response.ok();
		if (query.contains("OPTION r43ples:SPARQL_JOIN")) {
			try {
				String query_rewritten = query.replace("OPTION r43ples:SPARQL_JOIN", "");
				query_rewritten = SparqlRewriter.rewriteQuery(query_rewritten);
				String result = TripleStoreInterface.executeQueryWithAuthorization(query_rewritten, format);
				return responseBuilder.entity(result).type(format).build();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				throw new InternalServerErrorException("Query contain errors:\n" + query);
			}
		}
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
				revisionNumber = "MASTER";
			}
			String headerRevisionNumber;
			if (revisionNumber.equalsIgnoreCase("MASTER")) {
				// Respond with MASTER revision - nothing to be done - MASTER revisions are already created in the named graphs
				headerRevisionNumber = "MASTER";
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

			// Respond with specified revision
			responseBuilder.header(graphName + "-revision-number", headerRevisionNumber);
			responseBuilder.header(graphName + "-revision-number-of-MASTER",
					RevisionManagement.getMasterRevisionNumber(graphName));
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

		TripleStoreInterface.executeQueryWithAuthorization(queryM);

		queryM = query;
		m = patternGraph.matcher(queryM);
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

			// Respond with next revision number
			responseBuilder.header(graphName + "-revision-number", newRevisionNumber);
			responseBuilder.header(graphName + "-revision-number-of-MASTER", 
					RevisionManagement.getMasterRevisionNumber(graphName));
			logger.info("Respond with new revision number " + newRevisionNumber);
			queryM = m.replaceAll(String.format("GRAPH <%s> ", graphName));
			m = patternGraph.matcher(queryM);
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
			// Execute SPARQL query
			String querySparql = m.group();
			responseBuilder.entity(TripleStoreInterface.executeQueryWithAuthorization(querySparql, format));
			// Add R43ples information
			RevisionManagement.putGraphUnderVersionControl(graphName);
			responseBuilder.header(graphName + "-revision-number", 0);
			responseBuilder.header(graphName + "-revision-number-of-MASTER", 0);
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

			// Respond with next revision number
			responseBuilder.header(graphName + "-revision-number", 
					RevisionManagement.getRevisionNumber(graphName, referenceName));
			responseBuilder.header(graphName + "-revision-number-of-MASTER",
					RevisionManagement.getMasterRevisionNumber(graphName));

		}
		if (!foundEntry) {
			throw new InternalServerErrorException("Error in query: " + sparqlQuery);
		}

		return responseBuilder.build();
	}

}
