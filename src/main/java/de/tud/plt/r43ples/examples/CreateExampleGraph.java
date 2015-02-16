package de.tud.plt.r43ples.examples;

import org.apache.commons.configuration.ConfigurationException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceFactory;


public class CreateExampleGraph {

	/** The graph name. **/
	private static String graphName = "http://exampleGraph";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws ConfigurationException 
	 * @throws InternalErrorException 
	 */
	public static void main(String[] args) throws ConfigurationException, InternalErrorException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterfaceFactory.createInterface();
		
		SampleDataSet.createSampleDataSetMerging(graphName);
	}
}