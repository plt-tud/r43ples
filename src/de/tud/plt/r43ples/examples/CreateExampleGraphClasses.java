package de.tud.plt.r43ples.examples;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;

/**
 * Create an example graph which contains classes.
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraphClasses {

	/** The graph name. **/
	private static String graphName = "http://exampleGraphClasses";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationException, HttpException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
				
		SampleDataSet.createSampleDataSetMergingClasses(graphName);
	}
	
}