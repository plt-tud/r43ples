package de.tud.plt.r43ples.test.merge;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;
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
import freemarker.template.TemplateException;


public class TestFastForwardMerge {

	/** The graph name. **/
	private static String graphName;
	/** The user. **/
	private static String user = "xinyu";
	
	
	/**
	 * Initialize TestClass
	 * 
	 * @throws ConfigurationException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalize(true);
		Config.readConfig("r43ples.test.conf");
	}
	
	
	/**
	 * Set up.
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	
	 */
	@Before
	public void setUp() throws InternalErrorException, TemplateException, IOException {
		// Create the initial data set
		graphName = SampleDataSet.createSampleDataSetFastForward();
	}
	
	
	
	/**
	 * Test the created graph.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testCreatedGraph() throws IOException, SAXException, InternalErrorException, TemplateException {
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B1"));
		String expected1 = ResourceManagement.getContentFromResource("fastforward/response-B1.xml");
		assertXMLEqual(expected1, result1);	
		
		// Test branch MASTER
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected2 = ResourceManagement.getContentFromResource("fastforward/response-MASTER.xml");
		assertXMLEqual(expected2, result2);
	}
	
	
	/**
	 * Test FastForward Merge.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testFastForwardMerge() throws SAXException, IOException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		
		executeR43plesQuery(createFastForwardMergeQuery(graphName, sdd, user, "Merge B1 into Master", "B1", "master"));
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		
		System.out.println("result1 : "+ result1);
		String expected1 = ResourceManagement.getContentFromResource("fastforward/response-B1-into-Master-Master.xml");
		System.out.println("result1 : "+ expected1);
		assertXMLEqual(expected1, result1);
		
	}
	
	
	
	/**
	 * Create the SELECT query.
	 * 
	 * @param graphName the graph name
	 * @param revision the revision
	 * @return the query
	 */
	private String createSelectQuery(String graphName, String revision) {
		return String.format( "SELECT * FROM <%s> REVISION \"%s\" %n"
							+ "WHERE { %n"
							+ "	?s ?p ?o . %n"
							+ "} %n"
							+ "ORDER BY ?s ?p ?o", graphName, revision);
	}
	


	/**
	 * Create Fast Forward-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createFastForwardMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE ff GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
	}

	
	/**
	 * Executes a SPARQL-query against the R43ples r43ples_endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public static String executeR43plesQuery(String query) throws InternalErrorException, TemplateException, IOException {
		return executeR43plesQueryWithFormat(query, "application/xml");
	}
	
	/**
	 * Executes a SPARQL-query against the R43ples r43ples_endpoint
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public static String executeR43plesQueryWithFormat(String query, String format) throws InternalErrorException, TemplateException, IOException {
		Endpoint ep = new Endpoint();
		Response response = ep.sparql(format, query);
		if (response.getEntity()!=null)
			return response.getEntity().toString();
		else
			return "";
	}
	
	
	/**
	 * Executes a SPARQL-query against the triple store without authorization using HTTP-POST.
	 * 
	 * @param query the SPARQL query
	 * @return the response
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public static Response executeR43plesQueryResponse(String query) throws InternalErrorException, TemplateException, IOException {
		Endpoint ep = new Endpoint();
		return ep.sparql("application/xml", query);
	}

}
