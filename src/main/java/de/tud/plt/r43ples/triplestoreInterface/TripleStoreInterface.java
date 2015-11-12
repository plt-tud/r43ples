package de.tud.plt.r43ples.triplestoreInterface;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.JenaModelManagement;
import de.tud.plt.r43ples.management.RevisionManagement;


public abstract class TripleStoreInterface {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	
	protected void init() {
		if (!RevisionManagement.checkGraphExistence(Config.revision_graph)){
			logger.debug("Create revision graph: "+ Config.revision_graph);
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
	 	}
		
		if (!RevisionManagement.checkGraphExistence(Config.sdd_graph)){
			// Insert default content into SDD graph
			logger.info("Create sdd graph from " + Config.sdd_graph_defaultContent);
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
			
			Model jena_model = JenaModelManagement.readTurtleFileToJenaModel(Config.sdd_graph_defaultContent);
			String model = JenaModelManagement.convertJenaModelToNTriple(jena_model);
			logger.debug("SDD model: " + model);	
			RevisionManagement.executeINSERT(Config.sdd_graph, model);
	 	}		
	}
	
	protected abstract void close();
		
	
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
		if (patternSelectQuery.matcher(sparqlQuery).find())
			result = executeSelectQuery(sparqlQuery, format);
		else if (patternAskQuery.matcher(sparqlQuery).find())
			result = executeAskQuery(sparqlQuery)?"true":"false";
		else if (patternConstructQuery.matcher(sparqlQuery).find())
			result = executeConstructQuery(sparqlQuery, format);
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

	
}
