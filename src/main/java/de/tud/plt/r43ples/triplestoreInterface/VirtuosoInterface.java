package de.tud.plt.r43ples.triplestoreInterface;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import virtuoso.jena.driver.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Interface for accessing Virtuoso over a JDBC bridge
 * 
 * Currently not working since all results are empty
 * 
 * 
 * @author Markus Graube
 *
 */
public class VirtuosoInterface extends TripleStoreInterface {
	
	/** The logger. **/
	private static Logger logger = LogManager.getLogger(VirtuosoInterface.class);

	private VirtGraph set;
	
	public VirtuosoInterface(String virtuoso_url, String virtuoso_user, String virtuoso_password) {
		
//		set = new VirtGraph (virtuoso_url, virtuoso_user, virtuoso_password);
	}

	@Override
	public void close() {
//		set.close();
//		set = null;
	}


	@Override
	public ResultSet executeSelectQuery(String selectQueryString) {
//		Query query =  QueryFactory.create(selectQueryString);
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
//		ResultSet result = vqe.execSelect();
//		vqe.close();
//		return result;
		return null;
	}

	@Override
	public Model executeConstructQuery(String constructQueryString) {
//		Query query =  QueryFactory.create(constructQueryString);
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query,  set);
//		Model result = vqe.execConstruct();
//		vqe.close();
//		return result;
		return null;
	}


	@Override
	public boolean executeAskQuery(String askQueryString) {
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(askQueryString, set);
//		boolean result = vqe.execAsk();
//		vqe.close();
//		return result;
		return false;
	}

	@Override
	public void executeUpdateQuery(String updateQueryString) {
//		logger.debug(updateQueryString);
//		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create(updateQueryString, set);
//		try {
//			vqe.exec();
//		}
//		catch (Exception e) {
//			logger.error(e);
//			throw e;
//		}
	}

	@Override
	public void executeCreateGraph(String graph) {
//		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create("CREATE GRAPH <"+graph+">", set);
//		try {
//			vqe.exec();
//		}
//		catch (Exception e) {
//			logger.error(e);
//			throw e;
//		}
	}

	@Override
	public Iterator<String> getGraphs() {
		ResultSet resultSet = executeSelectQuery("SELECT DISTINCT ?graph WHERE { GRAPH ?graph { ?s ?p ?o}}");
		List<String> list = new ArrayList<String>();
		while (resultSet.hasNext())
			list.add(resultSet.next().getResource("?graph").toString());
		return list.iterator();
	}

	@Override
	public Model executeDescribeQuery(String describeQueryString) {
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(describeQueryString, set);
//		return vqe.execDescribe();
		return null;
	}

	@Override
	public void dropAllGraphsAndReInit() {
		Iterator<String> list = getGraphs();
		while(list.hasNext()) {
			executeUpdateQuery("DROP SILENT GRAPH <" + list.next() + ">");
		}
		init();
	}

}
