/**
 * 
 */
package de.tud.plt.r43ples.test;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.DataSetGenerationResult;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter;

/**
 * @author Markus Graube
 *
 */
public class TestSparqlRewriter {

	private static DataSetGenerationResult ds;
	
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException{
		Config.readConfig("r43ples.test.conf");
		ds = SampleDataSet.createSampleDataset1();
	}
	
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery1() throws InternalErrorException {
		String query = String.format("PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?s ?o "
				+ "WHERE {"
				+ "  GRAPH <%s> REVISION \"%s\" {?s :knows ?o }"
				+ "} ORDER BY ?s ?o",
				ds.graphName, ds.revisions.get("master-2"));
		
		String result = SparqlRewriter.rewriteQuery(query);
		String expected = String.format(
				ResourceManagement.getContentFromResource("rewritten-query1.rq"),
				ds.revisions.get("master-5"), ds.revisions.get("master-4"), ds.revisions.get("master-3"));
		Assert.assertEquals(expected, result);
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery2() throws InternalErrorException {
		String query = String.format("PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?p1 ?p2 "
				+ "WHERE {"
				+ "  GRAPH <%s> REVISION \"%s\" {"
				+ "	   ?p1 :knows ?p2."
				+ "	   MINUS {?p1 :knows :Danny}"
				+ "  }"
				+ "} ORDER BY ?p1 ?p2",
				ds.graphName, ds.revisions.get("master-2"));  
				
		String result = SparqlRewriter.rewriteQuery(query);
		String expected = String.format(
				ResourceManagement.getContentFromResource("rewritten-query2.rq"),
				ds.revisions.get("master-5"), ds.revisions.get("master-4"), ds.revisions.get("master-3"));
		Assert.assertEquals(expected, result);
	}

}
