/**
 * 
 */
package de.tud.plt.r43ples.test;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.http.HttpException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;

/**
 * @author mgraube
 *
 */
public class TestMergeManagement {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		SampleDataSet.createSampleDataSetMerging("exampleGraph");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		RevisionManagement.purgeGraph("exampleGraph");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link de.tud.plt.r43ples.management.MergeManagement#getCommonRevisionWithShortestPath(java.lang.String, java.lang.String)}.
	 * @throws HttpException 
	 * @throws IOException 
	 */
	@Test
	public final void testGetCommonRevisionWithShortestPath() throws IOException, HttpException {
		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath("exampleGraph-revision-1.0-1", "exampleGraph-revision-1.1-1");
		Assert.assertEquals("exampleGraph-revision-1", commonRevision);
	}

	/**
	 * Test method for {@link de.tud.plt.r43ples.management.MergeManagement#getPathBetweenStartAndTargetRevision(java.lang.String, java.lang.String)}.
	 * @throws HttpException 
	 * @throws IOException 
	 */
	@Test
	public final void testGetPathBetweenStartAndTargetRevision() throws IOException, HttpException {
		LinkedList<String> path = MergeManagement.getPathBetweenStartAndTargetRevision("exampleGraph-revision-0", "exampleGraph-revision-1.1-1");
		LinkedList<String> expected = new LinkedList<String>();
		expected.add("exampleGraph-revision-0");
		expected.add("exampleGraph-revision-1");
		expected.add("exampleGraph-revision-1.1-0");
		expected.add("exampleGraph-revision-1.1-1");
		Assert.assertEquals(expected, path);
	}
	
	@Test
	public void testResponseHeader() throws IOException, HttpException {
		String sparql = "SELECT *"
				+ "FROM <exampleGraph>"
				+ "WHERE { ?s ?p ?o}";
				
		String result = RevisionManagement.getResponseHeader(sparql);
		Assert.assertEquals("", result);
	}

}
