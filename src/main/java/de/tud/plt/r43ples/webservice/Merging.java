package de.tud.plt.r43ples.webservice;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import de.tud.plt.r43ples.existentobjects.RevisionControl;
import de.tud.plt.r43ples.iohelper.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.mergingUI.model.structure.MergeCommitModel;
import de.tud.plt.r43ples.mergingUI.ui.MergingControl;

@Path("merging")
public class Merging {
	
	private final static Logger logger = LogManager.getLogger(Merging.class);

	/**
	 * get merging HTML start page and input merging information
	 * */
	@GET
	public final Response getMerging() {
		logger.info("Merging - Start page");		
		
		List<String> graphList = new LinkedList<>();
		graphList.addAll(new RevisionControl().getRevisedGraphs().keySet());
		Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("merging_active", true);
		scope.put("graphList", graphList);
		
		StringWriter sw = new StringWriter();
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/merge_start.mustache");		
		mustache.execute(sw, scope);		
		return Response.ok().entity(sw.toString()).type(MediaType.TEXT_HTML).build();
	}
	
	
	
	/**
	 * Perform a Merge query on html interface
	 * create RevisionProcess Model A
	 * create RevisionProcess Model B
	 * create Difference model 
	 */
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response mergingPOST(
			@FormParam("graph") final String graphName,
			@FormParam("sdd") final String sddName,
			@FormParam("strategy") final String strategy,
			@FormParam("branch1") final String branch1,
			@FormParam("branch2") final String branch2,
			@FormParam("user") final String user,
			@FormParam("message") final String message) throws InternalErrorException {
		
		if (graphName==null)
			throw new InternalErrorException("graph name has to be provided");
		if (branch1==null || branch2==null)
			throw new InternalErrorException("branch names have to be provided");
		if (user==null)
			throw new InternalErrorException("user name has to be provided");
		if (message==null)
			throw new InternalErrorException("commit message name has to be provided");
		if (strategy==null)
			throw new InternalErrorException("strategy name has to be provided");
					
		ResponseBuilder response = Response.ok();
		RevisionGraph graph = new RevisionGraph(graphName);
		
		// Fast Forward
		if(strategy.equals("Fast-Forward")){
			MergeCommitModel commitModel = new MergeCommitModel(graphName, sddName, user, message, branch1, branch2, "Fast-Forward", null);
			// TODO Create date by using commit information
			String dateString = "2017-08-17T13:57:11";
			// TODO Change to new merge commit structure using R43ples core
			boolean ff_successful = false;//FastForwardControl.performFastForward(graph, branch1, branch2, user, dateString, message);

			if (ff_successful){
				response = Response.ok();
				response.entity(commitModel.getReportView());
				return response.build();
			}
			else {
				throw new InternalErrorException("Error during fast forward process");
			}
		}
		// Rebase
		else if(strategy.equals("Rebase")){
		
			//String rebaseQuery = StrategyManagement.createRebaseQuery(graphName, sddName, user, message, branch1, branch2, type, null);
			
			
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
			
//			rebaseControl.createCommitModel(graphName, sddName, user, message, branch1, branch2, "Rebase", type.toString());
			
			//Response responsePost = null;
			//if (Endpoint.patternRebaseQuery.matcher(rebaseQuery).find()) {
			//	responsePost= ep.getRebaseResponse(rebaseQuery, user, message, "HTML");
			//	logger.debug("rebase response status: "+responsePost.getStatusInfo().toString());	
			//	logger.debug("rebase response Header: "+responsePost.getHeaders().get("merging-strategy-information").get(0).toString());	
			//	logger.debug("rebase response Header: "+responsePost.getHeaders().keySet().toString());	
			//}

//			String rebaseResultView = rebaseControl.getRebaseReportView(graphName);
//			response.entity(rebaseResultView);
			return response.build();		
		}
		// Three Way Merge
		else {			
//			MergeCommitModel commitModel = new MergeCommitModel(graphName, sddName, user, message, branch1, branch2, "Three-Way", null);
//			R43plesMergeCommit commit = new R43plesMergeCommit(graphName, branch1, branch2, user, message, "text/turtle");
//			// TODO Change to new merge commit structure using R43ples core
//			MergeResult mresult = null;//Interface.mergeThreeWay(commit);
//
//			if(!mresult.hasConflict){
//				response.entity(commitModel.getReportView());
//				return response.build();
//			} else {
//				MergingControl mergingControl = new MergingControl();
//				// show page for resolving conflicts
//				mergingControl.getMergeProcess(commitModel, mresult);
//				response.entity(mergingControl.getViewHtmlOutput());
				return response.build();
//			}
		}	
	}			

}
