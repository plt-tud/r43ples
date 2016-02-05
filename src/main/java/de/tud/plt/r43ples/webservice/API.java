package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.FastForwardControl;
import de.tud.plt.r43ples.management.RevisionManagement;

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
	
	
	@Path("getBranches")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public final ArrayList<String> getBranchesOfGraph(@QueryParam("graph") final String graph) throws IOException {
		return RevisionManagement.getAllBranchNamesOfGraph(graph);
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
		

}
