package de.tud.plt.r43ples.client;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;

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
	 * @throws ConfigurationException 
	 * @throws InternalErrorException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ConfigurationException, IOException, InternalErrorException {
		
		Config.readConfig("r43ples.conf");
				
		SampleDataSet.createSampleDataSetMergingClasses(graphName);
	}
	
}