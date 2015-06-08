package de.tud.plt.r43ples.client;

import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;

public class PreparationDBpediaTest {

	private static Logger logger = Logger
			.getLogger(PreparationDBpediaTest.class);

	public static void main(String[] args) throws ConfigurationException, InternalErrorException {
		logger.info("Create DBpedia dataset");
		
		String path = args[0];
		
		String graph_name = "http://dbpedia.org";
		
		Config.readConfig("r43ples.stardog.dbpedia.conf");

		// load instance data to triplestore (dbpedia_2013_07_18.nt)
		String file_name = "dbpedia_2013_07_18.nt";
		//String command = String.format("ld_dir('%s', '%s', '%s');%n", path, file_name, graph_name);
		String command = String.format("stardog data add dbpedia -v --named-graph %s %s/%s.nt %n", graph_name, path, file_name);
		
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
			//command += String.format("ld_dir('%s', '%06d.added.nt', '%s');%n", path, i, addSetGraphUri );
			//command += String.format("ld_dir('%s', '%06d.removed.nt', '%s');%n", path, i, removeSetGraphUri );
			command += String.format("stardog data add dbpedia -v --named-graph %s %s%06d.added.nt %n", addSetGraphUri, path, i);
			command += String.format("stardog data add dbpedia -v --named-graph %s %s%06d.removed.nt %n", removeSetGraphUri, path, i);			
			
//mgraube@mgraube-Studio-XPS-1645:/media/mgraube/d2c5807d-cfc7-4cb4-afb0-937ff268bc5e/dbpedia/2015_06_18$ stardog data add dbpedia -v --named-graph http://dbpedia.org /media/mgraube/d2c5807d-cfc7-4cb4-afb0-937ff268bc5e/dbpedia/2015_06_18/dbpedia_2014.owl.bz2
//Adding data from file: /media/mgraube/d2c5807d-cfc7-4cb4-afb0-937ff268bc5e/dbpedia/2015_06_18/dbpedia_2014.owl.bz2
//Added 27.063 triples in 00:03:45.495
//mgraube@mgraube-Studio-XPS-1645:/media/mgraube/d2c5807d-cfc7-4cb4-afb0-937ff268bc5e/dbpedia/2015_06_18$ stardog data add dbpedia -v --named-graph http://dbpedia.org /media/mgraube/d2c5807d-cfc7-4cb4-afb0-937ff268bc5e/dbpedia/2015_06_18/instance_types_en.nt.bz2 Adding data from file: /media/mgraube/d2c5807d-cfc7-4cb4-afb0-937ff268bc5e/dbpedia/2015_06_18/instance_types_en.nt.bz2
//Added 15.893.902 triples in 00:35:42.311

			RevisionManagement.addMetaInformationForNewRevision(graph_name, user, "create revision " + newRevisionNumber, list,
					newRevisionNumber, addSetGraphUri, removeSetGraphUri);	
		}
		
//		command += "select * from DB.DBA.load_list;\n"
//				+ "rdf_loader_run();";
		logger.info("Command to be executed on ISQL interface in Virtuoso:\n" + command);
	}

}
