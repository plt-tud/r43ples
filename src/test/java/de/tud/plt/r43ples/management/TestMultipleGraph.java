/**
 * 
 */
package de.tud.plt.r43ples.management;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.core.HeaderInformation;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertTrue;

/**
 * @author Markus Graube
 * @author Stephan Hensel
 * 
 */
public class TestMultipleGraph extends R43plesTest {

	private static DataSetGenerationResult ds1;
	private static DataSetGenerationResult ds2;

	private String result;
	private String expected;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.readConfig("r43ples.test.conf");
		ds1 = SampleDataSet.createSampleDataset1();
		ds2 = SampleDataSet.createSampleDataset2();
	}

	private static String get_query_template(String rev1, String rev2){
		return String.format(""
			+ "PREFIX : <http://test.com/> %n"
			+ "SELECT ?address %n"
			+ "WHERE {"
			+ "  GRAPH <%s> REVISION \"%s\" { :Adam :knows ?a. }%n"
			+ "  GRAPH <%s> REVISION \"%s\" {?a :address ?address. }%n"
			+ "} %n"
			+ "ORDER BY ?address",
			ds1.graphName, ds1.revisions.get(rev1), ds2.graphName, ds2.revisions.get(rev2));
	}

	/**
     * @throws IOException
     * @throws InternalErrorException
	 */
	@Test
    public final void testMultipleGraphs() throws IOException, InternalErrorException {
        result = ep.sparql("text/turtle", get_query_template("master-1", "master-1")).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-1-1.ttl");
        assertTrue(check_isomorphism(result, expected));

        result = ep.sparql("text/turtle", get_query_template("master-2", "master-1")).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-2-1.ttl");
        assertTrue(check_isomorphism(result, expected));

        result = ep.sparql("text/turtle", get_query_template("master-2", "master-2")).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-2-2.ttl");
        assertTrue(check_isomorphism(result, expected));
    }
	
	/**
     * @throws IOException
     * @throws InternalErrorException
	 */
	@Test
    public final void testMultipleGraphsQueryRewriting() throws IOException, InternalErrorException {
        result = ep.sparql("text/turtle", get_query_template("master-1", "master-1"), true).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-1-1.ttl");
        assertTrue(check_isomorphism(result, expected));

        result = ep.sparql("text/turtle", get_query_template("master-2", "master-1"), true).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-2-1.ttl");
        assertTrue(check_isomorphism(result, expected));

        result = ep.sparql("text/turtle", get_query_template("master-2", "master-2"), true).getEntity().toString();
		expected = ResourceManagement.getContentFromResource("response-TwoGraphs-2-2.ttl");
        assertTrue(check_isomorphism(result, expected));
    }
	
	@Test
	public void testResponseHeader() {
		String sparql = "SELECT *"
				+ "FROM <" + ds1.graphName +">"
				+ "WHERE { ?s ?p ?o}";

		String result = new HeaderInformation().getResponseHeaderFromQuery(sparql);
		Assert.assertThat(result, containsString("Master"));
	}
	
	@Test
	public void testResponseHeader2() {
		String sparql = "SELECT *"
				+ "FROM <" + ds1.graphName +">"
				+ "FROM <" + ds2.graphName +">"
				+ "WHERE { ?s ?p ?o}";
				
		String result = new HeaderInformation().getResponseHeaderFromQuery(sparql);
		Assert.assertThat(result, containsString("Master"));
	}

}
