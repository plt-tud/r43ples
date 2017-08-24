/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.draftobjects;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class R43plesCoreTest {

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
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_initial.ttl");

        Model model_result = JenaModelManagement.readStringToJenaModel(result, "TURTLE");
        Model model_expected = JenaModelManagement.readStringToJenaModel(expected, "TURTLE");

        // Remove timestamp for test
        Property provAtTime = model_result.getProperty("http://www.w3.org/ns/prov#atTime");
        StmtIterator stmtIterator = model_result.listStatements(null, provAtTime, (RDFNode) null);
        model_result.remove(stmtIterator);
        StmtIterator stmtIterator2 = model_expected.listStatements(null, provAtTime, (RDFNode) null);
        model_expected.remove(stmtIterator2);

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
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_initial.ttl");

        Model model_result = JenaModelManagement.readStringToJenaModel(result, "TURTLE");
        Model model_expected = JenaModelManagement.readStringToJenaModel(expected, "TURTLE");

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

}