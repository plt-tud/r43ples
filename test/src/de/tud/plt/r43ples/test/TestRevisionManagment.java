package de.tud.plt.r43ples.test;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.webservice.Endpoint;


public class TestRevisionManagment {
	
	final static String graph1 = "test_dataset_user";

	@Before
	public void setUp() throws HttpException, IOException, ConfigurationException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.putGraphUnderVersionControl(graph1);
		RevisionManagement.createNewRevision(graph1, 
				ResourceManagement.getContentFromResource("samples/test-delta-added-1.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-1.nt"),
				"test_user", "test commit message 1", list);		
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision(graph1, 
				ResourceManagement.getContentFromResource("samples/test-delta-added-2.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-2.nt"),
				"test_user", "test commit message 2", list);		
		list.remove("1");
		list.add("2");
		RevisionManagement.createNewRevision(graph1, 
				ResourceManagement.getContentFromResource("samples/test-delta-added-3.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-3.nt"),
				"test_user", "test commit message 3", list);		
		list.remove("2");
		list.add("3");
		RevisionManagement.createNewRevision(graph1, 
				ResourceManagement.getContentFromResource("samples/test-delta-added-4.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-4.nt"),
				"test_user", "test commit message 4", list);
	}
	
	@After
	public void tearDown() throws HttpException, IOException{
		RevisionManagement.purgeGraph(graph1);
	}
	
	@Test
	public void test_reference_uri() throws HttpException, IOException {
		String res = RevisionManagement.getReferenceUri(graph1, "master");
		Assert.assertEquals("test_dataset_user-master", res);
	}
	
	@Test
	public void test_revision_uri() throws HttpException, IOException {
		String a = RevisionManagement.getRevisionUri(graph1, "master");
		Assert.assertEquals("test_dataset_user-revision-4", a);
	}
	
	@Test
	public void test_master_number() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber(graph1);
		Assert.assertEquals("4", revNumberMaster);
	}
	
	@Test
	public void testSparqlRewrite_two_statements() throws HttpException, IOException {
		String result = SparqlRewriter.rewriteQuery("SELECT * FROM <" + graph1 + "> REVISION \"3\" WHERE {?a ?p ?b. ?b ?p ?c.}");
		Assert.assertNotEquals("", result);		
	}
	
	@Test
	public void testSelectWithRewriting() throws IOException{
		Endpoint ep = new Endpoint();
        String result;
        String expected;
        String query_template = "#OPTION r43ples:SPARQL_JOIN%n"
        		+ "SELECT ?s ?p ?o FROM <"+graph1+"> REVISION \"%d\"%n"
        		+ "WHERE {?s ?p ?o} ORDER By ?s ?p ?o";
        String format = "application/sparql-results+xml";
        
        result = ep.sparql(format, null, String.format(query_template, 0)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev0.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, null, String.format(query_template, 1)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev1.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, null, String.format(query_template, 2)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev2.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, null, String.format(query_template, 3)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev3.xml");
        Assert.assertEquals(expected, result);
        
        result = ep.sparql(format, null, String.format(query_template, 4)).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("response-test-rev4.xml");
        Assert.assertEquals(expected, result);
	}

	@Test
	public void testSelect() throws HttpException, IOException {
		String graphName = "test_dataset_user";
		String result;
		String expected;
		String query = "SELECT ?s ?p ?o FROM <tempGraphName> WHERE {?s ?p ?o} ORDER By ?s ?p ?o"; 
		
		RevisionManagement.generateFullGraphOfRevision(graphName, "0", "tempGraphName");
		result = TripleStoreInterface.executeQueryWithAuthorization(query);
		expected = ResourceManagement.getContentFromResource("response-test-rev0.xml");
		Assert.assertEquals(expected, result);
		
		RevisionManagement.generateFullGraphOfRevision(graphName, "1", "tempGraphName");
		result = TripleStoreInterface.executeQueryWithAuthorization(query);
		expected = ResourceManagement.getContentFromResource("response-test-rev1.xml");
		Assert.assertEquals(expected, result);
		
		RevisionManagement.generateFullGraphOfRevision(graphName, "2", "tempGraphName");
		result = TripleStoreInterface.executeQueryWithAuthorization(query);
		expected = ResourceManagement.getContentFromResource("response-test-rev2.xml");
		Assert.assertEquals(expected, result);
		
		RevisionManagement.generateFullGraphOfRevision(graphName, "3", "tempGraphName");
		result = TripleStoreInterface.executeQueryWithAuthorization(query);
		expected = ResourceManagement.getContentFromResource("response-test-rev3.xml");
		Assert.assertEquals(expected, result);
		
		RevisionManagement.generateFullGraphOfRevision(graphName, "4", "tempGraphName");
		result = TripleStoreInterface.executeQueryWithAuthorization(query);
		expected = ResourceManagement.getContentFromResource("response-test-rev4.xml");
		Assert.assertEquals(expected, result);
	}
	
	
	@Test
	public void testBranching() throws HttpException, IOException, IdentifierAlreadyExistsException {
		RevisionManagement.createReference("branch", graph1, "2", "testBranch", "test_user", "branching as junit test");
		ArrayList<String> usedRevisionNumber = new ArrayList<String>();
		usedRevisionNumber.add("testBranch");
		RevisionManagement.createNewRevision(graph1, "<a> <b> <c>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber = RevisionManagement.getRevisionNumber(graph1, "testBranch");
		Assert.assertEquals("2.0-0", revNumber);
		
		RevisionManagement.createNewRevision(graph1, "<a> <b> <d>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber2 = RevisionManagement.getRevisionNumber(graph1, "testBranch");
		Assert.assertEquals("2.0-1", revNumber2);
		
		RevisionManagement.createReference("branch", graph1, "2.0-1", "testBranch2", "test_user", "branching as junit test");
		usedRevisionNumber.clear();
		usedRevisionNumber.add("testBranch2");
		RevisionManagement.createNewRevision(graph1, "<a> <b> <e>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber3 = RevisionManagement.getRevisionNumber(graph1, "testBranch2");
		Assert.assertEquals("2.0-1.0-0", revNumber3);
		
		RevisionManagement.createReference("branch", graph1, "2.0-1", "testBranch2a", "test_user", "branching as junit test");
		usedRevisionNumber.clear();
		usedRevisionNumber.add("testBranch2a");
		RevisionManagement.createNewRevision(graph1, "<a> <b> <f>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber4 = RevisionManagement.getRevisionNumber(graph1, "testBranch2a");
		Assert.assertEquals("2.0-1.1-0", revNumber4);
	}
	
}
