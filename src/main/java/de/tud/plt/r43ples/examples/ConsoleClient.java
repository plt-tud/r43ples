package de.tud.plt.r43ples.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;

public class ConsoleClient {

	private static Logger logger = Logger
			.getLogger(ConsoleClient.class);

	public static void main(String[] args) throws ConfigurationException, IOException {
		
		
		// command-line-parser: JCommander-1.29
		JCommanderImpl jci = new JCommanderImpl();
		JCommander jc = new JCommander(jci);

		// wrong input => display usage
		try {
			jc.setProgramName("de.tud.plt.r43ples.examples.ConsoleClient");
			jc.parse(args);
		} catch (ParameterException e) {
			jc.usage();
			System.err.println(e.toString());
			System.exit(0);
		}
		
		if (jci.help) {
			jc.usage();
			System.exit(0);
		}
		
		logger.info("graph: " + jci.graph);
		logger.info("create: " + jci.create);
		logger.info("add set file: " + jci.add_set);
		logger.info("delete set file: " +  jci.delete_set);
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.database_directory);
		
		
		if (jci.create) {
			TripleStoreInterface.executeCreateGraph(jci.graph);
			RevisionManagement.putGraphUnderVersionControl(jci.graph);
		}
		
		String addSet = readFile(jci.add_set);
		String deleteSet = readFile(jci.delete_set);
				


		
		String user = "test";
		
		String master = RevisionManagement.getMasterRevisionNumber(jci.graph);
		ArrayList<String> list = new ArrayList<String>();
		list.add(master);
		String result = RevisionManagement.createNewRevision(jci.graph, addSet, deleteSet, user, "automatic commit", list);
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
