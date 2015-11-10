package de.tud.plt.r43ples.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;

public class TestSampleDataSet {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Config.readConfig("r43ples.test.conf");
	}

	@Test
	public final void testCreateSampleDataset1() throws InternalErrorException {
		String graph = SampleDataSet.createSampleDataset1().graphName;
		Assert.assertEquals("http://test.com/r43ples-dataset-1", graph);
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
