package de.tud.plt.r43ples.visualisation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.webservice.Endpoint;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;


public class VisualisationD3 {
	
//	public static String getHtmlOutput(String graphName) {
//		MustacheFactory mf = new DefaultMustacheFactory();
//	    Mustache mustache = mf.compile("templates/graphvisualisation_d3.mustache");
//	    StringWriter sw = new StringWriter();
//	    
//	    Map<String, Object> scope = new HashMap<String, Object>();
//	    scope.put("graphName", graphName);
//	    
//	    mustache.execute(sw, scope);		
//		return sw.toString();
//	}	
	
	public static String getHtmlOutput(String graphName) throws TemplateException, IOException {
		StringWriter sw = new StringWriter();
	    
	    //freemarker template engine
	    freemarker.template.Template temp = null; 
		String name = "graphvisualisation_d3.ftl";
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
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("graphName", graphName);
	    scope.put("version", Endpoint.class.getPackage().getImplementationVersion() );
	    scope.put("git", GitRepositoryState.getGitRepositoryState());
	    
	    temp.process(scope,sw);
	    
		return sw.toString();
	}	
}
