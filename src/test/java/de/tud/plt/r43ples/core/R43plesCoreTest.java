package de.tud.plt.r43ples.core;

import org.apache.jena.rdf.model.Model;
import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.LinkedList;

/**
 * This class tests the R43ples core interface.
 *
 * @author Markus Graube
 * @author Stephan Hensel
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class R43plesCoreTest extends R43plesTest {

    static TripleStoreInterface tripleStoreInterface;

    private R43plesCore core = new R43plesCore();

    @BeforeClass
    public static void setUpBefore()  throws Exception {
        Config.readConfig("r43ples.test.conf");
        tripleStoreInterface = TripleStoreInterfaceSingleton.get();
    }

    @Before
    public void setUp() {
        tripleStoreInterface.dropAllGraphsAndReInit();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createInitialCommit() throws Exception {
        String graphName = "http://example.com/test";

        core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        RevisionGraph rg = new RevisionGraph(graphName);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("core/R43plesCore/revisiongraph_initial.ttl");

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
            logger.debug("Internal error was thrown as expected.");
        }

        rg.purgeRevisionInformation();
    }

    @Test
    public void createInitialCommitWithRequest() throws Exception {
        R43plesRequest req = new R43plesRequest("USER \"TestUser\" MESSAGE \"initial commit during test\" CREATE GRAPH <http://example.com/test>", null, null);
        core.createInitialCommit(req);

        RevisionGraph rg = new RevisionGraph("http://example.com/test");

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("core/R43plesCore/revisiongraph_initial.ttl");

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
            logger.debug("Internal error was thrown as expected.");
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
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_updatecommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();

        //TODO check content of add/del sets and full graph
    }

    @Test
    public void createUpdateCommitWithRequest() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        R43plesRequest req = new R43plesRequest("USER \"TestUser\" " +
                "MESSAGE \"update commit during test\" " +
                "INSERT {\n" +
                "    GRAPH <" + graphName + "> BRANCH \"" + initialCommit.getGeneratedBranch().getReferenceIdentifier() + "\" {\n" +
                "        " + addSet1 + "\n" +
                "    }\n" +
                "} WHERE {?s ?p ?o}", null, null);

        core.createUpdateCommit(req);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_updatecommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();

        //TODO check content of add/del sets and full graph
    }

    @Test
    public void createRevertCommit() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        core.createRevertCommit(rg, updateCommit.getGeneratedRevision().getAssociatedBranch(), "TestUser", "revert commit during test");

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_revertcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();

        //TODO check content of add/del sets and full graph
    }

    @Test
    public void createRevertCommitWithRequest() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        R43plesRequest req = new R43plesRequest("USER \"TestUser\" " +
                "MESSAGE \"revert commit during test\" " +
                "REVERT GRAPH <" + graphName + "> BRANCH \"" + updateCommit.getGeneratedRevision().getAssociatedBranch().getReferenceIdentifier() + "\"", null, null);

        core.createRevertCommit(req);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_revertcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();

        //TODO check content of add/del sets and full graph
    }

    @Test
    public void createReferenceCommitBranch() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        core.createReferenceCommit(rg, "develop", updateCommit.getGeneratedRevision(), "TestUser", "branch commit during test", true);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_branchcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();
    }

    @Test
    public void createReferenceCommitTag() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        core.createReferenceCommit(rg, "Version 1.1.0", updateCommit.getGeneratedRevision(), "TestUser", "tag commit during test", false);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_tagcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();
    }

    @Test
    public void createReferenceCommitWithRequestBranch() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        R43plesRequest req = new R43plesRequest("USER \"TestUser\" " +
                "MESSAGE \"branch commit during test\" " +
                "BRANCH GRAPH <" + graphName + "> REVISION \""+ updateCommit.getGeneratedRevision().getRevisionIdentifier() +"\"TO \"develop\"", null, null);

        core.createReferenceCommit(req);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_branchcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();
    }

    @Test
    public void createReferenceCommitWithRequestTag() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        R43plesRequest req = new R43plesRequest("USER \"TestUser\" " +
                "MESSAGE \"tag commit during test\" " +
                "TAG GRAPH <" + graphName + "> REVISION \""+ updateCommit.getGeneratedRevision().getRevisionIdentifier() +"\"TO \"Version 1.1.0\"", null, null);

        core.createReferenceCommit(req);

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_tagcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();
    }

    @Test
    public void createBranchCommit() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        core.createBranchCommit(rg, "develop", updateCommit.getGeneratedRevision(), "TestUser", "branch commit during test");

        // Check all corresponding graphs
        // List all
        LinkedList<String> allGraphs = new LinkedList<>();
        allGraphs.add("http://eatld.et.tu-dresden.de/r43ples-sdg");
        allGraphs.add("http://eatld.et.tu-dresden.de/r43ples-rules");
        allGraphs.add("http://example.com/test-revisiongraph");
        allGraphs.add("http://example.com/test");
        allGraphs.add("http://eatld.et.tu-dresden.de/r43ples-revisions");
        allGraphs.add("http://example.com/test-addSet-0-1");
        allGraphs.add("http://example.com/test-develop");
        allGraphs.add(Config.evolution_graph + "-revisiongraph");
        assertListAll(allGraphs, tripleStoreInterface);
        // R43ples revisions
        String expected_r43ples = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Branch/r43ples-revisions_branchcommit.ttl");
        assertContentOfGraph("http://eatld.et.tu-dresden.de/r43ples-revisions", expected_r43ples);
        // Revision graph
        String result_rg = rg.getContentOfRevisionGraph("TURTLE");
        String expected_rg = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Branch/revisiongraph_branchcommit.ttl");
        assertIsomorphism(result_rg, expected_rg);
        // Master full graph
        String expected_master = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Branch/master_branchcommit.ttl");
        assertContentOfGraph("http://example.com/test", expected_master);
        // Add set 0-1 full graph
        String expected_addSet01 = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Branch/addSet01_branchcommit.ttl");
        assertContentOfGraph("http://example.com/test-addSet-0-1", expected_addSet01);
        // Develop full graph
        String expected_develop = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Branch/develop_branchcommit.ttl");
        assertContentOfGraph("http://example.com/test-develop", expected_develop);

        rg.purgeRevisionInformation();

    }

    @Test
    public void createTagCommit() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Adam> <http://test.com/knows> <http://test.com/Bob> .\n" +
                "<http://test.com/Carlos> <http://test.com/knows> <http://test.com/Danny> .";

        UpdateCommit updateCommit = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        core.createTagCommit(rg, "Version 1.1.0", updateCommit.getGeneratedRevision(), "TestUser", "tag commit during test");

        String result = rg.getContentOfRevisionGraph("TURTLE");
        String expected = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/revisiongraph_tagcommit_1.ttl");

        Model model_result = JenaModelManagement.readTurtleStringToJenaModel(result);
        Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

        this.removeTimeStampFromModel(model_result);
        this.removeTimeStampFromModel(model_expected);

        Assert.assertTrue(check_isomorphism(model_result, model_expected));

        rg.purgeRevisionInformation();

    }

    @Test
    public void createFastForwardMergeCommit() throws Exception {

        String graphName = "http://example.com/test";
        RevisionGraph rg = new RevisionGraph(graphName);

        InitialCommit initialCommit = core.createInitialCommit(graphName, null, null, "TestUser", "initial commit during test");

        String addSet1 =    "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/A> .\n" +
                "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/B> .";

        UpdateCommit updateCommit1 = core.createUpdateCommit(graphName, addSet1, null, "TestUser", "update commit during test", initialCommit.getGeneratedBranch().getReferenceIdentifier());

        BranchCommit branchCommit = core.createBranchCommit(rg, "develop", updateCommit1.getGeneratedRevision(), "TestUser", "branch commit during test");

        String addSet2 =    "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/E> .\n" +
                "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/N> .";

        String delSet2 =    "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/A> .";

        core.createUpdateCommit(graphName, addSet2, delSet2, "TestUser", "update commit during test", branchCommit.getGeneratedReference().getReferenceIdentifier());

        String addSet3 =    "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/K> .\n" +
                "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/X> .";

        String delSet3 =    "<http://test.com/Alphabet> <http://test.com/has> <http://test.com/B> .";

        core.createUpdateCommit(graphName, addSet3, delSet3, "TestUser", "update commit during test", branchCommit.getGeneratedReference().getReferenceIdentifier());

        R43plesRequest req = new R43plesRequest("USER \"TestUser\" " +
                "MESSAGE \"merge commit during test\" " +
                "MERGE GRAPH <" + graphName + "> BRANCH \""+ branchCommit.getGeneratedReference().getReferenceIdentifier() +"\" INTO BRANCH \"master\"", null, null);

        core.createMergeCommit(req);

        // Check all corresponding graphs
        // List all
        LinkedList<String> allGraphs = new LinkedList<>();
        allGraphs.add("http://eatld.et.tu-dresden.de/r43ples-sdg");
        allGraphs.add("http://eatld.et.tu-dresden.de/r43ples-rules");
        allGraphs.add("http://example.com/test-revisiongraph");
        allGraphs.add("http://example.com/test");
        allGraphs.add("http://eatld.et.tu-dresden.de/r43ples-revisions");
        allGraphs.add("http://example.com/test-addSet-0-1");
        allGraphs.add("http://example.com/test-develop");
        allGraphs.add("http://example.com/test-addSet-1-2");
        allGraphs.add("http://example.com/test-deleteSet-1-2");
        allGraphs.add("http://example.com/test-addSet-2-3");
        allGraphs.add("http://example.com/test-deleteSet-2-3");
        allGraphs.add(Config.evolution_graph + "-revisiongraph");

        assertListAll(allGraphs,tripleStoreInterface);
        // R43ples revisions
        String expected_r43ples = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/r43ples-revisions_fastforwardcommit.ttl");
        assertContentOfGraph("http://eatld.et.tu-dresden.de/r43ples-revisions", expected_r43ples);
        // Revision graph
        String result_rg = rg.getContentOfRevisionGraph("TURTLE");
        String expected_rg = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/revisiongraph_fastforwardcommit.ttl");
        assertIsomorphism(result_rg, expected_rg);
        // Master full graph
        String expected_master = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/master_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test", expected_master);
        // Add set 0-1 full graph
        String expected_addSet01 = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/addSet01_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test-addSet-0-1", expected_addSet01);
        // Develop full graph
        String expected_develop = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/develop_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test-develop", expected_develop);
        // Add set 1-2 full graph
        String expected_addSet12 = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/addSet12_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test-addSet-1-2", expected_addSet12);
        // Del set 1-2 full graph
        String expected_delSet12 = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/delSet12_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test-deleteSet-1-2", expected_delSet12);
        // Add set 2-3 full graph
        String expected_addSet23 = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/addSet23_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test-addSet-2-3", expected_addSet23);
        // Del set 2-3 full graph
        String expected_delSet23 = ResourceManagement.getContentFromResource("draftobjects/R43plesCore/Merge/delSet23_fastforwardcommit.ttl");
        assertContentOfGraph("http://example.com/test-deleteSet-2-3", expected_delSet23);

        rg.purgeRevisionInformation();
    }

    @Test @Ignore
    public void createThreeWayMergeCommit() {
        Assert.fail();
        //TODO 3-Way-Merges are currently tested within merging/ThreeWayMergeTest
    }

}