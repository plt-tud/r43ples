package de.tud.plt.r43ples.examples;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;


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
	 * @throws IOException 
	 * @throws ConfigurationException 
	 * @throws HttpException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationException, HttpException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.database_directory);
		
		SampleDataSet.createSampleDataSetDA(graphName);
	}
}