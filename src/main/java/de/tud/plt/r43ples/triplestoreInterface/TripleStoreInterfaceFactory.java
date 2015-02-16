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
		if (Config.database_directory != null)
			triplestore = createJenaTDBInterface(Config.database_directory);
		else
			triplestore = createVirtuosoInterface(Config.database_directory);
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
	
	public static TripleStoreInterface createVirtuosoInterface(String link) {
		if (triplestore!=null) {
			triplestore = new VirtuosoInterface();
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
