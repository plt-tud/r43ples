package de.tud.plt.r43ples.examples;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceFactory;

/**
 * Create an example graph which contains individuals.
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraphIndividuals {

	/** The graph name. **/
	private static String graphName = "http://exampleGraphIndividuals";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ConfigurationException, IOException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterfaceFactory.createInterface();
				
		SampleDataSet.createSampleDataSetMergingClasses(graphName);
	}
	
}