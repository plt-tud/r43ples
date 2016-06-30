package de.tud.plt.r43ples;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import de.tud.plt.r43ples.client.R43plesArgs;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import de.tud.plt.r43ples.webservice.API;
import de.tud.plt.r43ples.webservice.Configuration;
import de.tud.plt.r43ples.webservice.Debug;
import de.tud.plt.r43ples.webservice.Endpoint;
import de.tud.plt.r43ples.webservice.ExceptionMapper;
import de.tud.plt.r43ples.webservice.Merging;
import de.tud.plt.r43ples.webservice.Misc;


/**
 * R43ples Web Service.
 * Main Class starting the web server on grizzly.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class R43plesService {

	/** The logger */
	private static Logger logger = Logger.getLogger(R43plesService.class);
	/** The HTTP server. **/
	private static HttpServer server;
	
	
	/**
	 * Starts the server.
	 * 
	 * @param args
	 * @throws ConfigurationException
	 * @throws URISyntaxException
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		
		
		// command-line-parser: JCommander-1.29
		R43plesArgs jci = new R43plesArgs();
		JCommander jc = new JCommander(jci);

		// wrong input => display usage
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			jc.usage();
			System.err.println(e.toString());
			System.exit(0);
		}
		
		if (jci.help) {
			jc.usage();
			System.exit(0);
		}
		
		logger.debug("config: " + jci.config);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { R43plesService.stop(); }
		});
		try {
			Config.readConfig(jci.config);
			start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		logger.info("Press ctrl+c to quit the server");
		while (true);		
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
			.registerClasses(Endpoint.class, 
					Misc.class,
					API.class, 
					Configuration.class, 
					Debug.class, 
					Merging.class)
			.property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates")
			.register(MustacheMvcFeature.class)
			.register(ExceptionMapper.class)
			.register(JacksonFeature.class);
		
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
		
		for (NetworkListener l : server.getListeners()) { l.getFileCache().setEnabled(false); }
		logger.info("File cache disabled");
		
		server.getServerConfiguration().addHttpHandler(
		        new CLStaticHttpHandler(R43plesService.class.getClassLoader(),"webapp/"), "/static/");

		server.start();

		logger.info(String.format("Server started - R43ples endpoint available under: %s/sparql", BASE_URI));
		
		String version = R43plesService.class.getPackage().getImplementationVersion();
		if (version==null){
			try{
				version = "Commit: " +GitRepositoryState.getGitRepositoryState().commitIdAbbrev;
			}
			catch(Exception e){
                version="No version information available";
            }
		}
		logger.info("Version: "+ version);
	}
	
	
	/**
	 * Stops the server.
	 */
	public static void stop() {
		logger.info("Server shutdown ...");
		TripleStoreInterfaceSingleton.close();
		server.shutdown();
	}
	
}
