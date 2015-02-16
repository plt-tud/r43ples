package de.tud.plt.r43ples.triplestoreInterface;

import java.util.Iterator;

import com.hp.hpl.jena.query.ResultSet;


public abstract class TripleStoreInterface {
	
	protected abstract void close();
		
	
	public abstract String executeSelectConstructAskQuery(String sparqlQuery, String format);
	
	
	public abstract ResultSet executeSelectQuery(String selectQueryString) ;
	
	
	/**
	 * Executes a SELECT query.
	 * 
	 * @param selectQueryString the SELECT query
	 * @param format the format
	 * @return result set
	 */
	public abstract String executeSelectQuery(String selectQueryString, String format) ;
	
	
	/**
	 * Executes a CONSTRUCT query.
	 * 
	 * @param constructQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public abstract String executeConstructQuery(String constructQueryString, String format) ;
	
	/**
	 * Executes a DESCRIBE query.
	 * 
	 * @param describeQueryString the CONSTRUCT query
	 * @param format the result format
	 * @return formatted result
	 */
	public abstract String executeDescribeQuery(String describeQueryString, String format);
	

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
