package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.merging.control.FastForwardControl;
import de.tud.plt.r43ples.merging.control.MergingControl;
import de.tud.plt.r43ples.merging.management.BranchManagement;
import de.tud.plt.r43ples.merging.model.structure.TableRow;

@Path("api/")
public class API {
	
	private final static Logger logger = Logger.getLogger(API.class);
	
	/**
	 * Provide information about revised graphs
	 * 
	 * @return list of graphs which are under revision control
	 */
	@Path("getRevisedGraphs")
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public final String getRevisedGraphs(@HeaderParam("Accept") final String format_header,
			@QueryParam("format") @DefaultValue("application/json") final String format_query) {
		logger.info("Get Revised Graphs");
		String format = (format_query != null) ? format_query : format_header;
		logger.debug("format: " + format);
		return RevisionManagement.getRevisedGraphsSparql(format);
	}
	
	
	/**
	 * select property and get the new triple table
	 *  */
	@Path("filterProcess")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public final List<TableRow> filterPOST(
			@FormParam("properties") @DefaultValue("") final String properties, 
			@FormParam("graph") @DefaultValue("") final String graph, 
			@FormParam("client") @DefaultValue("") final String user ) {
		
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		return mergingControl.updateTripleTable(properties);
	}	
	
	@Path("getBranches")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public final ArrayList<String> getBranchesOfGraph(@QueryParam("graph") final String graph) throws IOException {
		return BranchManagement.getAllBranchNamesOfGraph(graph);
	}
	
	/**
	 * through graph name, branch1 and branch2 to check the right of fast forward strategy
	 * */
	@Path("fastForwardCheckProcess")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public final boolean fastForwardCheckGET(@HeaderParam("Accept") final String formatHeader, @QueryParam("graph") @DefaultValue("") final String graphName,
			@QueryParam("branch1") @DefaultValue("") final String branch1, @QueryParam("branch2") @DefaultValue("") final String branch2) throws IOException, InternalErrorException {
		logger.info("FastForwardCheckProcess (graph: "+ graphName+"; branch1:"+branch1+"; branch2:"+branch2+")");
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		return FastForwardControl.fastForwardCheck(revisionGraph, branch1, branch2);
	}
	
	
	/**
	 * select the difference in difference tree and renew the triple table
	 * */
	@Path("treeFilterProcess")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
	public final Response treeFilterPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("triples") @DefaultValue("") final String triples, @FormParam("graph") @DefaultValue("") final String graph, 
			@FormParam("client") @DefaultValue("") final String user ) {
		logger.info("TreeFilterProcess Array: "+ triples);
		ResponseBuilder response = Response.ok();

		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		response.entity(mergingControl.updateTripleTableByTree(triples));
		return response.build();
	}	
	
	/**
	 * by triple approve to server call this method
	 */
	
	@Path("approveProcess")
	@POST
	public final void approvePOST(
			@FormParam("isChecked") @DefaultValue("") final String isChecked,
			@FormParam("id")        @DefaultValue("") final String id, 
			@FormParam("graph")     @DefaultValue("") final String graph,
			@FormParam("client")    @DefaultValue("") final String user) throws InternalErrorException {
		
		logger.info("ApproveProcess test: "+id+" - isChecked: " + isChecked);		
		
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		mergingControl.approveToDifferenceModel(id, isChecked);
		//FIXME: should return boolean response if request was successful
	}
	

}
