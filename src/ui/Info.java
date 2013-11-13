package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Info panel of the UI. Shows informational messages about apps functionality
 * in runtime.
 * @author slinkola
 *
 */
public class Info extends JPanel {
	/** Serialization UID. */
	static final long serialVersionUID = 1L;
	/** JPanel width */
	private final int width = 300;
	/** JPanel height */
	private int height;
	/** Actual panel for info texts. */
	private final JTextArea infoText = new JTextArea(34, 23);
	/** list of actual line lengths in infoText. */
	private ArrayList<Integer> lines = new ArrayList<Integer>();
	/** Maximum number of text rows to show. */
	private int maxRows = 34;
	/** Line length of infoText. */
	private int lineLength = 40;
	
	/**
	 * Default constructor.
	 * @param height height of the panel
	 * @param offset offset of panels bounds in y-coordinates.
	 */
	public Info(int height, int offset) {
		this.height = height;
		Dimension d = new Dimension(this.width-12, 300);
		this.setMinimumSize(d);
		this.setOpaque(false);
		this.setBorder(UIScheme.createTitledBorder("Info"));
		d = new Dimension(this.width-12, this.height - 30);
		this.infoText.setBorder(null);
		this.infoText.setOpaque(false);
		this.infoText.setMaximumSize(d);
		this.infoText.setPreferredSize(d);
		this.infoText.setForeground(Color.WHITE);
		this.infoText.setEditable(false);
		this.infoText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		this.add(this.infoText);
	}
	
	/** Print text to info panel with concatenated new line. Texts with new 
	 * line characters originally in them may cause unexpected behaviour. */
	public void println(String text) {
		int max = (text.length() / this.lineLength) + 1;
		for (int i = 0; i < max; i++) {
			int start = i*this.lineLength;
			int end = (i+1)*this.lineLength > text.length() ? text.length(): (i+1)*this.lineLength;
			String s = text.substring(start, end);
			if (this.lines.size() >= this.maxRows) {
				this.infoText.replaceRange("", 0, lines.get(0)+1);
				this.lines.remove(0);
			}
			this.infoText.append(s + "\n");
			this.lines.add(s.length());

		}
		this.infoText.repaint();
	}
}
