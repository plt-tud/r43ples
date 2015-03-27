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
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Endpoint;


public class TestR43ples {

	/** The graph name. **/
	private static String graphName;
	
	private final Endpoint ep = new Endpoint();
	private final String format = "application/sparql-results+xml";
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException{
		XMLUnit.setIgnoreWhitespace(true);
		Config.readConfig("r43ples.test.conf");
		graphName = SampleDataSet.createSampleDataSetMerging();
	}
	
	
	@Test
	public void testSelect() throws InternalErrorException, SAXException, IOException {
		String query = String.format(""
				+ "SELECT * FROM <%s> REVISION \"B2\" "
				+ "WHERE { ?s ?p ?o. }"
				+ "ORDER BY ?s ?p ?o", graphName);
		String result3 = ep.sparql(format, query).getEntity().toString();
		String expected3 = ResourceManagement.getContentFromResource("response-1.1-3.xml");
		assertXMLEqual(expected3, result3);
	}
	
	
	@Test
	public void testSelectSparqlJoinOption() throws IOException, SAXException, InternalErrorException{		
		String query = String.format(""
						+ "SELECT ?s ?p ?o "
						+ "FROM <%s> REVISION \"B1\" "
						+ "FROM <%s> REVISION \"B2\" "
						+ "WHERE { ?s ?p ?o. }"
						+ "ORDER BY ?s ?p ?o", graphName, graphName);
		String result4 = ep.sparql(format, query, true).getEntity().toString();
		String expected4 = ResourceManagement.getContentFromResource("response-b1-b2.xml");
		assertXMLEqual(expected4, result4);
	}
	

	@Test
	public void testHtmlQueryForm() throws IOException, InternalErrorException{
		String result = ep.sparql(MediaType.TEXT_HTML, "").getEntity().toString();
		Assert.assertThat(result, containsString("<form"));
	}
	
	
	@Test
	public void testSelectQueryWithoutRevision() throws IOException, SAXException, InternalErrorException {
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
	 */
	@Test public void testExampleQueries() throws IOException, InternalErrorException {
		String graph1 = SampleDataSet.createSampleDataset1();
		String graph2 = SampleDataSet.createSampleDataset2();
		
		String query = "SELECT * FROM <" + graph1 + "> REVISION \"3\" WHERE { ?s ?p ?o. }";
				
		String result = ep.sparql(format, query).getEntity().toString();
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		query = ""
		+ "	SELECT ?s ?p ?o"
		+ "	FROM <" + graph1 + "> REVISION \"master\""
		+ "	FROM <" + graph2 + "> REVISION \"2\""
		+ "	WHERE {"
		+ "	?s ?p ?o."
		+ "	}";
		result = ep.sparql(format, query).getEntity().toString();
		Assert.assertThat(result, containsString("http://test.com/Adam"));
		
		query = ""
		+ "USER \"mgraube\""
		+ "MESSAGE \"test commit\""
		+ "	INSERT DATA { GRAPH <" + graph1 + "> REVISION \"5\""
		+ "	{	<a> <b> <c> .	}}";
		result = ep.sparql(format, query).getEntity().toString();
		
		query = ""
		+ "USER \"mgraube\""
		+ "MESSAGE \"test branch commit\""
		+ "	BRANCH GRAPH <" + graph1 + "> REVISION \"2\" TO \"unstable\"";
		result = ep.sparql(format, query).getEntity().toString();
		
		query = ""
		+ "USER \"mgraube\" "
		+ "MESSAGE \"test tag commit\" "
		+ "TAG GRAPH <" + graph1 + "> REVISION \"2\" TO \"v0.3-alpha\"";
		result = ep.sparql(format, query).getEntity().toString();
	
	}

}
