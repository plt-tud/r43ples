/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.PickCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class R43plesCoreTest extends R43plesTest {


    R43plesCore core = new R43plesCore();

    @Before
    public void setUp() throws Exception {
        Config.readConfig("r43ples.test.conf");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createInitialCommit() throws Exception {
        String graphName = "http://example.com/test";

        core.createInitialCommit(graphName, null, null, "test engine", "initial commit during test");

        RevisionGraph rg = new RevisionGraph(graphName);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("core/R43plesCore/revisiongraph_initial.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(model_result.isIsomorphicWith(model_expected));

        // Try to make another initial commit on same graph -> should throw exception
        try {
            core.createInitialCommit(graphName, null, null, "test engine", "initial commit during test");

            Assert.fail("Try to make another initial commit on same graph should throw exception");
        } catch (InternalErrorException e) {
        }

        rg.purgeRevisionInformation();
    }

    @Test
    public void createInitialCommitWithRequest() throws Exception {
        R43plesRequest req = new R43plesRequest( "CREATE GRAPH <http://example.com/test>" , null, null );
        core.createInitialCommit(req);

        RevisionGraph rg = new RevisionGraph("http://example.com/test");

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("core/R43plesCore/revisiongraph_initial.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        // Remove timestamp for test
        Property provAtTime = model_result.getProperty("http://www.w3.org/ns/prov#atTime");
        StmtIterator stmtIterator = model_result.listStatements(null, provAtTime, (RDFNode) null);
        model_result.remove(stmtIterator);
        StmtIterator stmtIterator2 = model_expected.listStatements(null, provAtTime, (RDFNode) null);
        model_expected.remove(stmtIterator2);

        Assert.assertTrue(model_result.isIsomorphicWith(model_expected));

        // Try to make another initial commit on same graph -> should throw exception
        try {
            core.createInitialCommit(req);

            Assert.fail("Try to make another initial commit on same graph should throw exception");
        } catch (InternalErrorException e) {
        }

        rg.purgeRevisionInformation();

    }

    @Test
    public void createUpdateCommit() throws Exception {
    }

    @Test
    public void createUpdateCommitWithRequest() throws Exception {
    }

    @Test
    public void createReferenceCommit() throws Exception {
    }

    @Test
    public void createReferenceCommit1() throws Exception {
    }

    @Test
    public void createThreeWayMergeCommit() throws Exception {
    }

    @Test
    public void createThreeWayMergeCommit1() throws Exception {
    }

    @Test
    public void createPickCommit() throws Exception {
        String graphName = SampleDataSet.createSampleDataSetComplexStructure().graphName;
        RevisionGraph rg = new RevisionGraph(graphName);

        Assert.assertEquals(rg.getMasterRevision().getRevisionIdentifier(), "13");

        String query = String.format("" +
                        "USER \"test\" " +
                        "MESSAGE \"pick test\"" +
                        "PICK GRAPH <%s> REVISION \"%s\" INTO BRANCH \"%s\"",
                graphName, "1", "master");
        R43plesRequest request = new R43plesRequest(query, "text/turtle");
        PickCommit result = core.createPickCommit(request);

        Revision masterRevision = result.getGeneratedRevisions().get(0);
        Assert.assertEquals(masterRevision.getRevisionIdentifier(), "14");
        Assert.assertEquals(rg.getMasterRevision().getRevisionIdentifier(), "14");

        String revisiongraph_actual = rg.getContentOfRevisionGraph("TURTLE");
        String revisiongraph_expected = ResourceManagement.getContentFromResource("core/R43plesCore/revisiongraph_datasetComplex_pick_1.ttl");
        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_actual);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(revisiongraph_expected);
        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);
        Assert.assertTrue(this.check_isomorphism(model_result, model_expected));

        String query_content = String.format("CONSTRUCT {?s ?p ?o} WHERE { GRAPH <%s> {?s ?p ?o} }", graphName);
        String content_actual = core.getSparqlSelectConstructAskResponse(new R43plesRequest(query_content, "text/turtle"), false);
        String content_expected = ResourceManagement.getContentFromResource("core/R43plesCore/datasetComplex_pick_1_into_master.ttl");
        Assert.assertTrue(this.check_isomorphism(content_actual, content_expected));
    }

}