package de.tud.plt.r43ples.test;

import static org.hamcrest.core.StringContains.containsString;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Service;


public class TestR43ples {

	/** The endpoint. **/
	private static String endpoint = "http://localhost:9998/r43ples/sparql";
	/** The graph name. **/
	private static String graphName = "http://exampleGraph.com/r43ples";
	
	
	@BeforeClass
	public static void setUp() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException{
		XMLUnit.setIgnoreWhitespace(true);
		Config.readConfig("r43ples.test.conf");
		Service.start();
		SampleDataSet.createSampleDataSetMerging(graphName);
	}
	
	@AfterClass
	public static void tearDown() {
		Service.stop();
	}
	
	
	@Test
	public void testSelect() throws IOException, SAXException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result3 = executeR43plesQuery(query);
		String expected3 = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		assertXMLEqual(expected3, result3);
	}
	
	
	@Test
	public void testSelectSparqlJoinOption() throws IOException, SAXException{		
		String query = String.format(""
						+ "OPTION r43ples:SPARQL_JOIN %n"
						+ "SELECT ?s ?p ?o "
						+ "FROM <%s> REVISION \"B1\" "
						+ "FROM <%s> REVISION \"B2\" "
						+ "WHERE { ?s ?p ?o. }"
						+ "ORDER BY ?s ?p ?o", graphName, graphName);
		String result4 = executeR43plesQuery(query);
		String expected4 = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		assertXMLEqual(expected4, result4);
	}
	

	
	@Test
	public void testServiceDescription() throws IOException{
		String result = executeR43plesQueryWithFormat("", "text/turtle");
		Assert.assertThat(result, containsString("sd:r43ples"));
	}
	
	@Test
	public void testHtmlQueryForm() throws IOException{
		String result = executeR43plesQueryWithFormat("", MediaType.TEXT_HTML);
		Assert.assertThat(result, containsString("<form"));
	}
	
	
	@Test
	public void testSelectQueryWithoutRevision() throws IOException, SAXException {
		String query = String.format(""
				+ "select * from <%s> %n"
				+ "where { ?s ?p ?o. } %n"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result = executeR43plesQuery(query);
		String expected = ResourceManagement.getContentFromResource("response-master.xml");
		assertXMLEqual(expected, result);
	}
	
	
	/**
	 *  Test example queries from html site
	 * @throws IOException 
	 * @throws InternalErrorException 
	 */
	@Test public void testExampleQueries() throws IOException, InternalErrorException {
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
		+ "	INSERT DATA { GRAPH <http://test.com/r43ples-dataset-1> REVISION \"5\""
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
		return executeR43plesQueryWithFormat(query, "application/sparql-results+xml");
	}
	
	/**
	 * Executes a SPARQL-query against the R43ples endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws IOException 
	 */
	private static String executeR43plesQueryWithFormat(String query, String format) throws IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
	    HttpPost request = new HttpPost(endpoint);
		
		//set up HTTP Post Request (look at http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSSparqlProtocol for Protocol)
	    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("format",format));
		nameValuePairs.add(new BasicNameValuePair("query", query));
    	request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
    	request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
    	HttpResponse response = httpClient.execute(request);

		InputStreamReader in = new InputStreamReader(response.getEntity().getContent());
		String result = IOUtils.toString(in);
		return result;
	
	}

}
