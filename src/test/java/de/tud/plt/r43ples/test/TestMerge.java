package de.tud.plt.r43ples.test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.Endpoint;


public class TestMerge {

	/** The graph name. **/
	private static String graphName = "http://exampleGraph.com/r43ples/merge";
	/** The user. **/
	private static String user = "shensel";
	
	
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
	
	@AfterClass
	public static void tearDownAfterClass() {
		TripleStoreInterfaceSingleton.close();
	}
	
	/**
	 * Set up.
	 * @throws InternalErrorException 
	
	 */
	@Before
	public void setUp() throws InternalErrorException {
		// Create the initial data set
		SampleDataSet.createSampleDataSetComplexStructure(graphName);
	}
	
	/**
	 * Tear down.
	 */
	@After
	public void tearDown() {
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
		String expected1 = ResourceManagement.getContentFromResource("merge/response-B1.xml");
		assertXMLEqual(expected1, result1);
		
		// Test branch B1X
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "B1X"));
		String expected2 = ResourceManagement.getContentFromResource("merge/response-B1X.xml");
		assertXMLEqual(expected2, result2);
		
		// Test branch B2
		String result3 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected3 = ResourceManagement.getContentFromResource("merge/response-B2.xml");
		assertXMLEqual(expected3, result3);
		
		// Test branch B2X
		String result4 = executeR43plesQuery(createSelectQuery(graphName, "B2X"));
		String expected4 = ResourceManagement.getContentFromResource("merge/response-B2X.xml");
		assertXMLEqual(expected4, result4);
		
		// Test branch MASTER
		String result5 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected5 = ResourceManagement.getContentFromResource("merge/response-MASTER.xml");
		assertXMLEqual(expected5, result5);
	}
	
	
	/**
	 * Test AUTO-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testAutoMerge() throws SAXException, IOException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1X into B1
		executeR43plesQuery(createAutoMergeQuery(graphName, sdd, user, "Merge B1X into B1", "B1X", "B1"));
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B1"));
		String expected1 = ResourceManagement.getContentFromResource("merge/auto/response-B1-B1X-into-B1.xml");
		assertXMLEqual(expected1, result1);
		
		
		// Merge B2X into B2
		executeR43plesQuery(createAutoMergeQuery(graphName, sdd, user, "Merge B2X into B2", "B2X", "B2"));
		
		// Test branch B2
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected2 = ResourceManagement.getContentFromResource("merge/auto/response-B2-B2X-into-B2.xml");
		assertXMLEqual(expected2, result2);
		
		
		// Merge B1 into B2
		executeR43plesQuery(createAutoMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		
		// Test branch B2
		String result3 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected3 = ResourceManagement.getContentFromResource("merge/auto/response-B2-B1-into-B2.xml");
		assertXMLEqual(expected3, result3);
		
		
		// Merge B2 into MASTER
		executeR43plesQuery(createAutoMergeQuery(graphName, sdd, user, "Merge B2 into MASTER", "B2", "master"));
		
		// Test branch MASTER
		String result4 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected4 = ResourceManagement.getContentFromResource("merge/auto/response-MASTER-B2-into-MASTER.xml");
		assertXMLEqual(expected4, result4);
	}
	
	
	/**
	 * Test common MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testCommonMerge() throws IOException, SAXException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1X into B1
		Response queryResult1 = executeR43plesQueryResponse(createCommonMergeQuery(graphName, sdd, user, "Merge B1X into B1", "B1X", "B1"));
		Assert.assertNull(queryResult1.getEntity());
		
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B1"));
		String expected1 = ResourceManagement.getContentFromResource("merge/common/response-B1-B1X-into-B1.xml");
		assertXMLEqual(expected1, result1);
		
		
		// Merge B2X into B2
		Response queryResult2 = executeR43plesQueryResponse(createCommonMergeQuery(graphName, sdd, user, "Merge B2X into B2", "B2X", "B2"));
		Assert.assertNull(queryResult2.getEntity());
		
		// Test branch B2
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected2 = ResourceManagement.getContentFromResource("merge/common/response-B2-B2X-into-B2.xml");
		assertXMLEqual(expected2, result2);
		
		
		// Merge B1 into B2
		Response queryResult3 = executeR43plesQueryResponse(createCommonMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		Assert.assertThat(queryResult3.getEntity().toString(), containsString("hasTripleStateB"));
		
		// Merge B1 into B2 (WITH)
		String triples = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		Response queryResult3_1 = executeR43plesQueryResponse(createMergeWithQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		Assert.assertNull(queryResult3_1.getEntity());

		// Test branch B2
		String result3 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected3 = ResourceManagement.getContentFromResource("merge/common/response-B2-B1-into-B2.xml");
		assertXMLEqual(expected3, result3);

		
		// Merge B2 into MASTER
		Response queryResult4 = executeR43plesQueryResponse(createCommonMergeQuery(graphName, sdd, user, "Merge B2 into MASTER", "B2", "master"));
		Assert.assertThat(queryResult4.getEntity().toString(), containsString("hasTripleStateB"));
		
		// Merge B2 into MASTER (WITH)
		triples = "<http://example.com/testS> <http://example.com/testP> \"M\". \n";
		
		Response queryResult4_1 = executeR43plesQueryResponse(createMergeWithQuery(graphName, sdd, user, "Merge B2 into MASTER", "B2", "master", triples));
		Assert.assertNull(queryResult4_1.getEntity());
		
		// Test branch MASTER
		String result4 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected4 =ResourceManagement.getContentFromResource("merge/common/response-MASTER-B2-into-MASTER.xml");
		assertXMLEqual(expected4, result4);
	}
	
	
	/**
	 * Test MANUAL-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testManualMerge() throws IOException, SAXException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1X into B1
		String triples = "<http://example.com/testS> <http://example.com/testP> \"X\". \n";
		
		executeR43plesQuery(createManualMergeQuery(graphName, sdd, user, "Merge B1X into B1", "B1X", "B1", triples));
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B1"));
		String expected1 = ResourceManagement.getContentFromResource("merge/manual/response-B1-B1X-into-B1.xml");
		assertXMLEqual(expected1, result1);
		
		
		// Merge B2X into B2
		triples = "<http://example.com/testS> <http://example.com/testP> \"Y\". \n";
		
		executeR43plesQuery(createManualMergeQuery(graphName, sdd, user, "Merge B2X into B2", "B2X", "B2", triples));
		
		// Test branch B2
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected2 = ResourceManagement.getContentFromResource("merge/manual/response-B2-B2X-into-B2.xml");
		assertXMLEqual(expected2, result2);
		
		
		// Merge B1 into B2
		triples = "<http://example.com/testS> <http://example.com/testP> \"X\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"Z\". \n";
		
		executeR43plesQuery(createManualMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		
		// Test branch B2
		String result3 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected3 = ResourceManagement.getContentFromResource("merge/manual/response-B2-B1-into-B2.xml");
		assertXMLEqual(expected3, result3);
		
		
		// Merge B2 into MASTER
		triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		executeR43plesQuery(createManualMergeQuery(graphName, sdd, user, "Merge B2 into MASTER", "B2", "master", triples));
		
		// Test branch MASTER
		String result4 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected4 = ResourceManagement.getContentFromResource("merge/manual/response-MASTER-B2-into-MASTER.xml");
		assertXMLEqual(expected4, result4);
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
	 * Create AUTO-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createAutoMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE AUTO GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
	}
	
	
	/**
	 * Create common MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createCommonMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
	}

	
	/**
	 * Create MERGE-WITH query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @param triples the triples which should be in the WITH part as N-Triples
	 * @return the query
	 */
	private String createMergeWithQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB, String triples) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
							+ "	%s %n"
							+ "}", user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
	}
	
	
	/**
	 * Create MANUAL-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @param triples the triples which should be in the WITH part as N-Triples
	 * @return the query
	 */
	private String createManualMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB, String triples) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
							+ "	%s %n"
							+ "}", user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
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
