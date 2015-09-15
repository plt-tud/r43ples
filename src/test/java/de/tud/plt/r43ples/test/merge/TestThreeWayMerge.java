package de.tud.plt.r43ples.test.merge;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

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


public class TestThreeWayMerge {

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
		graphName = SampleDataSet.createSampleDataSetMerging().graphName;
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
		String expected1 = ResourceManagement.getContentFromResource("threeway/response-B1.xml");
		assertXMLEqual(expected1, result1);
		
		
		// Test branch B2
		String result3 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected3 = ResourceManagement.getContentFromResource("threeway/response-B2.xml");
		assertXMLEqual(expected3, result3);
		
		
		// Test branch MASTER
		String result5 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected5 = ResourceManagement.getContentFromResource("threeway/response-MASTER.xml");
		assertXMLEqual(expected5, result5);
	}
	
	
	/**
	 * Test AUTO-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testAutoMerge() throws SAXException, IOException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		executeR43plesQuery(createAutoMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		
		String expected1 = ResourceManagement.getContentFromResource("threeway/auto/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);
		
	}
	
	
	/**
	 * Test common MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testCommonMerge() throws IOException, SAXException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2 (WITH)
		String triples = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		Response queryResult1 = executeR43plesQueryResponse(createMergeWithQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		Assert.assertNull(queryResult1.getEntity());

		// Test branch B2
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		

		String expected1 = ResourceManagement.getContentFromResource("threeway/common/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);

	}
	
	
	/**
	 * Test MANUAL-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testManualMerge() throws IOException, SAXException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";		
		
		// Merge B1 into B2
		String triples = "<http://example.com/testS> <http://example.com/testP> \"C\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"I\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		
		executeR43plesQuery(createManualMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		
		// Test branch B2
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected1 = ResourceManagement.getContentFromResource("threeway/manual/response-B1-into-B2.xml");
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
