package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.TripleStoreInterface;


/**
 * R43ples Web Service.
 * Main Class starting the web server on grizzly.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class Service {

	/** The logger */
	private static Logger logger = Logger.getLogger(Service.class);
	
	
	public static void main(String[] args) throws ConfigurationException, IOException, AuthenticationException {
		
		logger.info("Starting R43ples on grizzly...");
		Config.readConfig("Service.conf");
		URI BASE_URI = UriBuilder.fromUri(Config.service_uri).port(Config.service_port).build();
		ResourceConfig rc = new PackagesResourceConfig("de.tud.plt.r43ples.webservice");
		GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
		logger.info(String.format("Server started: %s", BASE_URI));
		logger.info(String.format("HTML site available under: %sr43ples/sparql", BASE_URI));
		
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		
		while(true);
	}
	
}
