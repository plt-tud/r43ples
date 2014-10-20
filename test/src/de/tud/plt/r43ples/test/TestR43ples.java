package de.tud.plt.r43ples.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.webservice.Service;


public class TestR43ples {

	/** The logger. */
	private static Logger logger = Logger.getLogger(TestR43ples.class);
	/** The endpoint. **/
	private static String endpoint = "http://localhost:9998/r43ples/sparql";
	/** The graph name. **/
	private static String graphName = "http://exampleGraph.com/r43ples";
	
	
	@BeforeClass
	public static void setUp() throws ConfigurationException, IOException, HttpException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		SampleDataSet.createSampleDataSetMerging(graphName);
		Service.start();
	}
	
	@AfterClass
	public static void tearDown() {
		Service.stop();
	}
	
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	@Test
	public void testmain() throws IOException {
		
		// restructure commit to B2
		logger.info("Restructure commit to B2");
		String query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"restructure commit to B2.\" %n"
				+ "DELETE { GRAPH <%s> REVISION \"B2\" {"
				+ " <http://example.com/testS> <http://example.com/testP> ?o."
				+ "} } %n"
				+ "WHERE { GRAPH <%s> REVISION \"B2\" {"
				+ "	<http://example.com/testS> <http://example.com/testP> ?o"
				+ "} } %n"
				+ "INSERT { GRAPH <%s> REVISION \"B2\" {"
				+ " <http://example.com/newTestS> <http://example.com/newTestP> ?o."
				+ "} } %n"
				+ "WHERE { GRAPH <%s> REVISION \"B2\" {"
				+ "	<http://example.com/testS> <http://example.com/testP> ?o"
				+ "} }", 
				graphName, graphName, graphName, graphName);
		logger.debug("Execute query: \n" + query);
		logger.debug("Response: \n" + executeR43plesQuery(query));
		

		
		query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result3 = executeR43plesQueryWithFormat(query, "application/xml");
		String expected3 = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		Assert.assertEquals(expected3, result3);
		
		query = String.format(""
						+ "OPTION r43ples:SPARQL_JOIN %n"
						+ "SELECT ?s ?p ?o "
						+ "FROM <%s> REVISION \"B1\" "
						+ "FROM <%s> REVISION \"B2\" "
						+ "WHERE { ?s ?p ?o. }"
						+ "ORDER BY ?s ?p ?o", graphName, graphName);
		String result4 = executeR43plesQueryWithFormat(query, "application/xml");
		String expected4 = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		Assert.assertEquals(expected4, result4);
	}
	
	@Test public void testServiceDescription() throws IOException{
		String result = executeR43plesQueryWithFormat("", "text/turtle");
		Assert.assertThat(result, containsString("sd:r43ples"));
	}
	
	
	@Test public void testSelectQueryWithoutRevision() throws IOException {
		String query = String.format(""
				+ "select * from <%s>"
				+ "where { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result = executeR43plesQueryWithFormat(query, "application/xml");
		String expected = ResourceManagement.getContentFromResource("response-master.xml");
		Assert.assertEquals(expected, result);
	}
	
	@Test public void testConstructQuery() throws IOException {
		String query = String.format(""
				+ "CONSTRUCT {?s ?p ?o} "
				+ "FROM <%s> REVISION \"1\""
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ASC(?o)", graphName);
		String result = executeR43plesQueryWithFormat(query, "text/turtle");
		
		Assert.assertThat(result, containsString("\"A\""));
		Assert.assertThat(result, containsString("\"B\""));
		Assert.assertThat(result, containsString("\"C\""));
		Assert.assertThat(result, not(containsString("\"D\"")));
		Assert.assertThat(result, not(containsString("\"E\"")));
	}
	
	/**
	 * Executes a SPARQL-query against the R43ples endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeR43plesQuery(String query) throws IOException {
		return executeR43plesQueryWithFormat(query, "application/xml");
	}
	
	/**
	 * Executes a SPARQL-query against the R43ples endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeR43plesQueryWithFormat(String query, String format) throws IOException {
		URL url = null;
		
		url = new URL(endpoint+ "?query=" + URLEncoder.encode(query, "UTF-8")+ "&format=" + URLEncoder.encode(format, "UTF-8") );
		logger.debug(url.toString());

		URLConnection con = null;
		InputStream in = null;
		con = url.openConnection();
		in = con.getInputStream();
	
		String encoding = con.getContentEncoding();
		encoding = (encoding == null) ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		return body;
	}

}
