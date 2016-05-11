package de.tud.plt.r43ples.management;

import static org.junit.Assert.*;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;


public class ConfigTest {

	@Test
	public final void testReadConfig() throws ConfigurationException {
		Config.readConfig("r43ples.test.conf");
		assertEquals("database/test", Config.triplestore_url);
	}

	@Test
	public final void testGetPrefixes() throws ConfigurationException {
		Config.readConfig("r43ples.test.conf");
		String prefixes = Config.getPrefixes();
		assertEquals("PREFIX ex: <http://example.com/> \nPREFIX test: <http://test.com/> \n", prefixes);
		
	}

}
