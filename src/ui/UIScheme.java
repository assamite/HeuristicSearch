package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * Scheme for different UI elements looks. 
 * 
 * @author slinkola
 *
 */
public class UIScheme {
	public static final Color BLUE_VVD = new Color(18, 85, 107);
	public static final Color BLUE_VD = new Color(38, 107, 137);
	public static final Color BLUE_D = new Color(85, 157, 188);
	public static final Color BLUE_DA = new Color(85, 157, 188, 200);
	public static final Color BLUE_M = new Color(131, 195, 222);
	public static final Color BLUE_MA = new Color(131, 195, 222, 200);
	public static final Color BLUE_L = new Color(156, 202, 222);
	public static final Color BLUE_LA = new Color(156, 202, 222, 200);
	public static final Color BLUE_VL = new Color(191, 223, 233);
	public static final Color BLUE_VLA = new Color(191, 213, 233, 200);
	
	public static final Color LILA_VD = new Color(37, 53, 157);
	public static final Color LILA_M = new Color(149, 133, 255);
	public static final Color LILA_L = new Color(169, 181, 255);
	public static final Color LILA_VL = new Color(201, 213, 255);
	
	public static final Color GREEN_VL = new Color(142, 246, 177);
	public static final Color GREEN_L = new Color(80, 226, 129);
	public static final Color GREEN_M = new Color(86, 154, 109);
	public static final Color GREEN_D = new Color(16, 125, 53);
	
	public static final Color CYAN = new Color(0, 255, 255);
	public static final Color MAGENTA = new Color(255, 0, 255);
	public static final Color MAGENTA_ALPHA = new Color(255, 0, 255, 15);
	public static final Color RED = new Color(255, 0, 0);
	public static final Color BLUE = new Color(0, 0, 255);
	
	public static final Color GRAYBLUE = new Color(159, 192, 214);
	public static final Color GRAYBLUE2 = new Color(153, 182, 201);
	public static final Color GRAYBLUE3 = new Color(89, 115, 133);
	public static final Color GRAYBLUE4 = new Color(51, 69, 82);	

	public static final Color ALPHA = new Color(0, 0, 0, 0);
	public static final Color ALPHA2 = new Color(0, 0, 0, 20);
	
	public static final Border etchedBorder = BorderFactory.createEtchedBorder(UIScheme.BLUE_M, UIScheme.BLUE_VD);
	public static final Border BUTTON_BORDER = new LineBorder(UIScheme.BLUE_VD, 1, false);
	public static final Border IEBORDER = new IEBorder();
	
	public static final Border createTitledBorder(String title) {
		return new TitledBorder(UIScheme.IEBORDER, 
				title, TitledBorder.LEFT , TitledBorder.CENTER, 
				new Font("sans-serif", 0, 15), UIScheme.GRAYBLUE4);
	}
	
	/**
	 * Convenience function to init utility buttons. Sets size and text of the
	 * button and makes it visible after it has been added as a child to given
	 * wrapper JPanel. 
	 * @param b JButton to alter
	 * @param w preferred width
	 * @param h preferred height
	 * @param wrap JPanel wrapper to wrap b in. 
	 * @param text text to show in button, also acts as an ActionCommand for b
	 * @param a action listener for this button
	 */
	public static void initButton(final AbstractButton b, int w, int h, JPanel wrap, String text, ActionListener a) {
		b.setText(text);
		b.setPreferredSize(new Dimension(w, h));
		b.setActionCommand(text);
		b.addActionListener(a);
		b.setOpaque(false);
		b.setForeground(UIScheme.BLUE_VD);
		b.setBorder(UIScheme.IEBORDER);
		b.addMouseListener(new MouseAdapter() {
			  public void mousePressed(MouseEvent e) {
				  b.setForeground(UIScheme.BLUE_D);
				  b.repaint();
			  }
			  public void mouseReleased(MouseEvent e) {
				  b.setForeground(UIScheme.BLUE_VD);
				  b.repaint();
			  }
			});
		if (wrap != null) wrap.add(b);
	}
}
