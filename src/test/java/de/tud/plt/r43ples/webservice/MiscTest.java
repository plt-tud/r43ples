/**
 * 
 */
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
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.core.StringContains.containsString;

/**
 * @author Markus Graube
 *
 */
public class MiscTest extends JerseyTest {
	
	private static DataSetGenerationResult dataset;

	@Override
    protected Application configure() {
		set("jersey.config.test.container.port", "9996");
		return new ResourceConfig(Misc.class);
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException {
		Config.readConfig("r43ples.test.conf");
		dataset = SampleDataSet.createSampleDataSetComplexStructure();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		TripleStoreInterfaceSingleton.close();
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Misc#getLandingPage()}.
	 */
	@Test
	public final void testGetLandingPage() {
		String result = target().request(MediaType.TEXT_HTML).get(String.class);
		Assert.assertThat(result, containsString("html"));
		result = target("index").request(MediaType.TEXT_HTML).get(String.class);
		Assert.assertThat(result, containsString("html"));
	}

	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Misc#createSampleDataset(java.lang.String)}.
	 */
	@Test
	public final void testCreateSampleDataset() {
		String result = target("createSampleDataset").queryParam("dataset", "1").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "2").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "3").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "merging").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "merging-classes").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "renaming").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "complex-structure").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "rebase").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "forcerebase").request().get(String.class);
		result = target("createSampleDataset").queryParam("dataset", "fastforward").request().get(String.class);		
		result = target("createSampleDataset").request().get(String.class);
		Assert.assertThat(result, containsString(dataset.graphName));
	}

	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Misc#getRevisionGraph(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetRevisionGraph() {
		String result = target("revisiongraph").queryParam("graph", dataset.graphName).queryParam("format", "text/turtle").request().get(String.class);
		Assert.assertThat(result, containsString("@prefix"));
		
		result = target("revisiongraph").queryParam("graph", dataset.graphName).queryParam("format", "table").request().get(String.class);
		Assert.assertThat(result, containsString("<svg"));
		
		result = target("revisiongraph").queryParam("graph", dataset.graphName).queryParam("format", "graph").request().get(String.class);
		Assert.assertThat(result, containsString("<div id=\"visualisation\""));
	}

	
	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Misc#getContentOfGraph(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetContentOfGraph() {
		String result = target("contentOfGraph").queryParam("graph", dataset.graphName).request().get(String.class);
		Assert.assertThat(result, containsString("type"));
		
		result = target("contentOfGraph").queryParam("graph", dataset.graphName).queryParam("format", "text/turtle").request().get(String.class);
		Assert.assertThat(result, containsString("<http://"));
	}
	
	
	

}
