package de.tud.plt.r43ples.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
		//Collections.reverse(commits);

		// generic config for graph generation
		int y_start = 20;
		int lineheight = -20;
		Font standardFont = new Font("Mono", Font.PLAIN, 16);
		g.setFont(standardFont);
		FontMetrics fm = g.getFontMetrics();
		DateFormat df = DateFormat.getDateTimeInstance();
		g.translate(0, -lineheight * commits.size());
		
		int maxX = 0;
		int maxY = 0;

		// first iteration - graphical column
		int messageOffset = 0;
		int y = y_start + lineheight; //skip header line
		
		//lanes list saves last commit of graph lane
		List<Commit> lanes = new LinkedList<Commit>();
		
		for (Commit c : commits) {
			
			//test whether existing lane available
			int laneNumber = -1;
			for(Commit pre : c.Predecessors) {
				if(lanes.contains(pre)){
					laneNumber = lanes.indexOf(pre);
					//update lane
					lanes.set(laneNumber, c);
					//draw connecting line
					g.drawLine(25 + 20 * laneNumber, y_start + lineheight * (commits.indexOf(pre) + 1) - 5, 25 + 20 * laneNumber, y);
					break;
				}
			}
			
			//if no lane available, create new one
			if(laneNumber < 0) {
				lanes.add(c);
				laneNumber = lanes.indexOf(c);
			}
			
			
			// circle in front of commit
			// TODO: color variable
			g.setColor(new Color(0x901010));
			// TODO: x-position variable
			g.fillOval(20 + 20 * laneNumber, y - 10, 10, 10);
			
			//calculate offset of next column
			messageOffset = Math.max(messageOffset, 20 + 20 * laneNumber);

			// advance to next line
			y += lineheight;
		}
		messageOffset += 30;
		
		
		int timeOffset = 0;
		y = y_start;
		
		//header line
		g.drawString("Commit Message", messageOffset, y);
		timeOffset = fm.stringWidth("Commit Message");
		y += lineheight;
		
		for(Commit c : commits) {
			String message = c.getMessage();
			
			//infos of commit
			g.setColor(new Color(0x000000));
			g.drawString(message, messageOffset, y);
			
			//calculate offset of next column
			timeOffset = Math.max(timeOffset, fm.stringWidth(message));
			
			y += lineheight;
		}
		timeOffset += messageOffset + 20;
		
		int authorOffset = 0;
		y = y_start;
		
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
		y = y_start;
		
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
		
		g.setSVGCanvasSize(new Dimension(rightBorder, Math.abs(y-y_start)));

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
