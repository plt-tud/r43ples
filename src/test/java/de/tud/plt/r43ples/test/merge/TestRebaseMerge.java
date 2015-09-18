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


public class TestRebaseMerge {

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
		graphName = SampleDataSet.createSampleDataSetRebase();
	}
	
	
	
	/**
	 * Test the created graph.
	 * 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testCreatedGraph() throws IOException, SAXException, InternalErrorException, TemplateException {
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B1"));
		String expected1 = ResourceManagement.getContentFromResource("rebase/response-B1.xml");
		assertXMLEqual(expected1, result1);
		
		
		// Test branch B2
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected2 = ResourceManagement.getContentFromResource("rebase/response-B2.xml");
		assertXMLEqual(expected2, result2);
		
		
		// Test branch MASTER
		String result3 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected3 = ResourceManagement.getContentFromResource("rebase/response-MASTER.xml");
		assertXMLEqual(expected3, result3);
	}
	
	
	
	/**
	 * Test REBASE-MERGE B1 to Master.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testRebaseMerge1() throws SAXException, IOException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into Master
		executeR43plesRebaseQuery(createCommonRebaseMergeQuery(graphName, sdd, user, "Merge B1 into Master", "B1", "master"));
		// Test branch master
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected1 = ResourceManagement.getContentFromResource("rebase/response-B1-into-Master.xml");
		assertXMLEqual(expected1, result1);;
		
	}
	
	/**
	 * Test REBASE-MERGE B2 to Master.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testRebaseMerge2() throws SAXException, IOException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B2 into Master
		executeR43plesRebaseQuery(createCommonRebaseMergeQuery(graphName, sdd, user, "Merge B2 into Master", "B2", "master"));
		// Test branch master
		String result2 = executeR43plesQuery(createSelectQuery(graphName, "master"));
		String expected2 = ResourceManagement.getContentFromResource("rebase/response-B2-into-Master.xml");
		assertXMLEqual(expected2, result2);
		
	}
	
	/**
	 * Test FORCE-REBASE-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testForceRebaseMerge() throws SAXException, IOException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		executeR43plesQuery(createForceRebaseMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected1 = ResourceManagement.getContentFromResource("rebase/force/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);
		
	}
	

	/**
	 * Test AUTO-REBASE-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testAutoRebaseMerge() throws SAXException, IOException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		executeR43plesQuery(createAutoRebaseMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		// Test branch B1
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected1 = ResourceManagement.getContentFromResource("rebase/auto/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);
		
	}
	
	
	/**
	 * Test common Rebase MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testCommonRebaseMerge() throws IOException, SAXException, InternalErrorException, TemplateException {
		
		// Create the initial data set
		String graphWithConflict = SampleDataSet.createSampleDataSetMerging().graphName;
		
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2 (WITH)
		String triples = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		Response queryResult1 = executeR43plesQueryResponse(createRebaseMergeWithQuery(graphWithConflict, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		Assert.assertNull(queryResult1.getEntity());

		// Test branch B2
		String result1 = executeR43plesQuery(createSelectQuery(graphWithConflict, "B2"));
		String expected1 = ResourceManagement.getContentFromResource("rebase/common/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);

	}
	
	
	/**
	 * Test Rebase MANUAL-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws TemplateException 
	 */
	@Test
	public void testManualRebaseMerge() throws IOException, SAXException, InternalErrorException, TemplateException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";		
		
		// Merge B1 into B2
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"G\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n" ;
		
		executeR43plesQuery(createManualRebaseMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		
		// Test branch B2
		String result1 = executeR43plesQuery(createSelectQuery(graphName, "B2"));
		String expected1 = ResourceManagement.getContentFromResource("rebase/manual/response-B1-into-B2.xml");
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
	 * Create COMMON-REBASE-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createCommonRebaseMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "REBASE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
	}

	
	/**
	 * Create FORCE-REBASE-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createForceRebaseMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "REBASE FORCE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
	}

	/**
	 * Create AUTO-REBASE-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param sdd the SDD
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createAutoRebaseMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "REBASE AUTO GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
	}
	

	
	/**
	 * Create REBASE-MERGE-WITH query.
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
	private String createRebaseMergeWithQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB, String triples) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "REBASE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
							+ "	%s %n"
							+ "}", user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
	}
	
	
	/**
	 * Create REBASE-MANUAL-MERGE query.
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
	private String createManualRebaseMergeQuery(String graphName, String sdd, String user, String commitMessage, String branchNameA, String branchNameB, String triples) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "REBASE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO \"%s\" WITH { %n"
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
	public static String executeR43plesQuery(String query) throws InternalErrorException, TemplateException, IOException{
		return executeR43plesQueryWithFormat(query, "application/xml");
	}
	
	public static String executeR43plesRebaseQuery(String query) throws InternalErrorException, TemplateException, IOException {
		return executeR43plesQueryWithFormat(query, "text/html");
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
