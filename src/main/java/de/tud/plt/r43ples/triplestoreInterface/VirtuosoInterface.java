package de.tud.plt.r43ples.triplestoreInterface;

import java.util.Iterator;

import virtuoso.jena.driver.VirtDataset;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class VirtuosoInterface extends TripleStoreInterface {

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
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
		return vqe.execConstruct();
	}


	@Override
	public boolean executeAskQuery(String askQueryString) {
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(askQueryString, set);
		return vqe.execAsk();
	}

	@Override
	public void executeUpdateQuery(String updateQueryString) {
		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create(updateQueryString, set);
		vqe.exec();
	}

	@Override
	public void executeCreateGraph(String graph) {
		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create("CREATE GRAPH <"+graph+">", set);
		vqe.exec();
	}

	@Override
	public Iterator<String> getGraphs() {
		return ((VirtDataset) set).listNames();
	}

	@Override
	public Model executeDescribeQuery(String describeQueryString) {
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(describeQueryString, set);
		return vqe.execDescribe();
	}

}
