package de.tud.plt.r43ples.examples;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;

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
	public static void main(String[] args) throws IOException, ConfigurationException, HttpException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_update, Config.sparql_user, Config.sparql_password);
				
		SampleDataSet.createSampleDataSetMergingClasses(graphName);
	}
	
}