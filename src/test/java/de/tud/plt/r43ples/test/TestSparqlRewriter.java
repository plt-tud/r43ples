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
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter;

/**
 * @author Markus Graube
 *
 */
public class TestSparqlRewriter {

	private static String graph_test = "http://test.com/dataset1";
	
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException{
		Config.readConfig("r43ples.test.conf");
		graph_test = SampleDataSet.createSampleDataset1();
	}
	
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery1() throws InternalErrorException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?s ?o "
				+ "FROM <" + graph_test + "> REVISION \"2\""
				+ "WHERE {"
				+ "	?s :knows ?o"
				+ "} ORDER BY ?s ?o";	
		
		String result = SparqlRewriter.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query1.rq");
		Assert.assertEquals(expected, result);
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery2() throws InternalErrorException {
		String query = "PREFIX : <http://test.com/> "

		+ "SELECT DISTINCT ?p1 ?p2 "
		+ "FROM <" + graph_test + "> REVISION \"2\""
		+ "WHERE {"
		+ "	?p1 :knows ?p2."
		+ "	MINUS {?p1 :knows :Danny}"
		+ "} ORDER BY ?p1 ?p2";  
		
		String result = SparqlRewriter.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query2.rq");
		Assert.assertEquals(expected, result);
	}

}
