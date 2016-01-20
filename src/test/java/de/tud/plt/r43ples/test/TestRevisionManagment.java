package de.tud.plt.r43ples.test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.DataSetGenerationResult;
import de.tud.plt.r43ples.management.ResourceManagement;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.webservice.Endpoint;


public class TestRevisionManagment {
	
	
    private static DataSetGenerationResult ds;
	private final String format = "application/sparql-results+xml";
    private final Endpoint ep = new Endpoint();
	private String result;
	private String expected;

	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException{
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalize(true);
		Config.readConfig("r43ples.test.conf");
		ds = SampleDataSet.createSampleDataset1();
	}
	
	private final String query_template = ""
    		+ "SELECT ?s ?p ?o %n"
    		+ "WHERE { %n"
    		+ "  GRAPH <"+ds.graphName+"> REVISION \"%s\" {?s ?p ?o} %n"
			+ "} ORDER BY ?s ?p ?o";
	
	
	private final String query_template_2_triples_filter = "PREFIX : <http://test.com/> "
			+ "SELECT DISTINCT ?p1 ?p2 "
			+ "WHERE {"
			+ "  GRAPH <"+ ds.graphName + "> REVISION \"%s\" {"
			+ "  	?p1 :knows ?t."
			+ "	    ?p2 :knows ?t."
			+ "     FILTER (?p1!=?p2)"
			+ "  }"
			+ "} ORDER BY ?p1 ?p2"; 
	
	private String query_template_minus = ""
			+ "PREFIX : <http://test.com/> "
			+ "SELECT DISTINCT ?p1 ?p2 "
			+ "WHERE {"
			+ "  GRAPH <"+ ds.graphName + "> REVISION \"%s\"{ "
			+ "	  ?p1 :knows ?p2."
			+ "	  MINUS {?p1 :knows :Danny}"
			+ "  }"
			+ "} ORDER BY ?p1 ?p2"; 
	
	@Test
	public void test_reference_uri() throws InternalErrorException {
		String revisionGraph = RevisionManagement.getRevisionGraph(ds.graphName);
		String res = RevisionManagement.getBranchUri(revisionGraph, "master");
		Assert.assertEquals(ds.graphName+"-master", res);
	}
	
	@Test
	public void test_revision_uri() throws InternalErrorException {
		String uri = RevisionManagement.getRevisionUri(RevisionManagement.getRevisionGraph(ds.graphName), ds.revisions.get("master-4"));
		Assert.assertEquals(ds.graphName+"-revision-"+ds.revisions.get("master-4"), uri);
	}
	
	@Test
	public void test_master_number() {
		String revNumberMaster = RevisionManagement.getMasterRevisionNumber(ds.graphName);
		Assert.assertEquals(ds.revisions.get("master-5"), revNumberMaster);
	}
	
	@Test
	public void testSelectMaster() throws SAXException, IOException, InternalErrorException {
        String query = "SELECT ?s ?p ?o "
        		+ "FROM <"+ds.graphName+"> REVISION \"master\""
        		+ "WHERE {?s ?p ?o} ORDER BY ?s ?p ?o";
        
        result = ep.sparql(format, query).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev5.xml");
        assertXMLEqual(expected, result);
        
        query = "SELECT ?s ?p ?o "
        		+ "WHERE {"
        		+ "	GRAPH <"+ds.graphName+"> REVISION \"MASTER\" {?s ?p ?o}"
				+ "} ORDER BY ?s ?p ?o";
        
        result = ep.sparql(format, query).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev5.xml");
        assertXMLEqual(expected, result);   
	}
	
	@Test
	public void testSelectWithRewriting_0() throws SAXException, IOException, InternalErrorException {
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-0")), true).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev0.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelectWithRewriting_1() throws SAXException, IOException, InternalErrorException {
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-1")), true).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev1.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelectWithRewriting_2() throws SAXException, IOException, InternalErrorException {    
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-2")), true).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev2.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelectWithRewriting_3() throws SAXException, IOException, InternalErrorException {
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-3")), true).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev3.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelectWithRewriting_4() throws SAXException, IOException, InternalErrorException {
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-4")), true).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev4.xml");
        assertXMLEqual(expected, result);
   }
	
	@Test
	public void testSelectWithRewriting_5() throws SAXException, IOException, InternalErrorException {
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-5")), true).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev5.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect_0() throws SAXException, IOException, InternalErrorException {
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-0"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev0.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect_1() throws SAXException, IOException, InternalErrorException {    
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-1"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev1.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect_2() throws SAXException, IOException, InternalErrorException {    
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-2"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev2.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect_3() throws SAXException, IOException, InternalErrorException {            
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-3"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev3.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect_4() throws SAXException, IOException, InternalErrorException {            
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-4"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev4.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect_5() throws SAXException, IOException, InternalErrorException {            
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-5"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev5.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect2Pattern() throws SAXException, IOException, InternalErrorException {
		expected = ResourceManagement.getContentFromResource("2patterns/response-rev3.xml");
		result = ep.sparql(format, String.format(query_template_2_triples_filter,ds.revisions.get("master-3"))).getEntity().toString();
		assertXMLEqual(expected, result);
		
		expected = ResourceManagement.getContentFromResource("2patterns/response-rev4.xml");
		result = ep.sparql(format, String.format(query_template_2_triples_filter,ds.revisions.get("master-4"))).getEntity().toString();
		assertXMLEqual(expected, result);
	}
	
	@Test
	public void testSelect2Pattern_query_rewriting() throws SAXException, IOException, InternalErrorException {
		expected = ResourceManagement.getContentFromResource("2patterns/response-rev3.xml");
		result = ep.sparql(format, String.format(query_template_2_triples_filter,ds.revisions.get("master-3")), true).getEntity().toString();
		assertXMLEqual(expected, result);
		
		expected = ResourceManagement.getContentFromResource("2patterns/response-rev4.xml");
		result = ep.sparql(format, String.format(query_template_2_triples_filter,ds.revisions.get("master-4")), true).getEntity().toString();
		assertXMLEqual(expected, result);
	}
	
	
	
	
	@Test
	public void testTagging() throws InternalErrorException, SAXException, IOException {
		String expected, result;
		RevisionManagement.createTag(ds.graphName, ds.revisions.get("master-3") , "v0.1", "test_user", "Version v0.1 published");
		
		result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-1"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev1.xml");
        assertXMLEqual(expected, result);
        
		result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-2"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev2.xml");
        assertXMLEqual(expected, result);
		
        result = ep.sparql(format, String.format(query_template, ds.revisions.get("master-3"))).getEntity().toString();
        expected = ResourceManagement.getContentFromResource("dataset1/response-test-rev3.xml");
        assertXMLEqual(expected, result);
	}
	
	@Test
	public void testBranching() throws InternalErrorException {
		RevisionManagement.createBranch(ds.graphName, ds.revisions.get("master-2"), "testBranch", "test_user", "branching as junit test");

		String revisionGraph = RevisionManagement.getRevisionGraph(ds.graphName);
		
		String rev = RevisionManagement.createNewRevision(ds.graphName, "<a> <b> <c>", "", "test_user", "test_commitMessage", "testBranch");
		String revNumber = RevisionManagement.getRevisionNumber(revisionGraph, "testBranch");
		Assert.assertEquals(rev, revNumber);
		
		String rev2 = RevisionManagement.createNewRevision(ds.graphName, "<a> <b> <d>", "", "test_user", "test_commitMessage", "testBranch");
		String revNumber2 = RevisionManagement.getRevisionNumber(revisionGraph, "testBranch");
		Assert.assertEquals(rev2, revNumber2);
		
		RevisionManagement.createBranch(ds.graphName, rev2, "testBranch2", "test_user", "branching as junit test");
		String rev3 = RevisionManagement.createNewRevision(ds.graphName, "<a> <b> <e>", "", "test_user", "test_commitMessage", "testBranch2");
		String revNumber3 = RevisionManagement.getRevisionNumber(revisionGraph, "testBranch2");
		Assert.assertEquals(rev3, revNumber3);
		
		RevisionManagement.createBranch(ds.graphName, rev2, "testBranch2a", "test_user", "branching as junit test");
		String rev4 = RevisionManagement.createNewRevision(ds.graphName, "<a> <b> <f>", "", "test_user", "test_commitMessage", "testBranch2a");
		String revNumber4 = RevisionManagement.getRevisionNumber(revisionGraph, "testBranch2a");
		Assert.assertEquals(rev4, revNumber4);
	}
	
	@Test
	public void test_minus() throws SAXException, IOException, InternalErrorException {
		expected = ResourceManagement.getContentFromResource("minus/response-rev2.xml");
		result = ep.sparql(format, String.format(query_template_minus,ds.revisions.get("master-2"))).getEntity().toString();
		assertXMLEqual(expected, result);
		
		expected = ResourceManagement.getContentFromResource("minus/response-rev3.xml");
		result = ep.sparql(format, String.format(query_template_minus,ds.revisions.get("master-3"))).getEntity().toString();
		assertXMLEqual(expected, result);
		
		expected = ResourceManagement.getContentFromResource("minus/response-rev4.xml");
		result = ep.sparql(format, String.format(query_template_minus,ds.revisions.get("master-4"))).getEntity().toString();
		assertXMLEqual(expected, result);
	}
	
	@Test
	public void test_minus_query_rewriting() throws SAXException, IOException, InternalErrorException {
		expected = ResourceManagement.getContentFromResource("minus/response-rev2.xml");
		result = ep.sparql(format, String.format(query_template_minus,ds.revisions.get("master-2")), true).getEntity().toString();
		assertXMLEqual(expected, result);
		
		expected = ResourceManagement.getContentFromResource("minus/response-rev3.xml");
		result = ep.sparql(format, String.format(query_template_minus,ds.revisions.get("master-3")), true).getEntity().toString();
		assertXMLEqual(expected, result);
		
		expected = ResourceManagement.getContentFromResource("minus/response-rev4.xml");
		result = ep.sparql(format, String.format(query_template_minus,ds.revisions.get("master-4")), true).getEntity().toString();
		assertXMLEqual(expected, result);
	}
	
}
