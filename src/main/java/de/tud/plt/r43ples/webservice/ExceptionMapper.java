package de.tud.plt.r43ples.webservice;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.GitRepositoryState;

@Provider
public class ExceptionMapper implements
		javax.ws.rs.ext.ExceptionMapper<InternalErrorException> {
	private static Logger logger = Logger.getLogger(ExceptionMapper.class);

	@Override
	public Response toResponse(InternalErrorException e) {
		logger.error(e.getMessage(), e);

		
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/error.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> htmlMap = new HashMap<String, Object>();
	    htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
	    htmlMap.put("git", GitRepositoryState.getGitRepositoryState());
		htmlMap.put("error", e);
		
		mustache.execute(sw, htmlMap);
		String content = sw.toString();
		
		return Response.serverError().entity(content).type(MediaType.TEXT_HTML).build();
	}

}
