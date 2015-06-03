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
	private static Logger logger = Logger.getLogger(TripleStoreInterfaceSingleton.class);
	
	
	/** Create interface according to Config
	 * can be a Jena TDB Interface, a Virtuoso interface or a HTTP interface
	 * 
	 * @return triplestoreinterface
	 */
	public static TripleStoreInterface get() {
		if (triplestore!=null)
			return triplestore;
		else {
			if (Config.triplestore_type.equals("tdb"))
				triplestore = createJenaTDBInterface(Config.triplestore_url);
			else if (Config.triplestore_type.equals("virtuoso"))
				triplestore = createVirtuosoInterface(Config.triplestore_url, Config.triplestore_user, Config.triplestore_password);
			else if (Config.triplestore_type.equals("http"))
				triplestore = createHttpInterface(Config.triplestore_url, Config.triplestore_user, Config.triplestore_password);
			else if (Config.triplestore_type.equals("http_virtuoso"))
				triplestore = createVirtuosoHttpInterface(Config.triplestore_url, Config.triplestore_user, Config.triplestore_password);
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
	
	public static TripleStoreInterface createVirtuosoHttpInterface(String http_url, String http_user, String http_password) {
		if (triplestore==null) {
			triplestore = new VirtuosoHttpInterface(http_url, http_user, http_password);
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
