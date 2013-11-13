package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Utility panel for the UI. Should be at the side of the map. Has fixed width 
 * of 300 px and contains robot control panel and info panel.
 * 
 * @author slinkola
 *
 */
public class Util extends JPanel {
	/** Serialization UID. */
	static final long serialVersionUID = 1L;
	/** Fixed width of the utility panel. */
	private final int width = 300;	
	/** Height of the utility panel. */
	private final int height;
	/** UI section to command robot. */
	public final Control control;
	/** UI section for info messages during apps runtime. */
	private final Info info;
	
	/**
	 * Default constructor. Initializes robot control panel and information 
	 * panel.
	 * 
	 * @param height Height of the utility panel, should not be less than 500px.
	 * @param offset y-coordinate offset
	 */
	public Util(int height, int offset) {
		this.height = height - offset;
		this.setBounds(0, offset, this.width, this.height);
		this.setLayout(new BorderLayout(0, 0));
		Dimension d = new Dimension(this.width, this.height);
		this.setMinimumSize(d);
		this.setBackground(UIScheme.ALPHA);
		this.control = new Control();
		this.add(control, BorderLayout.PAGE_START);
		this.info = new Info(this.height - this.control.getHeight(), this.control.getHeight());
		this.add(info);
	}
	
	public Info getInfo() {
		return this.info;
	}
	
	@Override
	protected void paintComponent(Graphics gc) {
        Graphics2D g2d = (Graphics2D) gc;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0,
                UIScheme.GRAYBLUE2, 0, getHeight(),
                UIScheme.GRAYBLUE3);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        //super.paintComponent(grphcs);
	}
}
