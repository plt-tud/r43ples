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
	public int LaneWidth = 15;
	
	private Dimension2D dimension;
	// saves terminal commits of lanes
	private List<Commit> terminalCommits = new LinkedList<Commit>();
	// saves on which lane a commit was drawn
	private Map<Commit, Integer> commit_lane = new HashMap<Commit, Integer>();
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
			int currentLane = getLaneForCommit(c);

			// find color
			Color currentColor;
			if (terminalCommits.get(currentLane) != c)
				currentColor = commit_color.get(terminalCommits.get(currentLane));
			else
				currentColor = getNextColor();

			// update memory
			terminalCommits.set(currentLane, c);
			commit_lane.put(c, currentLane);
			commit_color.put(c, currentColor);
			nodes.add(new GraphNode(currentLine, currentLane));

			// connect with any succeeding commits
			for (Commit suc : c.Successors) {

				// calculate and set starting point
				GeneralPath path = new GeneralPath();
				int x = LaneWidth * (currentLane + 1) + CircleDiameter / 2;
				int y = LineHeight * currentLine + CircleDiameter / 2;
				path.moveTo(x, y);

				// set color
				if (currentLane < commit_lane.get(suc))
					g.setColor(commit_color.get(suc));
				else
					g.setColor(currentColor);

				// get line and lane of successor
				int endLine = commits.indexOf(suc);
				int endLane = commit_lane.get(suc);

				int lane = currentLane;
				int line;
				for (line = currentLine; line > endLine + 1; line--) {
					// find lane
					while (nodes.contains(new GraphNode(line - 1, lane)))
						lane++;
					while (!nodes.contains(new GraphNode(line - 1, lane - 1))
							&& lane > currentLane)
						lane--;

					// drawConnection
					drawConnectionTo(g, path, line - 1, lane);
					nodes.add(new GraphNode(line - 1, lane));
					maxLane = Math.max(maxLane, lane);
				}
				drawConnectionTo(g, path, endLine, endLane);
				g.draw(path);

				if (terminalCommits.contains(suc)) {
					// if successor has no more unprocessed predecessors then
					// clear lane
					boolean hasUnprocessedPredecessor = false;
					for (Commit suc_pre : suc.Predecessors) {
						if (commits.indexOf(suc_pre) > line)
							hasUnprocessedPredecessor = true;
					}
					if (!hasUnprocessedPredecessor) {
						terminalCommits.remove(suc);
					}

				}
			}

			// advance to next line
			currentLine++;
		}

		// draw circles
		for (int l = 0; l < commits.size(); l++) {
			Commit c = commits.get(l);
			g.setColor(commit_color.get(c));
			int lane = commit_lane.get(c);
			drawCircle(g, l, lane);
		}

		// calculate dimensions
		dimension = new Dimension();
		dimension.setSize((maxLane + 1) * LaneWidth, commits.size()
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
		int x2 = LaneWidth * (lane + 1) + CircleDiameter / 2;
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
		g.fillOval(LaneWidth * (lane + 1), LineHeight * line, CircleDiameter,
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
			}
		}

		// no successor terminates a lane, add new lane
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
