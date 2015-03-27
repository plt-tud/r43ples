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
	 * can be a Jena TDB Interface, a Virtuoso interface or a HTTP interface
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
				triplestore = createVirtuosoInterface(Config.virtuoso_url, Config.virtuoso_user, Config.virtuoso_password);
			else if (Config.http_url != null)
				triplestore = createHttpInterface(Config.http_url, Config.http_user, Config.http_password);
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
	
	public static TripleStoreInterface createHttpInterface(String http_url, String http_user, String http_password) {
		if (triplestore==null) {
			triplestore = new HttpInterface(http_url, http_user, http_password);
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
