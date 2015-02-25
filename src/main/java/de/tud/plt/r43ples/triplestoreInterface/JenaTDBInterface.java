package de.tud.plt.r43ples.triplestoreInterface;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
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
 * @author Markus Graube
 *
 */
public class JenaTDBInterface extends TripleStoreInterface {

	/** The TDB dataset. **/
	private static Dataset dataset;
	/** The logger. */
	private static Logger logger = Logger.getLogger(JenaTDBInterface.class);
	

	/**
	 * The constructor.
	 * 
	 * @param databaseDirectory the database directory of TDB
	 */
	public JenaTDBInterface(String databaseDirectory) {
		
		// if the directory does not exist, create it
		Location location = new Location(databaseDirectory);
		File theDir = new File(location.getDirectoryPath());
		if (!theDir.exists()) {
			logger.info("creating directory: " + theDir.toString());
		    if (theDir.mkdirs())
		    	logger.info("Directory successfully created");
		    else
		    	logger.error("Could not create directory");
		  }
		
		// Initialize the database
		dataset = TDBFactory.createDataset(location);

		
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
		try {
			QueryExecution qExec = QueryExecutionFactory.create(selectQueryString, dataset);
			return qExec.execSelect();
		} finally {
			dataset.end();
		}
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
		try {
			QueryExecution qExec = QueryExecutionFactory.create(constructQueryString, dataset);
			return qExec.execConstruct();
		} finally {
			dataset.end();
		}
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
		try {
			QueryExecution qExec = QueryExecutionFactory.create(describeQueryString, dataset);
			return qExec.execDescribe();
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
	@Override
	public boolean executeAskQuery(String askQueryString) {
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
	@Override
	public void executeUpdateQuery(String updateQueryString) {
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
	

	@Override
	public void executeCreateGraph(String graph) {
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

	@Override
	public Iterator<String> getGraphs() {
		dataset.begin(ReadWrite.READ);
		Iterator<String> list = dataset.listNames();
		dataset.end();
		return list;
	}
	
}
