package de.tud.plt.r43ples.optimization;

import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Path;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimplePathCalculationTest {

    private static DataSetGenerationResult ds1;
    private static DataSetGenerationResult ds2;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Config.readConfig("r43ples.test.conf");
        ds1 = SampleDataSet.createSampleDataset1();
        ds2 = SampleDataSet.createSampleDataSetComplexStructure();
    }

    /**
     * @throws InternalErrorException
     */
    @Test
    public final void testPathToLeaf() throws InternalErrorException {

        RevisionGraph revisionGraph = new RevisionGraph(ds1.graphName);
        Revision revision = revisionGraph.getRevision("2");
        SimplePathCalculation pathCalc = new SimplePathCalculation(revisionGraph);
        Path test = pathCalc.getPathToRevisionWithFullGraph(revision);

        Assert.assertEquals(4, test.getRevisionPath().size());
        Assert.assertEquals("2", test.getRevisionPath().get(0).getRevisionIdentifier());
        Assert.assertEquals("3", test.getRevisionPath().get(1).getRevisionIdentifier());
        Assert.assertEquals("4", test.getRevisionPath().get(2).getRevisionIdentifier());
        Assert.assertEquals("5", test.getRevisionPath().get(3).getRevisionIdentifier());

        
        RevisionGraph revisionGraph2 = new RevisionGraph(ds2.graphName);
        Revision revision2 = revisionGraph2.getRevision("1");
        SimplePathCalculation pathCalc2 = new SimplePathCalculation(revisionGraph2);
        Path test2 = pathCalc2.getPathToRevisionWithFullGraph(revision2);
        Assert.assertEquals(3, test2.getRevisionPath().size());
        Assert.assertEquals("1", test2.getRevisionPath().get(0).getRevisionIdentifier());
        Assert.assertEquals("2", test2.getRevisionPath().get(1).getRevisionIdentifier());
        Assert.assertEquals("5", test2.getRevisionPath().get(2).getRevisionIdentifier());
    }
}
