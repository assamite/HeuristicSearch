package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.SampleModel;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import robot.SearchBot;
import searchs.Node;


/**
 * Map part of main UI for showing search bot's current map and its planned 
 * travel route.  
 * 
 * @author slinkola
 *
 */
public class Map extends JPanel implements MouseListener, MouseMotionListener, 
	ActionListener {	
	/** Serialization UID. */
	static final long serialVersionUID = 1L;
	/** Width of the JLayeredPane. */
	private int width; 	
	/** Height of the JLayeredPane. */
	private int height;	
	/** Map layers. */
	private JLayeredPane layers = new JLayeredPane();
	/** Foreground map layer. Shows robot's planned and executed path. */
	private JPanel foreground = new JPanel();
	/** Main map layer. Currently unused */
	private JPanel map = new JPanel();
	/** Background map layer. Shows current image map. */
	private JPanel bgMap = new JPanel();
	/** Currently monitored robot. */
	public SearchBot robot;
	/** Current map. */
	public BufferedImage mapImage;
	/** Current closed list for search. */
	public BufferedImage searchedImage = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	/** Drawing line start. */
	private int[] lineStart = {0, 0};
	/** Color space for drawing. */
	private ColorSpace cSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	/** Current drawing color. */
	private Color drawingColor = new Color(cSpace, new float[] {0}, 1);
	
	/** Constructor with fixed size 800 x 600 */
	Map () {
		this(800, 600, 0);
	}
	
	/**
	 * Constructor for variable sized maps.
	 * @param height map's height
	 * @param width map's width
	 * @param offset offset of the map's panel in x-coordinates.
	 */
	Map (int width, int height, int offset) {
		// Init main frame.
		this.width = width;
		this.height = height;
		this.setBounds(offset, 0, this.width, this.height);
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		
		// Internal layered panel for map layers.
		Rectangle mapRect = new Rectangle(0, 0, this.width, this.height);
		this.layers.setBounds(mapRect);
		this.layers.setPreferredSize(new Dimension(mapRect.width, mapRect.height));
		this.layers.setOpaque(true);
		this.layers.setBackground(Color.WHITE);
		
		// Init background map
		this.bgMap.setBounds(mapRect);
		this.bgMap.setOpaque(false);
		this.layers.add(this.bgMap, new Integer(0));
		// Init map.
		this.map.setBounds(mapRect);
		this.map.setOpaque(false);
		this.map.addMouseMotionListener(this);
		this.map.addMouseListener(this);
		this.layers.add(this.map, new Integer(5));
		// Init foreground.
		this.foreground.setBounds(mapRect);
		this.foreground.setOpaque(false);
		this.layers.add(this.foreground, new Integer(10));
		this.add(this.layers, BorderLayout.PAGE_END);
		
	}
	
	/** Change drawing color to the specified gray value. */
	public void setDrawingColor(int value) {
		this.drawingColor = new Color(this.cSpace, new float[] {(float)value / 255}, 1);
	}
	
	/**
	 * Set mapImage and create new searchedImage. 
	 * @param img
	 */
	public void setMapImage(BufferedImage img) {
		this.mapImage = img;
		Graphics gc = this.bgMap.getGraphics();
		gc.drawImage(img, 0, 0, null);
		this.clearSearchedImage();
		this.repaint();
	}
	
	/** Create new searchedImage for drawing searched nodes. */
	public void clearSearchedImage() {
		int w = this.mapImage.getWidth();
		int h = this.mapImage.getHeight();
		this.searchedImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
	}
	
	/** Change or reset this map's robot and draw robot's information on map. */
	public void updateRobot(SearchBot r) {
		this.robot = r;
		this.repaint();
	}
	
	/** Draw robot's path. */ 
	private void drawPath() {
		Graphics2D gc = (Graphics2D)this.foreground.getGraphics();
		if (this.robot != null) {
			this.robot.draw(gc);
		}
		gc.dispose();
	}
	
	/** Draw currently searched nodes. */ 
	private void drawSearched() {
		if (this.robot != null) {
			this.robot.drawSearched(this.searchedImage.getRaster());
		}
		Graphics2D gc = (Graphics2D)this.map.getGraphics();
		gc.drawImage(this.searchedImage, 0, 0, null);
		gc.dispose();
	}
	
	@Override
	public void paint(Graphics g) {	
		this.paintComponents(g);
		Graphics gc = this.bgMap.getGraphics();
		gc.drawImage(this.mapImage, 0, 0, null);
		gc.dispose();
		this.drawSearched();
		this.drawPath();
	}

	@Override
	/** Start line drawing. */
	public void mousePressed(MouseEvent e) {
		lineStart[0] = e.getX();
		lineStart[1] = e.getY();
	}
	
	@Override
	/** Draw line between the point where mouse was pressed and the point where 
	 * mouse was released. */
	public void mouseReleased(MouseEvent e) {
		int x = e.getX(); int y = e.getY();
		if (x == lineStart[0] && y == lineStart[1]) return;
		
		Graphics2D g = (Graphics2D)mapImage.getGraphics();
        g.setColor(this.drawingColor);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
        g.drawLine(lineStart[0], lineStart[1], e.getX(), e.getY());
        g.dispose();
        this.repaint();
        
        EventHandler.setRobotMap(this.mapImage, new int[] {lineStart[0], lineStart[1], x, y});
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
			int[] p = { e.getX(), e.getY() };
			EventHandler.setRobotRoot(p);
		}
		
		if ((e.getModifiers() & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK) {
			int[] p = { e.getX(), e.getY() };
			EventHandler.setRobotGoal(p);
		}
		
	}

	// MouseMotionListener and MouseListener dummys for implementing interface.
	@Override
	public void mouseDragged(MouseEvent e) {}
	
	@Override
	public void mouseMoved(MouseEvent e) {}
	
	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) { }
}
