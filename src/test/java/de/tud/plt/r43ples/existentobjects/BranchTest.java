/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.management.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BranchTest {
    private String graphName;

    @Before
    public void setUp() throws Exception {
        Config.readConfig("r43ples.test.conf");
        graphName = SampleDataSet.createSampleDataSetMerging().graphName;
    }

    @Test
    public void getLeafRevision() throws Exception {
        RevisionGraph rg = new RevisionGraph(graphName);
        Branch b = new Branch(rg, "b1", true);
        Assert.assertEquals(b.getReferenceURI(), "http://test.com/r43ples-dataset-merging-branch-b1");
        Revision r = b.getLeafRevision();
        Assert.assertEquals(r.getRevisionURI(), "http://test.com/r43ples-dataset-merging-revision-3");
    }

}