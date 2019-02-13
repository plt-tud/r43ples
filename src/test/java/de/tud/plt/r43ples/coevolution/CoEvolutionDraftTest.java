package de.tud.plt.r43ples.coevolution;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.rdf.model.Model;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;


/**
 * Test the coevolution.
 *
 * @author Stephan Hensel
 */

public class CoEvolutionDraftTest extends R43plesTest {

	/** The graph name. **/
	private static String graphName;
	/** The revision graph. **/
	private static RevisionGraph revisionGraph;
	/** The user. **/
	private static String user = "jUnitUser";
	/** The triple store interface. **/
	private static TripleStoreInterface tripleStoreInterface;
    /** The result format of queries. **/
    private final static String format = "application/sparql-results+xml";

	/**
	 * Initialize TestClass
	 *
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalize(true);
		Config.readConfig("r43ples.test.conf");
		tripleStoreInterface = TripleStoreInterfaceSingleton.get();
		tripleStoreInterface.dropAllGraphsAndReInit();
	}
	
	
	/**
	 * Set up.
	 * @throws InternalErrorException
	 */
	@Before
	public void setUp() throws InternalErrorException {
		// Create the initial data sets
		SampleDataSet.createSampleDataSetHLCAggregation();
		graphName = SampleDataSet.createSampleDataSetCoEvolution().graphName;
		revisionGraph = new RevisionGraph(graphName);
	}
	

	/**
	 * Test the created graph.
	 *
	 * @throws InternalErrorException
	 */
	@Test
	public void testCreatedGraph() throws InternalErrorException {
		// Test the revision graph
		String result = Helper.getContentOfNamedGraphAsN3(revisionGraph.getRevisionGraphUri());
		String expected = ResourceManagement.getContentFromResource("coevolution/revisiongraph_coevo_before.ttl");

		Model model_result = JenaModelManagement.readNTripleStringToJenaModel(result);
		Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		this.removeTimeStampFromModel(model_result);
		this.removeTimeStampFromModel(model_expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));

		// Test the full graph of the master branch
		result = Helper.getContentOfNamedGraphAsN3(revisionGraph.getMasterRevision().getAssociatedBranch().getFullGraphURI());
		expected = ResourceManagement.getContentFromResource("coevolution/fullgraph_coevo.ttl");

		model_result = JenaModelManagement.readNTripleStringToJenaModel(result);
		model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));
	}

	/**
	 * Test the coevolution.
     *
     * @throws InternalErrorException
	 */
	@Test
	public void testCoEvolution() throws InternalErrorException {
		// Create the corresponding R43ples request
        String query = "USER \"" + user + "\" MESSAGE \"test coevolution\" COEVO GRAPH <http://test.com/r43ples-dataset-hlc-aggregation> REVISION \"1\" TO REVISION \"2\"";

        R43plesRequest r43plesRequest = new R43plesRequest(query, format);

        Evolution evolution = R43plesCoreSingleton.getInstance().coevolveAll(r43plesRequest);

		// Check the semantic description of the coevolution
		Assert.assertEquals(evolution.getStartRevision().getRevisionIdentifier(), "1");
		Assert.assertEquals(evolution.getEndRevision().getRevisionIdentifier(), "2");
		Assert.assertEquals(evolution.getUsedSourceRevisionGraph().getGraphName(), "http://test.com/r43ples-dataset-hlc-aggregation");

		Assert.assertEquals(evolution.getAssociatedSemanticChangeList().size(), 1);
		Assert.assertEquals(evolution.getAssociatedSemanticChangeList().getFirst().getUsedRuleURI(), "http://eatld.et.tu-dresden.de/rules#instances/rename-class/definition");

		Assert.assertEquals(evolution.getPerformedCoEvolutionList().size(), 1);

		CoEvolution coEvolution = evolution.getPerformedCoEvolutionList().getFirst();
		Assert.assertEquals(coEvolution.getUsedTargetBranch().getReferenceURI(), "http://test.com/r43ples-dataset-coevolution-master");
		Assert.assertEquals(coEvolution.getUsedTargetRevisionGraph().getRevisionGraphUri(), "http://test.com/r43ples-dataset-coevolution-revisiongraph");
		Assert.assertEquals(coEvolution.getGeneratedRevision().getRevisionIdentifier(), "2");

		Assert.assertEquals(coEvolution.getAppliedCoEvolutionRuleList().size(), 1);
		Assert.assertEquals(coEvolution.getAppliedCoEvolutionRuleList().getFirst().getUsedRule().getSpinAddSetInsertQueryURI(), "http://eatld.et.tu-dresden.de/rules#instances/rename-class-coevo/add/element-1");
		Assert.assertEquals(coEvolution.getAppliedCoEvolutionRuleList().getFirst().getUsedSemanticChange().getUsedRuleURI(), "http://eatld.et.tu-dresden.de/rules#instances/rename-class/definition");
		Assert.assertEquals(coEvolution.getAppliedCoEvolutionRuleList().getFirst().getSparqlVariableGroupList().size(), 1);
		Assert.assertEquals(coEvolution.getAppliedCoEvolutionRuleList().getFirst().getSparqlVariableGroupList().getFirst().getSparqlVariableList().getFirst().getVariableName(), "subject");
		Assert.assertEquals(coEvolution.getAppliedCoEvolutionRuleList().getFirst().getSparqlVariableGroupList().getFirst().getSparqlVariableList().getFirst().getValue(), "http://example-house.com/yellowhouse");

		// Check the full graph of the evolved graph
		String result = Helper.getContentOfNamedGraphAsN3(revisionGraph.getMasterRevision().getAssociatedBranch().getFullGraphURI());
		String expected = ResourceManagement.getContentFromResource("coevolution/fullgraph_coevo_after.ttl");

		Model model_result = JenaModelManagement.readNTripleStringToJenaModel(result);
		Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));

		// Check the revision graph of the coevolution revision graph
		RevisionGraph coEvolutionRevisionGraph = new RevisionGraph(Config.evolution_graph);
		result = Helper.getContentOfNamedGraphAsN3(coEvolutionRevisionGraph.getRevisionGraphUri());
		expected = ResourceManagement.getContentFromResource("coevolution/revisiongraph_coevo_storage_after.ttl");

		model_result = JenaModelManagement.readNTripleStringToJenaModel(result);
		model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		this.removeTimeStampFromModel(model_result);
		this.removeTimeStampFromModel(model_expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));
	}

}
