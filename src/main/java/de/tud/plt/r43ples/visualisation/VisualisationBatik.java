package de.tud.plt.r43ples.visualisation;

import java.awt.Dimension;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.revisionTree.Commit;
import de.tud.plt.r43ples.revisionTree.StructuredTree;

public class VisualisationBatik {
	
	private static final int TIMELINEVIEW_WIDTH = 70;

	/**
	 * Vertical distance between commits
	 */
	protected final static int LINE_HEIGHT = 22;
	
	protected final static int PADDING = 10;
	
	private TimeLineView timeView;

	private List<Commit> commits;

	private CommitGraphView graphView;

	private MessagesTableView msgView;
	

	public VisualisationBatik(StructuredTree revisionTree) {
		commits = revisionTree.getCommits();
		Collections.reverse(commits);
		
		timeView = new TimeLineView(commits);
		graphView = new CommitGraphView(commits);
		msgView = new MessagesTableView(revisionTree);
	}
	
	
	private String buildSVG() throws SVGGraphics2DIOException {
		
		// generate svg document
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		Document doc = impl.createDocument(svgNS, "svg", null);

		SVGGraphics2D g = new SVGGraphics2D(doc);
		

		// translate to first text-baseline
		g.translate(0, LINE_HEIGHT);		
		timeView.draw(g);

		g.translate(TIMELINEVIEW_WIDTH, 0);
		graphView.drawGraph(g);

		g.translate(graphView.getDimension().getWidth() + PADDING, 0);
		msgView.draw(g);
		
		g.translate(-graphView.getDimension().getWidth() - PADDING - TIMELINEVIEW_WIDTH, 0);

		// calculate overall width
		int totalWidth = (int) (TIMELINEVIEW_WIDTH + graphView.getDimension().getWidth() + PADDING + msgView.getDimension().getWidth());
		
		
		// draw header line
		int offset = g.getFontMetrics(msgView.TextFont).getDescent();
		g.drawLine(0, offset, totalWidth, offset);

		int totalHeight = LINE_HEIGHT * (commits.size()+1) + offset;
		g.setSVGCanvasSize(new Dimension(totalWidth, totalHeight));

		Writer writer = new StringWriter();
		g.stream(writer);

		return writer.toString();
	}

	public static String getHtmlOutput(String graphName) {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("templates/graphvisualisation_batik.mustache");
		StringWriter sw = new StringWriter();

		// get graph tree
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		StructuredTree graphTree = StructuredTree.getTreeOfGraph(revisionGraph);
		VisualisationBatik visu = new VisualisationBatik(graphTree);
		
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("graphName", graphName);
		try {
			scope.put("svg_content", visu.buildSVG());
		} catch (SVGGraphics2DIOException e) {
			scope.put("svg_content", "Error while creating SVG");
		}

		mustache.execute(sw, scope);
		return sw.toString();
	}
}
