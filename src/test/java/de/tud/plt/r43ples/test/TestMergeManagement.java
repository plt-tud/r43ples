/**
 * 
 */
package de.tud.plt.r43ples.test;

import static org.hamcrest.core.StringContains.containsString;

import java.util.LinkedList;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;

/**
 * @author Markus Graube
 *
 */
public class TestMergeManagement {

	final static String graph = "http://exampleGraph.com/merging";
	
	/**
	 * @throws ConfigurationException 
	 * @throws InternalErrorException 
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException {
		Config.readConfig("r43ples.conf");
		SampleDataSet.createSampleDataSetMerging(graph);
	}





	/**
	 * Test method for {@link de.tud.plt.r43ples.management.MergeManagement#getCommonRevisionWithShortestPath(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetCommonRevisionWithShortestPath() {
		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(graph+"-revision-1.0-1", graph+"-revision-1.1-1");
		Assert.assertEquals(graph+"-revision-1", commonRevision);
	}

	/**
	 * Test method for {@link de.tud.plt.r43ples.management.MergeManagement#getPathBetweenStartAndTargetRevision(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetPathBetweenStartAndTargetRevision() {
		LinkedList<String> path = MergeManagement.getPathBetweenStartAndTargetRevision(graph+"-revision-0", graph+"-revision-1.1-1");
		LinkedList<String> expected = new LinkedList<String>();
		expected.add(graph+"-revision-0");
		expected.add(graph+"-revision-1");
		expected.add(graph+"-revision-1.1-0");
		expected.add(graph+"-revision-1.1-1");
		Assert.assertEquals(expected, path);
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.MergeManagement#getPathBetweenStartAndTargetRevision(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetPathBetweenStartAndTargetRevision2() {
		LinkedList<String> path = MergeManagement.getPathBetweenStartAndTargetRevision(graph+"-revision-1", graph+"-revision-1.0-1");
		LinkedList<String> expected = new LinkedList<String>();
		expected.add(graph+"-revision-1");
		expected.add(graph+"-revision-1.0-0");
		expected.add(graph+"-revision-1.0-1");
		Assert.assertEquals(expected, path);
	}
	
	@Test
	public void testResponseHeader() {
		String sparql = "SELECT * "
				+ "FROM <"+graph+">"
				+ "WHERE { ?s ?p ?o}";
				
		String result = RevisionManagement.getResponseHeaderFromQuery(sparql);
		Assert.assertThat(result, containsString("Master"));
	}

}
