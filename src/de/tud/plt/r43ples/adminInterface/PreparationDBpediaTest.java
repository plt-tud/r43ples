package de.tud.plt.r43ples.adminInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.TripleStoreInterface;

public class PreparationDBpediaTest {

	private static Logger logger = Logger
			.getLogger(PreparationDBpediaTest.class);

	public static void main(String[] args) throws IOException, HttpException, ConfigurationException {
		logger.info("Create DBpedia dataset");
		
		String path = args[0];
		
		String graph_name = "http://dbpedia.org";
		
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);

		// load instance data to triplestore (dbpedia_2013_07_18.nt)
		String file_name = "dbpedia_2013_07_18.nt";
		String command = String.format(""
				+ "ld_dir('%s', '%s', '%s');%n"
				+ "select * from DB.DBA.load_list;%n"
				+ "rdf_loader_run();",
				path, file_name, graph_name);
		logger.info("Command to be executed on ISQL interface in Virtuoso:\n" + command);
		// create revision information for instance data
//		RevisionManagement.putGraphUnderVersionControl(graph_name);
		
		String path_name_added;
		String path_name_removed;
		String addedAsNTriples;
		String removedAsNTriples;
		
		// insert changesets into R43ples
		final int MAX_REV = 277;
		for (int i=0; i <= 3; i++){
//		for (int i=0; i <= MAX_REV; i++){
			path_name_added = String.format("%s/%06d.added.nt", path, i);
			try {
				addedAsNTriples = FileUtils.readFileToString(new File(path_name_added));
			} catch (FileNotFoundException e)
			{
				addedAsNTriples = "";
			}
			
			path_name_removed = String.format("%s/%06d.removed.nt", path, i);
			try {
				removedAsNTriples = FileUtils.readFileToString(new File(path_name_removed));
			}
			catch (FileNotFoundException e) {
				removedAsNTriples = "";
			}
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(Integer.toString(i));
			
//			RevisionManagement.createNewRevision(graph_name, addedAsNTriples, removedAsNTriples, "test-user", "create revision " + i, list);	
		}
	}

}
