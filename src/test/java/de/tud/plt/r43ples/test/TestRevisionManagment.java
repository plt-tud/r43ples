package de.tud.plt.r43ples.test;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.webservice.Endpoint;


public class TestRevisionManagment {
	
	private final static String graph_test = "http://test_dataset_user";
    private final static String format = "application/sparql-results+xml";
    Endpoint ep;
	String result;
	String expected;

	@BeforeClass
	public static void setUpBeforeClass() throws HttpException, IOException, ConfigurationException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		SampleDataSet.createSampleDataset1(graph_test);
	}
	
	@AfterClass
	public static void tearDown() throws HttpException, IOException{
	//	RevisionManagement.purgeGraph(graph_test);
	}
	
	@Before
	public void setUp() {
		ep = new Endpoint();
	}
	
	@Test
	public void test_reference_uri() throws HttpException, IOException {
		String res = RevisionManagement.getReferenceUri(graph_test, "master");
		Assert.assertEquals("http://test_dataset_user-master", res);
	}
	
	@Test
	public void test_revision_uri() throws HttpException, IOException {
		String uri = RevisionManagement.getRevisionUri(graph_test, "4");
		Assert.assertEquals("http://test_dataset_user-revision-4", uri);
	}
	
	@Test
	public void test_master_number() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber(graph_test);
		Assert.assertEquals("5", revNumberMaster);
	}
	
	@Test
	public void testSelectMaster() throws IOException, HttpException{
        String query = "SELECT ?s ?p ?o "
        		+ "FROM <"+graph_test+"> REVISION \"master\""
        		+ "WHERE {?s ?p ?o} ORDER BY ?s ?p ?o";
        
        result = ep.sparql(format, query).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev5.xml");
        Assert.assertEquals(expected, result);
        
        query = "SELECT ?s ?p ?o "
        		+ "FROM <"+graph_test+"> REVISION \"MASTER\""
        		+ "WHERE {?s ?p ?o} ORDER BY ?s ?p ?o";
        
        result = ep.sparql(format, query).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev5.xml");
        Assert.assertEquals(expected, result);   
	}
	
	@Test
	public void testSelectWithRewriting() throws IOException, HttpException{
        String query_template = "OPTION r43ples:SPARQL_JOIN%n"
        		+ "SELECT ?s ?p ?o FROM <"+graph_test+"> REVISION \"%d\"%n"
        		+ "WHERE {?s ?p ?o} ORDER BY ?s ?p ?o";
        
        result = ep.sparql(format, String.format(query_template, 0)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev0.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 1)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev1.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 2)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev2.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 3)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev3.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 4)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev4.xml");
        Assert.assertEquals(expected, result);
	}

	@Test
	public void testSelect() throws HttpException, IOException {
        String query_template = ""
        		+ "SELECT ?s ?p ?o FROM <"+graph_test+"> REVISION \"%d\"%n"
        		+ "WHERE {?s ?p ?o} ORDER By ?s ?p ?o";
        
        result = ep.sparql(format, String.format(query_template, 0)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev0.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 1)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev1.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 2)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev2.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 3)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev3.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 4)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev4.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, String.format(query_template, 5)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev5.xml");
        Assert.assertEquals(expected, result);
	}
	
	@Test
	public void testSelect2Pattern() throws HttpException, IOException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?p1 ?p2 "
				+ "FROM <"+ graph_test + "> REVISION \"%d\" "
				+ "WHERE {"
				+ "	?p1 :knows ?t."
				+ "	?p2 :knows ?t."
				+ " FILTER (?p1!=?p2)"
				+ "} ORDER BY ?p1 ?p2"; 
		String option = "OPTION r43ples:SPARQL_JOIN\n";
		
		expected = ResourceManagement.getContentFromResource("response-test2Pattern-rev3.xml");
		result = ep.sparql(format, String.format(query,3)).getEntity().toString();
		Assert.assertEquals(expected, result);
		result = ep.sparql(format, option + String.format(query,3)).getEntity().toString();
		Assert.assertEquals(expected, result);
		
		expected = ResourceManagement.getContentFromResource("response-test2Pattern-rev4.xml");
		result = ep.sparql(format, String.format(query,4)).getEntity().toString();
		Assert.assertEquals(expected, result);
		result = ep.sparql(format, option + String.format(query,4)).getEntity().toString();
		Assert.assertEquals(expected, result);
	}
	
	
	@Test
	public void testBranching() throws HttpException, IOException, IdentifierAlreadyExistsException {
		RevisionManagement.createReference("branch", graph_test, "2", "testBranch", "test_user", "branching as junit test");
		ArrayList<String> usedRevisionNumber = new ArrayList<String>();
		usedRevisionNumber.add("testBranch");
		RevisionManagement.createNewRevision(graph_test, "<a> <b> <c>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber = RevisionManagement.getRevisionNumber(graph_test, "testBranch");
		Assert.assertEquals("2.0-0", revNumber);
		
		RevisionManagement.createNewRevision(graph_test, "<a> <b> <d>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber2 = RevisionManagement.getRevisionNumber(graph_test, "testBranch");
		Assert.assertEquals("2.0-1", revNumber2);
		
		RevisionManagement.createReference("branch", graph_test, "2.0-1", "testBranch2", "test_user", "branching as junit test");
		usedRevisionNumber.clear();
		usedRevisionNumber.add("testBranch2");
		RevisionManagement.createNewRevision(graph_test, "<a> <b> <e>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber3 = RevisionManagement.getRevisionNumber(graph_test, "testBranch2");
		Assert.assertEquals("2.0-1.0-0", revNumber3);
		
		RevisionManagement.createReference("branch", graph_test, "2.0-1", "testBranch2a", "test_user", "branching as junit test");
		usedRevisionNumber.clear();
		usedRevisionNumber.add("testBranch2a");
		RevisionManagement.createNewRevision(graph_test, "<a> <b> <f>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber4 = RevisionManagement.getRevisionNumber(graph_test, "testBranch2a");
		Assert.assertEquals("2.0-1.1-0", revNumber4);
	}
	
	
	@Test
	public void test_minus() throws IOException, HttpException {
		String query = "PREFIX : <http://test.com/> "
				+ "SELECT DISTINCT ?p1 ?p2 "
				+ "FROM <"+ graph_test + "> REVISION \"%d\" "
				+ "WHERE {"
				+ "	?p1 :knows ?p2."
				+ "	MINUS {?p1 :knows :Danny}"
				+ "} ORDER BY ?p1 ?p2"; 
		String option = "OPTION r43ples:SPARQL_JOIN\n";
		
		expected = ResourceManagement.getContentFromResource("response-testMinus-rev2.xml");
		result = ep.sparql(format, String.format(query,2)).getEntity().toString();
		Assert.assertEquals(expected, result);
		result = ep.sparql(format, option + String.format(query,2)).getEntity().toString();
		Assert.assertEquals(expected, result);
		
		expected = ResourceManagement.getContentFromResource("response-testMinus-rev3.xml");
		result = ep.sparql(format, String.format(query,3)).getEntity().toString();
		Assert.assertEquals(expected, result);
		result = ep.sparql(format, option + String.format(query,3)).getEntity().toString();
		Assert.assertEquals(expected, result);
		
		expected = ResourceManagement.getContentFromResource("response-testMinus-rev4.xml");
		result = ep.sparql(format, String.format(query,4)).getEntity().toString();
		Assert.assertEquals(expected, result);
		result = ep.sparql(format, option + String.format(query,4)).getEntity().toString();
		Assert.assertEquals(expected, result);
	}
	

	
}
