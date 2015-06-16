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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.management.Interface;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.merge.MergeResult;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.visualisation.VisualisationBatik;
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
	private final Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
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
	private final Pattern patternMergeQuery =  Pattern.compile(
			"MERGE\\s*(?<action>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
			patternModifier);
	
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
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
		List<String> graphs = new ArrayList<>();
		if (graph.equals("1") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset1());
		}
		if (graph.equals("2") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset2());
		}
		if (graph.equals("merging") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetMerging());
		}
		if (graph.equals("merging-classes") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetMergingClasses());
		}
		if (graph.equals("renaming") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetRenaming());
		}
		if (graph.equals("complex-structure") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetComplexStructure());
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
			response.entity(VisualisationBatik.getHtmlOutput(graph));
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
			@FormParam("format") final String formatQuery, 
			@FormParam("query") @DefaultValue("") final String sparqlQuery,
			@FormParam("join_option") @DefaultValue("") final String join_option) throws InternalErrorException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		return sparql(format, sparqlQuery, join_option);
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
			@QueryParam("format") final String formatQuery, 
			@QueryParam("query") @DefaultValue("") final String sparqlQuery,
			@QueryParam("join_option") @DefaultValue("") final boolean join_option) throws InternalErrorException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		
		String sparqlQueryDecoded;
		try {
			sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			sparqlQueryDecoded = sparqlQuery;
		}
		return sparql(format, sparqlQueryDecoded, join_option);
	}
	
	
	@Path("debug")
	@GET
	public final String debug(@DefaultValue("") @QueryParam("query") final String sparqlQuery) {
		if (sparqlQuery.equals("")) {
			return getHTMLDebugResponse();
		} else {
			logger.info("Debug query was requested. Query: " + sparqlQuery);
			if (sparqlQuery.contains("INSERT")) {
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(sparqlQuery);
				return "Query executed";
			}
			else {
				String result = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(sparqlQuery, "text/html");
				return getHTMLResult( result, sparqlQuery);
			}
		}
	}
	
	
	/**
	 * Landing page
	 *
	 */
	@Path("/")
	@GET
	@Template(name = "/home.mustache")
	public final Map<String, Object> getLandingPage() {
		logger.info("Get Landing page");
		return htmlMap;
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
		htmlMap.put("merging_active", true);
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
	 * @throws InternalErrorException 
	 */
	public final Response sparql(final String format, final String sparqlQuery, final String join_option) throws InternalErrorException {
		if (sparqlQuery.equals("")) {
			if (format.contains(MediaType.TEXT_HTML)) {
				return getHTMLResponse();
			} else {
				return getServiceDescriptionResponse(format);
			}
		} else {
			return getSparqlResponse(format, sparqlQuery, join_option);
		}
	}
	
	public final Response sparql(final String format, final String sparqlQuery, final boolean join_option) throws InternalErrorException {
		if (join_option)
			return sparql(format, sparqlQuery, "new");
		else
			return sparql(format, sparqlQuery, "");
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
		return sparql(format, sparqlQuery, "");
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
	    htmlMap.put("endpoint_active", true);
	    mustache.execute(sw, htmlMap);		
		
		String content = sw.toString();
		
		return Response.ok().entity(content).type(MediaType.TEXT_HTML).build();
	}
	
	
	/**
	 * Get HTML debug response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return string containing html site
	 */
	private String getHTMLDebugResponse() {		
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/debug.mustache");
	    StringWriter sw = new StringWriter();
	    htmlMap.put("graphs", TripleStoreInterfaceSingleton.get().getGraphs());
	    htmlMap.put("revisionGraph", Config.revision_graph);
	    htmlMap.put("triplestore_type", Config.triplestore_type);
	    htmlMap.put("triplestore_url", Config.triplestore_url);
	    htmlMap.put("sdd_graph", Config.sdd_graph);
	    htmlMap.put("debug_active", true);
	    mustache.execute(sw, htmlMap);		
		
		String content = sw.toString();
		return content;
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
	 * @param format
	 * 			requested mime type 
	 * @param sparqlQuery
	 * 			string containing the SPARQL query
	 * @return HTTP response of evaluating the sparql query 
	 * @throws InternalErrorException
	 */
	private Response getSparqlResponse(final String format, String sparqlQuery, final String join_option) throws InternalErrorException {
		logger.info(String.format("SPARQL request (format=%s, join_option=%s) -> %n %s", format, join_option, sparqlQuery));
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
		
		String result;
		if (patternSelectAskConstructQuery.matcher(sparqlQuery).find()) {
			result = Interface.sparqlSelectConstructAsk(sparqlQuery, format, join_option);
		}
		else if (patternUpdateQuery.matcher(sparqlQuery).find()) {
			Interface.sparqlUpdate(sparqlQuery, user, message);
			result = "Query executed";
		}
		else if (patternCreateGraph.matcher(sparqlQuery).find()) {
			String graphName = Interface.sparqlCreateGraph(sparqlQuery);
			result = "Graph <"+graphName+"> successfully created";
		}
		else if (patternMergeQuery.matcher(sparqlQuery).find()) {
			return getMergeResponse(sparqlQuery, user, message, format);
		}
		else if (patternDropGraph.matcher(sparqlQuery).find()) {
			Interface.sparqlDropGraph(sparqlQuery);
			result = "Graph successfully dropped";
		}
		else if (patternBranchOrTagQuery.matcher(sparqlQuery).find()) {
			Interface.sparqlTagOrBranch(sparqlQuery, user, message);
			result = "Tagging or branching successful";
		}
		else
			throw new QueryErrorException("No R43ples query detected");

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
		
		MergeResult mresult = Interface.sparqlMerge(sparqlQuery, user, commitMessage, format);
		
		if (mresult.hasConflict) {
			responseBuilder = Response.status(Response.Status.CONFLICT);
			responseBuilder.entity(mresult.conflictModel);
		}
		String graphNameHeader;
		try {
			graphNameHeader = URLEncoder.encode(mresult.graph, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			graphNameHeader = mresult.graph;
		}
		
		// Return the revision number which were used (convert tag or branch identifier to revision number)
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-A", RevisionManagement.getRevisionNumber(mresult.graph, mresult.branchA));
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-B", RevisionManagement.getRevisionNumber(mresult.graph, mresult.branchB));			
		
		if (mresult.newRevisionNumber != null) {
			// Respond with next revision number
	    	responseBuilder.header(graphNameHeader + "-revision-number", mresult.newRevisionNumber);
			responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(mresult.graph));
			logger.debug("Respond with new revision number " + mresult.newRevisionNumber + ".");
		}

		return responseBuilder.build();	
	}

	
}
