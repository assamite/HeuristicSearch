package ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

/**
 * Simple custom border style for nicer rounded corners in UI's borders. 
 * Slightly altered code from some stackoverflow question I forgot to copy link 
 * to.
 * 
 * @author slinkola
 *
 */
public class SBBorder extends AbstractBorder {
	/** Default serialization UID. */
	private static final long serialVersionUID = 1L;
	
	@Override 
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
	    Graphics2D g2 = (Graphics2D)g.create();
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    int r = 15;
	    RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width-1, height-1, r, r);
	    Container parent = c.getParent();
	    if(parent!=null) {
	    	g2.setColor(UIScheme.ALPHA);
	    	Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
	    	corner.subtract(new Area(round));
	    	g2.fill(corner);
	    }
	    g2.setColor(UIScheme.GRAYBLUE3);
	    g2.draw(round);
	    g2.dispose();
	}
	
  @Override public Insets getBorderInsets(Component c) {
    return new Insets(4, 8, 4, 8);
  }
  
  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.left = insets.right = 8;
    insets.top = insets.bottom = 4;
    return insets;
  }
}

