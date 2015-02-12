package de.tud.plt.r43ples.management;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NoWriterForLangException;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;


/** 
 * Provides a interface to the TDB triple store.
 *
 * @author Stephan Hensel
 *
 */
public class TripleStoreInterface {

	/** The TDB dataset. **/
	private static Dataset dataset;
	/** The logger. */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	

	/**
	 * The constructor.
	 * 
	 * @param databaseDirectory the database directory of TDB
	 * @throws UnsupportedEncodingException 
	 */
	public static void init(String databaseDirectory) throws UnsupportedEncodingException {
		
		// if the directory does not exist, create it
		Location location = new Location(databaseDirectory);
		File theDir = new File(location.getDirectoryPath());
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.toString());
		    theDir.mkdirs();
		  }
		
		// Initialize the database
		dataset = TDBFactory.createDataset(location);

		if (!RevisionManagement.checkGraphExistence(Config.revision_graph)){
			logger.info("Create revision graph");
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
	 	}
		
		// Create SDD graph
		if (!RevisionManagement.checkGraphExistence(Config.sdd_graph)){
			logger.info("Create sdd graph");
			executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
			// Insert default content into SDD graph
			RevisionManagement.executeINSERT(Config.sdd_graph, MergeManagement.convertJenaModelToNTriple(MergeManagement.readTurtleFileToJenaModel(Config.sdd_graph_defaultContent)));
	 	}		
	}
	
	public static void close() {
		dataset.close();
	}
	
	
	
	
	public static String executeSelectConstructAskQuery(String sparqlQuery, String format) {
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
	public static ResultSet executeSelectQuery(String selectQueryString) {
		dataset.begin(ReadWrite.READ);
		try {
			QueryExecution qExec = QueryExecutionFactory.create(selectQueryString, dataset);
			return qExec.execSelect();
		} finally {
			dataset.end();
		}
	}
	
	
	/**
	 * Executes a SELECT query.
	 * 
	 * @param selectQueryString the SELECT query
	 * @param format the format
	 * @return result set
	 */
	public static String executeSelectQuery(String selectQueryString, String format) {
		ResultSet results = executeSelectQuery(selectQueryString);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (format.equals("application/sparql-results+xml") || format.equals("application/xml") || format.equals("text/xml"))
			ResultSetFormatter.outputAsXML(baos, results);
		else if (format.equals("text/turtle") )
			ResultSetFormatter.outputAsRDF(baos, "Turtle", results);
		else if (format.equals("application/json") )
			ResultSetFormatter.outputAsJSON(baos, results);
		else if (format.equals("text/plain") ) {
			ResultSetFormatter.out(baos, results);
			return baos.toString();
		}
		else {
			ResultSetFormatter.out(baos, results);
			return "<pre>"+StringEscapeUtils.escapeHtml(baos.toString())+"</pre>";
		}
		return baos.toString();
	}
	
	
	/**
	 * Executes a CONSTRUCT query.
	 * 
	 * @param constructQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public static String executeConstructQuery(String constructQueryString, String format) {
		logger.debug("Query: " + constructQueryString);
		dataset.begin(ReadWrite.READ);
		try {
			QueryExecution qExec = QueryExecutionFactory.create(constructQueryString, dataset);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Model result = qExec.execConstruct();
			
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
					return "<pre>"+StringEscapeUtils.escapeHtml(baos.toString())+"</pre>";
				}
			}
			logger.debug("Result: " + baos.toString());
			return baos.toString();
		} finally {
			dataset.end();
		}
	}
	
	/**
	 * Executes a DESCRIBE query.
	 * 
	 * @param describeQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public static String executeDescribeQuery(String describeQueryString, String format) {
		logger.debug("Query: " + describeQueryString);
		dataset.begin(ReadWrite.READ);
		try {
			QueryExecution qExec = QueryExecutionFactory.create(describeQueryString, dataset);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			qExec.execDescribe().write(baos, format);
		    return baos.toString();
		} finally {
			dataset.end();
		}
	}
	

	/**
	 * Executes an ASK query.
	 * 
	 * @param askQueryString the ASK query
	 * @return boolean result of ASK query
	 */
	public static boolean executeAskQuery(String askQueryString) {
		dataset.begin(ReadWrite.READ);
		try {
			QueryExecution qe = QueryExecutionFactory.create(askQueryString, dataset);
			return qe.execAsk();
		} finally {
			dataset.end();
		}
	}
	
	/**
	 * Executes an UPDATE query.
	 * 
	 * @param updateQueryString the UPDATE query
	 */
	public static void executeUpdateQuery(String updateQueryString) {
		logger.debug("Query:" + updateQueryString);
		dataset.begin(ReadWrite.WRITE);
		try {
			GraphStore graphStore = GraphStoreFactory.create(dataset) ;
		    UpdateRequest request = UpdateFactory.create(updateQueryString) ;
		    UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
		    proc.execute() ;

		    dataset.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataset.end();
		}
	}


	public static void executeCreateGraph(String graph) {
		dataset.begin(ReadWrite.WRITE);
		try {
			GraphStore graphStore = GraphStoreFactory.create(dataset) ;

		    UpdateRequest request = UpdateFactory.create("CREATE GRAPH <"+graph+">") ;
		    UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
		    proc.execute() ;

		    dataset.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataset.end();
		}
	}

	public static Object getGraphs() {
		dataset.begin(ReadWrite.READ);
		Iterator<String> list = dataset.listNames();
		dataset.end();
		return list;
	}
	
}
