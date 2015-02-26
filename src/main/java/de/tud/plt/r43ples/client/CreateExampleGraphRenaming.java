package de.tud.plt.r43ples.client;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;


public class CreateExampleGraphRenaming {

	/** The graph name. **/
	private static String graphName = "http://exampleGraphRenaming";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws ConfigurationException
	 * @throws InternalErrorException 
	 */
	public static void main(String[] args) throws ConfigurationException, IOException, InternalErrorException {
		
		Config.readConfig("r43ples.conf");
	
		SampleDataSet.createSampleDataSetRenaming(graphName);
	}
}
