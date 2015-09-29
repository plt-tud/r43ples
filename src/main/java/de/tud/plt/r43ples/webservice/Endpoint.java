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
import java.util.Iterator;
import java.util.LinkedList;
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
import de.tud.plt.r43ples.management.Interface;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.merging.MergeManagement;
import de.tud.plt.r43ples.merging.MergeQueryTypeEnum;
import de.tud.plt.r43ples.merging.MergeResult;
import de.tud.plt.r43ples.merging.RebaseQueryTypeEnum;
import de.tud.plt.r43ples.merging.control.FastForwardControl;
import de.tud.plt.r43ples.merging.control.MergingControl;
import de.tud.plt.r43ples.merging.control.RebaseControl;
import de.tud.plt.r43ples.merging.management.ProcessManagement;
import de.tud.plt.r43ples.merging.management.StrategyManagement;
import de.tud.plt.r43ples.merging.model.structure.CommitModel;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.visualisation.VisualisationBatik;
import de.tud.plt.r43ples.visualisation.VisualisationD3;

/**
 * Provides SPARQL endpoint via [host]:[port]/r43ples/.
 * Supplies version information, service description as well as SPARQL queries.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 * @author Xinyu Yang
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
	
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"", 
			patternModifier);

	private final Pattern patternMergeQuery =  Pattern.compile(
			"MERGE\\s*(?<action>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
			patternModifier);
	private final Pattern patternFastForwardQuery =  Pattern.compile(
			"MERGE\\s*FF\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"",
			patternModifier);
	private final Pattern patternRebaseQuery =  Pattern.compile(
			"REBASE\\s*(?<action>AUTO|MANUAL|FORCE)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(\\s*(?<sdd>SDD)?\\s*<(?<sddURI>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(\\s*(?<with>WITH)?\\s*\\{(?<triples>.*)\\})?",
			patternModifier);

	
	@Context
	private UriInfo uriInfo;
	
	

	
	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Endpoint.class);
	
	
	/**map for client and mergingControlMap
	 * for each client there is a mergingControlMap**/
	protected static HashMap<String, HashMap<String, MergingControl>> clientMap = new HashMap<String, HashMap<String, MergingControl>>();
	
	
	
	/**
	 * Creates sample datasets
	 * @return information provided as HTML response
	 * @throws InternalErrorException 
	 */
	@Path("createSampleDataset")
	@GET
	@Template(name = "/exampleDatasetGeneration.mustache")
	public final Map<String, Object> createSampleDataset(@QueryParam("dataset") @DefaultValue("all") final String graph) throws InternalErrorException {
		List<String> graphs = new ArrayList<>();
		
		if (graph.equals("1") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset1().graphName);
		}
		if (graph.equals("2") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset2().graphName);
		}
		if (graph.equals("merging") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetMerging().graphName);
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
		if (graph.equals("rebase") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetRebase());
		}
		if (graph.equals("forcerebase") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetForceRebase());
		}
		if (graph.equals("fastforward") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetFastForward());
		}
		Map<String, Object> htmlMap = new HashMap<String, Object>();
	    htmlMap.put("graphs", graphs);
	    
		return htmlMap;			
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
			MediaType.APPLICATION_SVG_XML, "application/ld+json" })
	public final Response getRevisionGraph(@HeaderParam("Accept") final String format_header,
			@QueryParam("format") final String format_query, @QueryParam("graph") @DefaultValue("") final String graph) {
		logger.info("Get Revision Graph: " + graph);
		String format = (format_query != null) ? format_query : format_header;
		logger.info("format: " + format);
		logger.info("format_header"+ format_header);
		
		ResponseBuilder response = Response.ok();
		if (format.equals("batik")) {
			response.type(MediaType.TEXT_HTML);
			response.entity(VisualisationBatik.getHtmlOutput(graph));
		} else if (format.equals("d3")) {
			response.entity(VisualisationD3.getHtmlOutput(graph));
		}
		else {
			response.entity(RevisionManagement.getRevisionInformation(graph, format));
			response.type(format);
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
		logger.debug("SPARQL POST query (format: "+format+", query: "+sparqlQuery +")");
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
			@QueryParam("join_option") @DefaultValue("") final String join_option) throws InternalErrorException {
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
	public final String debug(@DefaultValue("") @QueryParam("query") final String sparqlQuery) throws UnsupportedEncodingException, InternalErrorException {
		if (sparqlQuery.equals("")) {
			return getHTMLDebugResponse();
		} else {
			String query =  URLDecoder.decode(sparqlQuery, "UTF-8");
			logger.info("Debug query was requested. Query: " + query);
			if (sparqlQuery.contains("INSERT")) {
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
				return "Query executed";
			}
			else {
				String result = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(query, "text/html");
				return getHTMLResult( result, sparqlQuery);
			}
		}
	}
	
	
	/**
	 * Landing page
	 *
	 */
	@GET
	@Template(name = "/home.mustache")
	//@Produces(MediaType.TEXT_HTML)
	public final Map<String, Object> getLandingPage() {
		logger.info("Get Landing page");
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		htmlMap.put("git", GitRepositoryState.getGitRepositoryState());	
		return htmlMap;
	}
	
	/**
	 * get merging seite and input merging information
	 * */
	@Path("merging")
	@GET
    @Produces({ "text/turtle", "application/rdf+xml", MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,
		 MediaType.APPLICATION_SVG_XML })
	public final Response getMerging(
			@HeaderParam("Accept") final String format_header,
			@QueryParam("graph") final String graph) {
		logger.info("Merging -- graph: " + graph);		
		ResponseBuilder response = Response.ok();
		response.entity(MergingControl.getMenuHtmlOutput()).type(MediaType.TEXT_HTML);
		return response.build();
	}
	
	
	
	/**
	 * mergingProcess: create mergingQuery 
	 * create RevisionProcess Model A
	 * create RevisionProcess Model B
	 * create Difference model 
	 * @throws ConfigurationException 
	 * @throws IOException 
	 */
	@Path("mergingProcess")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response mergingPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("optradio") final String model, 
			@FormParam("graph") @DefaultValue("") final String graphName,
			@FormParam("sdd") final String sddName,
			@FormParam("strategie") final String strategie,
			@FormParam("Branch1") final String branch1,
			@FormParam("Branch2") final String branch2,
			@FormParam("user") @DefaultValue("") final String user,
			@FormParam("message") @DefaultValue("") final String message) throws InternalErrorException, ConfigurationException, IOException {
			
		ResponseBuilder response = Response.ok();
		
		// Fast Forward
		if(strategie.equals("Fast-Forward")){
			CommitModel commitModel = new CommitModel(graphName, sddName, user, message, branch1, branch2, "Fast-Forward", null);
			StrategyManagement.saveGraphVorMergingInMap(graphName, "application/json");
			executeFastForward(graphName, branch1, branch2);
			response.entity(commitModel.getReportView());
			return response.build();	
		}
		// Rebase
		else if(strategie.equals("Rebase")){
			RebaseQueryTypeEnum type = null;			
			if (model.equals("auto")) {
				type = RebaseQueryTypeEnum.AUTO;
			} else if(model.equals("common")){
				type = RebaseQueryTypeEnum.COMMON;
			} else{
				type = RebaseQueryTypeEnum.MANUAL;
			}
		
			String rebaseQuery = StrategyManagement.createRebaseQuery(graphName, sddName, user, message, branch1, branch2, type, null);
			StrategyManagement.saveGraphVorMergingInMap(graphName, "application/json");
			
			Matcher userMatcher = patternUser.matcher(rebaseQuery);
			logger.debug("test mergeQuery:"+rebaseQuery);
			if (userMatcher.find()) {
				rebaseQuery = userMatcher.replaceAll("");
			}
			Matcher messageMatcher = patternCommitMessage.matcher(rebaseQuery);
			if (messageMatcher.find()) {
				rebaseQuery = messageMatcher.replaceAll("");
			}
			
			//for each client, create a mergingControlMap and for each named graph create a mergingControl, first check, if namedgraph exist .
			//first create the mergingcontrol and than create the rebasecontrol
			MergingControl mergingControl;
			if(!clientMap.containsKey(user)){
				mergingControl = new MergingControl();
				clientMap.put(user, new HashMap<String, MergingControl>());
				clientMap.get(user).put(graphName, mergingControl);
			}else if(clientMap.containsKey(user) && (!clientMap.get(user).containsKey(graphName))) {
				mergingControl = new MergingControl();
				clientMap.get(user).put(graphName, mergingControl);
			}else{
				mergingControl = clientMap.get(user).get(graphName);
			}
					
			
			mergingControl.setRebaseControl();
			mergingControl.closeRebaseModel();
			
			RebaseControl rebaseControl = mergingControl.getRebaseControl();
			
			rebaseControl.createCommitModel(graphName, sddName, user, message, branch1, branch2, "Rebase", type.toString());
			
			Response responsePost = null;
			if (patternRebaseQuery.matcher(rebaseQuery).find()) {
				responsePost= getRebaseResponse(rebaseQuery, user, message, "HTML");
				logger.debug("rebase response status: "+responsePost.getStatusInfo().toString());	
				logger.debug("rebase response Header: "+responsePost.getHeaders().get("merging-strategy-information").get(0).toString());	
				logger.debug("rebase response Header: "+responsePost.getHeaders().keySet().toString());	
			}
			
			if((responsePost.getHeaders().get("merging-strategy-information").get(0).toString().equals("rebase-unfreundlich"))) {
				// to do show force rebase html view , show the rebase result graph
				//RebaseControl.forceRebaseProcess(graphName);
				
				//boolean isRebaeFreundlich = RebaseControl.checkRebaseFreundlichkeit(responsePost, graphName, branch1, branch2);
				
				// to do manual arbeit
				logger.info("rebase unfreundlich !");
				
				response.entity(rebaseControl.showRebaseDialogView());
				
				return response.build();
			}else{
				logger.info("sparql query is force rebase! ");	
			}

			String rebaseResultView = rebaseControl.getRebaseReportView(graphName);
			response.entity(rebaseResultView);
			return response.build();		
		}
		// Three Way Merge
		else {
			Response responsePost = null;
			MergeQueryTypeEnum type = null;
			if (model.equals("auto")) {
				type = MergeQueryTypeEnum.AUTO;
			} else if (model.equals("common")) {
				type = MergeQueryTypeEnum.COMMON;
			} else {
				type = MergeQueryTypeEnum.MANUAL;
			}
			
			//for each client ,create a mergingControl, first check, if named graph exists
			MergingControl mergingControl;
			if(!clientMap.containsKey(user)){
				mergingControl = new MergingControl();
				clientMap.put(user, new HashMap<String, MergingControl>());
				clientMap.get(user).put(graphName, mergingControl);
			}else if(clientMap.containsKey(user) && (!clientMap.get(user).containsKey(graphName))) {
				mergingControl = new MergingControl();
				clientMap.get(user).put(graphName, mergingControl);
			}else{
				mergingControl = clientMap.get(user).get(graphName);
			}

			mergingControl.closeRebaseModel();
			
			//save commit information in MergingControl
			mergingControl.createCommitModel(graphName, sddName, user, message, branch1, branch2, "Three-Way", type.toString());
						
			
			//save the graph information vor merging 
			StrategyManagement.saveGraphVorMergingInMap(graphName, "application/json" );
				
			String mergeQuery = ProcessManagement.createMergeQuery(graphName, sddName, user, message, type, branch1, branch2, null);
			logger.debug("test mergeQuery:"+mergeQuery);
			
			Matcher userMatcher = patternUser.matcher(mergeQuery);
			if (userMatcher.find()) {
				mergeQuery = userMatcher.replaceAll("");
			}
			Matcher messageMatcher = patternCommitMessage.matcher(mergeQuery);
			if (messageMatcher.find()) {
				mergeQuery = messageMatcher.replaceAll("");
			}

			if (patternMergeQuery.matcher(mergeQuery).find()) {
				responsePost= getMergeResponse(mergeQuery, user, message,"HTML");
			}
							
			if(!(responsePost.getStatusInfo() == Response.Status.CONFLICT)){
				response.entity(mergingControl.getThreeWayReportView(null));				
				return response.build();
			}

			mergingControl.getMergeProcess(responsePost, graphName, branch1, branch2, "text/html");
			response.entity(mergingControl.getViewHtmlOutput());
			return response.build();
		}	
	}
	
	


	/**
	 * query revision information and get the graph
	 * by ajax  */
	@Path("loadOldGraphProcess")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public final Response fastForwardGET(@HeaderParam("Accept") final String formatHeader,
			 @QueryParam("graph") @DefaultValue("") final String graph, @QueryParam("format") @DefaultValue("application/json") final String format) throws InternalErrorException {

		ResponseBuilder response = Response.ok();
		logger.info("loadOldGraphProcess: "+ graph);

		response.type(format);
		response.entity(StrategyManagement.loadGraphVorMergingFromMap(graph));
		
		return response.build();
	}	
	
	/**
	 * by rebase unfreundlich, select the force rebase process
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	@Path("forceRebaseProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response forceRebaseProcessGET(@QueryParam("graph") @DefaultValue("") final String graph, 
			@QueryParam("client") @DefaultValue("") final String user ) throws InternalErrorException {

		ResponseBuilder response = Response.ok();
		//get rebaseControl form map
		RebaseControl rebaseControl = clientMap.get(user).get(graph).getRebaseControl();
		rebaseControl.forceRebaseProcess(graph);
		response.entity(rebaseControl.getRebaseReportView(null));
		return response.build();
	}	
	
	/**
	 * by rebase unfreundlich, select the manuell rebase process
	 * @throws InternalErrorException 
	 * @throws ConfigurationException 
	 * @throws IOException 
	 */
	@Path("manualRebaseProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response manualRebaseProcessGET( @QueryParam("graph") @DefaultValue("") final String graph, 
			@QueryParam("client") @DefaultValue("") final String user ) throws InternalErrorException, ConfigurationException, IOException {

		ResponseBuilder response = Response.ok();
		
		//get MergingControl and rebaseControl form map
		MergingControl mergingControl = clientMap.get(user).get(graph);	
		RebaseControl rebaseControl = mergingControl.getRebaseControl();	
		rebaseControl.manualRebaseProcess();
		
		response.entity(mergingControl.getViewHtmlOutput());
		
		return response.build();
	}	
	
	
	/**
	 * by triple approve to server call this method
	 */
	
	@Path("approveProcess")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final void approvePOST(@HeaderParam("Accept") final String formatHeader, @FormParam("isChecked") @DefaultValue("") final String isChecked,
			@FormParam("id") @DefaultValue("") final String id, @FormParam("graph") @DefaultValue("") final String graph,
			@FormParam("client") @DefaultValue("") final String user) throws IOException, InternalErrorException {
		logger.info("approve test: "+id);
		logger.info("isChecked: " + isChecked);
		
		MergingControl mergingControl = clientMap.get(user).get(graph);
		
		mergingControl.approveToDifferenceModel(id, isChecked);
		
		
	}
	
	
	/**
	 * by high level view approve to server call this method
	 */
	@Path("approveHighLevelProcess")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final void approveHighLevelPOST(@HeaderParam("Accept") final String formatHeader, @FormParam("isChecked") @DefaultValue("") final String isChecked,
			@FormParam("id") @DefaultValue("") final String id, @FormParam("graph") @DefaultValue("") final String graph, 
			@FormParam("client") @DefaultValue("") final String user) throws IOException, InternalErrorException {
		logger.info("approve high test: "+id);
		logger.info("isChecked: " + isChecked);
		
		MergingControl mergingControl = clientMap.get(user).get(graph);
		
		mergingControl.approveHighLevelToDifferenceModel(id, isChecked);
	}
	
	
	/**push check : coflict approved check , difference approved change check
	 * reportResult create
	 * save the triplesId in checkbox
	 * todo 
	 * @throws ConfigurationException 
	*/
	@Path("reportProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response reportGET( @QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user ) throws InternalErrorException, ConfigurationException {
		
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = clientMap.get(user).get(graph);
		response.entity(mergingControl.createReportProcess());
		return response.build();	
	}	

	
	/** new push process with report view
	 * @throws TemplateException 
	 * @throws ConfigurationException 
	 * @throws IOException */
	@Path("pushProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response pushReportGET( @QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user) throws InternalErrorException, ConfigurationException, IOException {
		
		ResponseBuilder response = Response.ok();
			
		MergingControl mergingControl = clientMap.get(user).get(graph);
		//save the graph information before merging 
		StrategyManagement.saveGraphVorMergingInMap(graph, "application/json");
		
		String mergeQuery = mergingControl.updateMergeQuery();
		
		String userCommit = null;
		Matcher userMatcher = patternUser.matcher(mergeQuery);
		if (userMatcher.find()) {
			userCommit = userMatcher.group("user");
			mergeQuery = userMatcher.replaceAll("");
		}
		String messageCommit = null;
		Matcher messageMatcher = patternCommitMessage.matcher(mergeQuery);
		if (messageMatcher.find()) {
			messageCommit = messageMatcher.group("message");
			mergeQuery = messageMatcher.replaceAll("");
		}

		if (patternMergeQuery.matcher(mergeQuery).find()) {
			getMergeResponse(mergeQuery, userCommit, messageCommit,"HTML");
		}
			
		response.entity(mergingControl.getThreeWayReportView(null));
		
		clientMap.get(user).remove(graph);
		if(clientMap.get(user).isEmpty()){
			clientMap.remove(user);
		}
		
		return response.build();

	}	
	
	
	/** rebase push process with report view
	 * @throws ConfigurationException */
	@Path("rebasePushProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response rebasePushReportGET(@QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user ) throws IOException, InternalErrorException, ConfigurationException {
		
		ResponseBuilder response = Response.ok();
		
		MergingControl mergingControl = clientMap.get(user).get(graph);
		RebaseControl rebaseControl = mergingControl.getRebaseControl();
		
		mergingControl.transformDifferenceModelToRebase();
		
		// update the new rebase merge query
		String mergeQuery = mergingControl.updateMergeQuery();
		
		logger.info("rebase updated merge query: "+ mergeQuery);
		// execute the getRebaseResponse()
		Matcher userMatcher = patternUser.matcher(mergeQuery);
		if (userMatcher.find()) {
			mergeQuery = userMatcher.replaceAll("");
		}
		Matcher messageMatcher = patternCommitMessage.matcher(mergeQuery);
		if (messageMatcher.find()) {
			mergeQuery = messageMatcher.replaceAll("");
		}

		getRebaseResponse(mergeQuery, user, mergingControl.getCommitModel().getMessage(), "HTML");
				
		
		String rebaseResultView = rebaseControl.getRebaseReportView(null);
		
		response.entity(rebaseResultView);
		
		//close rebase model
		mergingControl.closeRebaseModel();
		
		//after push remove the graph and release the space
		clientMap.get(user).remove(graph);
		if(clientMap.get(user).isEmpty()){
			clientMap.remove(user);
		}
		
		return response.build();
	}	
	
	

	

	
	

	
	/**load individual View
	  */
	
	@Path("individualView")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response individualGET( @QueryParam("graph") @DefaultValue("") final String graph,
			 @QueryParam("client") @DefaultValue("") final String user ) {
		logger.info("Get individual view: "+ graph);
		ResponseBuilder response = Response.ok();
		
		MergingControl mergingControl = clientMap.get(user).get(graph);
		
		response.entity(mergingControl.getIndividualView());
		return response.build();
	}
	
	
	/**load updated triple View
	 * @throws ConfigurationException */
	
	@Path("tripleView")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response tripleViewGET(@QueryParam("graph") @DefaultValue("") final String graph, 
			@QueryParam("client") @DefaultValue("") final String user ) throws ConfigurationException {
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = clientMap.get(user).get(graph);
		response.entity(mergingControl.getTripleView());
		return response.build();
	}
	
	
	/**load High Level Change Table View 
	 * @throws TemplateException *
	 * @throws IOException *
	 * @throws ConfigurationException */
	
	@Path("highLevelView")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response highLevelGET(@QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user ) {
		
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = clientMap.get(user).get(graph);
		response.entity(mergingControl.getHighLevelView());
		return response.build();
	}
	
	
	
	/**with individual filter the Triple in tripleTable
	 * @param individualA individual of Branch A
	 * @param individualB individual of Branch B */
	
	@Path("individualFilter")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response individualFilterPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("individualA") @DefaultValue("null") final String individualA,
			@FormParam("individualB") @DefaultValue("null") final String individualB, @FormParam("graph") @DefaultValue("") final String graph, 
			@FormParam("client") @DefaultValue("") final String user ) {
		
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = clientMap.get(user).get(graph);
		
		logger.info("individualFilter A Array :"+ individualA);
		logger.info("individualFilter B Array :"+ individualB);
		
		String individualFilter = mergingControl.getIndividualFilter(individualA, individualB);
		
		logger.info(individualB.isEmpty());
		response.entity(individualFilter);
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
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public final Response sparql(final String format, final String sparqlQuery, final boolean join_option) throws InternalErrorException {
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
	
	private final Response sparql(final String format, final String sparqlQuery, final String join_option) throws InternalErrorException {
		String option = join_option.toLowerCase();
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
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public final Response sparql(final String format, final String sparqlQuery) throws InternalErrorException {
		return sparql(format, sparqlQuery, false);
	}
	
	
	/**
	 * Get HTML debug response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return HTML response for SPARQL form
	 * @throws InternalErrorException 
	 */
	private String getHTMLDebugResponse() throws InternalErrorException {		
		logger.info("Get Debug page");
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		htmlMap.put("git", GitRepositoryState.getGitRepositoryState());
		htmlMap.put("graphs", TripleStoreInterfaceSingleton.get().getGraphs());
	    htmlMap.put("revisionGraph", Config.revision_graph);
	    htmlMap.put("triplestore_type", Config.triplestore_type);
	    htmlMap.put("triplestore_url", Config.triplestore_url);
	    htmlMap.put("sdd_graph", Config.sdd_graph);
	    htmlMap.put("debug_active", true);
	    StringWriter sw = new StringWriter();	    
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/debug.mustache");
	    mustache.execute(sw, htmlMap);		
		return sw.toString();
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
	    htmlMap.put("graphList", RevisionManagement.getRevisedGraphs());
	    htmlMap.put("endpoint_active", true);
	    mustache.execute(sw, htmlMap);		
		String content = sw.toString();
		return Response.ok().entity(content).type(MediaType.TEXT_HTML).build();
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
		Map<String, Object> htmlMap = new HashMap<String, Object>();
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
	private Response getSparqlResponse(final String format, String sparqlQuery, final boolean join_option) throws InternalErrorException {
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
		else if (patternFastForwardQuery.matcher(sparqlQuery).find()) {					
			result = getFastForwardResponse(sparqlQuery, user, message);
		}
		else if (patternRebaseQuery.matcher(sparqlQuery).find()) {
			return getRebaseResponse(sparqlQuery, user, message, format);
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
	 * Using command: MERGE GRAPH \<graphURI\> BRANCH "branchNameA" INTO "branchNameB"
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
	
	/** 
	 * Creates fast forward merging.
	 * 
	 * Using command: MERGE FF GRAPH /<graphURI/> BRANCH "branchNameA" INTO "branchNameB"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	private String getFastForwardResponse(final String sparqlQuery, final String user, final String commitMessage) throws InternalErrorException {
		Matcher m = patternFastForwardQuery.matcher(sparqlQuery);
		if (!m.find())
			throw new InternalErrorException("Error in query: " + sparqlQuery);
			
		String graphName = m.group("graph");
		String branchNameA = m.group("branchNameA").toLowerCase();
		String branchNameB = m.group("branchNameB").toLowerCase();
		
		if (executeFastForward(graphName, branchNameA, branchNameB)) {
			return "Fast Forward succesful";
		}
		else {	
			return "Fast Forward not possible";
		}
	}
	
	/**
	 * 
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * 
	 * @return if fast-forward was successful
	 * @throws InternalErrorException 
	 */
	private boolean executeFastForward(String graphName, String branchNameA, String branchNameB) throws InternalErrorException
	{
		if (!FastForwardControl.fastForwardCheck(graphName, branchNameA, branchNameB)) {
			return false;
		}
		String branchUriA = RevisionManagement.getBranchUri(graphName, branchNameA);
		String branchUriB = RevisionManagement.getBranchUri(graphName, branchNameB);
		
		String fullGraphUriA = RevisionManagement.getFullGraphUri(branchUriA);
		String fullGraphUriB = RevisionManagement.getFullGraphUri(branchUriB);

		logger.info("ff fullgraph : "+ branchUriA + branchUriB + fullGraphUriA+ fullGraphUriB);
		String revisionUriA = RevisionManagement.getRevisionUri(graphName, branchNameA);
		String revisionUriB = RevisionManagement.getRevisionUri(graphName, branchNameB);
		
		StrategyManagement.moveBranchReference(branchUriB, revisionUriB, revisionUriA);
		StrategyManagement.updateRevisionOfBranch(branchUriB, revisionUriB, revisionUriA);	
		StrategyManagement.fullGraphCopy(fullGraphUriA, fullGraphUriB);
		return true;
	}
	
	/** 
	 * Creates response query and get Response of it.
	 * 
	 * Using command: RESBASE (AUTO|FORCE|MANUAL) GRAPH <graphURI> BRANCH "branchNameA" INTO "branchNameB"
	 * 
	 * @param sparqlQuery the SPARQL query
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	
	private Response getRebaseResponse(final String sparqlQuery, final String user, final String commitMessage, final String format) throws InternalErrorException {
		
		ResponseBuilder responseBuilder = Response.created(URI.create(""));
		Matcher m = patternRebaseQuery.matcher(sparqlQuery);
		
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
			String revisionUriA = RevisionManagement.getRevisionUri(graphName, branchNameA);
			String revisionUriB = RevisionManagement.getRevisionUri(graphName, branchNameB);
				
			// Check if graph already exists
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
			
			
			LinkedList<String> revisionList = MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriA);
			revisionList.remove(commonRevision);
			 
			Iterator<String> iter = revisionList.iterator();
			while(iter.hasNext()) {
				logger.info("revision--patch: " + iter.next().toString() );
			}
			 
			// create the patch and patch group
			rebaseControl.createPatchGroupOfBranch(revisionUriB, revisionList);
			
			
			// transform the response to the rebase freundlich check process
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
			
			// transform the graph strategy to the httpheader
			String graphStrategy = null;
			try {
				graphStrategy = URLEncoder.encode("merging-strategy-information", "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				graphStrategy = "merging-strategy-information";
			}
 			
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
			
			MergeManagement.createRevisionProgresses(MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriA), 
					graphNameA, uriA, MergeManagement.getPathBetweenStartAndTargetRevision(commonRevision, revisionUriB), graphNameB, uriB);
			
			
			// Create difference model
			MergeManagement.createDifferenceTripleModel(graphName,  graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI);
			
			if ((action != null) && (action.equalsIgnoreCase("AUTO")) && (with == null) && (triples == null)) {
				logger.info("AUTO REBASE query detected");
				// Create the merged revision
				ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, RebaseQueryTypeEnum.AUTO, "");
				String addedAsNTriples = addedAndRemovedTriples.get(0);
				String removedAsNTriples = addedAndRemovedTriples.get(1);
				
				String basisRevisionNumber = rebaseControl.forceRebaseProcess(graphName);
				newRevisionNumber = RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples,
						user, commitMessage, basisRevisionNumber);
				
				responseBuilder.header(graphStrategy, "auto-rebase");
									
			} else if ((action != null) && (action.equalsIgnoreCase("MANUAL")) && (with != null) && (triples != null)) {
				logger.info("MANUAL REBASE query detected");
				// Create the merged revision
				ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, RebaseQueryTypeEnum.MANUAL, triples);
				String addedAsNTriples = addedAndRemovedTriples.get(0);
				String removedAsNTriples = addedAndRemovedTriples.get(1);
				
				String basisRevisionNumber = rebaseControl.forceRebaseProcess(graphName);
				newRevisionNumber = RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples,
						user, commitMessage, basisRevisionNumber);
				
				responseBuilder.header(graphStrategy, "manual-rebase");
				
			} else if ((action == null) && (with != null) && (triples != null)) {
				logger.info("REBASE WITH query detected");
				// Create the merged revision -- newTriples
				ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName, branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI, RebaseQueryTypeEnum.WITH, triples);
				String addedAsNTriples = addedAndRemovedTriples.get(0);
				String removedAsNTriples = addedAndRemovedTriples.get(1);
				
				String basisRevisionNumber = rebaseControl.forceRebaseProcess(graphName);
				newRevisionNumber = RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples,
						user, commitMessage, basisRevisionNumber);
				
				responseBuilder.header(graphStrategy, "with-rebase");
							
			} else if ((action == null) && (with == null) && (triples == null)) {
				//get the difference Model String 
				String differenceModelString = RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, "text/html");
				
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
					//initial the differenceGraphModel in rebaseControl
					rebaseControl.checkRebaseFreundlichkeit(differenceModelString, graphName, branchNameA, branchNameB, format);
					// write the diffenece model in the response builder
					responseBuilder.entity(RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format));
				} else{
					
					boolean isRebaeFreundlich = rebaseControl.checkRebaseFreundlichkeit(differenceModelString, graphName, branchNameA, branchNameB,format);
					
					if(isRebaeFreundlich) {
						// reabase freundlich force rebase
						rebaseControl.forceRebaseProcess(graphName);
						responseBuilder.header(graphStrategy, "rebase-freundlich");
					}else{
						responseBuilder.header(graphStrategy, "rebase-unfreundlich");
						// write the diffenece model in the response builder
						responseBuilder.entity(RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format));				
					}
				}
						
			} else {
				throw new InternalErrorException("This is not a valid MERGE query: " + sparqlQuery);
			}				
		
			if (newRevisionNumber != null) {
				// Respond with next revision number
		    	responseBuilder.header(graphNameHeader + "-revision-number", newRevisionNumber);
				responseBuilder.header(graphNameHeader + "-revision-number-of-MASTER", RevisionManagement.getMasterRevisionNumber(graphName));
				logger.debug("Respond with new revision number " + newRevisionNumber + ".");
			}	
		}			
		if (!foundEntry){
			throw new InternalErrorException("Error in query: " + sparqlQuery);
		}			
		return responseBuilder.build();			
	}
	
}
