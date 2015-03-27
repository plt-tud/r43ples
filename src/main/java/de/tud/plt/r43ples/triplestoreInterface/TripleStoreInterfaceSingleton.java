package de.tud.plt.r43ples.triplestoreInterface;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.Config;

/**
 * 
 * @author Markus Graube
 * @navassoc 1 - 1 TripleStoreInterface
 *
 */
public class TripleStoreInterfaceSingleton {
	
	private static TripleStoreInterface triplestore;
	/** The logger */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	
	/** Create interface according to Config
	 * can be a Jena TDB Interface or a Virtuoso interface
	 * 
	 * @return triplestoreinterface
	 */
	public static TripleStoreInterface get() {
		if (triplestore!=null)
			return triplestore;
		else {
			if (Config.jena_tdb_directory != null)
				triplestore = createJenaTDBInterface(Config.jena_tdb_directory);
			else if (Config.virtuoso_url != null)
				triplestore = createVirtuosoHttpInterface(Config.virtuoso_url, Config.virtuoso_user, Config.virtuoso_password);
			else {
				logger.error("No database specified in config");
				System.exit(1);
			}
			triplestore.init();
			return triplestore;
		}
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
			triplestore = new HttpInterface(virtuoso_url, virtuoso_user, virtuoso_password);
			return triplestore;
		}
		else
			return null;
	}
	

	public static void close(){
		if (triplestore!=null) {
			triplestore.close();
			triplestore = null;
		}
	}

}
