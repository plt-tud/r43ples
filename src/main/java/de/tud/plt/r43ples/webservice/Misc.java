package de.tud.plt.r43ples.webservice;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;

import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.visualisation.VisualisationTable;
import de.tud.plt.r43ples.visualisation.VisualisationGraph;

import static de.tud.plt.r43ples.webservice.Endpoint.*;

@Path("/")
public class Misc {

	@Context
	private Request request;
	
	private final static Logger logger = LogManager.getLogger(Misc.class);
	
	/**
	 * Landing page
	 *
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public final Response getLandingPage() {
		logger.info("Get Landing page");
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		
		StringWriter sw = new StringWriter();
	    MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/home.mustache");
	    mustache.execute(sw, htmlMap);		
	    ResponseBuilder response = Response.ok().entity(sw.toString()).type(MediaType.TEXT_HTML);
		return response.build();
	}
	
	@Path("index")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public final Response getIndex(){
		return getLandingPage();
	}
	
	@GET
	@Path("help")
	@Template(name = "/help.mustache")
	@Produces(MediaType.TEXT_HTML)
	public final Map<String, Object> getHelpPage() {
		logger.info("Get Landing page");
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("help_active", true);
		htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		return htmlMap;
	}
	
	/**
	 * Creates sample datasets
	 * @return information provided as HTML response
	 * @throws InternalErrorException 
	 */
	@Path("createSampleDataset")
	@GET
	public final Response createSampleDataset(@QueryParam("dataset") @DefaultValue("all") final String graph) throws InternalErrorException {
		List<String> graphs = new ArrayList<>();
		
		if (graph.equals("1") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset1().graphName);
		}
		if (graph.equals("2") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset2().graphName);
		}
		if (graph.equals("3") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataset3().graphName);
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
			graphs.add(SampleDataSet.createSampleDataSetComplexStructure().graphName);
		}
		if (graph.equals("rebase") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetRebase());
		}
		if (graph.equals("hlc-aggregation") || graph.equals("all")){
			graphs.add(SampleDataSet.createSampleDataSetHLCAggregation().graphName);
		}
		Map<String, Object> htmlMap = new HashMap<String, Object>();
	    htmlMap.put("graphs", graphs);
	    
	    StringWriter sw = new StringWriter();
	    MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/exampleDatasetGeneration.mustache");
	    mustache.execute(sw, htmlMap);		
	    ResponseBuilder response = Response.ok().entity(sw.toString()).type(MediaType.TEXT_HTML);
		return response.build();			
	}
	
	
	/**
	 * Provide revision information about R43ples system.
	 * 
	 * @param graphName
	 *            Provide only information about this graph
	 * @return RDF model of revision information
	 */
	@Path("revisiongraph")
	@GET
	@Produces({ TEXT_TURTLE, APPLICATION_RDF_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
	public final Response getRevisionGraph(@HeaderParam("Accept") final String format_header,
			@QueryParam("format") final String format_query,
			@QueryParam("graph")  final String graphName) throws Exception {
		String format = null;
		if (format_query == null){
			List<Variant> reqVariants = Variant.mediaTypes(TEXT_TURTLE_TYPE,
					APPLICATION_RDF_XML_TYPE, MediaType.APPLICATION_JSON_TYPE).build();
			Variant bestVariant = request.selectVariant(reqVariants);
			if (bestVariant == null) {
				throw new Exception("Requested datatype not available");
			}
			MediaType reqMediaType = bestVariant.getMediaType();
			format = reqMediaType.toString();
		}
		else {
			format = format_query;
		}
		logger.info("Get Revision Graph: " + graphName + " (format: " + format+")");
		
		ResponseBuilder response = Response.ok();
		if ("table".equalsIgnoreCase(format)) {
			response.type(MediaType.TEXT_HTML);
			response.entity(VisualisationTable.getHtmlOutput(graphName));
		}  else if ("graph".equalsIgnoreCase(format)) {
			response.type(MediaType.TEXT_HTML);
			response.entity(VisualisationGraph.getHtmlOutput(graphName));
		}
		else {
			RevisionGraph graph = new RevisionGraph(graphName);
			response.entity(graph.getContentOfRevisionGraph(format));
			response.type(format);
		}
		return response.build();
	}
	
	/**
	 * Provides content of graph in the attached triple store
	 * 
	 * @return content of specified graph in specified serialisation
	 */
	@Path("contentOfGraph")
	@GET
	@Produces({ "text/turtle", "application/rdf+xml", MediaType.APPLICATION_JSON, "application/ld+json" })
	public final Response getContentOfGraph(
			@HeaderParam("Accept") final String format_header,
			@QueryParam("format") @DefaultValue("application/json") final String format_query,
			@QueryParam("graph") final String graphName) {
		logger.info("Get Content of graph " + graphName);
		String format = (format_query != null) ? format_query : format_header;
		logger.debug("format: " + format);

		String result = Helper.getContentOfGraph(graphName, format);
		ResponseBuilder response = Response.ok();
		return response.entity(result).type(format).build();
	}
	
	

}
