package de.tud.plt.r43ples.triplestoreInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
//		set.begin(ReadWrite.READ);
//		QueryExecution vqe = QueryExecutionFactory.create(query, set);
//		ResultSet result = vqe.execSelect();
//		set.end();
//		return result;
//		Query query =  VirtuosoQueryExecutionFactory.cre QueryFactory.create(selectQueryString);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, set);
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(selectQueryString, (VirtGraph) set);
		return vqe.execSelect();
		
	}

	@Override
	public Model executeConstructQuery(String constructQueryString) {
		Query query =  QueryFactory.create(constructQueryString);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, (VirtGraph) set);
		return vqe.execConstruct();
	}


	@Override
	public boolean executeAskQuery(String askQueryString) {
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(askQueryString, (VirtGraph) set);
		return vqe.execAsk();
	}

	@Override
	public void executeUpdateQuery(String updateQueryString) {
		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create(updateQueryString, (VirtGraph) set);
		vqe.exec();
	}

	@Override
	public void executeCreateGraph(String graph) {
		VirtuosoUpdateRequest vqe = VirtuosoUpdateFactory.create("CREATE GRAPH <"+graph+">", (VirtGraph) set);
		vqe.exec();
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
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(describeQueryString, (VirtGraph) set);
		return vqe.execDescribe();
	}

}
