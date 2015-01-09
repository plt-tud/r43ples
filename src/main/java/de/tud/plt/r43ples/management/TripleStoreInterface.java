package de.tud.plt.r43ples.management;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.tdb.TDBFactory;
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
	public static Dataset dataset;
	/** The logger. */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	

	/**
	 * The constructor.
	 * 
	 * @param databaseDirectory the database directory of TDB
	 * @throws HttpException
	 * @throws IOException
	 */
	public static void init(String databaseDirectory) throws HttpException, IOException {

		// Initialize the database
		dataset = TDBFactory.createDataset(databaseDirectory);

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
		dataset.begin(ReadWrite.READ);
		try {
			QueryExecution qExec = QueryExecutionFactory.create(selectQueryString, dataset);
			ResultSet results = qExec.execSelect();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// FIXME Add format selection
			ResultSetFormatter.out(baos, results);
			return baos.toString();
		} finally {
			dataset.end();
		}
	}
	
	
	/**
	 * Executes a CONSTRUCT query.
	 * 
	 * @param constructQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public static String executeConstructQuery(String constructQueryString, String format) {
		dataset.begin(ReadWrite.READ);
		try {
			QueryExecution qExec = QueryExecutionFactory.create(constructQueryString, dataset);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			qExec.execConstruct().write(baos, format);
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
		dataset.begin(ReadWrite.WRITE);
		try {
			GraphStore graphStore = GraphStoreFactory.create(dataset) ;

		    UpdateRequest request = UpdateFactory.create(updateQueryString) ;
		    UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
		    proc.execute() ;

		    dataset.commit();
		} finally {
			dataset.end();
		}
	}
	
}
