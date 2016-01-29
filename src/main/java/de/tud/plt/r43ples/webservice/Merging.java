package de.tud.plt.r43ples.webservice;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.MergeQueryTypeEnum;
import de.tud.plt.r43ples.merging.RebaseQueryTypeEnum;
import de.tud.plt.r43ples.merging.control.FastForwardControl;
import de.tud.plt.r43ples.merging.control.MergingControl;
import de.tud.plt.r43ples.merging.control.RebaseControl;
import de.tud.plt.r43ples.merging.management.ProcessManagement;
import de.tud.plt.r43ples.merging.management.StrategyManagement;
import de.tud.plt.r43ples.merging.model.structure.CommitModel;

@Path("merging")
public class Merging {
	
	private final static Logger logger = Logger.getLogger(Merging.class);
	private Endpoint ep;
	

	/**
	 * get merging HTML start page and input merging information
	 * */
	@GET
    @Produces(MediaType.TEXT_HTML)
	public final Response getMerging(@QueryParam("graph") final String graph) {
		logger.info("Merging -- graph: " + graph);		
		ResponseBuilder response = Response.ok();
		List<String> graphList = RevisionManagement.getRevisedGraphsList();	
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("merging_active", true);
		scope.put("graphList", graphList);
		
	    StringWriter sw = new StringWriter();
	    MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/merge_start_wip.mustache");
	    mustache.execute(sw, scope);		
	    response.entity(sw.toString()).type(MediaType.TEXT_HTML);
		return response.build();
	}
	
	
	
	/**
	 * Perform a Merge query on html interface
	 * create RevisionProcess Model A
	 * create RevisionProcess Model B
	 * create Difference model 
	 */
	@Path("mergingProcess")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response mergingPOST(
			@FormParam("graph") @DefaultValue("") final String graphName,
			@FormParam("sdd") final String sddName,
			@FormParam("strategy") final String strategy,
			@FormParam("method") final String method,
			@FormParam("branch1") final String branch1,
			@FormParam("branch2") final String branch2,
			@FormParam("user") @DefaultValue("") final String user,
			@FormParam("message") @DefaultValue("") final String message) throws InternalErrorException {
			
		ResponseBuilder response = Response.ok();
		
		// Fast Forward
		if(strategy.equals("Fast-Forward")){
			CommitModel commitModel = new CommitModel(graphName, sddName, user, message, branch1, branch2, "Fast-Forward", null);
			StrategyManagement.saveGraphVorMergingInMap(graphName, "application/json");
			FastForwardControl.executeFastForward(graphName, branch1, branch2);
			response.entity(commitModel.getReportView());
			return response.build();	
		}
		// Rebase
		else if(strategy.equals("Rebase")){
			RebaseQueryTypeEnum type = null;			
			if (method.equals("auto")) {
				type = RebaseQueryTypeEnum.AUTO;
			} else if(method.equals("common")){
				type = RebaseQueryTypeEnum.COMMON;
			} else{
				type = RebaseQueryTypeEnum.MANUAL;
			}
		
			String rebaseQuery = StrategyManagement.createRebaseQuery(graphName, sddName, user, message, branch1, branch2, type, null);
			StrategyManagement.saveGraphVorMergingInMap(graphName, "application/json");
			
			Matcher userMatcher = Endpoint.patternUser.matcher(rebaseQuery);
			logger.debug("test mergeQuery:"+rebaseQuery);
			if (userMatcher.find()) {
				rebaseQuery = userMatcher.replaceAll("");
			}
			Matcher messageMatcher = Endpoint.patternCommitMessage.matcher(rebaseQuery);
			if (messageMatcher.find()) {
				rebaseQuery = messageMatcher.replaceAll("");
			}
			
			//for each client, create a mergingControlMap and for each named graph create a mergingControl, first check, if namedgraph exist .
			//first create the mergingcontrol and than create the rebasecontrol
			MergingControl mergingControl;
			if(!Endpoint.clientMap.containsKey(user)){
				mergingControl = new MergingControl();
				Endpoint.clientMap.put(user, new HashMap<String, MergingControl>());
				Endpoint.clientMap.get(user).put(graphName, mergingControl);
			}else if(Endpoint.clientMap.containsKey(user) && (!Endpoint.clientMap.get(user).containsKey(graphName))) {
				mergingControl = new MergingControl();
				Endpoint.clientMap.get(user).put(graphName, mergingControl);
			}else{
				mergingControl = Endpoint.clientMap.get(user).get(graphName);
			}
					
			
			mergingControl.setRebaseControl();
			mergingControl.closeRebaseModel();
			
			RebaseControl rebaseControl = mergingControl.getRebaseControl();
			
			rebaseControl.createCommitModel(graphName, sddName, user, message, branch1, branch2, "Rebase", type.toString());
			
			Response responsePost = null;
			if (Endpoint.patternRebaseQuery.matcher(rebaseQuery).find()) {
				responsePost= ep.getRebaseResponse(rebaseQuery, user, message, "HTML");
				logger.debug("rebase response status: "+responsePost.getStatusInfo().toString());	
				logger.debug("rebase response Header: "+responsePost.getHeaders().get("merging-strategy-information").get(0).toString());	
				logger.debug("rebase response Header: "+responsePost.getHeaders().keySet().toString());	
			}

			String rebaseResultView = rebaseControl.getRebaseReportView(graphName);
			response.entity(rebaseResultView);
			return response.build();		
		}
		// Three Way Merge
		else {
			Response responsePost = null;
			MergeQueryTypeEnum type = null;
			if (method.equals("auto")) {
				type = MergeQueryTypeEnum.AUTO;
			} else if (method.equals("common")) {
				type = MergeQueryTypeEnum.COMMON;
			} else {
				type = MergeQueryTypeEnum.MANUAL;
			}
			
			//for each client ,create a mergingControl, first check, if named graph exists
			MergingControl mergingControl;
			if(!Endpoint.clientMap.containsKey(user)){
				mergingControl = new MergingControl();
				Endpoint.clientMap.put(user, new HashMap<String, MergingControl>());
				Endpoint.clientMap.get(user).put(graphName, mergingControl);
			}else if(Endpoint.clientMap.containsKey(user) && (!Endpoint.clientMap.get(user).containsKey(graphName))) {
				mergingControl = new MergingControl();
				Endpoint.clientMap.get(user).put(graphName, mergingControl);
			}else{
				mergingControl = Endpoint.clientMap.get(user).get(graphName);
			}

			mergingControl.closeRebaseModel();
			
			//save commit information in MergingControl
			mergingControl.createCommitModel(graphName, sddName, user, message, branch1, branch2, "Three-Way", type.toString());
						
			
			//save the graph information vor merging 
			StrategyManagement.saveGraphVorMergingInMap(graphName, "application/json" );
				
			String mergeQuery = ProcessManagement.createMergeQuery(graphName, sddName, user, message, type, branch1, branch2, null);
			logger.debug("test mergeQuery:"+mergeQuery);
			
			Matcher userMatcher = Endpoint.patternUser.matcher(mergeQuery);
			if (userMatcher.find()) {
				mergeQuery = userMatcher.replaceAll("");
			}
			Matcher messageMatcher = Endpoint.patternCommitMessage.matcher(mergeQuery);
			if (messageMatcher.find()) {
				mergeQuery = messageMatcher.replaceAll("");
			}

			if (Endpoint.patternMergeQuery.matcher(mergeQuery).find()) {
				responsePost= ep.getThreeWayMergeResponse(mergeQuery, user, message,"HTML");
			}
							
			if(!(responsePost.getStatusInfo() == Response.Status.CONFLICT)){
				response.entity(mergingControl.getThreeWayReportView(null));				
				return response.build();
			}

			mergingControl.getMergeProcess(responsePost, graphName, branch1, branch2);
			response.entity(mergingControl.getViewHtmlOutput());
			return response.build();
		}	
	}
	
	


	/**
	 * get old revision graph which was saved before in the cache of the server by 
	 * query revision information and get the graph
	 * */
	@Path("getOldRevisiongraph")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public final Response fastForwardGET(
			 @QueryParam("graph")  @DefaultValue("") final String graph) throws InternalErrorException {

		ResponseBuilder response = Response.ok();
		logger.info("Get old revision graph: "+ graph);

		response.type(MediaType.APPLICATION_JSON);
		response.entity(StrategyManagement.loadGraphVorMergingFromMap(graph));
		
		return response.build();
	}	
	
	/**
	 * select the force rebase process
	 * @throws InternalErrorException 
	 */
	@Path("forceRebaseProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response forceRebaseProcessGET(@QueryParam("graph") @DefaultValue("") final String graph, 
			@QueryParam("client") @DefaultValue("") final String user ) throws InternalErrorException {

		ResponseBuilder response = Response.ok();
		//get rebaseControl form map
		RebaseControl rebaseControl = Endpoint.clientMap.get(user).get(graph).getRebaseControl();
		rebaseControl.forceRebaseProcess(graph);
		response.entity(rebaseControl.getRebaseReportView(null));
		return response.build();
	}	
	
	/**
	 * select the manual rebase process
	 * @throws InternalErrorException 
	 */
	@Path("manualRebaseProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response manualRebaseProcessGET( @QueryParam("graph") @DefaultValue("") final String graph, 
			@QueryParam("client") @DefaultValue("") final String user ) throws InternalErrorException {

		ResponseBuilder response = Response.ok();
		
		//get MergingControl and rebaseControl form map
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);	
		RebaseControl rebaseControl = mergingControl.getRebaseControl();	
		rebaseControl.manualRebaseProcess();
		
		response.entity(mergingControl.getViewHtmlOutput());
		
		return response.build();
	}
	
	/**
	 * by high level view approve to server call this method
	 */
	@Path("approveHighLevelProcess")
	@POST
	public final void approveHighLevelPOST(
			@FormParam("isChecked") @DefaultValue("") final String isChecked,
			@FormParam("id")        @DefaultValue("") final String id, 
			@FormParam("graph")     @DefaultValue("") final String graph, 
			@FormParam("client")    @DefaultValue("") final String user) throws InternalErrorException {
		
		logger.info("ApproveHighLevelProcess test: "+id + " - isChecked: " + isChecked);
		
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
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
	public final Response reportGET(
			@QueryParam("graph")  @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user ) {
		
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		response.entity(mergingControl.createReportProcess());
		return response.build();	
	}	

	
	/** new push process with report view
	 * */
	@Path("pushProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response pushReportGET( @QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user) throws InternalErrorException {
		
		ResponseBuilder response = Response.ok();
			
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		//save the graph information before merging 
		StrategyManagement.saveGraphVorMergingInMap(graph, "application/json");
		
		String mergeQuery = mergingControl.updateMergeQuery();
		
		String userCommit = null;
		Matcher userMatcher = Endpoint.patternUser.matcher(mergeQuery);
		if (userMatcher.find()) {
			userCommit = userMatcher.group("user");
			mergeQuery = userMatcher.replaceAll("");
		}
		String messageCommit = null;
		Matcher messageMatcher = Endpoint.patternCommitMessage.matcher(mergeQuery);
		if (messageMatcher.find()) {
			messageCommit = messageMatcher.group("message");
			mergeQuery = messageMatcher.replaceAll("");
		}

		if (Endpoint.patternMergeQuery.matcher(mergeQuery).find()) {
			ep.getThreeWayMergeResponse(mergeQuery, userCommit, messageCommit,"HTML");
		}
			
		response.entity(mergingControl.getThreeWayReportView(null));
		
		Endpoint.clientMap.get(user).remove(graph);
		if(Endpoint.clientMap.get(user).isEmpty()){
			Endpoint.clientMap.remove(user);
		}
		
		return response.build();
	}	
	
	
	/** rebase push process with report view
	 * 
	 * */
	@Path("rebasePushProcess")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response rebasePushReportGET(@QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user ) throws InternalErrorException {
		
		ResponseBuilder response = Response.ok();
		
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		RebaseControl rebaseControl = mergingControl.getRebaseControl();
		
		mergingControl.transformDifferenceModelToRebase();
		
		// update the new rebase merge query
		String mergeQuery = mergingControl.updateMergeQuery();
		
		logger.info("rebasePushProcess: "+ mergeQuery);
		
		// execute the getRebaseResponse()
		Matcher userMatcher = Endpoint.patternUser.matcher(mergeQuery);
		if (userMatcher.find()) {
			mergeQuery = userMatcher.replaceAll("");
		}
		Matcher messageMatcher = Endpoint.patternCommitMessage.matcher(mergeQuery);
		if (messageMatcher.find()) {
			mergeQuery = messageMatcher.replaceAll("");
		}

		ep.getRebaseResponse(mergeQuery, user, mergingControl.getCommitModel().getMessage(), "HTML");
				
		
		String rebaseResultView = rebaseControl.getRebaseReportView(null);
		
		response.entity(rebaseResultView);
		
		//close rebase model
		mergingControl.closeRebaseModel();
		
		//after push remove the graph and release the space
		Endpoint.clientMap.get(user).remove(graph);
		if(Endpoint.clientMap.get(user).isEmpty()){
			Endpoint.clientMap.remove(user);
		}
		
		return response.build();
	}	
	
	
	

	
	/**load individual View
	  */
	
	@Path("individualView")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public final Response individualGET( @QueryParam("graph") @DefaultValue("") final String graph,
			 @QueryParam("client") @DefaultValue("") final String user ) {
		logger.info("Get individual view: "+ graph);
		ResponseBuilder response = Response.ok();
		
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		
		response.entity(mergingControl.getIndividualView());
		return response.build();
	}
	
	
	/**load updated triple View
	 *
	 * */
	
	@Path("tripleView")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public final Response tripleViewGET(@QueryParam("graph") @DefaultValue("") final String graph, 
			@QueryParam("client") @DefaultValue("") final String user ) {
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		response.entity(mergingControl.getTripleView());
		return response.build();
	}
	
	
	/**load High Level Change Table View 
	 * */
	
	@Path("highLevelView")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public final Response highLevelGET(@QueryParam("graph") @DefaultValue("") final String graph,
			@QueryParam("client") @DefaultValue("") final String user ) {
		
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
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
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		
		logger.info("individualFilter (A:"+ individualA +" B:"+ individualB+")");
		
		String individualFilter = mergingControl.getIndividualFilter(individualA, individualB);
		
		response.entity(individualFilter);
		return response.build();
	}	
	
	

}
