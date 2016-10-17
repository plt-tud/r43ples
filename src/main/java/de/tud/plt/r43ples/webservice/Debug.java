package de.tud.plt.r43ples.webservice;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

@Path("debug")
public class Debug {
	
	private final static Logger logger = Logger.getLogger(Debug.class);
	
	
	@POST
	public final String postDebug(){
		return "WIP";
	}
	
	/**
	 * Performs Debug query 
	 * 
	 * @param sparqlQuery SPARQL query which should be executed. If omitted, a HTML site will be displayed in order to enter a query
	 * @return	SPARQL result
	 * @throws InternalErrorException
	 */
	@GET
	public final String getDebugQuery(
			@QueryParam("query") final String sparqlQuery,
			@QueryParam("format")  @DefaultValue("text/html") final String format) throws InternalErrorException {
		if (sparqlQuery == null) {
			logger.info("Get Debug page");
			return getHTMLDebugResponse();
		} else {
			String query;
			try {
				query = URLDecoder.decode(sparqlQuery, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				query = sparqlQuery;
			}
			logger.info("Debug query was requested. Query: " + query);
			if (sparqlQuery.contains("INSERT")) {
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
				return "Query executed";
			}
			else {
				String result = TripleStoreInterfaceSingleton.get().executeSelectConstructAskQuery(query, format);
				if (format.equals("text/html")){
					return getHTMLResult( result, sparqlQuery);
				}
				else {
					return result;
				}
			}
		}
	}
	
	/**
	 * Get HTML debug response for standard sparql request form.
	 * Using mustache templates. 
	 * 
	 * @return HTML response for SPARQL form
	 * @throws InternalErrorException 
	 */
	private String getHTMLDebugResponse() throws InternalErrorException {		
		logger.info("Get Debug page");
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("graphs", TripleStoreInterfaceSingleton.get().getGraphs());
	    htmlMap.put("debug_active", true);
	    StringWriter sw = new StringWriter();	    
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/debug.mustache");
	    mustache.execute(sw, htmlMap);		
		return sw.toString();
	}
	
	/**
	 * Generates HTML representation of SPARQL query result 
	 * @param query SPARQL query which was passed to R43ples
	 * @param result result from the attached triplestore
	 */
	private String getHTMLResult(final String result, String query) {
		return getHTMLResult(result, query, null);
	}
	
	/**
	 * Generates HTML representation of SPARQL query result including the rewritten query 
	 * @param query SPARQL query which was passed to R43ples
	 * @param query_rewritten rewritten SPARQL query passed to triplestore
	 * @param result result from the attached triplestore
	 */
	private String getHTMLResult(final String result, String query, String query_rewritten) {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/result.mustache");
		StringWriter sw = new StringWriter();
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("result", result);
		htmlMap.put("query", query);
		htmlMap.put("query_rewritten", query_rewritten);
		htmlMap.put("debug_active", true);
		mustache.execute(sw, htmlMap);		
		return sw.toString();
	}
	

}
