package de.tud.plt.r43ples.dataset;

import com.hp.hpl.jena.rdf.model.Model;
import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.core.R43plesCoreInterface;
import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class SampleDataSetTest extends R43plesTest {

    private final String queryTemplate = "CONSTRUCT {?s ?p ?o} WHERE { GRAPH <%s> REVISION \"%s\" {?s ?p ?o} }";
    private static R43plesCoreInterface r43plesCore;

    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.readConfig("r43ples.test.conf");
        r43plesCore = R43plesCoreSingleton.getInstance();
	}

	// TODO Add checks for the single revisions

	@Test
	public final void testCreateSampleDataset1() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataset1().graphName;
		Assert.assertEquals("http://test.com/r43ples-dataset-1", graph);

		// Check revision graph
        RevisionGraph rg = new RevisionGraph(graph);
        String revisiongraph_turtle = rg.getContentOfRevisionGraph("TURTLE");
        String revisiongraph_expected = ResourceManagement.getContentFromResource("dataset/dataset1/revisiongraph.ttl");
        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_turtle);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_expected);
        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(this.check_isomorphism(model_result, model_expected));

        String query;
        R43plesRequest request;
        String result;
        String expected;


        // Check revision 1
        query = String.format(queryTemplate, graph, "1");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset1/rev-1.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 2
        query = String.format(queryTemplate, graph, "2");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset1/rev-2.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 3
        query = String.format(queryTemplate, graph, "3");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset1/rev-3.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 4
        query = String.format(queryTemplate, graph, "4");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset1/rev-4.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 5
        query = String.format(queryTemplate, graph, "5");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset1/rev-5.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));
    }

	@Test
	public final void testCreateSampleDataset2() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataset2().graphName;
		Assert.assertEquals("http://test.com/r43ples-dataset-2", graph);
        R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();
        // Check revision graph
        RevisionGraph rg = new RevisionGraph(graph);
        String revisiongraph_turtle = rg.getContentOfRevisionGraph("TURTLE");
        String revisiongraph_expected = ResourceManagement.getContentFromResource("dataset/dataset2/revisiongraph.ttl");
        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_turtle);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_expected);
        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);
        Assert.assertTrue(this.check_isomorphism(model_result, model_expected));

        // Check revision 1
        String query = String.format(queryTemplate, graph, "1");
        R43plesRequest request = new R43plesRequest(query, "text/turtle");
        String result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        String expected = ResourceManagement.getContentFromResource("dataset/dataset2/rev-1.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 2
        query = String.format(queryTemplate, graph, "2");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset2/rev-2.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));
	}

    @Test
    public void createSampleDataset3() throws Exception {
        String graph = SampleDataSet.createSampleDataset3().graphName;
        Assert.assertEquals("http://test.com/r43ples-dataset-3", graph);

        // Check revision graph
        RevisionGraph rg = new RevisionGraph(graph);
        String revisiongraph_turtle = rg.getContentOfRevisionGraph("TURTLE");
//        Assert.assertEquals("", revisiongraph_turtle);
        String revisiongraph_expected = ResourceManagement.getContentFromResource("dataset/dataset3/revisiongraph.ttl");
        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_turtle);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_expected);
        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);
        //Assert.assertTrue(this.check_isomorphism(model_result, model_expected));

        // Check revision 1
        String query = String.format(queryTemplate, graph, "1");
        R43plesRequest request = new R43plesRequest(query, "text/turtle");
        String result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        String expected = ResourceManagement.getContentFromResource("dataset/dataset3/rev-1.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 2
        query = String.format(queryTemplate, graph, "2");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset3/rev-2.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 3
        query = String.format(queryTemplate, graph, "3");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset3/rev-3.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 4
        query = String.format(queryTemplate, graph, "4");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset3/rev-4.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

        // Check revision 5
        query = String.format(queryTemplate, graph, "5");
        request = new R43plesRequest(query, "text/turtle");
        result = r43plesCore.getSparqlSelectConstructAskResponse(request, false);
        expected = ResourceManagement.getContentFromResource("dataset/dataset3/rev-5.ttl");
        Assert.assertTrue(this.check_isomorphism(result, expected));

    }



	@Test
	public final void testCreateSampleDataSetMerging() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataSetMerging().graphName;
		Assert.assertEquals("http://test.com/r43ples-dataset-merging", graph);
	}

	@Test
	public final void testCreateSampleDataSetMergingClasses() throws IOException, InternalErrorException {
		String graph = SampleDataSet.createSampleDataSetMergingClasses();
		Assert.assertEquals("http://test.com/r43ples-dataset-merging-classes", graph);
	}

	@Test
	public final void testCreateSampleDataSetRenaming() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataSetRenaming();
		Assert.assertEquals("http://test.com/r43ples-dataset-renaming", graph);
	}

	@Test
	public final void testCreateSampleDataSetComplexStructure() throws InternalErrorException {
		DataSetGenerationResult result = SampleDataSet.createSampleDataSetComplexStructure();
		Assert.assertEquals("http://test.com/r43ples-dataset-complex-structure", result.graphName);
	}

}
