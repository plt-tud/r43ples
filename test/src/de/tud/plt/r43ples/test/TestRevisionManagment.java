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


public class TestRevisionManagment {
	
	final String graph1 = "test_dataset_user";
	final String graph2 = "test1234";

	@Before
	public void setUp() throws HttpException, IOException, ConfigurationException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		
		RevisionManagement.putGraphUnderVersionControl(graph2);
		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision(graph2, "<a> <b> <c>.", "", "test", "test commit message", list);
		
		
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
		RevisionManagement.purgeGraph(graph2);
		RevisionManagement.purgeGraph(graph1);
	}
	
	
	
	
	@Test
	public void test1234_master_number() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber(graph2);
		Assert.assertEquals("1", revNumberMaster);
		
	}
	
	@Test
	public void test1234_reference_uri() throws HttpException, IOException {
		String res = RevisionManagement.getReferenceUri(graph2, "master");
		Assert.assertEquals("test1234-master", res);
		
	}
	
	@Test
	public void test1234_revision_uri() throws HttpException, IOException {
		String a = RevisionManagement.getRevisionUri(graph2, "master");
		Assert.assertEquals("test1234-revision-1", a);
	}
	
	@Test
	public void testR43ples_user() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber(graph1);
		Assert.assertEquals("4", revNumberMaster);
	}
	
	@Test
	public void testSparqlRewrite_simple() throws HttpException, IOException {
		String result = SparqlRewriter.rewriteQuery("SELECT ?s ?p ?o FROM <" + graph1 + "> REVISION \"2\" WHERE {?s ?p ?o.}");
		Assert.assertNotEquals("", result);
	}
	
	@Test
	public void testSparqlRewrite_two_statements() throws HttpException, IOException {
		String result = SparqlRewriter.rewriteQuery("SELECT * FROM <" + graph1 + "> REVISION \"3\" WHERE {?a ?p ?b. ?b ?p ?c.}");
		Assert.assertNotEquals("", result);		
	}

	@Test
	public void testBranching() throws HttpException, IOException, IdentifierAlreadyExistsException {
		String graphName = "test_dataset_user";
		
		RevisionManagement.createReference("branch", graphName, "2", "testBranch", "test_user", "branching as junit test");
		ArrayList<String> usedRevisionNumber = new ArrayList<String>();
		usedRevisionNumber.add("testBranch");
		RevisionManagement.createNewRevision(graphName, "<a> <b> <c>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber = RevisionManagement.getRevisionNumber(graphName, "testBranch");
		Assert.assertEquals("2.0-0", revNumber);
		
		RevisionManagement.createNewRevision(graphName, "<a> <b> <d>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber2 = RevisionManagement.getRevisionNumber(graphName, "testBranch");
		Assert.assertEquals("2.0-1", revNumber2);
		
		RevisionManagement.createReference("branch", graphName, "2.0-1", "testBranch2", "test_user", "branching as junit test");
		usedRevisionNumber.clear();
		usedRevisionNumber.add("testBranch2");
		RevisionManagement.createNewRevision(graphName, "<a> <b> <e>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber3 = RevisionManagement.getRevisionNumber(graphName, "testBranch2");
		Assert.assertEquals("2.0-1.0-0", revNumber3);
		
		RevisionManagement.createReference("branch", graphName, "2.0-1", "testBranch2a", "test_user", "branching as junit test");
		usedRevisionNumber.clear();
		usedRevisionNumber.add("testBranch2a");
		RevisionManagement.createNewRevision(graphName, "<a> <b> <f>", "", "test_user", "test_commitMessage", usedRevisionNumber);
		String revNumber4 = RevisionManagement.getRevisionNumber(graphName, "testBranch2a");
		Assert.assertEquals("2.0-1.1-0", revNumber4);
	}
	
}
