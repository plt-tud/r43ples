package de.tud.plt.r43ples.examples;

import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceFactory;

public class PreparationDBpediaTest {

	private static Logger logger = Logger
			.getLogger(PreparationDBpediaTest.class);

	public static void main(String[] args) throws ConfigurationException, InternalErrorException {
		logger.info("Create DBpedia dataset");
		
		String path = args[0];
		
		String graph_name = "http://dbpedia.org";
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterfaceFactory.createInterface();

		// load instance data to triplestore (dbpedia_2013_07_18.nt)
		String file_name = "dbpedia_2013_07_18.nt";
		String command = String.format("ld_dir('%s', '%s', '%s');%n", path, file_name, graph_name);

		// create revision information for instance data
		RevisionManagement.putGraphUnderVersionControl(graph_name);
		
		String user = "dbpedia-test";
		
		// insert changesets into R43ples
		final int MAX_REV = 277;
		for (int i=0; i <= MAX_REV; i++){
			logger.info("Add revision to dbpedia: "+i);
			String newRevisionNumber = String.format("%d",i +1);
			String addSetGraphUri = graph_name + "-delta-added-" + newRevisionNumber;
			String removeSetGraphUri = graph_name + "-delta-removed-" + newRevisionNumber;
			ArrayList<String> list = new ArrayList<String>();
			list.add(Integer.toString(i));
			command += String.format("ld_dir('%s', '%06d.added.nt', '%s');%n", path, i, addSetGraphUri );
			command += String.format("ld_dir('%s', '%06d.removed.nt', '%s');%n", path, i, removeSetGraphUri );
			
			RevisionManagement.addMetaInformationForNewRevision(graph_name, user, "create revision " + newRevisionNumber, list,
					newRevisionNumber, addSetGraphUri, removeSetGraphUri);	
		}
		
		command += "select * from DB.DBA.load_list;\n"
				+ "rdf_loader_run();";
		logger.info("Command to be executed on ISQL interface in Virtuoso:\n" + command);
	}

}
