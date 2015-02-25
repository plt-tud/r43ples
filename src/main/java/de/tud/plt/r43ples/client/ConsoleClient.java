package de.tud.plt.r43ples.client;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class ConsoleClient {

	private static Logger logger = Logger.getLogger(ConsoleClient.class);

	public static void main(String[] args) throws ConfigurationException, IOException, InternalErrorException {
		
		
		// command-line-parser: JCommander-1.29
		ConsoleClientArgs args_client = new ConsoleClientArgs();
		JCommander jc = new JCommander(args_client );

		// wrong input => display usage
		try {
			jc.setProgramName("de.tud.plt.r43ples.client.ConsoleClient");
			jc.parse(args);
		} catch (ParameterException e) {
			jc.usage();
			System.err.println(e.toString());
			System.exit(0);
		}
		
		if (args_client.r43ples.help) {
			jc.usage();
			System.exit(0);
		}
		
		logger.info("config: " + args_client.r43ples.config);
		logger.info("graph: " + args_client.graph);
		logger.info("create: " + args_client.create);
		logger.info("add set file: " + args_client.add_set);
		logger.info("delete set file: " +  args_client.delete_set);
		
		Config.readConfig(args_client.r43ples.config);
		
		
		if (args_client.create) {
			TripleStoreInterfaceSingleton.get().executeCreateGraph(args_client.graph);
			RevisionManagement.putGraphUnderVersionControl(args_client.graph);
		}
		
		String addSet = readFile(args_client.add_set);
		String deleteSet = readFile(args_client.delete_set);
				

		String user = "test";
		
		String master = RevisionManagement.getMasterRevisionNumber(args_client.graph);
		String result = RevisionManagement.createNewRevision(args_client.graph, addSet, deleteSet, user, "automatic commit", master);
		logger.info(result);			
	}

	/**
	 * @param filename
	 * @return 
	 * @throws IOException
	 */
	private static String readFile(String filename) throws IOException {
		if (filename != null) {
			File inputFile = new File(filename);
	
			if (!inputFile.canRead() || !inputFile.isFile()) {
				System.err
						.println("Wrong input file: There was no file found at "
								+ filename + "!");
				System.exit(0);
			}
			return FileUtils.readFileToString(inputFile);
		} else
			return "";
	}

}
