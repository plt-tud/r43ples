package de.tud.plt.r43ples.test;

import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
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
	public static void setUp() throws ConfigurationException, URISyntaxException, IOException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.database_directory);
		SampleDataSet.createSampleDataSetMerging(graphName);
		Service.start();
	}
	
	@AfterClass
	public static void tearDown() {
		Service.stop();
	}
	
	
	@Test
	public void testSelect() throws IOException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result3 = executeR43plesQueryWithFormat(query, MediaType.APPLICATION_XML);
		String expected3 = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		Assert.assertEquals(expected3, result3);
	}
	
	
	@Test
	public void testSelectSparqlJoinOption() throws IOException{		
		String query = String.format(""
						+ "OPTION r43ples:SPARQL_JOIN %n"
						+ "SELECT ?s ?p ?o "
						+ "FROM <%s> REVISION \"B1\" "
						+ "FROM <%s> REVISION \"B2\" "
						+ "WHERE { ?s ?p ?o. }"
						+ "ORDER BY ?s ?p ?o", graphName, graphName);
		String result4 = executeR43plesQueryWithFormat(query, MediaType.APPLICATION_XML);
		String expected4 = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		Assert.assertEquals(expected4, result4);
	}
	

	
	@Test
	public void testServiceDescription() throws IOException{
		String result = executeR43plesQueryWithFormat("", "turtle");
		Assert.assertThat(result, containsString("sd:r43ples"));
	}
	
	@Test
	public void testHtmlQueryForm() throws IOException{
		String result = executeR43plesQueryWithFormat("", MediaType.TEXT_HTML);
		Assert.assertThat(result, containsString("<form"));
	}
	
	
	@Test
	public void testSelectQueryWithoutRevision() throws IOException {
		String query = String.format(""
				+ "select * from <%s>"
				+ "where { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result = executeR43plesQueryWithFormat(query, MediaType.APPLICATION_XML);
		String expected = ResourceManagement.getContentFromResource("response-master.xml");
		Assert.assertEquals(expected, result);
	}
	
	
	/**
	 *  Test example queries from html site
	 * @throws IOException 
	 */
	@Test public void testExampleQueries() throws IOException {
		SampleDataSet.createSampleDataset1("http://test.com/r43ples-dataset-1");
		SampleDataSet.createSampleDataset2("http://test.com/r43ples-dataset-2");
		
		String result = executeR43plesQuery("CREATE SILENT GRAPH <http://test.com/r43ples-dataset-1>");
		
		result = executeR43plesQuery("SELECT * FROM <http://test.com/r43ples-dataset-1> REVISION \"3\" WHERE {	?s ?p ?o. }");
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		result = executeR43plesQuery("OPTION r43ples:SPARQL_JOIN \n"
		+ "	SELECT ?s ?p ?o"
		+ "	FROM <http://test.com/r43ples-dataset-1> REVISION \"master\""
		+ "	FROM <http://test.com/r43ples-dataset-2> REVISION \"2\""
		+ "	WHERE {"
		+ "	?s ?p ?o."
		+ "	}");
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		result = executeR43plesQuery(""
		+ "USER \"mgraube\""
		+ "MESSAGE \"test commit\""
		+ "	INSERT { GRAPH <http://test.com/r43ples-dataset-1> REVISION \"5\""
		+ "	{	<a> <b> <c> .	}}");
		
		result = executeR43plesQuery(""
		+ "USER \"mgraube\""
		+ "MESSAGE \"test branch commit\""
		+ "	BRANCH GRAPH <http://test.com/r43ples-dataset-1> REVISION \"2\" TO \"unstable\"");
		
		result = executeR43plesQuery(""
		+ "USER \"mgraube\" "
		+ "MESSAGE \"test tag commit\" "
		+ "TAG GRAPH <http://test.com/r43ples-dataset-1> REVISION \"2\" TO \"v0.3-alpha\"");
	
	}
		
	
	
	
	
	
	
	
	/**
	 * Executes a SPARQL-query against the R43ples endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws IOException 
	 */
	private static String executeR43plesQuery(String query) throws IOException {
		return executeR43plesQueryWithFormat(query, MediaType.APPLICATION_XML);
	}
	
	/**
	 * Executes a SPARQL-query against the R43ples endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws IOException 
	 */
	private static String executeR43plesQueryWithFormat(String query, String format) throws IOException {
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
