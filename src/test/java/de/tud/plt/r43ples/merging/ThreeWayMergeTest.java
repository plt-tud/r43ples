package de.tud.plt.r43ples.merging;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;


/**
 * Tests the 3-Way-Merge of the merging management.
 *
 * @author Xinyu Yang
 * @author Stephan Hensel
 */

public class ThreeWayMergeTest extends R43plesTest {

	/** The graph name. **/
	private static String graphName;
	/** The user. **/
	private static String user = "jUnitUser";
	/** The triple store interface. **/
	private static TripleStoreInterface tripleStoreInterface;

	
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
		tripleStoreInterface = TripleStoreInterfaceSingleton.get();
		tripleStoreInterface.dropAllGraphsAndReInit();
	}
	
	
	/**
	 * Set up.
	 * @throws InternalErrorException
	 */
	@Before
	public void setUp() throws InternalErrorException {
		// Create the initial data set
		graphName = SampleDataSet.createSampleDataSetMerging().graphName;
	}
	
	
	
	/**
	 * Test the created graph.
	 * 
	 * @throws InternalErrorException
	 */
	@Test
	public void testCreatedGraph() throws InternalErrorException {
		// Test branch B1
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "b1")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("threeway/response-B1.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));

		// Test branch B2
		String result3 = ep.sparql("text/turtle", createConstructQuery(graphName, "b2")).getEntity().toString();
		String expected3 = ResourceManagement.getContentFromResource("threeway/response-B2.ttl");
		assertTrue(check_isomorphism(result3, "TURTLE", expected3, "TURTLE"));

		// Test branch MASTER
		String result5 = ep.sparql("text/turtle", createConstructQuery(graphName, "master")).getEntity().toString();
		String expected5 = ResourceManagement.getContentFromResource("threeway/response-MASTER.ttl");
		assertTrue(check_isomorphism(result5, "TURTLE", expected5, "TURTLE"));
	}
	
	
	/**
	 * Test AUTO-MERGE.
	 * 
	 * @throws InternalErrorException
	 */
	@Test
	public void testAutoMerge() throws InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		ep.sparql(createAutoMergeQuery(graphName, sdd, user, "Merge B1 into B2", "b1", "b2"));
		// Test branch B1
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "b2")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("threeway/auto/response-B1-into-B2.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));
		
	}
	
	
	/**
	 * Test common MERGE.
	 * 
	 * @throws InternalErrorException
	 */
	@Test
	public void testCommonMerge() throws InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// conflicts in merging, therefore no success
		ep.sparql(createCommonMergeQuery(graphName, sdd, user, "Merge B1 into B2", "b1", "b2"));

		// Merge B1 into B2 (WITH)
		String triples = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		Response queryResult1 = ep.sparql(createMergeWithQuery(graphName, sdd, user, "Merge B1 into B2", "b1", "b2", triples));
		Assert.assertNull(queryResult1.getEntity());

		// Test branch B2
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "b2")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("threeway/common/response-B1-into-B2.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));

	}
	
	
	/**
	 * Test MANUAL-MERGE.
	 * 
	 * @throws InternalErrorException
	 */
	@Test
	public void testManualMerge() throws InternalErrorException {
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
		
		ep.sparql(createManualMergeQuery(graphName, sdd, user, "Merge B1 into B2", "b1", "b2", triples));
		
		// Test branch B2
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "b2")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("threeway/manual/response-B1-into-B2.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));
		
	}


	/**
	 * Create the CONSTRUCT query.
	 *
	 * @param graphName the graph name
	 * @param revision the revision
	 * @return the query
	 */
	private String createConstructQuery(String graphName, String revision) {
		return String.format( "CONSTRUCT FROM <%s> REVISION \"%s\" %n"
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
							+ "MERGE AUTO GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO BRANCH \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
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
							+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO BRANCH \"%s\"", user, commitMessage, graphName, sdd, branchNameA, branchNameB);
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
							+ "MERGE GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO BRANCH \"%s\" WITH { %n"
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
							+ "MERGE MANUAL GRAPH <%s> SDD <%s> BRANCH \"%s\" INTO BRANCH \"%s\" WITH { %n"
							+ "	%s %n"
							+ "}", user, commitMessage, graphName, sdd, branchNameA, branchNameB, triples);
	}

}
