package de.tud.plt.r43ples.webservice;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
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

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.management.Interface;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.merging.MergeManagement;
import de.tud.plt.r43ples.merging.MergeResult;
import de.tud.plt.r43ples.merging.RebaseQueryTypeEnum;
import de.tud.plt.r43ples.merging.control.MergingControl;
import de.tud.plt.r43ples.merging.control.RebaseControl;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;


/**
 * Provides SPARQL endpoint via [host]:[port]/r43ples/.
 * Supplies version information, service description as well as SPARQL queries.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * @author Xinyu Yang
 * 
 */
@Path("sparql")
public class Endpoint {

	private final static int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	
	private final static Pattern patternSelectAskConstructQuery = Pattern.compile(
			"(?<type>SELECT|ASK|CONSTRUCT).*WHERE\\s*\\{(?<where>.*)\\}", 
			patternModifier);
	private final static Pattern patternUpdateQuery = Pattern.compile(
			"(?<action>INSERT|DELETE).*<(?<graph>[^>]*)>",
			patternModifier);
	private final static Pattern patternCreateGraph = Pattern.compile(
			"CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final static Pattern patternDropGraph = Pattern.compile(
			"DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
			patternModifier);
	private final static Pattern patternBranchOrTagQuery = Pattern.compile(
			"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
			patternModifier);
	
	final static Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	final static Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
			patternModifier);

	final static Pattern patternMergeQuery =  Pattern.compile(
			"MERGE\\s*(?<action>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
			patternModifier);
	private final static Pattern patternFastForwardQuery =  Pattern.compile(
			"MERGE\\s*FF\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"",
			patternModifier);
	final static Pattern patternRebaseQuery =  Pattern.compile(
			"REBASE\\s*(?<action>AUTO|MANUAL|FORCE)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
			patternModifier);

	
	@Context
	private UriInfo uriInfo;
	
	

	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);
	
	
	/**map for client and mergingControlMap
	 * for each client there is a mergingControlMap**/
	protected static HashMap<String, HashMap<String, MergingControl>> clientMap = new HashMap<String, HashMap<String, MergingControl>>();
	
	


	/**
	 * HTTP POST interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * 
	 * @param formatHeader
	 *            format specified in the HTTP header
	 * @param formatQuery
	 *            format specified in the HTTP parameters
	 * @param sparqlQuery
	 *            the SPARQL query
	 * @param query_rewriting
	 * 			  should query rewriting option be used
	 * @return HTTP response
	 * @throws InternalErrorException 
	 */
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public final Response sparqlPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("format") final String formatQuery, 
			@FormParam("query") @DefaultValue("") final String sparqlQuery,
			@FormParam("query_rewriting") @DefaultValue("") final String query_rewriting) throws InternalErrorException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		logger.debug("SPARQL POST query (format: "+format+", query: "+sparqlQuery +")");
		return sparql(format, sparqlQuery, query_rewriting);
	}
	
	/**
	 * HTTP POST interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Direct method (http://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#query-via-post-direct)
	 * 
	 * @param formatHeader
	 *            format specified in the HTTP header
	 * @param sparqlQuery
	 *            the SPARQL query specified in the HTTP POST body
	 * @return HTTP response
	 * @throws InternalErrorException 
	 */
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	@Consumes("application/sparql-query")
	public final Response sparqlPOSTdirectly(@HeaderParam("Accept") final String formatHeader,
			final String sparqlQuery) throws InternalErrorException {
		logger.debug("SPARQL POST query directly (format: "+formatHeader+", query: "+sparqlQuery +")");
		return sparql(formatHeader, sparqlQuery);
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
	 * @param query_rewriting
	 * 			  should query rewriting option be used
	 * @return HTTP response
	 * @throws InternalErrorException 
	 */
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response sparqlGET(@HeaderParam("Accept") final String formatHeader,
			@QueryParam("format") final String formatQuery, 
			@QueryParam("query") @DefaultValue("") final String sparqlQuery,
			@QueryParam("query_rewriting") @DefaultValue("") final String query_rewriting) throws InternalErrorException {
		String format = (formatQuery != null) ? formatQuery : formatHeader;
		
		String sparqlQueryDecoded;
		try {
			sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			sparqlQueryDecoded = sparqlQuery;
		}
		return sparql(format, sparqlQueryDecoded, query_rewriting);
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
	 * @param query_rewriting
	 * 			  should query rewriting option be used
	 * @return the response
	 * @throws InternalErrorException 
	 */
	public final Response sparql(final String format, final String sparqlQuery, final boolean query_rewriting) throws InternalErrorException {
		if (sparqlQuery.equals("")) {
			if (format.contains(MediaType.TEXT_HTML)) {
				return getHTMLResponse();
			} else {
				return getServiceDescriptionResponse(format);
			}
		} else {
			return getSparqlResponse(format, sparqlQuery, query_rewriting);
		}
	}
	
	/**
	 * 
	 * @param format
	 *            mime type for response format
	 * @param sparqlQuery
	 *            decoded SPARQL query
	 * @param query_rewriting
	 * 			  string determining if query rewriting option be used
	 * @return
	 * @throws InternalErrorException
	 */
	private final Response sparql(final String format, final String sparqlQuery, final String query_rewriting) throws InternalErrorException {
		String option = query_rewriting.toLowerCase();
		if (option.equals("on") || option.equals("true") || option.equals("new"))
			return sparql(format, sparqlQuery, true);
		else
			return sparql(format, sparqlQuery, false);
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
		return sparql(format, sparqlQuery, false);
	}
	
	
	public final Response sparql(final String sparqlQuery) throws InternalErrorException {
		return sparql("application/xml", sparqlQuery, false);
	}
	
	
	

	/**
	 * Get HTML response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return HTML response for SPARQL form
	 */
	private Response getHTMLResponse() {
		logger.info("SPARQL form requested");
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/endpoint.mustache");
	    StringWriter sw = new StringWriter();
		Map<String, Object> htmlMap = new HashMap<String, Object>();
	    htmlMap.put("graphList", RevisionManagement.getRevisedGraphsList());
	    htmlMap.put("endpoint_active", true);
	    mustache.execute(sw, htmlMap);		
		String content = sw.toString();
		return Response.ok().entity(content).type(MediaType.TEXT_HTML).build();
	}


	
	/**
	 * Generates HTML representation of SPARQL query result 
	 * @param query SPARQL query which was passed to R43ples
	 * @param result result from the attached triplestore
	 */
	private String getHTMLResult(final String result, String query) {
		return getHTMLResult(result, query, null);
	}
	
	/**
	 * Generates HTML representation of SPARQL query result including the rewritten query 
	 * @param query SPARQL query which was passed to R43ples
	 * @param query_rewritten rewritten SPARQL query passed to triplestore
	 * @param result result from the attached triplestore
	 */
	private String getHTMLResult(final String result, String query, String query_rewritten) {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/result.mustache");
		StringWriter sw = new StringWriter();
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("result", result);
		htmlMap.put("query", query);
		htmlMap.put("query_rewritten", query_rewritten);
		mustache.execute(sw, htmlMap);		
		return sw.toString();
	}

	
	/**
	 * @param format
	 * 			requested mime type 
	 * @param sparqlQuery
	 * 			string containing the SPARQL query
	 * @param query_rewriting
	 * 			  should query rewriting option be used
	 * @return HTTP response of evaluating the sparql query 
	 * @throws InternalErrorException
	 */
	private Response getSparqlResponse(final String format, String sparqlQuery, final boolean query_rewriting) throws InternalErrorException {
		logger.info(String.format("SPARQL request (format=%s, query_rewriting=%s) -> %n %s", format, query_rewriting, sparqlQuery));
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
			result = Interface.sparqlSelectConstructAsk(sparqlQuery, format, query_rewriting);
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
			return getThreeWayMergeResponse(sparqlQuery, user, message, format);
		}
		else if (patternDropGraph.matcher(sparqlQuery).find()) {
			Interface.sparqlDropGraph(sparqlQuery);
			result = "Graph successfully dropped";
		}
		else if (patternBranchOrTagQuery.matcher(sparqlQuery).find()) {
			Interface.sparqlTagOrBranch(sparqlQuery, user, message);
			result = "Tagging or branching successful";
		}
		else if (patternFastForwardQuery.matcher(sparqlQuery).find()) {					
			if (Interface.sparqlFastForwardMerge(sparqlQuery, user, message))
				result = "Fast Forward Merge successful";
			else
				result = "Error in Fast Forward Merge";
		}
		else if (patternRebaseQuery.matcher(sparqlQuery).find()) {
			return getRebaseResponse(sparqlQuery, user, message, format);
		}
		else
			throw new QueryErrorException("No R43ples query detected");

		ResponseBuilder responseBuilder = Response.ok();
		if (format.equals("text/html")){
			if (query_rewriting) {
				responseBuilder.entity(getHTMLResult(result, sparqlQuery, SparqlRewriter.rewriteQuery(sparqlQuery)));
			}
			else {
				responseBuilder.entity(getHTMLResult(result, sparqlQuery));
			}
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
	 * Using command: MERGE GRAPH \<graphURI\> BRANCH "branchNameA" INTO "branchNameB"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @param format the result format
	 * @throws InternalErrorException 
	 */
	Response getThreeWayMergeResponse(final String sparqlQuery, final String user, final String commitMessage, final String format) throws InternalErrorException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Three-Way-Merge query detected");
		
		MergeResult mresult = Interface.sparqlThreeWayMerge(sparqlQuery, user, commitMessage, format);
		
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
		String revisionGraph = RevisionManagement.getRevisionGraph(mresult.graph);
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-A", RevisionManagement.getRevisionNumber(revisionGraph, mresult.branchA));
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-B", RevisionManagement.getRevisionNumber(revisionGraph, mresult.branchB));			
		
		responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(sparqlQuery));	
		
		return responseBuilder.build();	
	}
	
	
	
	/** 
	 * Creates response query and get Response of it.
	 * 
	 * Using command: RESBASE (AUTO|FORCE|MANUAL) GRAPH <graphURI> BRANCH "branchNameA" INTO "branchNameB"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @throws InternalErrorException 
	 */
	
	Response getRebaseResponse(final String sparqlQuery, final String user, final String commitMessage, final String format) throws InternalErrorException {
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		logger.info("Three-Way-Merge query detected");
		
		// TODO: make it similar to ThreeWayMerge
		//MergeResult mresult = Interface.sparqlThreeWayMerge(sparqlQuery, user, commitMessage, format);
		
		
		Matcher m = patternRebaseQuery.matcher(sparqlQuery);
		

		if (!m.find()){
			throw new InternalErrorException("Error in query: " + sparqlQuery);
		}
	
		String action = m.group("action");
		String graphName = m.group("graph");
		String sdd = m.group("sdd");
		String branchNameA = m.group("branchNameA").toLowerCase();
		String branchNameB = m.group("branchNameB").toLowerCase();
		String with = m.group("with");
		String triples = m.group("triples");
		String type;
		if(action ==null) {
			type = "COMMON";
		}else if (action.equalsIgnoreCase("AUTO")){
			type = "AUTO";
		}else if(action.equalsIgnoreCase("FORCE")){
			type = "FORCE";
		}else if(action.equalsIgnoreCase("MANUAL")){
			type = "MANUAL";
		}else {
			throw new InternalErrorException("Error in SPARQL Merge Anfrage , type is not right.");
		}
		
		MergingControl mergingControl;
		RebaseControl rebaseControl;
		
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
	
		if(clientMap.containsKey(user) && (clientMap.get(user).containsKey(graphName))) {
			mergingControl = clientMap.get(user).get(graphName);
			rebaseControl = mergingControl.getRebaseControl();
		}else{
			mergingControl = new MergingControl();
			mergingControl.setRebaseControl();
			rebaseControl = mergingControl.getRebaseControl();
			rebaseControl.createCommitModel(graphName, sdd, user, commitMessage, branchNameA, branchNameB, "Rebase", type);
		}
					
		// get the last revision of each branch
		String revisionUriA = RevisionManagement.getRevisionUri(revisionGraph, branchNameA);
		String revisionUriB = RevisionManagement.getRevisionUri(revisionGraph, branchNameB);
			
		checkIfRebaseIsPossible(graphName, branchNameA, branchNameB);

		// Differ between MERGE query with specified SDD and without SDD			
		String usedSDDURI = RevisionManagement.getSDD(graphName, sdd);

		// Get the common revision with shortest path
		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionGraph, revisionUriA, revisionUriB);
		
		 
		// create the patch and patch group
		LinkedList<String> revisionList = MergeManagement.getPathBetweenStartAndTargetRevision(
				revisionGraph, commonRevision, revisionUriA);
		rebaseControl.createPatchGroupOfBranch(revisionGraph, revisionUriB, revisionList);
		
		
		// transform the response to the rebase freundlich check process
		String graphNameHeader;
		try {
			graphNameHeader = URLEncoder.encode(graphName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			graphNameHeader = graphName;
		}
		
		// Return the revision number which were used (convert tag or branch identifier to revision number)
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-A", RevisionManagement.getRevisionNumber(revisionGraph, branchNameA));
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-B", RevisionManagement.getRevisionNumber(revisionGraph, branchNameB));	
		
		
		String graphStrategy = "merging-strategy-information";
		
		if((action!= null) && (action.equalsIgnoreCase("FORCE"))) {
			rebaseControl.forceRebaseProcess(graphName);	
			responseBuilder.header(graphStrategy, "force-rebase");
			return responseBuilder.build();	
		}
			
		// Create the revision progress for A and B
		String graphNameA = graphName + "-RM-REVISION-PROGRESS-A";
		String graphNameB = graphName + "-RM-REVISION-PROGRESS-B";
		String graphNameDiff = graphName + "-RM-DIFFERENCE-MODEL";
		String uriA = "http://eatld.et.tu-dresden.de/branch-A";
		String uriB = "http://eatld.et.tu-dresden.de/branch-B";
		
		MergeManagement.createRevisionProgresses(revisionGraph, graphName,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriA), graphNameA, uriA, 
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriB), graphNameB, uriB);
		
		
		// Create difference model
		MergeManagement.createDifferenceTripleModel(graphName,  graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI);
		if ((action != null) && (action.equalsIgnoreCase("AUTO")) && (with == null) && (triples == null)) {
			logger.info("AUTO REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, RebaseQueryTypeEnum.AUTO, "");
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);
			
			String basisRevisionNumber = rebaseControl.forceRebaseProcess(graphName);
			RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples,
					user, commitMessage, basisRevisionNumber);
			
			responseBuilder.header(graphStrategy, "auto-rebase");
								
		} else if ((action != null) && (action.equalsIgnoreCase("MANUAL")) && (with != null) && (triples != null)) {
			logger.info("MANUAL REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, RebaseQueryTypeEnum.MANUAL, triples);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);
			
			String basisRevisionNumber = rebaseControl.forceRebaseProcess(graphName);
			RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples,
					user, commitMessage, basisRevisionNumber);
			
			responseBuilder.header(graphStrategy, "manual-rebase");
			
		} else if ((action == null) && (with != null) && (triples != null)) {
			logger.info("REBASE WITH query detected");
			// Create the merged revision -- newTriples
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, RebaseQueryTypeEnum.WITH, triples);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);
			
			String basisRevisionNumber = rebaseControl.forceRebaseProcess(graphName);
			RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples,
					user, commitMessage, basisRevisionNumber);
			
			responseBuilder.header(graphStrategy, "with-rebase");
						
		} else if ((action == null) && (with == null) && (triples == null)) {
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
				logger.info("rebase conflict");
				responseBuilder = Response.status(Response.Status.CONFLICT);
				responseBuilder.header(graphStrategy, "rebase-unfreundlich");
				// write the difference model in the response builder
				responseBuilder.entity(RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format));
			} else{
				rebaseControl.forceRebaseProcess(graphName);	
				responseBuilder.entity(RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format));				
			}
					
		} else {
			throw new InternalErrorException("This is not a valid MERGE query: " + sparqlQuery);
		}				
	
		responseBuilder.header("r43ples-revisiongraph", RevisionManagement.getResponseHeaderFromQuery(sparqlQuery));	
				
		return responseBuilder.build();			
	}

	/** simple checks if rebase could be possible for these two branches of a graph
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * @throws InternalErrorException throws an error if it is not possible
	 */
	private void checkIfRebaseIsPossible(String graphName, String branchNameA,
			String branchNameB) throws InternalErrorException {
		// Check if graph already exists
		if (!RevisionManagement.checkGraphExistence(graphName)){
			logger.error("Graph <"+graphName+"> does not exist.");
			throw new InternalErrorException("Graph <"+graphName+"> does not exist.");
		}
	
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		// Check if A and B are different revisions
		if (RevisionManagement.getRevisionNumber(revisionGraph, branchNameA).equals(RevisionManagement.getRevisionNumber(revisionGraph, branchNameB))) {
			// Branches are equal - throw error
			throw new InternalErrorException("Specified branches are equal");
		}
		
		// Check if both are terminal nodes
		if (!(RevisionManagement.isBranch(graphName, branchNameA) && RevisionManagement.isBranch(graphName, branchNameB))) {
			throw new InternalErrorException("Non terminal nodes were used ");
		}
	}
	
}
