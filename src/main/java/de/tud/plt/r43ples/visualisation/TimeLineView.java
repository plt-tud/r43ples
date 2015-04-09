package de.tud.plt.r43ples.visualisation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import de.tud.plt.r43ples.revisionTree.Commit;

public class TimeLineView {

	/**
	 * Font of the Text
	 */
	private final Font TextFont = new Font("Monospaced", Font.PLAIN, 14);

	private List<Commit> commits;

	public TimeLineView(List<Commit> commits2) {
		commits = commits2;
	}

	public void draw(Graphics2D g) {

		int y = VisualisationBatik.LINE_HEIGHT;
		int totalHeight = commits.size() * VisualisationBatik.LINE_HEIGHT + 10;
		DateFormat dayFormat = new SimpleDateFormat("d");
		DateFormat monthFormat = new SimpleDateFormat("MMM");

		Font tmp = g.getFont();
		g.setFont(TextFont);
		int offset = g.getFontMetrics(TextFont).getDescent();

		String oldMonth = "";
		String oldDay = "";
		g.setColor(new Color(0x222222));
		g.fillRect(0, offset, 35, totalHeight);
		g.setColor(new Color(0x444444));
		g.fillRect(35, offset, 25, totalHeight);
		g.setColor(Color.BLACK);
		g.drawLine(35, offset, 35, totalHeight);
		g.setColor(Color.WHITE);

		for (Commit c : commits) {
			String month = monthFormat.format(c.getTime());
			String day = dayFormat.format(c.getTime());
			if (!month.equals(oldMonth)) {
				g.drawString(month, 4
						, y);
				oldMonth = month;
			}
			if (!day.equals(oldDay)) {
				g.drawString(day, 38, y);
				oldDay = day;
			}
			y += VisualisationBatik.LINE_HEIGHT;
		}
		g.setColor(Color.BLACK);
		g.setFont(tmp);
	}
}
