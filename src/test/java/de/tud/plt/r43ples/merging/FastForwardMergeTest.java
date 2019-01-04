package de.tud.plt.r43ples.merging;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


/**
 * Tests fast forwarding of the merging management.
 *
 * @author Xinyu Yang
 * @author Stephan Hensel
 */
public class FastForwardMergeTest extends R43plesTest {

	/** The graph name. **/
	private static String graphName;
	/** The user. **/
	private static String user = "jUnitUser";

	
	/**
	 * Initialize TestClass
	 *
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
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
		graphName = SampleDataSet.createSampleDataSetMerging().graphName;
	}
	
	
	/**
	 * Test FastForward Merge.
	 * 
	 * @throws InternalErrorException
	 */
	@Test
	public void testFastForwardMerge() throws InternalErrorException {
		// Test branch B1
		String result_b1 = ep.sparql("text/turtle", createConstructQuery(graphName, "b1")).getEntity().toString();
		String expected_b1 = ResourceManagement.getContentFromResource("threeway/response-B1.ttl");
		assertTrue(check_isomorphism(result_b1, "TURTLE", expected_b1, "TURTLE"));

		// Test branch MASTER
		String result_master = ep.sparql("text/turtle", createConstructQuery(graphName, "master")).getEntity().toString();
		String expected_master = ResourceManagement.getContentFromResource("threeway/response-MASTER.ttl");
		assertTrue(check_isomorphism(result_master, "TURTLE", expected_master, "TURTLE"));

		// Test fast forward
		ep.sparql("text/turtle", createFastForwardMergeQuery(graphName, user, "Merge b1 into master", "b1", "master"));
		result_master = ep.sparql("text/turtle", createConstructQuery(graphName, "master")).getEntity().toString();
		assertTrue(check_isomorphism(result_master, "TURTLE", expected_b1, "TURTLE"));
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
	 * Create Fast Forward-MERGE query.
	 * 
	 * @param graphName the graph name
	 * @param user the user
	 * @param commitMessage the commit message
	 * @param branchNameA the branch name A
	 * @param branchNameB the branch name B
	 * @return the query
	 */
	private String createFastForwardMergeQuery(String graphName, String user, String commitMessage, String branchNameA, String branchNameB) {
		return String.format( "USER \"%s\" %n"
							+ "MESSAGE \"%s\" %n"
							+ "MERGE GRAPH <%s> BRANCH \"%s\" INTO BRANCH \"%s\"", user, commitMessage, graphName, branchNameA, branchNameB);
	}


}
