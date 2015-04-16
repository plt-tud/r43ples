package de.tud.plt.r43ples.merging;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MergingControl {
	public static String getHtmlOutput(String optradio) {
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/mergingView1.mustache");
	    StringWriter sw = new StringWriter();
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("optradio", optradio);
	    
	    mustache.execute(sw, scope);		
		return sw.toString();
	}	
}
