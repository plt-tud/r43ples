package de.tud.plt.r43ples.merging.management;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Reads and stores the configuration parameters.
 * 
 * @author Stephan Hensel
 *
 */
public class ClientConfig {
	
	/** The properties configuration. **/
	private static PropertiesConfiguration config;
	/** The R43ples SPARQL endpoint. **/
	public static String r43ples_sparql_endpoint;
	/** The R43ples JSON HTTP-GET interface to get all revised graphs. **/
	public static String r43ples_json_revisedgraphs;
	/** The R43ples revision graph. **/
	public static String r43ples_revision_graph;
	/** The R43ples SDD graph. **/
	public static String r43ples_sdd_graph;
	/** The client prefix mappings. (key: mapping, value: prefix) **/
	public static HashMap<String, String> prefixMappings = new HashMap<String, String>();
	
	
	/**
	 * Read the configuration information from local file.
	 * 
	 * @param configFilePath path to configuration file
	 * @throws ConfigurationException
	 */
	public static void readConfig(final String configFilePath) throws ConfigurationException{
		config = new PropertiesConfiguration(configFilePath);
		r43ples_sparql_endpoint = config.getString("r43ples.sparql.endpoint");
		r43ples_json_revisedgraphs = config.getString("r43ples.json.revisedgraphs");
		r43ples_revision_graph = config.getString("r43ples.revision.graph");
		r43ples_sdd_graph = config.getString("r43ples.sdd.graph");
		String[] prefixMappingsArray = config.getStringArray("client.prefixmapping");
		// Generate hash map of prefix mappings
		prefixMappings.clear();
		for (int i = 0; i < prefixMappingsArray.length; i++) {
			String mapping = prefixMappingsArray[i];
			String[] parts = mapping.split("\\|");
			prefixMappings.put(parts[1], parts[0]);
		}
	}
	
	
	/**
	 * Write the configuration information to local file.
	 * 
	 * @param configFilePath path to configuration file
	 * @throws ConfigurationException
	 */
	public static void writeConfig(final String configFilePath) throws ConfigurationException {
		config.setProperty("r43ples.sparql.endpoint", r43ples_sparql_endpoint);
		config.setProperty("r43ples.json.revisedgraphs", r43ples_json_revisedgraphs);
		config.setProperty("r43ples.revision.graph", r43ples_revision_graph);
		config.setProperty("r43ples.sdd.graph", r43ples_sdd_graph);
		// Generate array of prefix mappings
		String[] prefixMappingsArray = new String[prefixMappings.size()];
		int index = 0;
		Iterator<String> itePrefixMappingsKeys = prefixMappings.keySet().iterator();
		while (itePrefixMappingsKeys.hasNext()) {
			String currentKey = itePrefixMappingsKeys.next();
			String currentValue = prefixMappings.get(currentKey);
			prefixMappingsArray[index] = currentValue + "|" + currentKey;
			index++;			
		}
		config.clearProperty("client.prefixmapping");
		config.setProperty("client.prefixmapping", prefixMappingsArray);
		config.save(new File(configFilePath));
	}

}