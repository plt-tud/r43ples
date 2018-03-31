package de.tud.plt.r43ples.webservice;

import de.tud.plt.r43ples.dataset.DataSetGenerationResult;
import de.tud.plt.r43ples.dataset.SampleDataSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.commons.configuration.ConfigurationException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;

import static org.hamcrest.core.StringContains.containsString;


public class APITest extends JerseyTest {
	
	private static DataSetGenerationResult ds1;

	@Override
    protected Application configure() {
		set("jersey.config.test.container.port", "9996");
        return new ResourceConfig(API.class);
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, InternalErrorException {
		Config.readConfig("r43ples.test.conf");
		ds1 = SampleDataSet.createSampleDataset1();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		TripleStoreInterfaceSingleton.close();
	}
	
	
	@Test
	public void testGetRevisedGraphs() throws InternalErrorException{
		String result = target("api/getRevisedGraphs").request().get(String.class);
		Assert.assertThat(result, containsString(ds1.graphName));
	}
	
	@Test
	public void testGetRevisedGraphsJSON() throws InternalErrorException{
		String result = target("api/getRevisedGraphs").queryParam("format", "application/json").request().get(String.class);
		Assert.assertThat(result, containsString(ds1.graphName));
	}
	
	@Test
	public void testGetRevisedGraphsTurtle() throws InternalErrorException{
		String result = target("api/getRevisedGraphs").queryParam("format", "text/turtle").request().get(String.class);
		Assert.assertThat(result, containsString(ds1.graphName));
	}


}
