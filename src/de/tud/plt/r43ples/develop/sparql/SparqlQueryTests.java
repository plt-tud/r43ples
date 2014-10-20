package de.tud.plt.r43ples.develop.sparql;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.develop.examples.CreateExampleGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.TripleStoreInterface;

/**
 * Contains SPARQL queries which will be used later. 
 * 
 * @author Stephan Hensel
 *
 */
public class SparqlQueryTests {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraph.class);

	
	/**
	 * Main entry point. Execute some tests.
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ConfigurationException 
	 */
	public static void main(String[] args) throws IOException, HttpException, ConfigurationException {
		
		// Start service
		//Service.main(null);
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		
		logger.info("Common revision: \n" + MergeManagement.getCommonRevisionWithShortestPath("exampleGraph-revision-1.0-1", "exampleGraph-revision-1.1-1"));
		
		logger.info("Common revision: \n" + MergeManagement.getCommonRevisionWithShortestPath("exampleGraph-revision-1", "exampleGraph-revision-1.1-1"));
		
		MergeManagement.createRevisionProgress(MergeManagement.getPathBetweenStartAndTargetRevision("exampleGraph-revision-1", "exampleGraph-revision-1.0-1"), "RM-REVISION-PROGRESS-A-exampleGraph", "http://example/branch-A");
		MergeManagement.createRevisionProgress(MergeManagement.getPathBetweenStartAndTargetRevision("exampleGraph-revision-1", "exampleGraph-revision-1.1-1"), "RM-REVISION-PROGRESS-B-exampleGraph", "http://example/branch-B");
	}
	
	
}
