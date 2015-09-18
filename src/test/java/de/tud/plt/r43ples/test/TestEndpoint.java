package de.tud.plt.r43ples.test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.DataSetGenerationResult;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Endpoint;
import freemarker.template.TemplateException;


public class TestEndpoint {

	/** The graph name. **/
	private static String graphName;
	
	private final Endpoint ep = new Endpoint();
	private final String format = "application/sparql-results+xml";
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException, TemplateException {
		XMLUnit.setIgnoreWhitespace(true);
		Config.readConfig("r43ples.test.conf");
		DataSetGenerationResult ds = SampleDataSet.createSampleDataSetMerging();
		graphName = ds.graphName;
	}
	
	
	@Test
	public void testSelect() throws InternalErrorException, SAXException, IOException, TemplateException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result3 = ep.sparql(format, query).getEntity().toString();
		String expected3 = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		assertXMLEqual(expected3, result3);
	}
	
	
	@Test
	public void testSelectSparqlJoinOption() throws IOException, SAXException, InternalErrorException, TemplateException {		
		String query = String.format(""
						+ "SELECT ?s ?p ?o "
						+ "WHERE { "
						+ "  {GRAPH <%s> REVISION \"B1\" { ?s ?p ?o}}"
						+ "  UNION "
						+ "  {GRAPH <%s> REVISION \"B2\" { ?s ?p ?o}}"
						+ "} ORDER BY ?s ?p ?o", graphName, graphName);
		String result4 = ep.sparql(format, query, true).getEntity().toString();
		String expected4 = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		assertXMLEqual(expected4, result4);
	}
	

	@Test
	public void testHtmlQueryForm() throws IOException, InternalErrorException, TemplateException {
		String result = ep.sparql(MediaType.TEXT_HTML, "").getEntity().toString();
		Assert.assertThat(result, containsString("<form"));
	}
	
	
	@Test
	public void testSelectQueryWithoutRevision() throws IOException, SAXException, InternalErrorException, TemplateException {
		String query = String.format(""
				+ "select * from <%s> %n"
				+ "where { ?s ?p ?o. } %n"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result = ep.sparql(format, query).getEntity().toString();
		String expected = ResourceManagement.getContentFromResource("response-master.xml");
		assertXMLEqual(expected, result);
	}
	
	
	/**
	 *  Test example queries from html site
	 * @throws IOException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test public void testExampleQueries() throws IOException, InternalErrorException, TemplateException {
		DataSetGenerationResult ds1 = SampleDataSet.createSampleDataset1();
		DataSetGenerationResult ds2 = SampleDataSet.createSampleDataset2();
		
		String query = String.format(
				"SELECT * FROM <%s> REVISION \"%s\" WHERE { ?s ?p ?o. }",
				ds1.graphName, ds1.revisions.get("master-3"));
				
		String result = ep.sparql(format, query).getEntity().toString();
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		query = String.format(""
				+ "	SELECT ?s ?p ?o"
				+ "	FROM <%s> REVISION \"master\""
				+ "	FROM <%s> REVISION \"%s\""
				+ "	WHERE {"
				+ "	?s ?p ?o."
				+ "	}", ds1.graphName, ds2.graphName, ds2.revisions.get("master-2"));
		result = ep.sparql(format, query).getEntity().toString();
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		query = String.format(""
				+ "USER \"mgraube\""
				+ "MESSAGE \"test commit\""
				+ "	INSERT DATA { GRAPH <%s> REVISION \"%s\""
				+ "	{	<a> <b> <c> .	}}",
				ds1.graphName, ds1.revisions.get("master-5"));	
		result = ep.sparql(format, query).getEntity().toString();
		
		query = String.format(""
				+ "USER \"mgraube\" "
				+ "MESSAGE \"test branch commit\" "
				+ "BRANCH GRAPH <%s> REVISION \"%s\" TO \"unstable\"",
				ds1.graphName, ds1.revisions.get("master-2"));
		result = ep.sparql(format, query).getEntity().toString();
		
		query = String.format(""
				+ "USER \"mgraube\" "
				+ "MESSAGE \"test tag commit\" "
				+ "TAG GRAPH <%s> REVISION \"%s\" TO \"v0.3-alpha\"",
				ds1.graphName, ds1.revisions.get("master-5"));
		result = ep.sparql(format, query).getEntity().toString();
	
	}

}
