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

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.management.TripleStoreInterface;

/**
 * @author mgraube
 *
 */
public class TestSparqlRewriter {

	private final static String graph_test = "http://test_dataset_user";
	
	@BeforeClass
	public static void setUpBeforeClass() throws HttpException, IOException, ConfigurationException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_update, Config.sparql_user, Config.sparql_password);
		SampleDataSet.createSampleDataset1(graph_test);
	}
	
	@AfterClass
	public static void tearDown() throws HttpException, IOException{
		RevisionManagement.purgeGraph(graph_test);
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
