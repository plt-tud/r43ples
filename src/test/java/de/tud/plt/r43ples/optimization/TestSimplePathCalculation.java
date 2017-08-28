package de.tud.plt.r43ples.optimization;

import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.Path;
import de.tud.plt.r43ples.existentobjects.Revision;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSimplePathCalculation {

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
        SimplePathCalculation pathCalc = new SimplePathCalculation();
        
        RevisionGraph revisionGraph = new RevisionGraph(ds1.graphName);
        Revision revision = revisionGraph.getRevision("2");
        Path test = pathCalc.getPathToRevisionWithFullGraph(revisionGraph, revision);
        Assert.assertEquals(5, test.getRevisionPath().size());
        
        RevisionGraph revisionGraph2 = new RevisionGraph(ds2.graphName);
        Revision revision2 = revisionGraph2.getRevision("1");
        Path test2 = pathCalc.getPathToRevisionWithFullGraph(revisionGraph2, revision2);
        Assert.assertEquals(4,test2.getRevisionPath().size());
    }
}
