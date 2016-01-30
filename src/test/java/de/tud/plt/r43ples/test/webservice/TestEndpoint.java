package de.tud.plt.r43ples.test.webservice;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.DataSetGenerationResult;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.Endpoint;
import de.tud.plt.r43ples.webservice.Merging;
import de.tud.plt.r43ples.webservice.Misc;


public class TestEndpoint extends JerseyTest {

	private static final String format = "application/sparql-results+xml";
	
	private static String query_union_b1_b2;
	private static DataSetGenerationResult ds1;
	private static DataSetGenerationResult ds2;
	private static DataSetGenerationResult dsm;
	
    @Override
    protected Application configure() {
        return new ResourceConfig(Endpoint.class, Misc.class, Merging.class);
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException {
		XMLUnit.setIgnoreWhitespace(true);
		Config.readConfig("r43ples.test.conf");
		ds1 = SampleDataSet.createSampleDataset1();
		ds2 = SampleDataSet.createSampleDataset2();
		dsm = SampleDataSet.createSampleDataSetMerging();
		query_union_b1_b2 = String.format(""
				+ "SELECT DISTINCT ?s ?p ?o "
				+ "WHERE { "
				+ "  {GRAPH <%1$s> REVISION \"B1\" { ?s ?p ?o}}"
				+ "  UNION "
				+ "  {GRAPH <%1$s> REVISION \"B2\" { ?s ?p ?o}}"
				+ "} ORDER BY ?s ?p ?o", dsm.graphName);
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		TripleStoreInterfaceSingleton.close();
	}
	
	
	@Test
	public void testSelect() throws SAXException, IOException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", dsm.graphName);
		String result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);
		String expected = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		assertXMLEqual(expected, result);
	}
	
	@Test
	public void testPOSTDirect() throws SAXException, IOException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", dsm.graphName);
		Response response = target("sparql").request(format).post(Entity.entity(query, "application/sparql-query"));
		String result = response.readEntity(String.class);
		String expected = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		assertXMLEqual(expected, result);
	}
	
	
	@Test
	public void testSelectSparqlUnion() throws SAXException, IOException {		
		String result = target("sparql").
				queryParam("query", URLEncoder.encode(query_union_b1_b2, "UTF-8")).
				queryParam("format", format).
				request().get(String.class);
		String expected = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		assertXMLEqual(expected, result);
	}
	
	
	@Test
	public void testSelectSparqlQueryRewritingOption() throws SAXException, IOException{		
		String result = target("sparql").
				queryParam("query", URLEncoder.encode(query_union_b1_b2, "UTF-8")).
				queryParam("format", format).
				queryParam("query_rewriting", "true").
				request().get(String.class);
		String expected = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		assertXMLEqual(expected, result);
	}
	

	@Test
	public void testHtmlQueryForm() throws IOException{
		String result = target("sparql").queryParam("query", "").queryParam("format", MediaType.TEXT_HTML).request().get(String.class);
		Assert.assertThat(result, containsString("<form"));
	}
	
	
	@Test
	public void testSelectQueryWithoutRevision() throws IOException, SAXException {
		String query = String.format(""
				+ "select * from <%s> %n"
				+ "where { ?s ?p ?o. } %n"
				+ "ORDER BY ?s ?p ?o", dsm.graphName);
		String result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);
		String expected = ResourceManagement.getContentFromResource("response-master.xml");
		assertXMLEqual(expected, result);
	}
	
	
	/**
	 *  Test example queries from html site
	 * @throws IOException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */

	@Test
	public void testExampleQueries() throws IOException {		
		String query = String.format(
				"SELECT * FROM <%s> REVISION \"%s\" WHERE { ?s ?p ?o. }",
				ds1.graphName, ds1.revisions.get("master-3"));
		String result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
		Assert.assertThat(result, containsString("http://test.com/Adam"));
				
		query = String.format(""
				+ "	SELECT ?s ?p ?o"
				+ "	FROM <%s> REVISION \"master\""
				+ "	FROM <%s> REVISION \"%s\""
				+ "	WHERE {"
				+ "	?s ?p ?o."
				+ "	}", ds1.graphName, ds2.graphName, ds2.revisions.get("master-2"));
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);
		Assert.assertThat(result, containsString("http://test.com/Adam"));

		query = String.format(""
				+ "USER \"mgraube\""
				+ "MESSAGE \"test commit\""
				+ "	INSERT DATA { GRAPH <%s> REVISION \"%s\""
				+ "	{	<a> <b> <c> .	}}",
				ds1.graphName, ds1.revisions.get("master-5"));	
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
	
		query = String.format(""
				+ "USER \"mgraube\" "
				+ "MESSAGE \"test branch commit\" "
				+ "BRANCH GRAPH <%s> REVISION \"%s\" TO \"unstable\"",
				ds1.graphName, ds1.revisions.get("master-2"));
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);
		
		query = String.format(""
				+ "USER \"mgraube\" "
				+ "MESSAGE \"test tag commit\" "
				+ "TAG GRAPH <%s> REVISION \"%s\" TO \"v0.3-alpha\"",
				ds1.graphName, ds1.revisions.get("master-5"));
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
	
	}
	
	@Test
	public void testGetRevisionGraph(){
		String result = target("revisiongraph").queryParam("format", "text/turtle").queryParam("graph", dsm.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("Revision"));
		result = target("revisiongraph").queryParam("format", "application/rdf+xml").queryParam("graph", dsm.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("http://eatld.et.tu-dresden.de/rmo#Revision"));
		result = target("revisiongraph").queryParam("format", "batik").queryParam("graph", ds1.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("svg"));
		result = target("revisiongraph").queryParam("format", "d3").queryParam("graph", ds1.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("visualisation"));
		result = target("revisiongraph").queryParam("format", "batik").queryParam("graph", dsm.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("svg"));
		result = target("revisiongraph").queryParam("format", "d3").queryParam("graph", dsm.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("visualisation"));
	}
	
	@Test
	public void testServiceDescription(){
		String result = target("sparql").queryParam("format", "text/turtle").request().get(String.class);
		Assert.assertThat(result, containsString("sd:r43ples"));
	}

	@Test
	public void testMergingPage() {
		String result = target("merging").request().get(String.class);
		Assert.assertThat(result, containsString("Merge"));
	}
	

}
