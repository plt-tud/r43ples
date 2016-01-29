package de.tud.plt.r43ples.webservice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.mvc.Template;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.GitRepositoryState;

@Path("configuration")
public class Configuration {
	
	@Context UriInfo uriInfo;
	
	private final static Logger logger = Logger.getLogger(Configuration.class);
	
	@GET
	@Template(name = "/configuration.mustache")
	public final Map<String, Object> getConfiguration() throws InternalErrorException {
		logger.info("Get Configuration page");
		Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
		htmlMap.put("git", GitRepositoryState.getGitRepositoryState());
	    htmlMap.put("revisionGraph", Config.revision_graph);
	    htmlMap.put("triplestore_type", Config.triplestore_type);
	    htmlMap.put("triplestore_url", Config.triplestore_url);
	    htmlMap.put("sdd_graph", Config.sdd_graph);
	    htmlMap.put("namespaces", Config.user_defined_prefixes.entrySet());
	    htmlMap.put("configuration_active", true);
		return htmlMap;
	}
	
	@POST
	@Template(name = "/configuration.mustache")
	public final Map<String, Object> addPrefix(
			@FormParam("prefix") final String prefix,
			@FormParam("namespace") final String namespace) throws InternalErrorException {
		if (prefix!=null && namespace!=null){
			Config.user_defined_prefixes.put(prefix, namespace);
		}
		return getConfiguration();
	}
	
	@GET
	@Path("deletePrefix")
	public final Response deletePrefix(
			@QueryParam("prefix") final String prefix) throws InternalErrorException {
		if (prefix!=null && Config.user_defined_prefixes.containsKey(prefix)){
			Config.user_defined_prefixes.remove(prefix);
		}
		URI uri = uriInfo.getBaseUriBuilder().path("configuration").build();
		return Response.seeOther(uri).build();
	}
	

}
