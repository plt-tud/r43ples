package de.tud.plt.r43ples.visualisation;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


public class VisualisationG6 {
	
	public static String getHtmlOutput(String graphName) {
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/graphvisualisation_g6.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("graphName", graphName);
	    
	    mustache.execute(sw, scope);		
		return sw.toString();
	}	
}
