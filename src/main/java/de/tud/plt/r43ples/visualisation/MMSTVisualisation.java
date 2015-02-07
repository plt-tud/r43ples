package de.tud.plt.r43ples.visualisation;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

	private static String buildSVG(StructuredTree revisionTree)
			throws SVGGraphics2DIOException {
		
		// generate svg document
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		Document doc = impl.createDocument(svgNS, "svg", null);

		SVGGraphics2D g = new SVGGraphics2D(doc);
		
		// reverse commits list to begin with newest commit
		List<Commit> commits = revisionTree.getCommits();
		Collections.reverse(commits);

		// generic config for graph generation
		int y_start = 20;
		int lineheight = 20;
		int totalHeight = y_start + lineheight * commits.size() + 10;

		// translate to first text-baseline
		g.translate(0, y_start);

		TimeLineView timeView = new TimeLineView(revisionTree);
		timeView.draw(g);

		g.translate(75, 10);

		CommitGraphView graphView = new CommitGraphView(revisionTree);
		graphView.drawGraph(g);

		g.translate(graphView.getDimension().getWidth() + 20, -10);

		MessagesTableView msgView = new MessagesTableView(revisionTree);
		msgView.draw(g);
		
		g.translate(-graphView.getDimension().getWidth() - 20 - 75, 0);

		// calculate overall width
		int totalWidth = (int) (75 + graphView.getDimension().getWidth() + 20 + msgView.getDimension().getWidth());

		// draw header line
		int offset = g.getFontMetrics(msgView.TextFont).getDescent();
		g.drawLine(0, offset, totalWidth, offset);

		g.setSVGCanvasSize(new Dimension(totalWidth, totalHeight));

		Writer writer = new StringWriter();
		g.stream(writer);

		return writer.toString();
	}

	public static String getHtmlOutput(String graphName) throws HttpException,
			IOException {
		// initialise mustache template
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/MMSTVisualisation.mustache");
		StringWriter sw = new StringWriter();

		// get graph tree
		StructuredTree graphTree = StructuredTree.getTreeOfGraph(graphName);

		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("graphName", graphName);
		scope.put("git", GitRepositoryState.getGitRepositoryState());
		scope.put("svg_content", buildSVG(graphTree));

		mustache.execute(sw, scope);
		return sw.toString();
	}
}
