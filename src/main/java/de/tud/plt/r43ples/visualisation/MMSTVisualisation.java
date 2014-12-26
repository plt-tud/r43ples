package de.tud.plt.r43ples.visualisation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.revisionTree.StructuredTree;
import de.tud.plt.r43ples.revisionTree.Tree;

public class MMSTVisualisation {

	private static String buildSVG(Tree revisionTree)
	{
		return "";
	}
	
	public static String getHtmlOutput(String graphName) throws HttpException, IOException
	{
		//initialise mustache template
		MustacheFactory mf = new DefaultMustacheFactory();
	    Mustache mustache = mf.compile("templates/MMSTVisualisation.mustache");
	    StringWriter sw = new StringWriter();
	    
	    //get graph tree
	    StructuredTree graphTree = StructuredTree.getTreeOfGraph(graphName);
	    
	    Map<String, Object> scope = new HashMap<String, Object>();
	    scope.put("graphName", graphName);
	    scope.put("git", GitRepositoryState.getGitRepositoryState());
	    
	    mustache.execute(sw, scope);		
		return sw.toString();
	}
}
