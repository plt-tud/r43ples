package de.tud.plt.r43ples.hlcAggregation;

import de.tud.plt.r43ples.R43plesTest;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;


/**
 * Test the HLC aggregation.
 *
 * @author Stephan Hensel
 */

public class AggregationDraftTest extends R43plesTest {

	/** The graph name. **/
	private static String graphName;
	/** The user. **/
	private static String user = "jUnitUser";
	/** The triple store interface. **/
	private static TripleStoreInterface tripleStoreInterface;

	
	/**
	 * Initialize TestClass
	 * 
	 * @throws ConfigurationException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException {
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
	}
	
	
	
	/**
	 * Test the created graph.
	 * 
	 * @throws InternalErrorException
	 */
	@Test
	public void testCreatedGraph() throws InternalErrorException {
		Assert.assertTrue(true);
		//TODO
	}

}
