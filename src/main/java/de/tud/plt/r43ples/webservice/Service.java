package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

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
	private static HttpServer server;
	
	public static void main(String[] args) throws ConfigurationException, IOException, HttpException {
		start();
		while(true);
	}
	
	public static void start()  throws ConfigurationException, IOException, HttpException {
		logger.info("Starting R43ples on grizzly...");
		Config.readConfig("r43ples.conf");
		URI BASE_URI = UriBuilder.fromUri(Config.service_uri).port(Config.service_port).build();
		ResourceConfig rc = new ResourceConfig().registerClasses(Endpoint.class);
		server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
		server.getServerConfiguration().addHttpHandler(
		        new StaticHttpHandler("./src/main/resources/webapp/"), "/static/");
		server.start();
		logger.info(String.format("Server started - R43ples endpoint available under: %sr43ples/sparql", BASE_URI));
		
		logger.info("Version: "+ Service.class.getPackage().getImplementationVersion());
		
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
	}
	
	public static void stop() {
		server.stop();
	}
	
}
