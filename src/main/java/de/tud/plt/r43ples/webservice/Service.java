package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

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
	
	public static void main(String[] args) throws ConfigurationException, IOException, HttpException, URISyntaxException {
		start();
		while(true);
	}
	
	public static void start()  throws ConfigurationException, IOException, HttpException, URISyntaxException {
		logger.info("Starting R43ples on grizzly...");
		Config.readConfig("r43ples.conf");
		URI BASE_URI = new URI(Config.service_uri);
	
		ResourceConfig rc = new ResourceConfig()
				.registerClasses(Endpoint.class)
				.property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates")
				.register(MustacheMvcFeature.class);
		server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
		server.getServerConfiguration().addHttpHandler(
		        new CLStaticHttpHandler(Service.class.getClassLoader(),"webapp/"), "/static/");
		server.start();
		logger.info(String.format("Server started - R43ples endpoint available under: %ssparql", BASE_URI));
		
		logger.info("Version: "+ Service.class.getPackage().getImplementationVersion());
		
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
	}
	
	public static void stop() {
		server.shutdown();
	}
	
}
