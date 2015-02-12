package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

import com.hp.hpl.jena.query.Dataset;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.GitRepositoryState;
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
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		try {
			Config.readConfig("r43ples.conf");
			start();
			logger.info("Press enter to quit the server");
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			stop();
		}
	}

	
	/**
	 * Starts the server. It is possible to enable a secure connection.
	 * 
	 * @throws ConfigurationException
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public static void start() throws ConfigurationException, URISyntaxException, IOException {
		logger.info("Starting R43ples on grizzly...");
		URI BASE_URI;
		
		ResourceConfig rc = new ResourceConfig()
			.registerClasses(Endpoint.class)
			.property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates")
			.register(MustacheMvcFeature.class)
			.register(ExceptionMapper.class);
		
		SSLContextConfigurator sslCon =  new SSLContextConfigurator();
		
		// Try to initialize SSL keys
		if (Config.ssl_keystore != null && Config.ssl_password != null) {
			sslCon.setKeyStoreFile(Config.ssl_keystore);
			sslCon.setKeyStorePass(Config.ssl_password);
			sslCon.setTrustStoreFile(Config.ssl_keystore);
			sslCon.setTrustStorePass(Config.ssl_password);
		}
		
		if (sslCon.validateConfiguration()) {
			logger.info("SSL context validated");
			BASE_URI = new URI("https", null, Config.service_host, Config.service_port, Config.service_path, null, null);
			server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc, true, new SSLEngineConfigurator(sslCon, false, false, false));		
		} else {
			logger.info("SSL context not validated");
			BASE_URI = new URI("http", null, Config.service_host, Config.service_port, Config.service_path, null, null);
			server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);			
		}
		
		server.getServerConfiguration().addHttpHandler(
		        new CLStaticHttpHandler(Service.class.getClassLoader(),"webapp/"), "/static/");

		server.start();

		logger.info(String.format("Server started - R43ples endpoint available under: %s/sparql", BASE_URI));
		
		String version = Service.class.getPackage().getImplementationVersion();
		if (version==null){
			version = "Commit: " +GitRepositoryState.getGitRepositoryState().commitIdAbbrev;
		}
		logger.info("Version: "+ version);
		
		TripleStoreInterface.init(Config.database_directory);
	}
	
	
	/**
	 * Stops the server.
	 */
	public static void stop() {
		logger.info("Server shutdown ...");
		TripleStoreInterface.close();
		server.shutdown();
	}
	
}
