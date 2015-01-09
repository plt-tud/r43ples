package de.tud.plt.r43ples.management;

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
	public static String database_directory;
	
	// Service settings
	/** The service URI. **/
	public static String service_uri;
	/** The service port. **/
	public static int service_port;
	/** Parameter to secure the service. **/
	public static boolean service_secure;

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
	
	// Visualisation settings
	/** Path to the yEd output file. **/
	public static String yed_filepath;
	/** The visualization file path. **/
	public static String visualisation_path;
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
			
			database_directory = config.getString("database.directory");
			
			service_uri = config.getString("service.uri");
			service_port = config.getInt("service.port");
			service_secure = config.getBoolean("service.secure");
			
			ssl_keystore = config.getString("ssl.keystore");
			ssl_password = config.getString("ssl.password");
			
			revision_graph = config.getString("revision.graph");
			
			sdd_graph = config.getString("sdd.graph");
			sdd_graph_defaultContent = config.getString("sdd.graph.defaultContent");
			
			yed_filepath = config.getString("yEd.filePath");
			visualisation_path = config.getString("visualisation.path");
		} catch (ConfigurationException e) {
			logger.warn("Could not read configuration file '" + configFilePath + "'. Switch to 'r43ples.dist.conf'.");
			readConfig("r43ples.dist.conf");
		}
	}

}
