package de.tud.plt.r43ples.develop.examples;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.SampleDataSet;
import de.tud.plt.r43ples.management.TripleStoreInterface;

/**
 * Create an example graph of the following structure:
 * 
 *                  ADD: 2D               ADD: 1G
 *               +-----X---------------------X--------- (Branch B1)
 *               |  DEL: 1A               DEL: 2D
 * ADD: 1A,1B,2C |
 * ---X----------+ (Master)
 * DEL: -        |
 *               |  ADD: 2D,2H            ADD: 2I
 *               +-----X---------------------X--------- (Branch B2)
 *                  DEL: 2C               DEL: -
 * 
 * Contains the renaming of 1A to 1G.
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraphRenaming {

	/** The graph name. **/
	private static String graphName = "http://exampleGraphRenaming";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ConfigurationException, HttpException {
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
	
		SampleDataSet.createSampleDataSetRenaming(graphName);
	}

}
