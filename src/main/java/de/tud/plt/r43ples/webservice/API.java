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

import de.tud.plt.r43ples.iohelper.Helper;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;

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
		return Helper.getRevisedGraphsSparql(format);
	}

	@Path("getBranches")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public final ArrayList<String> getBranchesOfGraph(@QueryParam("graph") final String graphName) throws IOException {
		RevisionGraph graph = new RevisionGraph(graphName);
		return graph.getAllBranchNames();
	}

	/**
	 * Provide Diffs between to revisions of specified graph
	 * @param graphName name of graph
	 * @param revA identifier of first revision (name, number or tag)
	 * @param revB identifier of second revision (name, number or tag)
	 * @param fileFormat preferred output format (trig, nquads)
	 * @return list of found diffs
	 */
	@Path("getDiffs")
	@GET
	@Produces({ MediaType.TEXT_PLAIN, MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, "application/rdf+xml", "text/turtle", "application/sparql-results+xml"})
	public final String getDiffsOfGraph(
			@QueryParam("graph") final String graphName,
			@QueryParam("revA") final String revA,
			@QueryParam("revB") final String revB,
			@QueryParam("format") final String fileFormat)
	{
		logger.info("Get Diffs (graph: " + graphName + " revision a: " + revA + " revision b: " + revB + " format: " + fileFormat + ")");

		// file format optional
		String format = fileFormat;
		if (format == null) {
			format = "trig";
		}

		// check fileformat before wasting cpu
		if (format.toLowerCase().contains("trig") || fileFormat.toLowerCase().contains("nquads")) {
			return Helper.getDiffsBetweenStartAndTargetRevision(graphName, revA, revB, format);
		} else {
			return "Wrong file format. Use either \"nquads\" or \"trig\" !";
		}

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
		RevisionGraph graph = new RevisionGraph(graphName);
		//TODO Why do we need this?
		return false;//FastForwardControl.fastForwardCheck(graph, branch1, branch2);
	}
		

}
