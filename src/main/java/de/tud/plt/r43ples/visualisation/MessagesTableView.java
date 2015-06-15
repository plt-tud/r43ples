package de.tud.plt.r43ples.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.plt.r43ples.revisionTree.Branch;
import de.tud.plt.r43ples.revisionTree.Commit;
import de.tud.plt.r43ples.revisionTree.StructuredTree;
import de.tud.plt.r43ples.revisionTree.Tag;

public class MessagesTableView {

	/**
	 * Font of the Text
	 */
	protected final Font TextFont = new Font("SansSerif", Font.PLAIN, 16);

	private List<Commit> commits;
	private Map<Commit, List<Branch>> branch_index;
	private Map<Commit, List<Tag>> tag_index;
	private Dimension dimension = null;

	public MessagesTableView(StructuredTree tree) {
		commits = tree.getCommits();

		// generate branches index
		branch_index = new HashMap<Commit, List<Branch>>();
		for (Branch b : tree.getBranches()) {
			if (branch_index.containsKey(b.getReference()))
				branch_index.get(b.getReference()).add(b);
			else {
				List<Branch> list = new LinkedList<Branch>();
				list.add(b);
				branch_index.put(b.getReference(), list);
			}
		}

		// generate tags index
		tag_index = new HashMap<Commit, List<Tag>>();
		for (Tag t : tree.getTags()) {
			if (tag_index.containsKey(t.getReference()))
				tag_index.get(t.getReference()).add(t);
			else {
				List<Tag> list = new LinkedList<Tag>();
				list.add(t);
				tag_index.put(t.getReference(), list);
			}
		}
	}

	public void draw(Graphics2D g) {

		int y = 0;
		int pos = 0;
		FontMetrics fm = g.getFontMetrics(TextFont);
		Font tmp = g.getFont();
		g.setColor(Color.BLACK);
		g.setFont(TextFont);
		Font branchTagFont = new Font("Monospaced", Font.PLAIN, TextFont.getSize() - 2);
		FontMetrics branchTagMetrics = g.getFontMetrics(branchTagFont);
		
		// Number
		// header line
		g.drawString("Number", pos, y);
		int numberWidth = fm.stringWidth("Number");
		y = VisualisationBatik.LINE_HEIGHT;

		for (Commit c : commits) {
			
			String rev = c.getNextRevision();
			g.drawString(rev, pos, y);

			// calculate offset of next column
			numberWidth = Math.max(numberWidth, fm.stringWidth(rev));

			y += VisualisationBatik.LINE_HEIGHT;
		}
		
		
		pos += numberWidth + VisualisationBatik.PADDING;
		
		// Commit message
		// header line
		y=0;
		g.drawString("Commit Message", pos, y);
		int commitWidth = fm.stringWidth("Commit Message");
		y += VisualisationBatik.LINE_HEIGHT;

		for (Commit c : commits) {
			String message = c.getMessage();

			int branchesWidth = 0;
			g.setFont(branchTagFont);

			// draw branches
			if (branch_index.containsKey(c)) {
				for (Branch b : branch_index.get(c)) {
					String name = b.getName();

					int width = branchTagMetrics.stringWidth(name);
					int height = branchTagMetrics.getAscent() + branchTagMetrics.getDescent();
					g.translate(0, y);
					g.setColor(new Color(0.5f, 0.1f, 0.1f));
					g.fillRoundRect(pos + branchesWidth, -branchTagMetrics.getAscent(), width + 8, height, 5, 5);
					g.setColor(Color.WHITE);
					g.drawString(name, pos + branchesWidth + 2, 0);
					g.translate(0, -y);
					branchesWidth += width + 12;
				}
			}

			// draw tags
			if (tag_index.containsKey(c)) {
				for (Tag t : tag_index.get(c)) {
					String name = t.getName();

					int width = branchTagMetrics.stringWidth(name);
					int height = branchTagMetrics.getAscent() + branchTagMetrics.getDescent();
					g.translate(0, y);
					g.setColor(new Color(0.1f, 0.1f, 0.5f));
					g.fillRoundRect(pos + branchesWidth, -branchTagMetrics.getAscent(), width + 8, height, 5, 5);
					g.setColor(Color.WHITE);
					g.drawString(name, pos + branchesWidth + 2, 0);
					g.translate(0, -y);
					branchesWidth += width + 12;
				}
			}

			g.setColor(Color.BLACK);
			g.setFont(TextFont);

			// infos of commit
			g.drawString(message, pos + branchesWidth, y);

			// calculate offset of next column
			commitWidth = Math.max(commitWidth, branchesWidth + fm.stringWidth(message));

			y += VisualisationBatik.LINE_HEIGHT;
		}
		
		pos += commitWidth + VisualisationBatik.PADDING;

		
		// Author
		y = 0;

		// header line
		g.drawString("Author", pos, y);
		int authorWidth = fm.stringWidth("Author");
		y += VisualisationBatik.LINE_HEIGHT;

		for (Commit c : commits) {
			String author = c.getAuthor();
			g.drawString(author, pos, y);

			// calculate offset of next column
			authorWidth = Math.max(authorWidth, fm.stringWidth(author));

			y += VisualisationBatik.LINE_HEIGHT;
		}
		pos += authorWidth;
		dimension = new Dimension(pos, y);

		g.setFont(tmp);
	}

	public Dimension getDimension() {
		return dimension;
	}

}
