package de.tud.plt.r43ples.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.http.HttpException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.management.GitRepositoryState;
import de.tud.plt.r43ples.revisionTree.Commit;
import de.tud.plt.r43ples.revisionTree.StructuredTree;

public class MMSTVisualisation {

	private static String buildSVG(StructuredTree revisionTree) throws SVGGraphics2DIOException
	{
		//generate svg document
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		Document doc = impl.createDocument(svgNS, "svg", null);

		SVGGraphics2D g = new SVGGraphics2D(doc);
		int y = 20;
		for(Commit c : revisionTree.getCommits()) {
			g.setColor(new Color(0x000000));
			g.setFont(new Font("Mono", Font.PLAIN, 16));
			g.drawString(c.getMessage(), 50, y);
			g.setColor(new Color(0x901010));
			g.fillOval(20, y-10, 10, 10);
			
			y += 20;
		}
		//g.setSVGCanvasSize(new Dimension(100, 100));
	    
		Writer writer = new StringWriter();
		g.stream(writer);
		
		return writer.toString();
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
	    scope.put("svg_content", buildSVG(graphTree));
	    
	    mustache.execute(sw, scope);		
		return sw.toString();
	}
}
