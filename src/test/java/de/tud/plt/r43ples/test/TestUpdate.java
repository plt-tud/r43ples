package de.tud.plt.r43ples.test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Endpoint;

public class TestUpdate {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(TestUpdate.class);
	/** The graph name. **/
	private static String graphName;
	private static String graph_test;
	
	private final static String format = "application/sparql-results+xml";
	
	private final Endpoint 	ep = new Endpoint();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalize(true);
		Config.readConfig("r43ples.test.conf");
		graph_test = SampleDataSet.createSampleDataset1();
	}

	@Before
	public void setUp() throws  ConfigurationException, InternalErrorException{
		graphName= SampleDataSet.createSampleDataSetMerging();
	}
	

	@Test
	public void test_insert_existing_triples() throws SAXException, IOException, InternalErrorException {
        String query_template = ""
        		+ "SELECT ?s ?p ?o FROM <"+graph_test+"> REVISION \"%d\"%n"
        		+ "WHERE {?s ?p ?o} ORDER By ?s ?p ?o";
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("5");
		
		String insert_template = ""
				+ "USER \"test_user\" %n"
				+ "MESSAGE \"test commit message 6 (same as 5)\" %n"
        		+ "INSERT DATA { GRAPH <%s> REVISION \"5\" { %s } }; %n"
        		+ "DELETE DATA { GRAPH <%s> REVISION \"5\" { %s } } ";
		ep.sparql(format, String.format(insert_template, 
				graph_test,	ResourceManagement.getContentFromResource("samples/dataset1/added-5.nt"), 
				graph_test, ResourceManagement.getContentFromResource("samples/dataset1/removed-5.nt")));
		
        String result = ep.sparql(format, String.format(query_template, 6)).getEntity().toString();
        String expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev5.xml");
        assertXMLEqual(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 5)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev5.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testRestructuring() throws SAXException, IOException, InternalErrorException {
		String query = "SELECT ?s ?p ?o FROM <"+graphName+"> REVISION \"B2\"\n"
        		+ "WHERE {?s ?p ?o} ORDER By ?s ?p ?o";
		String result = ep.sparql(format, query).getEntity().toString();
        String expected = ResourceManagement.getContentFromResource("dataset-merge/response-B2.xml");
        assertXMLEqual(expected, result);
        
		// restructure commit to B2
		logger.info("Restructure commit to B2");
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"restructure commit to B2.\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"B2\" {"
				+ " <http://example.com/newTestS> <http://example.com/newTestP> ?o."
				+ "} } %n"
				+ "WHERE { GRAPH <%s> REVISION \"B2\" {"
				+ "	<http://example.com/testS> <http://example.com/testP> ?o"
				+ "} };"
				+ "DELETE { GRAPH <%s> REVISION \"B2\" {"
				+ " <http://example.com/testS> <http://example.com/testP> ?o."
				+ "} } %n"
				+ "WHERE { GRAPH <%s> REVISION \"B2\" {"
				+ "	<http://example.com/testS> <http://example.com/testP> ?o"
				+ "} }", 
				graphName, graphName, graphName, graphName);
		logger.debug("Execute query: \n" + query);
		result = ep.sparql(format, query).toString();
		
		query = "SELECT ?s ?p ?o FROM <"+graphName+"> REVISION \"B2\"\n"
        		+ "WHERE {?s ?p ?o} ORDER By ?s ?p ?o";
		result = ep.sparql(format, query).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset-merge/response-B2-restructured.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testConstructQuery() throws InternalErrorException {
		String query = String.format(""
				+ "CONSTRUCT {?s ?p ?o} "
				+ "FROM <%s> REVISION \"1\""
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ASC(?o)", graphName);
		Response response = ep.sparql("text/turtle", query);
		String result = response.getEntity().toString();
		
		Assert.assertThat(result, containsString("\"A\""));
		Assert.assertThat(result, containsString("\"B\""));
		Assert.assertThat(result, containsString("\"C\""));
		Assert.assertThat(result, not(containsString("\"D\"")));
		Assert.assertThat(result, not(containsString("\"E\"")));
	}

}
