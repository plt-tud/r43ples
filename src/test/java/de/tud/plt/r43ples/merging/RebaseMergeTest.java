package de.tud.plt.r43ples.merging;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.*;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertTrue;


/**
 * Tests the 3-Way-Merge of the merging management.
 *
 * @author Xinyu Yang
 * @author Stephan Hensel
 */
@Ignore
public class RebaseMergeTest extends R43plesTest {

	/** The graph name. **/
	private static String graphName;
	/** The user. **/
	private static String user = "jUnitUser";

	
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
		graphName = SampleDataSet.createSampleDataSetRebase();
	}
	

	/**
	 * Test the created graph.
	 * 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testCreatedGraph() throws IOException, SAXException, InternalErrorException {
		// Test branch B1
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "B1")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("rebase/response-B1.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));
		
		// Test branch B2
		String result2 = ep.sparql("text/turtle", createConstructQuery(graphName, "B2")).getEntity().toString();
		String expected2 = ResourceManagement.getContentFromResource("rebase/response-B2.ttl");
		assertTrue(check_isomorphism(result2, "TURTLE", expected2, "TURTLE"));
		
		// Test branch MASTER
		String result3 = ep.sparql("text/turtle", createConstructQuery(graphName, "master")).getEntity().toString();
		String expected3 = ResourceManagement.getContentFromResource("rebase/response-MASTER.ttl");
		assertTrue(check_isomorphism(result3, "TURTLE", expected3, "TURTLE"));
	}
	
	
	
	/**
	 * Test REBASE-MERGE B1 to Master.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testRebaseMerge1() throws SAXException, IOException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into Master
		ep.sparql(createCommonRebaseMergeQuery(graphName, sdd, user, "Merge B1 into Master", "B1", "master"));
		// Test branch master
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "master")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("rebase/response-B1-into-Master.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));
	}
	
	/**
	 * Test REBASE-MERGE B2 to Master.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testRebaseMerge2() throws SAXException, IOException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B2 into Master
		ep.sparql(createCommonRebaseMergeQuery(graphName, sdd, user, "Merge B2 into Master", "B2", "master"));
		// Test branch master
		String result2 = ep.sparql("text/turtle", createConstructQuery(graphName, "master")).getEntity().toString();
		String expected2 = ResourceManagement.getContentFromResource("rebase/response-B2-into-Master.ttl");
		assertTrue(check_isomorphism(result2, "TURTLE", expected2, "TURTLE"));
	}
		

	/**
	 * Test AUTO-REBASE-MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testAutoRebaseMerge() throws SAXException, IOException, InternalErrorException {
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2
		ep.sparql(createAutoRebaseMergeQuery(graphName, sdd, user, "Merge B1 into B2", "B1", "B2"));
		// Test branch B1
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphName, "B2")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("rebase/auto/response-B1-into-B2.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));
	}
	
	
	/**
	 * Test common Rebase MERGE.
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws InternalErrorException 
	 */
	@Test
	public void testCommonRebaseMerge() throws IOException, SAXException, InternalErrorException {
		
		// Create the initial data set
		String graphWithConflict = SampleDataSet.createSampleDataSetMerging().graphName;
		
		// The SDD to use
		String sdd = "http://eatld.et.tu-dresden.de/sdd#defaultSDD";
		
		// Merge B1 into B2 (WITH)
		String triples = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		Response queryResult1 = ep.sparql(createRebaseMergeWithQuery(graphWithConflict, sdd, user, "Merge B1 into B2", "B1", "B2", triples));
		Assert.assertNull(queryResult1.getEntity());

		// Test branch B2
		String result1 = ep.sparql("text/turtle", createConstructQuery(graphWithConflict, "B2")).getEntity().toString();
		String expected1 = ResourceManagement.getContentFromResource("rebase/common/response-B1-into-B2.ttl");
		assertTrue(check_isomorphism(result1, "TURTLE", expected1, "TURTLE"));
	}


	//TODO Create tests for FORCE and MANUAL

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
	

}
