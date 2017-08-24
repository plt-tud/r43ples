package de.tud.plt.r43ples.webservice;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import de.tud.plt.r43ples.draftobjects.HeaderInformation;
import de.tud.plt.r43ples.draftobjects.R43plesCoreInterface;
import de.tud.plt.r43ples.draftobjects.R43plesCoreSingleton;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.existentobjects.MergeCommit;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.*;
import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.merging.MergeResult;
import de.tud.plt.r43ples.merging.ui.MergingControl;



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

	
	@Context
	private UriInfo uriInfo;
	@Context
	private Request request;
	
	

	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);


	static final MediaType TEXT_TURTLE_TYPE = new MediaType("text", "turtle");
	static final MediaType APPLICATION_RDF_XML_TYPE = new MediaType("application", "rdf+xml");
	static final MediaType APPLICATION_SPARQL_RESULTS_XML_TYPE = new MediaType("application", "sparql-results+xml");
	
	
	/**map for client and mergingControlMap
	 * for each client there is a mergingControlMap**/
	protected static HashMap<String, HashMap<String, MergingControl>> clientMap = new HashMap<String, HashMap<String, MergingControl>>();
	
	


	/**
	 * HTTP POST interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * 
	 * @param revision_information
	 *            (optional) recent revision information about used revision graphs. If exisitent, commits are only
	 *            performed if revision information is up to date.
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
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml"})
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public final Response sparqlPOST(
			@FormParam("format") final String formatQuery, 
			@FormParam("query") @DefaultValue("") final String sparqlQuery,
			@HeaderParam("r43ples-revisiongraph") @DefaultValue("") final String revision_information,
			@FormParam("query_rewriting") @DefaultValue("") final String query_rewriting) throws InternalErrorException {
		try {
			String format = getFormat(formatQuery);
			logger.info("SPARQL POST query (format: "+format+", query: "+sparqlQuery +")" + revision_information);
			return sparql(format, sparqlQuery, revision_information, query_rewriting);
		} catch (Exception e) {
			return Response.serverError().status(Response.Status.NOT_ACCEPTABLE).build();
		}
	}

	private String getFormat(final String formatQuery) throws Exception {
		if (formatQuery == null){
			List<Variant> reqVariants = Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE, 
					MediaType.APPLICATION_JSON_TYPE, TEXT_TURTLE_TYPE, APPLICATION_RDF_XML_TYPE, APPLICATION_SPARQL_RESULTS_XML_TYPE).build();
			Variant bestVariant = request.selectVariant(reqVariants);
	        if (bestVariant == null) {
	        	throw new Exception("Requested datatype not available");
	        }
        	MediaType reqMediaType = bestVariant.getMediaType();
        	return reqMediaType.toString();
		}
		else {
			return formatQuery;
		}
	}
	
	/**
	 * HTTP POST interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Direct method (http://www.w3.org/TR/2013/REC-sparql11-protocol-20130321/#query-via-post-direct)
	 *
	 * @param revision_information
	 *            (optional) recent revision information about used revision graphs. If exisitent, commits are only
	 *            performed if revision information is up to date.
	 * @param sparqlQuery
	 *            the SPARQL query specified in the HTTP POST body
	 * @return HTTP response
	 * @throws InternalErrorException 
	 */
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	@Consumes("application/sparql-query")
	public final Response sparqlPOSTdirectly(
			@HeaderParam("r43ples-revisiongraph") @DefaultValue("") final String revision_information,
			final String sparqlQuery) throws InternalErrorException {
		List<Variant> reqVariants = Variant.mediaTypes(MediaType.TEXT_PLAIN_TYPE, MediaType.TEXT_HTML_TYPE, 
				MediaType.APPLICATION_JSON_TYPE, TEXT_TURTLE_TYPE, APPLICATION_RDF_XML_TYPE, APPLICATION_SPARQL_RESULTS_XML_TYPE).build();
		Variant bestVariant = request.selectVariant(reqVariants);
        if (bestVariant == null) {
            return Response.serverError().status(Response.Status.NOT_ACCEPTABLE).build();
        }
    	MediaType reqMediaType = bestVariant.getMediaType();
    	String format = reqMediaType.toString();
		logger.info("SPARQL POST query directly (format: "+format+", query: "+sparqlQuery +")");
		return sparql(reqMediaType.toString(), sparqlQuery, revision_information, false);
	}
	
	/**
	 * HTTP GET interface for query and update (e.g. SELECT, INSERT, DELETE).
	 * Provides HTML form if no query is specified and HTML is requested
	 * Provides Service Description if no query is specified and RDF
	 * representation is requested
	 *
	 * @param revision_information
	 *            (optional) recent revision information about used revision graphs. If exisitent, commits are only
	 *            performed if revision information is up to date.
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
	public final Response sparqlGET(
			@QueryParam("format") final String formatQuery, 
			@QueryParam("query") @DefaultValue("") final String sparqlQuery,
			@HeaderParam("r43ples-revisiongraph") @DefaultValue("") final String revision_information,
			@QueryParam("query_rewriting") @DefaultValue("") final String query_rewriting) throws InternalErrorException {
		String format;
		try {
			format = getFormat(formatQuery);
		} catch (Exception e) {
			return Response.serverError().status(Response.Status.NOT_ACCEPTABLE).build();
		}		
		
		String sparqlQueryDecoded;
		try {
			sparqlQueryDecoded = URLDecoder.decode(sparqlQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			sparqlQueryDecoded = sparqlQuery;
		}
		logger.info("SPARQL GET query (format: "+format+", query: "+sparqlQueryDecoded +")");
		return sparql(format, sparqlQueryDecoded, revision_information, query_rewriting);
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
	 * @param revision_information
	 *            (optional) recent revision information about used revision graphs. If exisitent, commits are only
	 *            performed if revision information is up to date.
	 * @param query_rewriting
	 * 			  should query rewriting option be used
	 * @return the response
	 * @throws InternalErrorException 
	 */
	private final Response sparql(final String format, final String sparqlQuery, final String revision_information, final boolean query_rewriting) throws InternalErrorException {
		if ("".equals(sparqlQuery)) {
			if (format.contains(MediaType.TEXT_HTML)) {
				return getHTMLResponse();
			} else {
				return getServiceDescriptionResponse(format);
			}
		} else {
			return getSparqlResponse(format, sparqlQuery, revision_information, query_rewriting);
		}
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
		return sparql(format, sparqlQuery, null, query_rewriting);
	}
	
	/**
	 * 
	 * @param format
	 *            mime type for response format
	 * @param sparqlQuery
	 *            decoded SPARQL query
	 * @param revision_information
	 *            (optional) recent revision information about used revision graphs. If exisitent, commits are only
	 *            performed if revision information is up to date.
	 * @param query_rewriting
	 * 			  string determining if query rewriting option be used
	 * @return
	 * @throws InternalErrorException
	 */
	private Response sparql(final String format, final String sparqlQuery, final String revision_information, final String query_rewriting) throws InternalErrorException {
		String option = query_rewriting.toLowerCase();
		if (option.equals("on") || option.equals("true") || option.equals("new"))
			return sparql(format, sparqlQuery, revision_information, true);
		else
			return sparql(format, sparqlQuery, revision_information, false);
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
		return sparql(format, sparqlQuery, null, false);
	}
	
	
	public final Response sparql(final String sparqlQuery) throws InternalErrorException {
		return sparql("application/xml", sparqlQuery, null, false);
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
	    htmlMap.put("graphList", RevisionManagementOriginal.getRevisedGraphsList());
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
		htmlMap.put("endpoint_active", true);
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
	private Response getSparqlResponse(String format, String sparqlQuery, final String revision_information, final boolean query_rewriting) throws InternalErrorException {
		logger.info(String.format("SPARQL request (format=%s, query_rewriting=%s, header=%s) -> %n %s", format, query_rewriting,revision_information, sparqlQuery));

		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		R43plesRequest request = new R43plesRequest(sparqlQuery, format, revision_information);

		String result;
		if (request.isSelectAskConstructQuery()) {
			result = Interface.sparqlSelectConstructAsk(request, query_rewriting);
		}
		else if (request.isUpdateQuery()) {
			r43plesCore.createUpdateCommit(request);
			result = "Query executed";
		}
		else if (request.isCreateGraphQuery()) {
			InitialCommit initialCommit = r43plesCore.createInitialCommit(request);
			result = "Graph <" + initialCommit.getGeneratedRevision().getRevisionGraph().getGraphName() + "> successfully created";
		}
		else if (request.isDropGraphQuery()) {
			Interface.sparqlDropGraph(sparqlQuery);
			result = "Graph successfully dropped";
		}
		else if (request.isBranchOrTagQuery()) {
			r43plesCore.createReferenceCommit(request);
			result = "Tagging or branching successful";
		}
		else if (request.isMergeQuery()) {
            logger.info("Merge query detected");
            return getMergeResponse(r43plesCore.createMergeCommit(request), request);
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
		HeaderInformation hi = new HeaderInformation();
		responseBuilder.header("r43ples-revisiongraph", hi.getResponseHeaderFromQuery(sparqlQuery));
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
	 * Creates the merge response.
	 *
	 * @param commit merge commit object containing all information about the merge
     * @param request the original R43ples request.
	 *
	 * @return the http response
	 * @throws InternalErrorException 
	 */
	private Response getMergeResponse(MergeCommit commit, R43plesRequest request) throws InternalErrorException {
		ResponseBuilder responseBuilder = Response.created(URI.create(""));

		if (commit.isHasConflict()) {
			responseBuilder = Response.status(Response.Status.CONFLICT);
			responseBuilder.entity(commit.getConflictModel());
		}
		String graphNameHeader;
		try {
			graphNameHeader = URLEncoder.encode(commit.getRevisionGraph().getGraphName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			graphNameHeader = commit.getRevisionGraph().getGraphName();
		}
		
		// Return the revision identifiers which were used (convert tag or branch identifier to revision identifier)
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-From", commit.getUsedSourceRevision().getRevisionIdentifier());
		responseBuilder.header(graphNameHeader + "-revision-number-of-branch-Into", commit.getUsedTargetRevision().getRevisionIdentifier());

		HeaderInformation hi = new HeaderInformation();
		responseBuilder.header("r43ples-revisiongraph", hi.getResponseHeaderFromQuery(request.query_sparql));
		
		return responseBuilder.build();	
	}

}
