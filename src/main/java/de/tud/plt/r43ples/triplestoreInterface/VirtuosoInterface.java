package de.tud.plt.r43ples.triplestoreInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

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
	private static Logger logger = Logger.getLogger(VirtuosoInterface.class);

	private VirtGraph set;
	
	public VirtuosoInterface(String virtuoso_url, String virtuoso_user, String virtuoso_password) {
		
		set = new VirtGraph (virtuoso_url, virtuoso_user, virtuoso_password);
	}

	@Override
	public void close() {
		set.close();
		set = null;
	}


	@Override
	public ResultSet executeSelectQuery(String selectQueryString) {
		Query query =  QueryFactory.create(selectQueryString);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		return vqe.execSelect();
		
	}

	@Override
	public Model executeConstructQuery(String constructQueryString) {
		Query query =  QueryFactory.create(constructQueryString);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query,  set);
		return vqe.execConstruct();
	}


	@Override
	public boolean executeAskQuery(String askQueryString) {
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(askQueryString, set);
		return vqe.execAsk();
	}

	@Override
	public void executeUpdateQuery(String updateQueryString) {
		logger.debug(updateQueryString);
		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create(updateQueryString, set);
		try {
			vqe.exec();
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	@Override
	public void executeCreateGraph(String graph) {
		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create("CREATE GRAPH <"+graph+">", set);
		try {
			vqe.exec();
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	@Override
	public Iterator<String> getGraphs() {
	//	return set.listNames();
		ResultSet resultSet = executeSelectQuery("SELECT DISTINCT ?graph WHERE { GRAPH ?graph { ?s ?p ?o}}");
		List<String> list = new ArrayList<String>();
		while (resultSet.hasNext())
			list.add(resultSet.next().getResource("?graph").toString());
		return list.iterator();
	}

	@Override
	public Model executeDescribeQuery(String describeQueryString) {
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(describeQueryString, set);
		return vqe.execDescribe();
	}

}
