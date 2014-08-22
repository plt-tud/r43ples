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
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.management.TripleStoreInterface;


public class TestRevisionManagment {

	@Before
	public void setUp() throws HttpException, IOException, ConfigurationException{
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		
		RevisionManagement.putGraphUnderVersionControl("test1234");
		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision("test1234", "<a> <b> <c>.", "", "test", "test commit message", list);
		
		
		RevisionManagement.putGraphUnderVersionControl("test_dataset_user");
		RevisionManagement.createNewRevision("test_dataset_user", 
				ResourceManagement.getContentFromResource("samples/test-delta-added-1.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-1.nt"),
				"test_user", "test commit message 1", list);		
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision("test_dataset_user", 
				ResourceManagement.getContentFromResource("samples/test-delta-added-2.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-2.nt"),
				"test_user", "test commit message 2", list);		
		list.remove("1");
		list.add("2");
		RevisionManagement.createNewRevision("test_dataset_user", 
				ResourceManagement.getContentFromResource("samples/test-delta-added-3.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-3.nt"),
				"test_user", "test commit message 3", list);		
		list.remove("2");
		list.add("3");
		RevisionManagement.createNewRevision("test_dataset_user", 
				ResourceManagement.getContentFromResource("samples/test-delta-added-4.nt"), 
				ResourceManagement.getContentFromResource("samples/test-delta-removed-4.nt"),
				"test_user", "test commit message 4", list);
	}
	
	@After
	public void tearDown() throws HttpException, IOException{
		RevisionManagement.purgeGraph("test1234");
		RevisionManagement.purgeGraph("test_dataset_user");
	}
	
	
	
	
	@Test
	public void test1234_master_number() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber("test1234");
		Assert.assertEquals("1", revNumberMaster);
		
	}
	
	@Test
	public void test1234_reference_uri() throws HttpException, IOException {
		String res = RevisionManagement.getReferenceUri("test1234", "master");
		Assert.assertEquals("test1234-master", res);
		
	}
	
	@Test
	public void test1234_revision_uri() throws HttpException, IOException {
		String a = RevisionManagement.getRevisionUri("test1234", "master");
		Assert.assertEquals("test1234-revision-1", a);
	}
	
	@Test
	public void testR43ples_user() throws HttpException, IOException {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber("test_dataset_user");
		Assert.assertEquals("4", revNumberMaster);
	}
	
	@Test
	public void testSparqlRewrite_simple() throws HttpException, IOException {
		String result = SparqlRewriter.rewriteQuery("SELECT ?s ?p ?o FROM <http://test.com/r43ples-dataset> REVISION \"2\" WHERE {?s ?p ?o.}");
		System.out.println(result);
		Assert.assertEquals("", result);
	}
	
	@Test
	public void testSparqlRewrite_two_statements() throws HttpException, IOException {
		String result = SparqlRewriter.rewriteQuery("SELECT * FROM <test_dataset_user> REVISION \"3\" WHERE {?a ?p ?b. ?b ?p ?c.}");
		System.out.println(result);
		Assert.assertEquals("", result);
		
		
	}
	
}
