package de.tud.plt.r43ples.management;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	/** The SDG graph URI. **/
	public static String sdg_graph;
	/** The path to the SDG graph default content. **/
	public static String sdg_graph_defaultContent;
	/** Structural Definition Group within the Named Graph (sdg.graph) which should be associated with new graphs under revision control (mmo:hasDefaultSDG). **/
	public static String sdg_graph_defaultSDG;

	/** The rules graph URI. **/
	public static String rules_graph;
	/** The path to the rules graph default content. **/
	public static String rules_graph_defaultContent;

	public static HashMap<String, String> user_defined_prefixes = new HashMap<String, String>();
	
	
	/** The logger. **/
	private static Logger logger = LogManager.getLogger(Config.class);
	
	
	/**
	* Read the configuration information from local file.
	*
	* @param configFilePath path to configuration file
	* @throws ConfigurationException
	*/
	public static void readConfig(final String configFilePath) {
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
			
			sdg_graph = config.getString("sdg.graph");
			sdg_graph_defaultContent = config.getString("sdg.graph.defaultContent");
			sdg_graph_defaultSDG = config.getString("sdg.graph.defaultSDG");

			rules_graph = config.getString("rules.graph");
			rules_graph_defaultContent = config.getString("rules.graph.defaultContent");
			
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
	
	public static String getUserDefinedSparqlPrefixes(){
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

	//TODO spilt into RMO and others
	/** The SPARQL prefixes **/
	public static final String prefixes = 
			  "PREFIX rmo:	<http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms:	<http://purl.org/dc/terms/> \n"
			+ "PREFIX xsd:	<http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX mmo: <http://eatld.et.tu-dresden.de/mmo#> \n"
			//+ "PREFIX sdd:	<http://eatld.et.tu-dresden.de/sdd#> \n"
			//+ "PREFIX rpo: <http://eatld.et.tu-dresden.de/rpo#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"
			+ "PREFIX owl:	<http://www.w3.org/2002/07/owl#> \n"
			+ "PREFIX aero: <http://eatld.et.tu-dresden.de/aero#> \n"
			+ "PREFIX rules: <http://eatld.et.tu-dresden.de/rules#> \n"
			+ "PREFIX sp: <http://spinrdf.org/sp#> \n";
			//+ "PREFIX spin: <http://spinrdf.org/spin#> \n"; // Currently not used within queries

}
