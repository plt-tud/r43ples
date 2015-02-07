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
	 * Vertical distance between commits
	 */
	public int LineHeight = 20;

	/**
	 * Font of the Text
	 */
	public Font TextFont = new Font("SansSerif", Font.PLAIN, 16);

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
		int authorOffset = 0;
		FontMetrics fm = g.getFontMetrics(TextFont);
		Font tmp = g.getFont();
		g.setFont(TextFont);
		Font branchTagFont = new Font("Monospaced", Font.PLAIN, TextFont.getSize() - 2);
		FontMetrics branchTagMetrics = g.getFontMetrics(branchTagFont);

		// header line
		g.setColor(new Color(0x000000));
		g.drawString("Commit Message", 0, y);
		authorOffset = fm.stringWidth("Commit Message");
		y += LineHeight;

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
					g.fillRoundRect(branchesWidth, -branchTagMetrics.getAscent(), width + 8, height, 5, 5);
					g.setColor(Color.WHITE);
					g.drawString(name, branchesWidth + 2, 0);
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
					g.fillRoundRect(branchesWidth, -branchTagMetrics.getAscent(), width + 8, height, 5, 5);
					g.setColor(Color.WHITE);
					g.drawString(name, branchesWidth + 2, 0);
					g.translate(0, -y);
					branchesWidth += width + 12;
				}
			}

			g.setColor(Color.BLACK);
			g.setFont(TextFont);

			// infos of commit
			g.drawString(message, branchesWidth, y);

			// calculate offset of next column
			authorOffset = Math.max(authorOffset, branchesWidth + fm.stringWidth(message));

			y += LineHeight;
		}
		authorOffset += 20;

		int rightBorder = 0;
		y = 0;

		// header line
		g.drawString("Author", authorOffset, y);
		rightBorder = fm.stringWidth("Author");
		y += LineHeight;

		for (Commit c : commits) {
			String author = c.getAuthor();
			g.drawString(author, authorOffset, y);

			// calculate offset of next column
			rightBorder = Math.max(rightBorder, fm.stringWidth(author));

			y += LineHeight;
		}
		rightBorder += authorOffset + 20;
		dimension = new Dimension(rightBorder, y + 10);

		g.setFont(tmp);
	}

	public Dimension getDimension() {
		return dimension;
	}

}
