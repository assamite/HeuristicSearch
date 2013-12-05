package ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import robot.SearchBot;
import searchs.Node;
import searchs.SearchType;


/**
 * Main interface between UI and program logic. Handles information exchange to 
 * and from the UI.
 * 
 * @author slinkola
 *
 */
public class EventHandler {
	
	private static Object infoLock = new Object();

	/** Load image map, convert it to gray scale and assemble it to UI. */
	public static boolean loadMap(File file) {
		BufferedImage map;
		try {
			map = ImageIO.read(file);
		}
		catch (IOException e) {
			return false;
		}
		String msg = String.format("Loaded image: %s", file.getAbsolutePath());
		EventHandler.printInfo(msg);
		
		ColorConvertOp op = 
			    new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		op.filter(map, map);
		MainUI.map.setMapImage(map);
		SearchType s = MainUI.util.control.getSelectedSearchType();
		MainUI.map.updateRobot(new SearchBot(map, s));
		return true;
	}
	
	/** Clear current search from map. */
	public static void clearMap() {
		int w = MainUI.map.mapImage.getWidth();
		int h = MainUI.map.mapImage.getHeight();
		MainUI.map.clearSearchedImage();
		SearchType s = MainUI.util.control.getSelectedSearchType();
		MainUI.map.updateRobot(new SearchBot(MainUI.map.mapImage, s));	
	}
	
	/** Update robot's map with img and tell robot that the line between
	 * drawn [0, 1] and [2, 3] was drawn so that it knows to search for
	 * differencies on images from that section. 
	 * 
	 * @param img map from which robot gets its Raster data
	 * @param drawn atleast 4-length array from which line is extracted as
	 * {x1, y1, x2, y2..}, rest of the array is not taken into account.
	 * */
	public static void setRobotMap(BufferedImage img, int[] drawn) {
		MainUI.map.clearSearchedImage();
		MainUI.map.robot.setMap(img, drawn);
	}
	
	/**
	 * Tell UI to update robot and show message on info panel.
	 * @param r Robot to update
	 * @param msg alternative message, if null or "" is not shown.
	 */
	public static void updateRobot(SearchBot r, String msg) {
		MainUI.map.updateRobot(r);
		if (msg != null && msg.length() != 0)
			EventHandler.printInfo(msg);
	}
	
	/** Print info text in UI. Text should not contain any new line characters
	 * or unexpected behaviour may happen. All printing is done inside infoLock.
	 * */
	public static void printInfo(String info) {
		synchronized (EventHandler.infoLock) {
			MainUI.util.getInfo().println(info);
		}
	}
	
	public static void setRobotRoot(int[] point) {
		MainUI.map.robot.setRoot(point);
		MainUI.map.repaint();
	} 
	
	public static void setRobotGoal(int[] point) {
		MainUI.map.robot.setGoal(point);
		MainUI.map.repaint();
	} 
	
	/** Start robot's current search. */
	public static void startSearch() {
		MainUI.map.robot.startSearch();
	}
	
	/** Clear robot's current search. */
	public static void clearSearch() {
		MainUI.map.robot.clearSearch();
		MainUI.map.clearSearchedImage();
		MainUI.map.repaint();
	}
	
	/** Change search algorithm to s. */
	public static void changeSearchType(SearchType s) {
		MainUI.map.robot.changeSearchType(s);
	}
	
	public static void repaintMap() {
		MainUI.map.repaint();
	}
	
	public static void setDrawingColor(int value) {
		MainUI.map.setDrawingColor(value);
	}

}
