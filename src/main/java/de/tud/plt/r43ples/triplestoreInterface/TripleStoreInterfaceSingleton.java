package de.tud.plt.r43ples.triplestoreInterface;

import de.tud.plt.r43ples.management.Config;
import org.apache.log4j.Logger;

/**
 * Singleton for getting a TripleStore object
 * 
 * @author Markus Graube
 *
 */
public class TripleStoreInterfaceSingleton {
	
	private static TripleStoreInterface triplestore;
	/** The logger */
	private static Logger logger = Logger.getLogger(TripleStoreInterfaceSingleton.class);


	/** Returns interface according to Config
	 * can be a Jena TDB Interface, a Virtuoso interface or a HTTP interface
	 *
	 * @return triplestore
	 */
	public static TripleStoreInterface get() {
		if (triplestore!=null)
			return triplestore;
		else {
			logger.debug("Establishing connection to triplestore (type: " + Config.triplestore_type + " - url: " + Config.triplestore_url + ")");
			if (Config.triplestore_type.equals("tdb"))
				triplestore = new JenaTDBInterface(Config.triplestore_url);
			else if (Config.triplestore_type.equals("virtuoso"))
				triplestore = new VirtuosoInterface(Config.triplestore_url, Config.triplestore_user, Config.triplestore_password);
			else if (Config.triplestore_type.equals("http"))
				triplestore = new HttpInterface(Config.triplestore_url, Config.triplestore_user, Config.triplestore_password);
			else {
				logger.error("No triplestore specified in config");
				System.exit(1);
			}
			triplestore.init();
			return triplestore;
		}
	}

	/**
	 * Closes the triplestore
	 */
	public static void close(){
		if (triplestore!=null) {
			triplestore.close();
			triplestore = null;
		}
	}

}
