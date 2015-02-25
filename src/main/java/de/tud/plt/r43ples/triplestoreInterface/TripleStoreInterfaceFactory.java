package de.tud.plt.r43ples.triplestoreInterface;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.MergeManagement;
import de.tud.plt.r43ples.management.RevisionManagement;

public class TripleStoreInterfaceFactory {
	
	private static TripleStoreInterface triplestore;
	/** The logger */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	
	public static TripleStoreInterface get() {
		return triplestore;
	}
	
	/** Create interface according to Config
	 * can be a Jena TDB Interface or a Virtuoso interface
	 * 
	 * @return triplestoreinterface
	 */
	public static TripleStoreInterface createInterface() {
		if (Config.jena_tdb_directory != null)
			triplestore = createJenaTDBInterface(Config.jena_tdb_directory);
		else if (Config.virtuoso_url != null)
			triplestore = createVirtuosoHttpInterface(Config.virtuoso_url, Config.virtuoso_user, Config.virtuoso_password);
		else {
			logger.error("No database specified in config");
			System.exit(1);
		}
		init();
		return triplestore;
	}

	
	public static TripleStoreInterface createJenaTDBInterface(String databaseDirectory) {
		if (triplestore==null) {
			triplestore = new JenaTDBInterface(databaseDirectory);
			return triplestore;
		}
		else
			return triplestore;
	}
	
	public static TripleStoreInterface createVirtuosoInterface(String virtuoso_url, String virtuoso_user, String virtuoso_password) {
		if (triplestore==null) {
			triplestore = new VirtuosoInterface(virtuoso_url, virtuoso_user, virtuoso_password);
			return triplestore;
		}
		else
			return null;
	}
	
	public static TripleStoreInterface createVirtuosoHttpInterface(String virtuoso_url, String virtuoso_user, String virtuoso_password) {
		if (triplestore==null) {
			triplestore = new VirtuosoHttpInterface(virtuoso_url, virtuoso_user, virtuoso_password);
			return triplestore;
		}
		else
			return null;
	}
	
	
	private static void init() {
		if (!RevisionManagement.checkGraphExistence(Config.revision_graph)){
			logger.info("Create revision graph");
			triplestore.executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
	 	}
		
		// Create SDD graph
		if (!RevisionManagement.checkGraphExistence(Config.sdd_graph)){
			logger.info("Create sdd graph");
			triplestore.executeUpdateQuery("CREATE SILENT GRAPH <" + Config.revision_graph +">");
			// Insert default content into SDD graph
			RevisionManagement.executeINSERT(Config.sdd_graph, MergeManagement.convertJenaModelToNTriple(MergeManagement.readTurtleFileToJenaModel(Config.sdd_graph_defaultContent)));
	 	}		
	}

	public static void close(){
		triplestore.close();
		triplestore = null;
	}

}
