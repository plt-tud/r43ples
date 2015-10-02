package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.util.ArrayList;

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
import de.tud.plt.r43ples.merging.control.FastForwardControl;
import de.tud.plt.r43ples.merging.control.MergingControl;
import de.tud.plt.r43ples.merging.management.BranchManagement;

@Path("api/")
public class API {
	
	private final static Logger logger = Logger.getLogger(API.class);
	
	
	/**
	 * select property and get the new triple table
	 *  */
	@Path("filterProcess")
	@POST
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml" })
	public final Response filterPOST(@HeaderParam("Accept") final String formatHeader,
			@FormParam("properties") @DefaultValue("") final String properties, @FormParam("graph") @DefaultValue("") final String graph, 
			@FormParam("client") @DefaultValue("") final String user ) {
		
		ResponseBuilder response = Response.ok();
		MergingControl mergingControl = Endpoint.clientMap.get(user).get(graph);
		response.entity(mergingControl.updateTripleTable(properties));
		return response.build();
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
		return FastForwardControl.fastForwardCheck(graphName, branch1, branch2);
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
	

}
