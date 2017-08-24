package de.tud.plt.r43ples.dataset;

import com.hp.hpl.jena.rdf.model.Model;
import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class TestSampleDataSet extends R43plesTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.readConfig("r43ples.test.conf");
	}

	// TODO Add checks for the single revisions

	@Test
	public final void testCreateSampleDataset1() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataset1().graphName;
		Assert.assertEquals("http://test.com/r43ples-dataset-1", graph);

        RevisionGraph rg = new RevisionGraph(graph);
        String revisiongraph_turtle = rg.getContentOfRevisionGraph("TURTLE");
        String revisiongraph_expected = ResourceManagement.getContentFromResource("dataset/revisiongraph_dataset1.ttl");

        Model model_actual = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_turtle);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_expected);

        this.removeTimeStampFromModel(model_actual);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(model_actual.isIsomorphicWith(model_expected));
    }

	@Test
	public final void testCreateSampleDataset2() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataset2().graphName;
		Assert.assertEquals("http://test.com/r43ples-dataset-2", graph);
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
		String graph = SampleDataSet.createSampleDataSetComplexStructure();
		Assert.assertEquals("http://test.com/r43ples-dataset-complex-structure", graph);
	}

}
