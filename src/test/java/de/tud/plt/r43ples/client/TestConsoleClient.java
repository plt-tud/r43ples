package de.tud.plt.r43ples.client;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tud.plt.r43ples.client.ConsoleClient;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionGraph;

public class TestConsoleClient {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testMainHelp() throws ConfigurationException, IOException, InternalErrorException {
		ConsoleClient.main("--help".split(" "));
	}
	
	@Test
	public final void testMain() throws ConfigurationException, IOException, InternalErrorException {
		ConsoleClient.main("--new --graph http://test.com".split(" "));
		RevisionGraph graph = new RevisionGraph("http://test.com");
		String reference = graph.getReferenceGraph("master");
		Assert.assertEquals("http://test.com", reference);
	}

}
