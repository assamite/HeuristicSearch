package ui;

import javax.swing.JFrame;

/**
 * AD*'s main UI, which initializes and owns all the UI 
 * components.
 * 
 * @author slinkola
 *
 */
public class MainUI extends JFrame {
	/** Serialization UID. */
	private static final long serialVersionUID = 1L;
	/** Width of the frame. */
	private int width; 	
	/** Height of the frame. */
	private int height;	
	/** Main title of the frame. */
	private String title = "AD*";
	/** Main menus. */
	public static MenuBar menuBar;
	/** Map view of the application. */
	public static Map map;
	/** Utility view of the application. */
	public static Util util;
	
	/**
	 * Default constructor for whole UI. Calling other UI elements' constructors
	 * directly is not supported.
	 * @param width width of the UI frame.
	 * @param height height of the UI frame.
	 */
	public MainUI(int width, int height) {
		this.setTitle(this.title);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		menuBar = new MenuBar();
		this.setJMenuBar(menuBar);
		this.setLocation(0, 0);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setUndecorated(true);
		this.setVisible(true);
		this.height = this.getHeight();
		this.width = this.getWidth();
		
		// TODO: check how menubar behaves on other OS.
		//int offset = 0;
		//if (!isOSX) offset = 20;
		
		util = new Util(this.height, 0);
		this.rootPane.getLayeredPane().add(util);
		int uw = util.getWidth(); 
		map = new Map(this.width - uw, this.height, uw);
		this.rootPane.getLayeredPane().add(map);
		this.rootPane.getLayeredPane().setOpaque(false);
	}
}
