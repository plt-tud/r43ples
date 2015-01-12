package de.tud.plt.r43ples.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
import de.tud.plt.r43ples.revisionTree.Branch;
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

		// generate branches index
		Map<Commit, List<Branch>> branch_index = new HashMap<Commit, List<Branch>>();
		for (Branch b : revisionTree.getBranches()) {
			if (branch_index.containsKey(b.getReference()))
				branch_index.get(b.getReference()).add(b);
			else {
				List<Branch> list = new LinkedList<Branch>();
				list.add(b);
				branch_index.put(b.getReference(), list);
			}
		}

		// generic config for graph generation
		int y_start = 20;
		int lineheight = 20;
		g.setFont(g.getFont().deriveFont(16f));
		FontMetrics fm = g.getFontMetrics();
		DateFormat df = DateFormat.getDateTimeInstance();
		g.translate(0, y_start + lineheight - 10);

		g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		CommitGraphView graphView = new CommitGraphView(revisionTree);
		graphView.drawGraph(g);
		g.translate(0, -lineheight + 10);
		
		int messageOffset = (int) graphView.getDimension().getWidth() + 20;
		
		int timeOffset = 0;
		int y = 0;
		
		//header line
		g.setColor(new Color(0x000000));
		g.drawString("Commit Message", messageOffset, y);
		timeOffset = fm.stringWidth("Commit Message");
		y += lineheight;
		
		for(Commit c : commits) {
			String message = c.getMessage();

			// draw branches
			int branchesWidth = 0;
			if (branch_index.containsKey(c)) {
				for (Branch b : branch_index.get(c)) {
					String name = b.getName();

					Rectangle2D rect = g.getFontMetrics().getStringBounds(name, g);
					int width = g.getFontMetrics().stringWidth(name);
					g.translate(messageOffset, y);
					g.setColor(new Color(0.5f, 0.1f, 0.1f));
					g.fillRoundRect(0, -fm.getAscent(), width + 4, (int) rect.getHeight(), 5, 5);
					g.setColor(Color.WHITE);
					g.drawString(name, 2, 0);
					g.translate(-messageOffset, -y);
					branchesWidth += rect.getWidth();
				}
				branchesWidth += 10;
			}
			g.setColor(Color.BLACK);
			
			//infos of commit
			g.drawString(message, messageOffset + branchesWidth, y);
			
			//calculate offset of next column
			timeOffset = Math.max(timeOffset, branchesWidth + fm.stringWidth(message));
			
			y += lineheight;
		}
		timeOffset += messageOffset + 20;
		
		int authorOffset = 0;
		y = 0;
		
		//header line
		g.drawString("Time", timeOffset, y);
		authorOffset = fm.stringWidth("Time");
		y += lineheight;
		
		for(Commit c : commits) {
			String time = df.format(c.getTime());
			g.drawString(time, timeOffset, y);
			
			//calculate offset of next column
			authorOffset = Math.max(authorOffset, fm.stringWidth(time));
			
			y += lineheight;
		}
		authorOffset += timeOffset + 20;
		
		int rightBorder = 0;
		y = 0;
		
		//header line
		g.drawString("Author", authorOffset, y);
		rightBorder = fm.stringWidth("Author");
		y += lineheight;
		
		for(Commit c : commits) {
			String author = c.getAuthor();
			g.drawString(author, authorOffset, y);
			
			//calculate offset of next column
			rightBorder = Math.max(rightBorder, fm.stringWidth(author));
			
			y += lineheight;
		}
		rightBorder += authorOffset + 20;
		
		g.setSVGCanvasSize(new Dimension(rightBorder, Math.abs(y)));

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
