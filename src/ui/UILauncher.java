package ui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;

import javax.swing.UIManager;

public class UILauncher {
	/**
	 * Application launcher. This is needed so that OS X menu understands 
	 * application name, etc. 
	 * @param args omitted.
	 */
	public static void main(String[] args) {
		// If OS X use native menubar, else reserve space for it.
		boolean isOSX = System.getProperty("os.name").equals("Mac OS X");
		if (isOSX) {
			try {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SearchBot");
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e) {}
		}	
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		new MainUI(width, height);
		try {
			int[] s = {90, 100}; int[] g = {100, 250};
			File f = new File("/Users/slinkola/Eclipse_projects/ADstar/maps/italy.png");
			EventHandler.loadMap(f);
			MainUI.map.robot.setRoot(s);
			MainUI.map.robot.setGoal(g);
		}
		catch (Exception e){
			EventHandler.printInfo("No map loaded.");
		}
	}
}
