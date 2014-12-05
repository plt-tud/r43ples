/**
 * 
 */
package de.tud.plt.r43ples.test;

import java.io.IOException;

import org.apache.http.HttpException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.webservice.Endpoint;

/**
 * @author mgraube
 * 
 */
public class TestMultipleGraph {

	final static String graph1 = "http://test.com/graph1";
	final static String graph2 = "http://test.com/graph2";

	Endpoint ep;
	String result;
	String expected;
	final static String format = "application/sparql-results+xml";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		SampleDataSet.createSampleDataset1(graph1);
		SampleDataSet.createSampleDataset2(graph2);
	}

	@AfterClass
	public static void tearDownafterClass() throws HttpException, IOException {
		RevisionManagement.purgeGraph(graph1);
		RevisionManagement.purgeGraph(graph2);
	}

	@Before
	public void setUp() {
		ep = new Endpoint();
	}

	@After
	public void tearDown() {
	}

	/**
	 * @throws IOException 
	 * @throws HttpException 
	 */
	@Test
	public final void testMultipleGraphsSparqlJoin() throws IOException, HttpException {
		String query_template = ""
				+ "OPTION r43ples:SPARQL_JOIN %n"
				+ "PREFIX : <http://test.com/> %n"
				+ "SELECT ?address %n"
				+ "FROM <" + graph1	+ "> REVISION \"%d\"%n" 
				+ "FROM <" + graph2	+ "> REVISION \"%d\"%n"
				+ "WHERE {"
				+ "	:Adam :knows [ :address ?address ]"
				+ "} %n"
				+ "ORDER By ?address";

		result = ep.sparql(format, String.format(query_template, 1, 1)).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-1-1.xml");
		Assert.assertEquals(expected, result);
		
		result = ep.sparql(format, String.format(query_template, 2, 1)).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-2-1.xml");
		Assert.assertEquals(expected, result);
		
		result = ep.sparql(format, String.format(query_template, 2, 2)).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-2-2.xml");
		Assert.assertEquals(expected, result);
	}

}
