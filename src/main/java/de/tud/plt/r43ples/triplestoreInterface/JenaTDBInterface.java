package de.tud.plt.r43ples.triplestoreInterface;

import java.io.File;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;


/** 
 * Provides a interface to the TDB triple store.
 *
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class JenaTDBInterface extends TripleStoreInterface {

	/** The TDB dataset. **/
	private static Dataset dataset;
	/** The logger. */
	private static Logger logger = LogManager.getLogger(JenaTDBInterface.class);
	

	/**
	 * The constructor.
	 * 
	 * @param databaseDirectory the database directory of TDB
	 */
	public JenaTDBInterface(String databaseDirectory) {

    	// if the directory does not exist, create it
		File theDir = new File(databaseDirectory);
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.toString());
		    if (theDir.mkdirs())
		    	logger.info("Directory successfully created");
		    else
		    	logger.error("Could not create directory");
		  }
		
		// Initialize the database
		dataset = TDBFactory.createDataset(databaseDirectory);
	}
	
	@Override
	public void close() {
		dataset.close();
	}
	
	
	
	/**
	 * Executes a SELECT query.
	 * 
	 * @param selectQueryString the SELECT query
	 * @return result set
	 */
	@Override
	public ResultSet executeSelectQuery(String selectQueryString) {
		dataset.begin(ReadWrite.READ);
		logger.debug(selectQueryString);
		QueryExecution qExec = QueryExecutionFactory.create(selectQueryString, dataset);

		ResultSet result = qExec.execSelect();
		dataset.end();
		return result;
	}
	
	
	/**
	 * Executes a CONSTRUCT query.
	 * 
	 * @param constructQueryString the CONSTRUCT query
	 * @return model
	 */
	@Override
	public Model executeConstructQuery(String constructQueryString) {
		dataset.begin(ReadWrite.READ);
		QueryExecution qExec = QueryExecutionFactory.create(constructQueryString, dataset);
		Model result = qExec.execConstruct();
		qExec.close();
		dataset.end();
		return result;
	}
	
	
	
	/**
	 * Executes a DESCRIBE query.
	 * 
	 * @param describeQueryString the DESCRIBE query
	 * @return model
	 */
	@Override
	public Model executeDescribeQuery(String describeQueryString) {
		logger.debug("Query: " + describeQueryString);
		dataset.begin(ReadWrite.READ);
		QueryExecution qExec = QueryExecutionFactory.create(describeQueryString, dataset);
		Model result = qExec.execDescribe();
		qExec.close();
		dataset.end();
		return result;
	}
	

	/**
	 * Executes an ASK query.
	 * 
	 * @param askQueryString the ASK query
	 * @return boolean result of ASK query
	 */
	@Override
	public boolean executeAskQuery(String askQueryString) {
		dataset.begin(ReadWrite.READ);
		QueryExecution qe = QueryExecutionFactory.create(askQueryString, dataset);
		boolean result = qe.execAsk();
		qe.close();
		dataset.end();

		return result;
	}
	
	/**
	 * Executes an UPDATE query.
	 * 
	 * @param updateQueryString the UPDATE query
	 */
	@Override
	public void executeUpdateQuery(String updateQueryString) {
		logger.debug("Query:" + updateQueryString);
		dataset.begin(ReadWrite.WRITE);
		
		GraphStore graphStore = GraphStoreFactory.create(dataset) ;
		
	    UpdateRequest request = UpdateFactory.create(updateQueryString) ;
	    UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
	    proc.execute();
	    dataset.commit();
		dataset.end();
	}
	

	@Override
	public void executeCreateGraph(String graph) {
		dataset.begin(ReadWrite.WRITE);
		
		GraphStore graphStore = GraphStoreFactory.create(dataset) ;

	    UpdateRequest request = UpdateFactory.create("CREATE GRAPH <"+graph+">") ;
	    UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore) ;
	    proc.execute();

	    dataset.commit();
		dataset.end();
	}

	@Override
	public Iterator<String> getGraphs() {
		dataset.begin(ReadWrite.READ);
		Iterator<String> list = dataset.listNames();
		dataset.end();
		return list;
	}

	@Override
	public void dropAllGraphsAndReInit() {
		Iterator<String> list = getGraphs();
		dataset.begin(ReadWrite.WRITE);
		while(list.hasNext()) {
			dataset.removeNamedModel(list.next());
		}
		dataset.commit();
		dataset.end();
		init();
	}
	
}
