package de.tud.plt.r43ples.webservice;

import java.io.ByteArrayOutputStream;
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

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.mvc.Template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.MergeQueryTypeEnum;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.merging.MergingControl;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.visualisation.MMSTVisualisation;
import de.tud.plt.r43ples.visualisation.VisualisationD3;

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
	
	private final Pattern patternSelectAskConstructQuery = Pattern.compile(
			"(?<type>SELECT|ASK|CONSTRUCT).*WHERE\\s*\\{(?<where>.*)\\}", 
			patternModifier);
	private final Pattern patternSelectFromPart = Pattern.compile(
			"(?<type>FROM|GRAPH)\\s*<(?<graph>.*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
			patternModifier);
	
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
			patternModifier);
	private final Pattern patternUpdateRevision = Pattern.compile(
			"(?<action>INSERT|DELETE|WHERE)(?<data>\\s*DATA){0,1}\\s*\\{\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
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
		aMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		aMap.put("git", GitRepositoryState.getGitRepositoryState());
		htmlMap= aMap;
	}
		
	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);
	
	
	/**
	 * Creates sample datasets
	 * @return information provided as HTML response
	 * @throws IOException 
	 * @throws InternalErrorException 
	 */
	@Path("createSampleDataset")
	@GET
	@Template(name = "/exampleDatasetGeneration.mustache")
	public final List<String> createSampleDataset(@QueryParam("dataset") @DefaultValue("all") final String graph) throws IOException, InternalErrorException {
		final String graph_dataset1 = "http://test.com/r43ples-dataset-1";
		final String graph_dataset2 = "http://test.com/r43ples-dataset-2";
		final String graph_dataset_merging = "http://test.com/r43ples-dataset-merging";
		final String graph_dataset_merging_classes = "http://test.com/r43ples-dataset-merging-classes";
		final String graph_dataset_renaming = "http://test.com/r43ples-dataset-renaming";
		final String graph_dataset_complex_structure = "http://test.com/r43ples-dataset-complex-structure";
		List<String> graphs = new ArrayList<>();
		if (graph.equals(graph_dataset1) || graph.equals("all")){
			SampleDataSet.createSampleDataset1(graph_dataset1);
			graphs.add(graph_dataset1);
		}
		if (graph.equals(graph_dataset2) || graph.equals("all")){
			SampleDataSet.createSampleDataset2(graph_dataset2);
			graphs.add(graph_dataset2);
		}
		if (graph.equals(graph_dataset_merging) || graph.equals("all")){
			SampleDataSet.createSampleDataSetMerging(graph_dataset_merging);
			graphs.add(graph_dataset_merging);
		}
		if (graph.equals(graph_dataset_merging_classes) || graph.equals("all")){
			SampleDataSet.createSampleDataSetMergingClasses(graph_dataset_merging_classes);
			graphs.add(graph_dataset_merging_classes);
		}
		if (graph.equals(graph_dataset_renaming) || graph.equals("all")){
			SampleDataSet.createSampleDataSetRenaming(graph_dataset_renaming);
			graphs.add(graph_dataset_renaming);
		}
		if (graph.equals(graph_dataset_complex_structure) || graph.equals("all")){
			SampleDataSet.createSampleDataSetComplexStructure(graph_dataset_complex_structure);
			graphs.add(graph_dataset_complex_structure);
		}
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
		logger.info("Get Revision Graph: " + graph);
		String format = (format_query != null) ? format_query : format_header;
		logger.info("format: " + format);

		ResponseBuilder response = Response.ok();
		if (format.equals("batik")) {
			response.type(MediaType.TEXT_HTML);
			response.entity(MMSTVisualisation.getHtmlOutput(graph));
		} else if (format.equals("d3")) {
			response.entity(VisualisationD3.getHtmlOutput(graph));
		}
		else {
			response.type(format);
			response.entity(RevisionManagement.getRevisionInformation(graph, format));
		}
		return response.build();
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
		return RevisionManagement.getRevisedGraphsSparql(format);
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
	 * @throws InternalErrorException 
	 */
	@Path("sparql")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response sparqlPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("format") final String formatQuery, @FormParam("query") @DefaultValue("") final String sparqlQuery) throws InternalErrorException {
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
	 * @throws InternalErrorException 
	 */
	@Path("sparql")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response sparqlGET(@HeaderParam("Accept") final String formatHeader,
			@QueryParam("format") final String formatQuery, @QueryParam("query") @DefaultValue("") final String sparqlQuery) throws InternalErrorException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		
		String sparqlQueryDecoded;
		try {
			sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			sparqlQueryDecoded = sparqlQuery;
		}
		return sparql(format, sparqlQueryDecoded);
	}
	
	
	@Path("debug")
	@GET
	public final String debug(@DefaultValue("") @QueryParam("query") final String sparqlQuery) {
		if (sparqlQuery.equals("")) {
			return getHTMLDebugResponse();
		} else {
			return getSparqlDebugResponse(sparqlQuery);
		}
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
		
	@Path("mergingView")
	@GET
    @Produces({ "text/turtle", "application/rdf+xml", MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,
		 MediaType.APPLICATION_SVG_XML })
	public final Object getMergingView(@HeaderParam("Accept") final String format_header,
			@QueryParam("optradio") final String format_query,@QueryParam("graph") final String graph) {
		logger.info("in mergingView yxy");
		logger.info("Get Radio: " + graph);
		String format = (format_query != null) ? format_query : format_header;
		logger.info("format: " + format);
		
		ResponseBuilder response = Response.ok();
		
		if (format.equals("on")) {
			response.entity(MergingControl.getHtmlOutput(graph));
		}
		else {
			format = "application/json";
			response.type(format);
			response.entity(RevisionManagement.getRevisionInformation(graph, format));
		}
		return response.build();
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
	 * @throws InternalErrorException 
	 */
	public final Response sparql(final String format, final String sparqlQuery) throws InternalErrorException {
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
	 * get sparql interface direct on TDB interface
	 * @param sparqlQuery
	 * 			string containing the SPARQL query
	 * @return HTTP response of evaluating the sparql query 
	 */
	private String getSparqlDebugResponse(final String sparqlQuery) {
		logger.info("Debug query was requested. Query: " + sparqlQuery);
		if (sparqlQuery.contains("INSERT")) {
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(sparqlQuery);
			return "Query executed";
		}
		else
			return getHTMLResult(TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(sparqlQuery, "text/html"), sparqlQuery);
	}
	
	
	/**
	 * Get HTML debug response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return HTML response for SPARQL form
	 */
	private String getHTMLDebugResponse() {		
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/debug.mustache");
	    StringWriter sw = new StringWriter();
	    htmlMap.put("graphs", TripleStoreInterfaceSingleton.get().getGraphs());
	    htmlMap.put("revisionGraph", Config.revision_graph);
	    mustache.execute(sw, htmlMap);		
		
		String content = sw.toString();
		return content;
	}
	
	
	
	/**
	 * @param format
	 * 			requested mime type 
	 * @param sparqlQuery
	 * 			string containing the SPARQL query
	 * @return HTTP response of evaluating the sparql query 
	 * @throws InternalErrorException
	 */
	private Response getSparqlResponse(final String format, String sparqlQuery) throws InternalErrorException {
		logger.info("SPARQL query was requested. Query: " + sparqlQuery);
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

		if (patternSelectAskConstructQuery.matcher(sparqlQuery).find()) {
			return getSelectConstructAskResponse(sparqlQuery, format);
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
		throw new QueryErrorException("No R43ples query detected");
	}

	/**
	 * Get HTML response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return HTML response for SPARQL form
	 */
	private Response getHTMLResponse() {
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
		String triples =String.format("@prefix rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> . %n"
				+ "@prefix ns3:	<http://www.w3.org/ns/formats/> .%n"
				+ "@prefix sd:	<http://www.w3.org/ns/sparql-service-description#> .%n"
				+ "<%1$s>	rdf:type	sd:Service ;%n"
				+ "	sd:endpoint	<%1$s> ;%n"
				+ "	sd:feature	sd:r43ples ;"
				+ "	sd:resultFormat	ns3:SPARQL_Results_JSON ,%n"
				+ "		ns3:SPARQL_Results_XML ,%n"
				+ "		ns3:Turtle ,%n"
				+ "		ns3:N-Triples ,%n"
				+ "		ns3:N3 ,%n"
				+ "		ns3:RDF_XML ,%n"
				+ "		ns3:SPARQL_Results_CSV ,%n"
				+ "		ns3:RDFa ;%n"
				+ "	sd:supportedLanguage	sd:SPARQL10Query, sd:SPARQL11Query, sd:SPARQL11Query, sd:SPARQL11Update, sd:R43plesQuery  ;%n"
				+ "	sd:url	<%1$s> .%n", uriInfo.getAbsolutePath()) ;
		Model model = JenaModelManagement.readStringToJenaModel(triples, "TURTLE");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		if (format.toLowerCase().contains("xml") )
			model.write(baos, "RDF/XML");
		else if (format.toLowerCase().contains("turtle") )
			model.write(baos, "Turtle");
		else if (format.toLowerCase().contains("json") )
			model.write(baos, "RDF/JSON");
		else {
			try {
				model.write(baos, format);
			}
			catch (NoWriterForLangException e) {
				model.write(baos, "Turtle");
			}
		}
		return Response.ok().entity(baos.toString()).build();
	}

	/**
	 * Produce the response for a SELECT, ASK or CONSTRUCT SPARQL query. can handle multiple
	 * graphs
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @return the response with HTTP header for every graph (revision number
	 *         and MASTER revision number)
	 * @throws InternalErrorException 
	 */
	private Response getSelectConstructAskResponse(final String query, final String format) throws InternalErrorException {
		ResponseBuilder responseBuilder = Response.ok();
		String result;
		
		if (query.contains("OPTION r43ples:SPARQL_JOIN")) {
			String query_rewritten = query.replace("OPTION r43ples:SPARQL_JOIN", "");
			query_rewritten = SparqlRewriter.rewriteQuery(query_rewritten);
			result = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(query_rewritten, format);
		}
		else {
			result = getSelectConstructAskResponseClassic(query, format);
		}
		
		if (format.equals("text/html")){
			responseBuilder.entity(getHTMLResult(result, query));
		} else {
			responseBuilder.entity(result);
		}
		responseBuilder.type(format);
		responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(query));		
		return responseBuilder.build();
	}


	/**
	 * @param query
	 * @param responseBuilder
	 * @param result
	 */
	private String getHTMLResult(final String result, String query) {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/result.mustache");
		StringWriter sw = new StringWriter();
		htmlMap.put("result", result);
		htmlMap.put("query", query);
		mustache.execute(sw, htmlMap);		
		return sw.toString();
	}
	

	/**
	 * @param query
	 * @param format
	 * @return
	 * @throws InternalErrorException 
	 */
	private String getSelectConstructAskResponseClassic(final String query, final String format) throws InternalErrorException {
		String queryM = query;

		Matcher m = patternSelectFromPart.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String type = m.group("type");
			String revisionNumber = m.group("revision").toLowerCase();
			String newGraphName;

			// if no revision number is declared use the MASTER as default
			if (revisionNumber == null) {
				revisionNumber = "master";
			}
			if (revisionNumber.equalsIgnoreCase("master")) {
				// Respond with MASTER revision - nothing to be done - MASTER revisions are already created in the named graphs
				newGraphName = graphName;
			} else {
				if (RevisionManagement.isBranch(graphName, revisionNumber)) {
					newGraphName = RevisionManagement.getReferenceGraph(graphName, revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision must be generated - saved in graph <RM-TEMP-graphName>
					newGraphName = graphName + "-temp";
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
			}

			queryM = m.replaceFirst(type + " <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);
			
		}
		String response = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(queryM, format);
		return response;
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
	 * @throws InternalErrorException 
	 */
	private Response getUpdateResponse(final String query, final String user, final String commitMessage,
			final String format) throws InternalErrorException {
		logger.info("Update detected");
		
		// write to add and delete sets
		// (replace graph names in query)
		String queryM = query;
		Matcher m = patternUpdateRevision.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String revisionName = m.group("revision").toLowerCase(); 	// can contain revision
																		// numbers or reference
																		// names
			String action = m.group("action");
			String data = m.group("data");
			if (data == null)
				data = "";
			
			String newRevisionNumber = RevisionManagement.getNextRevisionNumber(graphName, revisionName);
			String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
			String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;
			if (!RevisionManagement.isBranch(graphName, revisionName)) {
				throw new InternalErrorException("Revision is not referenced by a branch");
			}
			if (action.equalsIgnoreCase("INSERT")) {
				queryM = m.replaceFirst(String.format("INSERT %s { GRAPH <%s>", data, addSetGraphUri));
			} else if (action.equalsIgnoreCase("DELETE")) {
				queryM = m.replaceFirst(String.format("INSERT %s { GRAPH <%s>", data, removeSetGraphUri));
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

		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryM);

		queryM = query;
		m = patternGraphWithRevision.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String revisionName = m.group("revision").toLowerCase();	// can contain revision
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
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
							"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }", addSetGraphUri,
							referenceFullGraph));
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
					"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
					removeSetGraphUri, removeSetGraphUri, referenceFullGraph));

			// merge change sets into reference graph
			// (copy add set to reference graph; remove delete set from reference graph)
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
						"INSERT { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
						referenceFullGraph,	addSetGraphUri));
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
					"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }", 
					referenceFullGraph,	removeSetGraphUri));

			// add meta information to R43ples
			ArrayList<String> usedRevisionNumber = new ArrayList<String>();
			usedRevisionNumber.add(revisionName);
			RevisionManagement.addMetaInformationForNewRevision(graphName, user, commitMessage, usedRevisionNumber,
					newRevisionNumber, addSetGraphUri, removeSetGraphUri);
			
			
			queryM = m.replaceAll(String.format("GRAPH <%s> ", graphName));
			m = patternGraphWithRevision.matcher(queryM);
		}
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		
		String result = "Query successfully executed";
		if (format.equals("text/html")){
			responseBuilder.entity(getHTMLResult(result, query));
		} else {
			responseBuilder.entity(result);
		}
		responseBuilder.type(format);
		responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(query));		
		return responseBuilder.build();
	}


	/**
	 * Creates a graph under version control for command "CREATE GRAPH <?>"
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @throws InternalErrorException 
	 */
	private Response getCreateGraphResponse(final String query, final String format) throws InternalErrorException {
		logger.info("Graph creation detected");

		String graphName = null;
		Matcher m = patternCreateGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
			graphName = m.group("graph");
//			String silent = m.group("silent");
			
			// Create graph
			TripleStoreInterfaceSingleton.get().executeCreateGraph(graphName);
		    
		    if (RevisionManagement.getMasterRevisionNumber(graphName) == null)
		    {
			    // Add R43ples information
			    RevisionManagement.putGraphUnderVersionControl(graphName);
			}
		}
		if (!found) {
			throw new QueryErrorException("Query doesn't contain a correct CREATE query:\n" + query);
		}
		String result = "Graph successfully created";
		
		ResponseBuilder responseBuilder = Response.created(URI.create(graphName));
		if (format.equals("text/html")){
			responseBuilder.entity(getHTMLResult(result, query));
		} else {
			responseBuilder.entity(result);
		}
		responseBuilder.type(format);
		responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(query));		
		return responseBuilder.build();
	}

	/**
	 * Drops a graph under version control for command "DROP (SILENT) GRAPH <?>"
	 * 
	 * @param query
	 *            the SPARQL query
	 * @param format
	 *            the result format
	 * @throws InternalErrorException 
	 */
	private Response getDropGraphResponse(final String query, final String format) throws InternalErrorException {
		// Clear R43ples information for specified graphs
		Matcher m = patternDropGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
			String graphName = m.group("graph");
			RevisionManagement.purgeGraph(graphName);
		}
		if (!found) {
			throw new QueryErrorException("Query contain errors:\n" + query);
		}
		String result = "Graph successfully dropped";
		
		ResponseBuilder responseBuilder = Response.ok();
		if (format.contains("text/html")){
			responseBuilder.entity(getHTMLResult(result, query));
			responseBuilder.type("text/html");
		} else {
			responseBuilder.entity(result);
			responseBuilder.type("text/plain");
		}
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
	 * @throws InternalErrorException 
	 */
	private Response getBranchOrTagResponse(final String sparqlQuery, final String user, final String commitMessage,
			final String format) throws InternalErrorException {
		logger.info("Tag or branch creation detected");

		// Add R43ples information
		Matcher m = patternBranchOrTagQuery.matcher(sparqlQuery);

		boolean foundEntry = false;
		while (m.find()) {
			foundEntry = true;
			String action = m.group("action");
			String graphName = m.group("graph");
			String revisionNumber = m.group("revision").toLowerCase();
			String referenceName = m.group("name").toLowerCase();
			if (action.equals("TAG")) {
				RevisionManagement.createTag(graphName, revisionNumber, referenceName, user, commitMessage);
			} else if (action.equals("BRANCH")) {
				RevisionManagement.createBranch(graphName, revisionNumber, referenceName, user, commitMessage);
			} else {
				throw new QueryErrorException("Error in query: " + sparqlQuery);
			}	    
		}
		if (!foundEntry) {
			throw new QueryErrorException("Error in query: " + sparqlQuery);
		}
		String result = "Tagging or branching successful";

		ResponseBuilder responseBuilder = Response.ok();
		if (format.equals("text/html")){
			responseBuilder.entity(getHTMLResult(result, sparqlQuery));
		} else {
			responseBuilder.entity(result);
		}
		responseBuilder.type(format);	
		responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(sparqlQuery));		
		return responseBuilder.build();
	}
	
	
	/** 
	 * Creates a merge between the specified branches.
	 * 
	 * Using command: MERGE GRAPH <graphURI> BRANCH "branchNameA" INTO "branchNameB"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @throws InternalErrorException 
	 */
	private Response getMergeResponse(final String sparqlQuery, final String user, final String commitMessage, final String format) throws InternalErrorException {
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
			String branchNameA = m.group("branchNameA").toLowerCase();
			String branchNameB = m.group("branchNameB").toLowerCase();
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
			
			if (!RevisionManagement.checkGraphExistence(graphName)){
				logger.error("Graph <"+graphName+"> does not exist.");
				throw new InternalErrorException("Graph <"+graphName+"> does not exist.");
			}
				
			
			// Check if A and B are different revisions
			if (RevisionManagement.getRevisionNumber(graphName, branchNameA).equals(RevisionManagement.getRevisionNumber(graphName, branchNameB))) {
				// Branches are equal - throw error
				throw new InternalErrorException("Specified branches are equal: " + sparqlQuery);
			}
			
			// Check if both are terminal nodes
			if (!(RevisionManagement.isBranch(graphName, branchNameA) && RevisionManagement.isBranch(graphName, branchNameB))) {
				throw new InternalErrorException("Non terminal nodes were used: " + sparqlQuery);
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
						+ "WHERE { GRAPH <%s> {	%n"
						+ "	<%s> a rmo:Graph ;%n"
						+ "		sddo:hasDefaultSDD ?defaultSDD . %n"
						+ "} }", Config.revision_graph, graphName);
				
				ResultSet resultSetSDD = TripleStoreInterfaceSingleton.get().executeSelectQuery(querySDD);
				if (resultSetSDD.hasNext()) {
					QuerySolution qs = resultSetSDD.next();
					usedSDDURI = qs.getResource("?defaultSDD").toString();
				} else {
					throw new InternalErrorException("Error in revision graph! Selected graph <" + graphName + "> has no default SDD referenced.");
				}
			}

			// Get the common revision with shortest path
			String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionUriA, revisionUriB);
			
			// Create the revision progress for A and B
			String graphNameA = graphName + "-RM-REVISION-PROGRESS-A";
			String graphNameB = graphName + "-RM-REVISION-PROGRESS-B";
			String graphNameDiff = graphName + "-RM-DIFFERENCE-MODEL";
			String uriA = "http://eatld.et.tu-dresden.de/branch-A";
			String uriB = "http://eatld.et.tu-dresden.de/branch-B";
			
			MergeManagement.createRevisionProgress(MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriA), graphNameA, uriA);
			MergeManagement.createRevisionProgress(MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriB), graphNameB, uriB);
			
			// Create difference model
			MergeManagement.createDifferenceTripleModel(graphName,  graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI);
			
			// Differ between the different merge queries
			if ((action != null) && (action.equalsIgnoreCase("AUTO")) && (with == null) && (triples == null)) {
				logger.info("AUTO MERGE query detected");
				// Create the merged revision
				newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.AUTO, "");
			} else if ((action != null) && (action.equalsIgnoreCase("MANUAL")) && (with != null) && (triples != null)) {
				logger.info("MANUAL MERGE query detected");
				// Create the merged revision
				newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.MANUAL, triples);
			} else if ((action == null) && (with != null) && (triples != null)) {
				logger.info("MERGE WITH query detected");
				// Create the merged revision
				newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.WITH, triples);
			} else if ((action == null) && (with == null) && (triples == null)) {
				logger.info("MERGE query detected");
				// Check if difference model contains conflicts
				String queryASK = String.format(
						  "ASK { %n"
						+ "	GRAPH <%s> { %n"
						+ " 	?ref <http://eatld.et.tu-dresden.de/sddo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
						+ "	} %n"
						+ "}", graphNameDiff);
				if (TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK)) {
					// Difference model contains conflicts
					// Return the conflict model to the client
					responseBuilder = Response.status(Response.Status.CONFLICT);
					responseBuilder.entity(RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format));
				} else {
					// Difference model contains no conflicts
					// Create the merged revision
					newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, MergeQueryTypeEnum.COMMON, "");
				}
			} else {
				throw new InternalErrorException("This is not a valid MERGE query: " + sparqlQuery);
			}
			
			String graphNameHeader;
			try {
				graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				graphNameHeader = graphName;
			}
			
			
			// Return the revision number which were used (convert tag or branch identifier to revision number)
			responseBuilder.header(graphNameHeader + "-revision-number-of-branch-A", RevisionManagement.getRevisionNumber(graphName, branchNameA));
			responseBuilder.header(graphNameHeader + "-revision-number-of-branch-B", RevisionManagement.getRevisionNumber(graphName, branchNameB));			
			
			if (newRevisionNumber != null) {
				// Respond with next revision number
		    	responseBuilder.header(graphNameHeader + "-revision-number", newRevisionNumber);
				responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
				logger.debug("Respond with new revision number " + newRevisionNumber + ".");
			}
		}
		if (!foundEntry)
			throw new InternalErrorException("Error in query: " + sparqlQuery);
		
		return responseBuilder.build();	
	}
	
}
