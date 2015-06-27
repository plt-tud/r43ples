package de.tud.plt.r43ples.test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Endpoint;


public class TestEndpoint extends JerseyTest {

	private static String graphNameDataset1;
	private static String graphNameDataset2;
	private static String graphNameMerging;
	private static final String format = "application/sparql-results+xml";
	
	private static String query_union_b1_b2;
	
    @Override
    protected Application configure() {
        return new ResourceConfig(Endpoint.class);
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException{
		XMLUnit.setIgnoreWhitespace(true);
		Config.readConfig("r43ples.test.conf");
		graphNameDataset1 = SampleDataSet.createSampleDataset1();
		graphNameDataset2 = SampleDataSet.createSampleDataset2();
		graphNameMerging = SampleDataSet.createSampleDataSetMerging();
		query_union_b1_b2 = String.format(""
				+ "SELECT DISTINCT ?s ?p ?o "
				+ "WHERE { "
				+ "  {GRAPH <%s> REVISION \"B1\" { ?s ?p ?o}}"
				+ "  UNION "
				+ "  {GRAPH <%s> REVISION \"B2\" { ?s ?p ?o}}"
				+ "} ORDER BY ?s ?p ?o", graphNameMerging, graphNameMerging);
	}
	
	
	@Test
	public void testSelect() throws SAXException, IOException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphNameMerging);
		String result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);
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
	public void testSelectSparqlJoinOption() throws SAXException, IOException{		
		String result = target("sparql").
				queryParam("query", URLEncoder.encode(query_union_b1_b2, "UTF-8")).
				queryParam("format", format).
				queryParam("join_option", "true").
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
	public void testHtmlDebugQueryForm() throws IOException{
		String result = target("debug").queryParam("query", "").queryParam("format", MediaType.TEXT_HTML).request().get(String.class);
		Assert.assertThat(result, containsString("<form"));
	}
	
	@Test
	public void testDebug() throws IOException{
		String query = "SELECT * WHERE { GRAPH ?g { ?s ?p ?o. } }";
		String result = target("debug").queryParam("query", URLEncoder.encode(query, "UTF-8")).request().get(String.class);
		Assert.assertThat(result, containsString("http://eatld.et.tu-dresden.de/r43ples-revisions"));
	}
	
	
	@Test
	public void testSelectQueryWithoutRevision() throws IOException, SAXException {
		String query = String.format(""
				+ "select * from <%s> %n"
				+ "where { ?s ?p ?o. } %n"
				+ "ORDER BY ?s ?p ?o", graphNameMerging);
		String result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);
		String expected = ResourceManagement.getContentFromResource("response-master.xml");
		assertXMLEqual(expected, result);
	}
	
	
	/**
	 *  Test example queries from html site
	 * @throws IOException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testExampleQueries() throws IOException {		
		String query = "SELECT * FROM <" + graphNameDataset1 + "> REVISION \"3\" WHERE { ?s ?p ?o. }";
		String result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		query = ""
		+ "	SELECT ?s ?p ?o"
		+ "	FROM <" + graphNameDataset1 + "> REVISION \"master\""
		+ "	FROM <" + graphNameDataset2 + "> REVISION \"2\""
		+ "	WHERE {"
		+ "	?s ?p ?o."
		+ "	}";
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		query = ""
		+ "USER \"mgraube\""
		+ "MESSAGE \"test commit\""
		+ "	INSERT DATA { GRAPH <" + graphNameDataset1 + "> REVISION \"5\""
		+ "	{	<a> <b> <c> .	}}";
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
		
		query = ""
		+ "USER \"mgraube\""
		+ "MESSAGE \"test branch commit\""
		+ "	BRANCH GRAPH <" + graphNameDataset1 + "> REVISION \"2\" TO \"unstable\"";
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
		
		query = ""
		+ "USER \"mgraube\" "
		+ "MESSAGE \"test tag commit\" "
		+ "TAG GRAPH <" + graphNameDataset1 + "> REVISION \"2\" TO \"v0.3-alpha\"";
		result = target("sparql").queryParam("query", URLEncoder.encode(query, "UTF-8")).queryParam("format", format).request().get(String.class);		
	
	}
	
	@Test
	public void testGetRevisionGraph(){
		String result = target("revisiongraph").queryParam("format", "text/turtle").queryParam("graph", graphNameMerging).request().get(String.class);
		Assert.assertThat(result, containsString("rmo:Revision"));
		result = target("revisiongraph").queryParam("format", "application/rdf+xml").queryParam("graph", graphNameMerging).request().get(String.class);
		Assert.assertThat(result, containsString("http://eatld.et.tu-dresden.de/rmo#Revision"));
		result = target("revisiongraph").queryParam("format", "batik").queryParam("graph", graphNameDataset1).request().get(String.class);
		Assert.assertThat(result, containsString("svg"));
		result = target("revisiongraph").queryParam("format", "d3").queryParam("graph", graphNameDataset1).request().get(String.class);
		Assert.assertThat(result, containsString("svg"));
		result = target("revisiongraph").queryParam("format", "batik").queryParam("graph", graphNameMerging).request().get(String.class);
		Assert.assertThat(result, containsString("svg"));
		result = target("revisiongraph").queryParam("format", "d3").queryParam("graph", graphNameMerging).request().get(String.class);
		Assert.assertThat(result, containsString("svg"));
		
	}
	
	@Test
	public void testServiceDescription(){
		String result = target("sparql").queryParam("format", "text/turtle").request().get(String.class);
		Assert.assertThat(result, containsString("sd:r43ples"));
	}
	
	@Test
	public void testLandingPage() {
		String result = target().request().get(String.class);
		Assert.assertThat(result, containsString("R43ples (Revision for triples) is an open source Revision Management Tool for the Semantic Web"));
	}
	
	@Test
	public void testMergingPage() {
		String result = target("merging").request().get(String.class);
		Assert.assertThat(result, containsString("Development is Work in Progress"));
	}
	
	@Test
	public void testGetRevisedGraphs() throws InternalErrorException{
		String result = target("getRevisedGraphs").queryParam("format", "text/turtle").request().get(String.class);
		Assert.assertThat(result, containsString(graphNameMerging));
	}


}
