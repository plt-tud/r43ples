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
			logger.info("Establishing connection to triplestore");
			logger.info("type: " + Config.triplestore_type);
			logger.info("url: " + Config.triplestore_url);
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

	public static void close(){
		if (triplestore!=null) {
			triplestore.close();
			triplestore = null;
		}
	}

}
