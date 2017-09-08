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
import de.tud.plt.r43ples.optimization.ChangeSetPath;
import de.tud.plt.r43ples.optimization.PathCalculationFabric;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ThreeWayMergeCommitDraftTest extends R43plesTest {

    private static DataSetGenerationResult ds;
    private static RevisionGraph revisionGraph;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Config.readConfig("r43ples.test.conf");
        ds = SampleDataSet.createSampleDataSetComplexStructure();
        revisionGraph = new RevisionGraph(ds.graphName);
    }

    @Test
    public void createRevisionProgress() throws Exception {
        ThreeWayMergeCommitDraft cd = new ThreeWayMergeCommitDraft(ds.graphName,
                "B1", "master",
                "butler", "test",
                null, null, MergeTypes.AUTO, false);

        Revision startRevision = revisionGraph.getRevision("1");
        Revision endRevision = revisionGraph.getRevision("3");
        FullGraph fullGraph = new FullGraph(revisionGraph, startRevision);

        ChangeSetPath path = PathCalculationFabric.getInstance(revisionGraph).getChangeSetsBetweenStartAndTargetRevision(startRevision, endRevision);
        String graphNameRevisionProgress = "http://test.com/revision-progress";

        cd.createRevisionProgress(fullGraph, path, graphNameRevisionProgress, "http://test.com/rp");

        String result = RevisionManagementOriginal.getContentOfGraph(graphNameRevisionProgress, "TURTLE");
        String expected = ResourceManagement.getContentFromResource("dataset/dataset1/revision-progress-1-3.ttl");
        Assert.assertEquals(expected, result);
    }

    @Test
    public void createRevisionProgresses() throws Exception {
        ThreeWayMergeCommitDraft cd = new ThreeWayMergeCommitDraft(ds.graphName,
                "B1", "master",
                "butler", "test",
                null, null, MergeTypes.AUTO, false);


        Revision commonRevision = revisionGraph.getRevision("0");
        Revision fromRevision = revisionGraph.getRevision("5");
        Revision toRevision = revisionGraph.getRevision("13");

        ChangeSetPath pathFrom = PathCalculationFabric.getInstance(revisionGraph).getChangeSetsBetweenStartAndTargetRevision(commonRevision, fromRevision);
        ChangeSetPath pathTo = PathCalculationFabric.getInstance(revisionGraph).getChangeSetsBetweenStartAndTargetRevision(commonRevision, toRevision);

        String graphFrom = "http://revision.from";
        String graphTo = "http://revision.into";
        String uriFrom = "http://revision.from/entry";
        String uriTo = "http://revision.into/entry";
        cd.createRevisionProgresses(pathFrom, graphFrom, uriFrom, pathTo, graphTo, uriTo, commonRevision);

        String resultFrom = RevisionManagementOriginal.getContentOfGraph(graphFrom, "TURTLE");
        String expectedFrom = ResourceManagement.getContentFromResource("dataset/dataset1/revision-progress-0-5.ttl");
        Assert.assertTrue(this.check_isomorphism(expectedFrom, resultFrom));

        String resultTo = RevisionManagementOriginal.getContentOfGraph(graphTo, "TURTLE");
        String expectedTo = ResourceManagement.getContentFromResource("dataset/dataset1/revision-progress-0-13.ttl");
        Assert.assertEquals(expectedTo, resultTo);
    }

}