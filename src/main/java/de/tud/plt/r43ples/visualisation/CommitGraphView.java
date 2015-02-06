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
	
	// dimensions of this graph, is null until drawn
	private Dimension2D dimension;
	// saves terminal commits of columns
	private List<Commit> terminalCommits = new LinkedList<Commit>();
	// saves on which column a commit was drawn
	private Map<Commit, Integer> commit_column = new HashMap<Commit, Integer>();
	// holds colors of branches, is cycled
	private Queue<Color> colorQueue;
	// list of commits
	private List<Commit> commits;

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
		int maxColumn = 0;
		
		Map<Commit, Color> commit_color = new HashMap<Commit, Color>();

		for (Commit c : commits) {

			// get column for this commit
			int currentColumn = getColumnForCommit(c);

			// find color
			Color currentColor = null;
			for (Commit suc : c.Successors) {
				if (suc.getBranch().equals(c.getBranch()))
					currentColor = commit_color.get(suc);
			}
			if (currentColor == null)
				currentColor = getNextColor();

			// connect with any succeeding commits
			for (Commit suc : c.Successors) {

				// calculate and set starting point
				GeneralPath path = new GeneralPath();
				int x = ColumnWidth * currentColumn + CircleDiameter / 2;
				int y = LineHeight * currentLine + CircleDiameter / 2;
				path.moveTo(x, y);

				// set color
				if (!suc.getBranch().equals(c.getBranch()) && suc.Predecessors.size() == 1)
					g.setColor(commit_color.get(suc));
				else
					g.setColor(currentColor);

				// get line and column of successor
				int endLine = commits.indexOf(suc);
				int endColumn = commit_column.get(suc);

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
					maxColumn = Math.max(maxColumn, column);
				}
				maxColumn = Math.max(maxColumn, column);
				drawConnectionTo(g, path, endLine, endColumn);
				g.draw(path);
			}
			
			// update memory
			terminalCommits.set(currentColumn, c);
			commit_column.put(c, currentColumn);
			commit_color.put(c, currentColor);

			// advance to next line
			currentLine++;
		}

		// draw circles
		for (int l = 0; l < commits.size(); l++) {
			Commit c = commits.get(l);
			g.setColor(commit_color.get(c));
			int column = commit_column.get(c);
			drawCircle(g, l, column);
		}

		// calculate dimensions
		dimension = new Dimension();
		dimension.setSize(maxColumn * ColumnWidth, commits.size()
				* LineHeight);
	}
	
	/**
	 * @return Dimension of drawn graph or null if nothing has been drawn yet
	 */
	public Dimension2D getDimension() {
		return dimension;
	}

	private void drawConnectionTo(Graphics2D g, GeneralPath path, int line,
			int column) {
		
		int x1 = (int) path.getCurrentPoint().getX();
		int y1 = (int) path.getCurrentPoint().getY();
		int x2 = ColumnWidth * column + CircleDiameter / 2;
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

	private void drawCircle(Graphics2D g, int line, int column) {
		g.fillOval(ColumnWidth * column, LineHeight * line, CircleDiameter,
				CircleDiameter);
	}

	private int getColumnForCommit(Commit c) {

		int column = -1;

		// test if there is a successor of same branch terminating a column
		for (Commit suc : c.Successors) {
			if (terminalCommits.contains(suc) && suc.getBranch().equals(c.getBranch())) {
				return terminalCommits.indexOf(suc);
			}
		}

		// test for reusable columns
		for (Commit term : terminalCommits) {
			boolean isReusable = true;

			// test whether terminal commit has predecessors not yet drawn
			for (Commit term_pre : term.Predecessors) {
				if (commits.indexOf(term_pre) > commits.indexOf(c)) {
					isReusable = false;
					break;
				}
				// test whether current commit has successors that would lead to
				// overlapping
				for (Commit suc : c.Successors) {
					if (!terminalCommits.contains(suc) && (commits.indexOf(suc) < commits.indexOf(term_pre))) {
						isReusable = false;
						break;
					}
				}
				if (!isReusable)
					break;
			}

			if (isReusable)
			{
				return terminalCommits.indexOf(term);
			}
		}

		// no column found, add new column
		if (column == -1) {
			terminalCommits.add(c);
			column = terminalCommits.indexOf(c);
		}

		return column;
	}

	private Color getNextColor() {
		Color c = colorQueue.poll();
		colorQueue.add(c);
		return c;
	}
}
