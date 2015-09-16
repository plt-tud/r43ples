package de.tud.plt.r43ples.test.merge;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Endpoint;


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
	
	 */
	@Before
	public void setUp() throws InternalErrorException {
		// Create the initial data set
		graphName = SampleDataSet.createSampleDataSetFastForward();
	}
	
	
	
	/**
	 * Test the created graph.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testCreatedGraph() throws IOException, SAXException, InternalErrorException {
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
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@Test
	public void testFastForwardMerge() throws InternalErrorException, SAXException, IOException {
		executeR43plesQuery(createFastForwardMergeQuery(graphName, user, "Merge B1 into Master", "B1", "master"));
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "master"));		
		String expected1 = ResourceManagement.getContentFromResource("fastforward/response-B1-into-Master-Master.xml");
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
	private String createFastForwardMergeQuery(String graphName, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE FF GRAPH <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, branchNameA, branchNameB);
	}

	
	/**
	 * Executes a SPARQL-query against the R43ples r43ples_endpoint
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query
	 * @throws InternalErrorException 
	 */
	public static String executeR43plesQuery(String query) throws InternalErrorException {
		return executeR43plesQueryWithFormat(query, "application/xml");
	}
	
	/**
	 * Executes a SPARQL-query against the R43ples r43ples_endpoint
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws InternalErrorException 
	 */
	public static String executeR43plesQueryWithFormat(String query, String format) throws InternalErrorException {
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
	 */
	public static Response executeR43plesQueryResponse(String query) throws InternalErrorException {
		Endpoint ep = new Endpoint();
		return ep.sparql("application/xml", query);
	}

}
