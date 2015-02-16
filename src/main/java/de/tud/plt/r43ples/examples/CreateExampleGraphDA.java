package de.tud.plt.r43ples.examples;

import java.io.UnsupportedEncodingException;

import org.apache.commons.configuration.ConfigurationException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceFactory;


/**
 * Create an example graph.
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraphDA {

	/** The graph name. **/
	private static String graphName = "http://exampleGraph";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * Used in diploma thesis of Stephan Hensel.
	 * 
	 * @param args
	 * @throws ConfigurationException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws ConfigurationException, UnsupportedEncodingException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterfaceFactory.createInterface();
		
		SampleDataSet.createSampleDataSetDA(graphName);
	}
}