/**
 * 
 */
package de.tud.plt.r43ples.test;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter_old;

/**
 * @author Markus Graube
 *
 */
@Ignore("DOES NOT WORK CORRECTLY RIGHT NOW!")
public class TestSparqlRewriter_Old {

	private static String graph_test;
	
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException{
		Config.readConfig("r43ples.test.conf");
		graph_test = SampleDataSet.createSampleDataset1();
	}
	
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter_old#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery1() throws InternalErrorException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?s ?o "
				+ "WHERE {"
				+ "  GRAPH <" + graph_test + "> REVISION \"2\" {?s :knows ?o }"
				+ "} ORDER BY ?s ?o";	
		
		String result = SparqlRewriter_old.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query1.rq");
		Assert.assertEquals(expected, result);
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter_old#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery1_From() throws InternalErrorException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?s ?o "
				+ "FROM <" + graph_test + "> REVISION \"2\""
				+ "WHERE {"
				+ " ?s :knows ?o. "
				+ "} ORDER BY ?s ?o";	
		
		String result = SparqlRewriter_old.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query1.rq");
		Assert.assertEquals(expected, result);
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter_old#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery2() throws InternalErrorException {
		String query = "PREFIX : <http://test.com/> "

		+ "SELECT DISTINCT ?p1 ?p2 "
		+ "WHERE {"
		+ "  GRAPH <" + graph_test + "> REVISION \"2\" {"
		+ "	   ?p1 :knows ?p2."
		+ "	   MINUS {?p1 :knows :Danny}"
		+ "  }"
		+ "} ORDER BY ?p1 ?p2";  
		
		String result = SparqlRewriter_old.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query2.rq");
		Assert.assertEquals(expected, result);
	}

}
