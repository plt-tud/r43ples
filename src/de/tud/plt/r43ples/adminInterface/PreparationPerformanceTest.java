package de.tud.plt.r43ples.adminInterface;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;

public class PreparationPerformanceTest {

	private static Logger logger = Logger
			.getLogger(PreparationPerformanceTest.class);

	public static void main(String[] args) throws IOException, HttpException, ConfigurationException {
		logger.info("Prepare jMeter performance test.");
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		int[] changesizes = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
		int[] datasizes = { 100, 1000, 10000, 100000, 1000000 };
		final int REVISIONS = 20;
		String path = args[0];

		for (int j = 0; j < datasizes.length; j++) {
			int datasize = datasizes[j];
			for (int i = 0; i < changesizes.length; i++) {
				int changesize = changesizes[i];
				String graphName = "dataset-" + datasize + "-" + changesize;
				TripleStoreInterface.executeQueryWithAuthorization(
						"DROP SILENT GRAPH <" + graphName + ">", "HTML");
				String pathName = path + "/dataset-" + datasize + ".nt";
				String dataSetAsNTriples = FileUtils.readFileToString(new File(pathName));
				RevisionManagement.putGraphUnderVersionControl(graphName);
				ArrayList<String> list = new ArrayList<String>();
				list.add("0");
				RevisionManagement.createNewRevision(graphName,
						dataSetAsNTriples, "", "test", "test creation", list);
				for (int revision = 1; revision <= REVISIONS; revision++) {
					pathName = path + "/addset-"+ changesize + "-" + revision + ".nt";
					String addedAsNTriples = FileUtils.readFileToString(new File(pathName));
					list = new ArrayList<>();
					list.add(Integer.toString(revision));
					RevisionManagement.createNewRevision(graphName,
							addedAsNTriples, "", "test", "test creation", list);
				}
			}
		}
	}

}
