/**
 * 
 */
package de.tud.plt.r43ples.test;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceFactory;

/**
 * @author Markus Graube
 *
 */
public class TestSparqlRewriter {

	private final static String graph_test = "http://test.com/dataset1";
	
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterfaceFactory.createInterface();
		SampleDataSet.createSampleDataset1(graph_test);
	}
	
	@AfterClass
	public static void tearDown() {
		RevisionManagement.purgeGraph(graph_test);
		TripleStoreInterfaceFactory.close();
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.management.SparqlRewriter#rewriteQuery(java.lang.String)}.
	 * @throws InternalErrorException 
	 */
	@Test
	public final void testRewriteQuery() throws InternalErrorException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?p1 ?p2 "
				+ "FROM <" + graph_test + "> REVISION \"2\""
				+ "WHERE {"
				+ "	?p1 :knows ?p2."
				+ "	MINUS {?p1 :knows :Danny}"
				+ "} ORDER BY ?p1 ?p2";  
		
		String result = SparqlRewriter.rewriteQuery(query);
		String expected = ResourceManagement.getContentFromResource("rewritten-query-minus.rq");
		Assert.assertEquals(expected, result);
	}

}
