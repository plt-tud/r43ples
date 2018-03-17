package de.tud.plt.r43ples.core;

import com.hp.hpl.jena.rdf.model.Model;
import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the R43ples core interface.
 *
 * @author Markus Graube
 * @author Stephan Hensel
 *
 */
public class R43plesCoreTest extends R43plesTest {
    private TripleStoreInterface tripleStoreInterface;

    R43plesCore core = new R43plesCore();

    @Before
    public void setUp() throws Exception {
        Config.readConfig("r43ples.test.conf");
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
        tripleStoreInterface.dropAllGraphs();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createInitialCommit() throws Exception {
        String graphName = "http://example.com/test";

        core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        RevisionGraph rg = new RevisionGraph(graphName);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_initial.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        // Try to make another initial commit on same graph -> should throw exception
        try {
            core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

            Assert.fail("Try to make another initial commit on same graph should throw exception");
        } catch (InternalErrorException e) {
        }

        rg.purgeRevisionInformation();
    }

    @Test
    public void createInitialCommitWithRequest() throws Exception {
        R43plesRequest req = new R43plesRequest("USER \"TestUser\" MESSAGE \"initial commit during test\" CREATE GRAPH <http://example.com/test>", null, null);
        core.createInitialCommit(req);

        RevisionGraph rg = new RevisionGraph("http://example.com/test");

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_initial.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        // Remove timestamp for test
        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

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

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                            "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_revisioncommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();
    }

    @Test
    public void createUpdateCommitWithRequest() throws Exception {
        //TODO test not working
        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        R43plesRequest req = new R43plesRequest("USER \"TestUser\" " +
                "MESSAGE \"update commit during test\" " +
                "INSERT {\n" +
                "    GRAPH <test> BRANCH \"" + initialCommit.getGeneratedBranch().getReferenceIdentifier() + "\" {\n" +
                "        " + addSet1 + "\n" +
                "    }\n" +
                "}", null, null);

        core.createUpdateCommit(req);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_revisioncommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();
    }

    @Test
    public void createReferenceCommit() throws Exception {
        Assert.fail();
    }

    @Test
    public void createReferenceCommit1() throws Exception {
        Assert.fail();
    }

    @Test
    public void createThreeWayMergeCommit() throws Exception {
        Assert.fail();
    }

    @Test
    public void createThreeWayMergeCommit1() throws Exception {
        Assert.fail();
    }

}