package de.tud.plt.r43ples.triplestoreInterface;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.NoWriterForLangException;
import org.apache.jena.sparql.resultset.ResultsFormat;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;


public abstract class TripleStoreInterface {
	
	/** The logger. */
	private static Logger logger = LogManager.getLogger(TripleStoreInterface.class);
	
	protected void init() {
		if (!checkGraphExistence(Config.revision_graph)){
			logger.debug("Create revision graph: "+ Config.revision_graph);
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
			// Create the evolution revision graph
			try {
				R43plesCoreSingleton.getInstance().createInitialCommit(Config.evolution_graph, null, null, "R43ples", "Evolution graph created by R43ples");
				// Add the coevolution revision graph type to the created revision graph
                String query = Config.prefixes + String.format(
                        "INSERT DATA { GRAPH <%1$s> {"
                                + "  <%2$s> a rmo:CoEvolutionRevisionGraph ."
                                + "} }",
                        Config.revision_graph, Config.evolution_graph);
                TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
			} catch (InternalErrorException e) {
				e.printStackTrace();
			}
		}
		
		if (!checkGraphExistence(Config.sdg_graph)) {
			// Insert default content into SDD graph
			logger.info("Create sdg graph from " + Config.sdg_graph_defaultContent);
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.sdg_graph + ">");

			Model jena_model_sdg = JenaModelManagement.readTurtleFileToJenaModel(Config.sdg_graph_defaultContent);
			String model_sdg = JenaModelManagement.convertJenaModelToNTriple(jena_model_sdg);
			logger.debug("SDG model: " + model_sdg);
			Helper.executeINSERT(Config.sdg_graph, model_sdg);
		}

		if (!checkGraphExistence(Config.rules_graph)){
			// Insert default content into SDD graph
			logger.info("Create rules graph from " + Config.rules_graph_defaultContent);
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.rules_graph + ">");

			Model jena_model_rules = JenaModelManagement.readTurtleFileToJenaModel(Config.rules_graph_defaultContent);
			String model_rules = JenaModelManagement.convertJenaModelToNTriple(jena_model_rules);
			logger.debug("Rules model: " + model_rules);
			Helper.executeINSERT(Config.rules_graph, model_rules);
	 	}		
	}
	
	protected abstract void close();


	/**
	 * Checks if graph exists in triple store. Works only when the graph is not
	 * empty.
	 *
	 * @param graphName
	 *            the graph name
	 * @return boolean value if specified graph exists and contains at least one
	 *         triple elsewhere it will return false
	 */
	public boolean checkGraphExistence(final String graphName){
		String query = "ASK { GRAPH <" + graphName + "> {?s ?p ?o} }";
		return this.executeAskQuery(query);
	}
	
	public String executeSelectConstructAskQuery(String sparqlQuery, String format) {
		logger.debug("Query: " + sparqlQuery);
		final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
		final Pattern patternSelectQuery = Pattern.compile(
				"SELECT.*WHERE\\s*\\{(?<where>.*)\\}", 
				patternModifier);
		final Pattern patternAskQuery = Pattern.compile(
				"ASK.*WHERE\\s*\\{(?<where>.*)\\}", 
				patternModifier);
		final Pattern patternConstructQuery = Pattern.compile(
				"CONSTRUCT.*WHERE\\s*\\{(?<where>.*)\\}", 
				patternModifier);
		String result;
		if (patternConstructQuery.matcher(sparqlQuery).find())
			result = executeConstructQuery(sparqlQuery, format);
		else if (patternAskQuery.matcher(sparqlQuery).find())
			result = executeAskQuery(sparqlQuery)?"true":"false";
		else if (patternSelectQuery.matcher(sparqlQuery).find())
			result = executeSelectQuery(sparqlQuery, format);
		else
			result = executeSelectQuery(sparqlQuery, format);
		logger.debug("Response: " + result);
		return result;
	}
	
	/**
	 * Executes a SELECT query.
	 * 
	 * @param selectQueryString the SELECT query
	 * @return result set
	 */
	public abstract ResultSet executeSelectQuery(String selectQueryString) ;
	
	

	/**
	 * Executes a SELECT query.
	 * 
	 * @param selectQueryString the SELECT query
	 * @param format the format
	 * @return result string in specified format
	 */
	public String executeSelectQuery(String selectQueryString, String format) {
		ResultSet results = executeSelectQuery(selectQueryString);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (format.equals("application/sparql-results+xml") || format.equals("application/xml") || format.equals("text/xml"))
			ResultSetFormatter.outputAsXML(baos, results);
		else if (format.equals("text/turtle") )
			ResultSetFormatter.output(baos, results, ResultsFormat.FMT_RDF_TTL);
		else if (format.equals("application/json") )
			ResultSetFormatter.outputAsJSON(baos, results);
		else if (format.equals("text/plain") )
			ResultSetFormatter.out(baos, results);
		else
			ResultSetFormatter.out(baos, results);
		return baos.toString();
	}
	
	
	/**
	 * Executes a CONSTRUCT query.
	 * 
	 * @param constructQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public String executeConstructQuery(String constructQueryString, String format) {
		logger.debug("Query: " + constructQueryString);
		Model result = executeConstructQuery(constructQueryString);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		if (format.toLowerCase().contains("xml") )
			result.write(baos, "RDF/XML");
		else if (format.toLowerCase().contains("turtle") )
			result.write(baos, "Turtle");
		else if (format.toLowerCase().contains("json") )
			result.write(baos, "RDF/JSON");
		else {
			try {
				result.write(baos, format);
			}
			catch (NoWriterForLangException e) {
				result.write(baos, "Turtle");
			}
		}
		logger.debug("Result: " + baos.toString());
		return baos.toString();
	}
	
	
	public abstract Model executeConstructQuery(String constructQueryString) ;
	
	
	/**
	 * Executes a DESCRIBE query.
	 * 
	 * @param describeQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public String executeDescribeQuery(String describeQueryString, String format) {
		logger.debug("Query: " + describeQueryString);
		Model result = executeConstructQuery(describeQueryString);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		result.write(baos, format);
		return baos.toString();
	}
	

	public abstract Model executeDescribeQuery(String describeQueryString) ;
	

	/**
	 * Executes an ASK query.
	 * 
	 * @param askQueryString the ASK query
	 * @return boolean result of ASK query
	 */
	public abstract boolean executeAskQuery(String askQueryString) ;
	
	/**
	 * Executes an UPDATE query.
	 * 
	 * @param updateQueryString the UPDATE query
	 */
	public abstract void executeUpdateQuery(String updateQueryString);

	
	public abstract void executeCreateGraph(String graph) ;

	
	public abstract Iterator<String> getGraphs();

	public abstract void dropAllGraphsAndReInit();

	
}
