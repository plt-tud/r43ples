/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FullGraphTest extends R43plesTest {

    private static DataSetGenerationResult ds1;

    @BeforeClass
    public static void setUp() throws Exception {
        Config.readConfig("r43ples.test.conf");
        ds1 = SampleDataSet.createSampleDataset1();
    }

    @Test
    public void createNewFullGraph() throws Exception {
        RevisionGraph revisionGraph = new RevisionGraph(ds1.graphName);
        Revision rev3 = revisionGraph.getRevision("3");
        FullGraph full3 = new FullGraph(revisionGraph, rev3);

        String result = RevisionManagementOriginal.getContentOfGraph(full3.getFullGraphUri(), "Turtle");
        String expected = ResourceManagement.getContentFromResource("dataset/dataset1/rev-3.ttl");

        Assert.assertTrue(this.check_isomorphism(result, expected));

        full3.purge();


    }

}