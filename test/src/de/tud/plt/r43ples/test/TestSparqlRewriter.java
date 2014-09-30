/**
 * 
 */
package de.tud.plt.r43ples.test;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.SparqlRewriter;

/**
 * @author mgraube
 *
 */
public class TestSparqlRewriter {

	
	@BeforeClass
	public static void setUpBeforeClass() throws HttpException, IOException, ConfigurationException{
		TestRevisionManagment.setUpBeforeClass();
	}
	
	@AfterClass
	public static void tearDown() throws HttpException, IOException{
		TestRevisionManagment.tearDown();
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter#rewriteQuery(java.lang.String)}.
	 * @throws IOException 
	 * @throws HttpException 
	 */
	@Test
	public final void testRewriteQuery() throws HttpException, IOException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?p1 ?p2 "
				+ "FROM <http://test_dataset_user> REVISION \"2\""
				+ "WHERE {"
				+ "	?p1 :knows ?p2."
				+ "	MINUS {?p1 :knows :Danny}"
				+ "} ORDER BY ?p1 ?p2";  
		
		String result = SparqlRewriter.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query-minus.rq");
		Assert.assertEquals(expected, result);
	}

}
