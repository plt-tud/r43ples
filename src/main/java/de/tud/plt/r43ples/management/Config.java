package de.tud.plt.r43ples.management;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
* General configuration handling.
* Stores all important configuration from R43ples.
* Can read from configuration file.
*
* @author Markus Graube
* @author Stephan Hensel
*
*/
public class Config {

	// Database settings
	
	// tdb, virtuoso, http_virtuoso, http
	public static String triplestore_type;
	public static String triplestore_url;
	public static String triplestore_user;
	public static String triplestore_password;
	
	
	// Service settings
	/** The service host. **/
	public static String service_host;
	/** The service port. **/
	public static int service_port;
	/** The service path. **/
	public static String service_path;

	// SSL settings
	/** The SSL keystore path. **/
	public static String ssl_keystore;
	/** The SSL keystore password. **/
	public static String ssl_password;
	
	// Internal r43ples settings
	/** The r43ples revision graph **/
	public static String revision_graph;
	/** The SDD graph URI. **/
	public static String sdd_graph;
	/** The path to the SDD graph default content. **/
	public static String sdd_graph_defaultContent;
	
	
	public static HashMap<String, String> user_defined_prefixes = new HashMap<String, String>();
	
	
	/** The logger. **/
	private static Logger logger = Logger.getLogger(Config.class);	
	
	
	/**
	* Read the configuration information from local file.
	*
	* @param configFilePath path to configuration file
	* @throws ConfigurationException
	*/
	public static void readConfig(final String configFilePath) throws ConfigurationException{
		PropertiesConfiguration config;
		try {
			config = new PropertiesConfiguration(configFilePath);
			
			triplestore_type = config.getString("triplestore.type");
			triplestore_url = config.getString("triplestore.url");
			triplestore_user = config.getString("triplestore.user");
			triplestore_password = config.getString("triplestore.password");
			
			service_host = config.getString("service.host");
			service_port = config.getInt("service.port");
			service_path = config.getString("service.path");
			
			ssl_keystore = config.getString("ssl.keystore");
			ssl_password = config.getString("ssl.password");
			
			revision_graph = config.getString("revision.graph");
			
			sdd_graph = config.getString("sdd.graph");
			sdd_graph_defaultContent = config.getString("sdd.graph.defaultContent");
			
			Iterator<String> it = config.getKeys("prefix");
			while ( it.hasNext()) {
				String prefix = it.next();
				String namespace = config.getString(prefix);
				prefix = prefix.replace("prefix.", "");
				user_defined_prefixes.put(prefix, namespace);
			}

		} catch (ConfigurationException e) {
			logger.warn("Could not read configuration file '" + configFilePath + "'. Switch to 'r43ples.dist.conf'.");
			readConfig("r43ples.dist.conf");
		}
	}
	
	public static String getPrefixes(){
		StringBuilder sb = new StringBuilder();
		Set<String> set = user_defined_prefixes.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()){
			String prefix = it.next();
			String namespace = user_defined_prefixes.get(prefix);
			sb.append("PREFIX "+prefix+": <"+namespace+"> \n");
		}
		return sb.toString();
	}

}
