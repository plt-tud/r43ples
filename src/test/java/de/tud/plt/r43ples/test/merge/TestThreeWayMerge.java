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


public class TestThreeWayMerge {

	/** The graph name. **/
	private static String graphName;
	/** The user. **/
	private static String user = "xinyu";
	
	private final Endpoint 	ep = new Endpoint();
	
	
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
	 *  
	
	 */
	@Before
	public void setUp() throws InternalErrorException, IOException {
		// Create the initial data set
		graphName = SampleDataSet.createSampleDataSetMerging().graphName;
	}
	
	
	
	/**
	 * Test the created graph.
	 * 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 * @throws IOException 
	 */
	@Test
	public void testCreatedGraph() throws SAXException, InternalErrorException, IOException {
		// Test branch B1
		String result1 = ep.sparql(createSelectQuery(graphName, "B1")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("threeway/response-B1.xml");
		assertXMLEqual(expected1, result1);
		
		
		// Test branch B2
		String result3 = ep.sparql(createSelectQuery(graphName, "B2")).getEntity().toString();
		String expected3 = ResourceManagement.getContentFromResource("threeway/response-B2.xml");
		assertXMLEqual(expected3, result3);
		
		
		// Test branch MASTER
		String result5 = ep.sparql(createSelectQuery(graphName, "master")).getEntity().toString();
		String expected5 = ResourceManagement.getContentFromResource("threeway/response-MASTER.xml");
		assertXMLEqual(expected5, result5);
	}
	
	
	/**
	 * Test AUTO-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 *  
	 */
	@Test
	public void testAutoMerge() throws SAXException, IOException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		ep.sparql(createAutoMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		// Test branch B1
		String result1 = ep.sparql(createSelectQuery(graphName, "B2")).getEntity().toString();
		
		String expected1 = ResourceManagement.getContentFromResource("threeway/auto/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);
		
	}
	
	
	/**
	 * Test common MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 *  
	 */
	@Test
	public void testCommonMerge() throws IOException, SAXException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// conflicts in merging, therefore no success
		ep.sparql(createCommonMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		
		
		// Merge B1 into B2 (WITH)
		String triples = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		Response queryResult1 = ep.sparql(createMergeWithQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		Assert.assertNull(queryResult1.getEntity());

		// Test branch B2
		String result1 = ep.sparql(createSelectQuery(graphName, "B2")).getEntity().toString();
		

		String expected1 = ResourceManagement.getContentFromResource("threeway/common/response-B1-into-B2.xml");
		assertXMLEqual(expected1, result1);

	}
	
	
	/**
	 * Test MANUAL-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 *  
	 */
	@Test
	public void testManualMerge() throws IOException, SAXException, InternalErrorException {
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
		
		ep.sparql(createManualMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		
		// Test branch B2
		String result1 = ep.sparql(createSelectQuery(graphName, "B2")).getEntity().toString();
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

}
