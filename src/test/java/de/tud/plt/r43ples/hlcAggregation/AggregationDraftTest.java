package de.tud.plt.r43ples.hlcAggregation;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.SemanticChange;
import de.tud.plt.r43ples.existentobjects.SparqlVariable;
import de.tud.plt.r43ples.existentobjects.Statement;
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

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import java.util.LinkedList;


/**
 * Test the HLC aggregation.
 *
 * @author Stephan Hensel
 */

public class AggregationDraftTest extends R43plesTest {

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
		// Create the initial data set
		graphName = SampleDataSet.createSampleDataSetHLCAggregation().graphName;
		revisionGraph = new RevisionGraph(graphName);
	}
	

	/**
	 * Test the aggregation.
	 *
	 * @throws InternalErrorException
	 */
	@Test
	public void testCreatedGraph() throws InternalErrorException {
		// Test the revision graph
		String result = Helper.getContentOfNamedGraphAsN3(revisionGraph.getRevisionGraphUri());
		String expected = ResourceManagement.getContentFromResource("hlcAggregation/revisiongraph_hlc_before.ttl");

		Model model_result = JenaModelManagement.readNTripleStringToJenaModel(result);
		Model model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		this.removeTimeStampFromModel(model_result);
		this.removeTimeStampFromModel(model_expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));

		// Test the full graph of the master branch
		result = Helper.getContentOfNamedGraphAsN3(revisionGraph.getMasterRevision().getAssociatedBranch().getFullGraphURI());
		expected = ResourceManagement.getContentFromResource("hlcAggregation/fullgraph_hlc.ttl");

		model_result = JenaModelManagement.readNTripleStringToJenaModel(result);
		model_expected = JenaModelManagement.readTurtleStringToJenaModel(expected);

		Assert.assertTrue(check_isomorphism(model_result, model_expected));
	}

	/**
	 * Test the created graph.
     *
     * @throws InternalErrorException
	 */
	@Test
	public void testHlcAggregation() throws InternalErrorException {
		// Create the corresponding R43ples request
        String query = "AGG GRAPH <http://test.com/r43ples-dataset-hlc-aggregation> REVISION \"1\" TO REVISION \"2\"";

        R43plesRequest r43plesRequest = new R43plesRequest(query, format);

        LinkedList<SemanticChange> semanticChanges = R43plesCoreSingleton.getInstance().aggregate(r43plesRequest);

        // One semantic change must be detected
        Assert.assertEquals(semanticChanges.size(), 1);

        // Get the semantic change and apply different checks
        SemanticChange semanticChange = semanticChanges.getFirst();
        Assert.assertEquals(semanticChange.getUsedRuleURI(), "http://eatld.et.tu-dresden.de/rules#instances/rename-class/definition");
        Assert.assertEquals(semanticChange.getSparqlVariableList().size(), 3);
        for (SparqlVariable sparqlVariable : semanticChange.getSparqlVariableList()) {
            switch (sparqlVariable.getVariableName()) {
                case "a":
                    Assert.assertEquals(sparqlVariable.getValue(), "http://example.com/house");
                    break;
                case "b":
                    Assert.assertEquals(sparqlVariable.getValue(), "http://example.com/myhouse");
                    break;
                case "resource":
                    Assert.assertEquals(sparqlVariable.getValue(), "http://example.com/building");
                    break;
                default:
                    Assert.fail();
                    break;
            }
        }
        Assert.assertEquals(semanticChange.getAdditons().getStatementList().size(), 2);
        for (Statement statement : semanticChange.getAdditons().getStatementList()) {
            switch (statement.getObject()) {
                case "http://example.com/building":
                    Assert.assertEquals(statement.getSubject(), "http://example.com/house");
                    Assert.assertEquals(statement.getPredicate(), "http://www.w3.org/2000/01/rdf-schema#subClassOf");
                    break;
                case "http://www.w3.org/2000/01/rdf-schema#Class":
                    Assert.assertEquals(statement.getSubject(), "http://example.com/house");
                    Assert.assertEquals(statement.getPredicate(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                    break;
                default:
                    Assert.fail();
                    break;
            }
        }
	}

}
