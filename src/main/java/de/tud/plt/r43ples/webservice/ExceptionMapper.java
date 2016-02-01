package de.tud.plt.r43ples.webservice;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Provider
public class ExceptionMapper implements
		javax.ws.rs.ext.ExceptionMapper<Throwable> {
	private static Logger logger = Logger.getLogger(ExceptionMapper.class);
	
	@Context
	HttpServletRequest request;
	 
	@Override
	public Response toResponse(Throwable e) {
		logger.error(e.getMessage(), e);
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/error.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> htmlMap = new HashMap<String, Object>();
		htmlMap.put("error", e);
		if (request!=null)
			htmlMap.put("request", request.getMethod() );
		
		mustache.execute(sw, htmlMap);
		String content = sw.toString();
		
		return Response.serverError().entity(content).type(MediaType.TEXT_HTML).build();
	}

}
