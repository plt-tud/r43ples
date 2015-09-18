package de.tud.plt.r43ples.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;


import de.tud.plt.r43ples.management.GitRepositoryState;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

@Provider
public class ExceptionMapper implements
		javax.ws.rs.ext.ExceptionMapper<Exception> {
	private static Logger logger = Logger.getLogger(ExceptionMapper.class);

//	@Override
//	public Response toResponse(Exception e) {
//		logger.error(e.getMessage(), e);
//
//		
//		MustacheFactory mf = new DefaultMustacheFactory();
//	    Mustache mustache = mf.compile("templates/error.mustache");
//	    StringWriter sw = new StringWriter();
//	    
//	    Map<String, Object> htmlMap = new HashMap<String, Object>();
//	    htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
//	    htmlMap.put("git", GitRepositoryState.getGitRepositoryState());
//		htmlMap.put("error", e);
//		
//		mustache.execute(sw, htmlMap);
//		String content = sw.toString();
//		
//		return Response.serverError().entity(content).type(MediaType.TEXT_HTML).build();
//	}
	
	@Override
	public Response toResponse(Exception e) {
		logger.error(e.getMessage(), e);
		
		StringWriter sw = new StringWriter();
	    
	    //freemarker template engine
	    freemarker.template.Template temp = null; 
		String name = "error.ftl";
		try {  
            // create the configuration of the template  
            Configuration cfg = new Configuration();  
            // set the path of the template 
            cfg.setClassForTemplateLoading(Endpoint.class, "/templates");
            // get the template page with this name
            temp = cfg.getTemplate(name);  
         } catch (IOException error) {  
            error.printStackTrace();  
         }  
			
	    Map<String, Object> htmlMap = new HashMap<String, Object>();
	    htmlMap.put("version", Endpoint.class.getPackage().getImplementationVersion() );
	    htmlMap.put("gitCommit", GitRepositoryState.getGitRepositoryState().commitIdAbbrev);
		htmlMap.put("gitBranch", GitRepositoryState.getGitRepositoryState().branch);
		htmlMap.put("error", e);
		
		try {
			temp.process(htmlMap,sw);
		} catch (TemplateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String content = sw.toString();
		
		return Response.serverError().entity(content).type(MediaType.TEXT_HTML).build();
	}
	

}
