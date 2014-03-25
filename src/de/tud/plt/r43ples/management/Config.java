package de.tud.plt.r43ples.management;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Config {
	
	public static String service_uri;
	public static int service_port;
	public static String sparql_endpoint;
	public static String sparql_user;
	public static String sparql_password;
	public static String yed_filepath;
	public static String visualisation_path;
	public static String revision_graph;

	
	/**
	 * read the configuration information from local file
	 * @throws ConfigurationException
	 */
	public static void readConfig(String configFilePath) throws ConfigurationException{
		
		PropertiesConfiguration config = new PropertiesConfiguration(configFilePath);
		service_uri = config.getString("service.uri");
		service_port = config.getInt("service.port");
		
		sparql_endpoint = config.getString("sparql.endpoint");
		sparql_user = config.getString("sparql.username");
		sparql_password = config.getString("sparql.password");
		
		revision_graph = config.getString("revision.graph");
		
		yed_filepath = config.getString("yEd.filePath");
		visualisation_path = config.getString("visualisation.path");
	}

}