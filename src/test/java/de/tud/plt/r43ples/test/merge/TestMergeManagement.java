/**
 * 
 */
package de.tud.plt.r43ples.test.merge;

import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.DataSetGenerationResult;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.merging.MergeManagement;

/**
 * @author Markus Graube
 *
 */
public class TestMergeManagement {

	private static DataSetGenerationResult ds;
	
	/**
	 * @throws ConfigurationException 
	 * @throws InternalErrorException 
	 * @throws IOException 
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException, IOException {
		Config.readConfig("r43ples.test.conf");
		ds = SampleDataSet.createSampleDataSetMerging();
	}





	/**
	 * Test method for {@link de.tud.plt.r43ples.merging.MergeManagement#getCommonRevisionWithShortestPath(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetCommonRevisionWithShortestPath() {
		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(
				RevisionManagement.getRevisionGraph(ds.graphName),
				ds.graphName+"-revision-"+ds.revisions.get("b1-1"), 
				ds.graphName+"-revision-"+ds.revisions.get("b2-2"));
		Assert.assertEquals(ds.graphName+"-revision-"+ds.revisions.get("master-1"), commonRevision);
	}

	/**
	 * Test method for {@link de.tud.plt.r43ples.merging.MergeManagement#getPathBetweenStartAndTargetRevision(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetPathBetweenStartAndTargetRevision() {
		LinkedList<String> path = MergeManagement.getPathBetweenStartAndTargetRevision(
				RevisionManagement.getRevisionGraph(ds.graphName),
				ds.graphName+"-revision-"+ds.revisions.get("master-0"), 
				ds.graphName+"-revision-"+ds.revisions.get("b2-1"));
		LinkedList<String> expected = new LinkedList<String>();
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("master-0"));
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("master-1"));
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("b2-0"));
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("b2-1"));
		Assert.assertEquals(expected, path);
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.merging.MergeManagement#getPathBetweenStartAndTargetRevision(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetPathBetweenStartAndTargetRevision2() {
		LinkedList<String> path = MergeManagement.getPathBetweenStartAndTargetRevision(
				RevisionManagement.getRevisionGraph(ds.graphName),
				ds.graphName+"-revision-"+ds.revisions.get("master-1"),
				ds.graphName+"-revision-"+ds.revisions.get("b1-1"));
		LinkedList<String> expected = new LinkedList<String>();
		
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("master-1"));
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("b1-0"));
		expected.add(ds.graphName+"-revision-"+ds.revisions.get("b1-1"));
		Assert.assertEquals(expected, path);
	}
	
	@Test
	public void testResponseHeader() {
		String sparql = "SELECT * "
				+ "FROM <"+ds.graphName+">"
				+ "WHERE { ?s ?p ?o}";
				
		String result = RevisionManagement.getResponseHeaderFromQuery(sparql);
		Assert.assertThat(result, containsString("Master"));
	}

}
