package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

import com.hp.hpl.jena.query.Dataset;

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
	/** The HTTP server. **/
	private static HttpServer server;
	/** The TDB dataset. **/
	public static Dataset dataset;
	
	
	/**
	 * Starts the server.
	 * 
	 * @param args
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws ConfigurationException, IOException, HttpException, URISyntaxException {
		start();
		while(true);
	}

	
	/**
	 * Starts the server. It is possible to enable a secure connection.
	 * 
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws HttpException
	 * @throws URISyntaxException
	 */
	public static void start() throws ConfigurationException, IOException, HttpException, URISyntaxException {
		logger.info("Starting R43ples on grizzly...");
		Config.readConfig("r43ples.conf");
		URI BASE_URI = UriBuilder.fromUri(Config.service_uri).port(Config.service_port).path("r43ples").build();
	
		ResourceConfig rc = new ResourceConfig()
				.registerClasses(Endpoint.class)
				.property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates")
				.register(MustacheMvcFeature.class)
				.register(ExceptionMapper.class);
		server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
		server.getServerConfiguration().addHttpHandler(
		        new CLStaticHttpHandler(Service.class.getClassLoader(),"webapp/"), "/static/");
		server.start();
		logger.info(String.format("Server started - R43ples endpoint available under: %s/sparql", BASE_URI));
		
		logger.info("Version: "+ Service.class.getPackage().getImplementationVersion());
		
		TripleStoreInterface.init(Config.database_directory);//Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
	}
	
	
	/**
	 * Stops the server.
	 */
	public static void stop() {
		server.shutdown();
	}
	
}
