package de.tud.plt.r43ples.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.tud.plt.r43ples.revisionTree.Commit;
import de.tud.plt.r43ples.revisionTree.StructuredTree;

public class CommitGraphView {
	
	/**
	 * Vertical distance between commits
	 */
	public int LineHeight = 20;
	
	/**
	 * Diameter of the circles representing commits
	 */
	public int CircleDiameter = 10;
	
	
	/**
	 * Horizontal distance between commits respective branches
	 */
	public int ColumnWidth = 15;
	
	private Dimension2D dimension;
	// saves terminal commits of lanes
	private List<Commit> terminalCommits = new LinkedList<Commit>();
	// saves on which lane a commit was drawn
	private Map<Commit, Integer> commit_column = new HashMap<Commit, Integer>();
	// saves non empty grid coordinates
	private List<GraphNode> nodes = new LinkedList<GraphNode>();
	private Queue<Color> colorQueue;
	
	private List<Commit> commits;

	private class GraphNode {
		public int Line;
		public int Lane;

		public GraphNode(int line, int lane) {
			this.Line = line;
			this.Lane = lane;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof GraphNode) {
				GraphNode node = (GraphNode) obj;
				return node.Line == Line && node.Lane == Lane;
			}
			return super.equals(obj);
		}
	}

	public CommitGraphView(StructuredTree tree) {
		commits = tree.getCommits();
		colorQueue = new LinkedList<Color>();
		colorQueue.add(new Color(0.5f, 0.1f, 0.1f));
		colorQueue.add(new Color(0.1f, 0.5f, 0.1f));
		colorQueue.add(new Color(0.1f, 0.1f, 0.5f));
		colorQueue.add(new Color(0.5f, 0.5f, 0.1f));
		colorQueue.add(new Color(0.5f, 0.1f, 0.5f));
		colorQueue.add(new Color(0.1f, 0.5f, 0.5f));
	}
	
	public void drawGraph(Graphics2D g) {

		int currentLine = 0;
		int maxLane = 0;
		
		Map<Commit, Color> commit_color = new HashMap<Commit, Color>();

		for (Commit c : commits) {

			// get lane for this commit
			int currentColumn = getLaneForCommit(c);

			// find color
			Color currentColor;
			if (terminalCommits.get(currentColumn) != c)
				currentColor = commit_color.get(terminalCommits.get(currentColumn));
			else
				currentColor = getNextColor();

			// connect with any succeeding commits
			for (Commit suc : c.Successors) {

				// calculate and set starting point
				GeneralPath path = new GeneralPath();
				int x = ColumnWidth * currentColumn + CircleDiameter / 2;
				int y = LineHeight * currentLine + CircleDiameter / 2;
				path.moveTo(x, y);

				// set color
				if (currentColumn < commit_column.get(suc))
					g.setColor(commit_color.get(suc));
				else
					g.setColor(currentColor);

				// get line and lane of successor
				int endLine = commits.indexOf(suc);
				int endLane = commit_column.get(suc);

				// find column for connection line
				int column = currentColumn; // fallback
				if (terminalCommits.contains(suc)) {
					// if successor terminates a column, use his column
					column = terminalCommits.indexOf(suc);
				} else if (!terminalCommits.contains(c)) {
					// if successor is not terminating a column and current
					// commit is not the first commit of his column, use new
					// column
					column = terminalCommits.size();
					terminalCommits.add(null);
				}
				int line;
				for (line = currentLine; line > endLine + 1; line--) {
					// drawConnection
					drawConnectionTo(g, path, line - 1, column);
					nodes.add(new GraphNode(line - 1, column));
					maxLane = Math.max(maxLane, column);
				}
				maxLane = Math.max(maxLane, column);
				drawConnectionTo(g, path, endLine, endLane);
				g.draw(path);
			}
			
			// update memory
			terminalCommits.set(currentColumn, c);
			commit_column.put(c, currentColumn);
			commit_color.put(c, currentColor);
			nodes.add(new GraphNode(currentLine, currentColumn));

			// advance to next line
			currentLine++;
		}

		// draw circles
		for (int l = 0; l < commits.size(); l++) {
			Commit c = commits.get(l);
			g.setColor(commit_color.get(c));
			int lane = commit_column.get(c);
			drawCircle(g, l, lane);
		}

		// calculate dimensions
		dimension = new Dimension();
		dimension.setSize(maxLane * ColumnWidth, commits.size()
				* LineHeight);
	}
	
	/**
	 * @return Dimension of drawn graph or null if nothing has been drawn yet
	 */
	public Dimension2D getDimension() {
		return dimension;
	}

	private void drawConnectionTo(Graphics2D g, GeneralPath path, int line,
			int lane) {
		
		int x1 = (int) path.getCurrentPoint().getX();
		int y1 = (int) path.getCurrentPoint().getY();
		int x2 = ColumnWidth * lane + CircleDiameter / 2;
		int y2 = LineHeight * line + CircleDiameter / 2;

		if (x1 == x2) {
			// draw straight line
			path.lineTo(x2, y2);
		} else {
			// draw curved line
			int curvedLineBase = (int) (LineHeight * 0.7);
			path.curveTo(x1, y1 - curvedLineBase, x2, y2 + curvedLineBase,
					x2, y2);
		}
	}

	private void drawCircle(Graphics2D g, int line, int lane) {
		g.fillOval(ColumnWidth * lane, LineHeight * line, CircleDiameter,
				CircleDiameter);
	}

	private int getLaneForCommit(Commit c) {

		int lane = -1;
		for (Commit suc : c.Successors) {
			// test if there is a successor terminating a lane
			if (terminalCommits.contains(suc)) {
				// choose the lowest lane
				if (lane == -1 || lane > terminalCommits.indexOf(suc))
					lane = terminalCommits.indexOf(suc);
				// but always choose the lane with the same branch if existing
				if (suc.getBranch().equals(c.getBranch())) {
					lane = terminalCommits.indexOf(suc);
					break;
				}
			}
		}

		// no column found, add new column
		if (lane == -1) {
			terminalCommits.add(c);
			lane = terminalCommits.indexOf(c);
		}

		return lane;
	}

	private Color getNextColor() {
		Color c = colorQueue.poll();
		colorQueue.add(c);
		return c;
	}
}
