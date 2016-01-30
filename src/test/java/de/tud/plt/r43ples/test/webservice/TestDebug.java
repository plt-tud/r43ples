/**
 * 
 */
package de.tud.plt.r43ples.test.webservice;

import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.custommonkey.xmlunit.XMLUnit;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.Debug;

/**
 * @author Markus Graube
 *
 */
public class TestDebug extends JerseyTest {
	
	@Override
    protected Application configure() {
        return new ResourceConfig(Debug.class);
    }
    
	@BeforeClass
	public static void setUpBeforeClass() throws ConfigurationException, URISyntaxException, IOException, InternalErrorException {
		XMLUnit.setIgnoreWhitespace(true);
		Config.readConfig("r43ples.test.conf");
		SampleDataSet.createSampleDataset1();
	}
	
	@AfterClass
	public static void tearDownAfterClass() {
		TripleStoreInterfaceSingleton.close();
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Debug#postDebug()}.
	 */
	@Test
	public final void testPostDebug() {
		String query = "SELECT * WHERE { GRAPH ?g { ?s ?p ?o. } }";
		String result = target("debug").request().post(Entity.entity(query, "application/sparql-query"), String.class);
		Assert.assertThat(result, containsString("WIP"));
	}


	
	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Debug#getDebugQuery(java.lang.String)}.
	 */
	@Test
	public void testHtmlDebugQueryForm() throws IOException{
		String result = target("debug").queryParam("format", MediaType.TEXT_HTML).request().get(String.class);
		Assert.assertThat(result, containsString("<form"));
	}
	
	/**
	 * Test method for {@link de.tud.plt.r43ples.webservice.Debug#getDebugQuery(java.lang.String)}.
	 */
	@Test
	public void testDebug() throws IOException{
		String query = "SELECT * WHERE { GRAPH ?g { ?s ?p ?o. } }";
		String result = target("debug").queryParam("query", URLEncoder.encode(query, "UTF-8")).request().get(String.class);
		Assert.assertThat(result, containsString("http://eatld.et.tu-dresden.de/r43ples-revisions"));
	}
	

}
