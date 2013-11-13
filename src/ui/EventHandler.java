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

import robot.Robot;
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
	
	private static ReentrantLock infoLock = new ReentrantLock();
	
	/** Send bluetooth observation message to currently connected device. */
	public static void observe() {
		
	}
	
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
		MainUI.map.updateRobot(new Robot(map, s));
		return true;
	}
	
	/** Clear current search from map. */
	public static void clearMap() {
		int w = MainUI.map.mapImage.getWidth();
		int h = MainUI.map.mapImage.getHeight();
		MainUI.map.closedImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		SearchType s = MainUI.util.control.getSelectedSearchType();
		MainUI.map.updateRobot(new Robot(MainUI.map.mapImage, s));	
	}
	
	/** Update robot's map. */
	public static void setRobotMap(BufferedImage img, int[] drawn) {
		MainUI.map.clearClosedImage();
		MainUI.map.robot.setMap(img, drawn);
	}
	
	/**
	 * Tell UI to update robot and show message on info panel.
	 * @param r
	 * @param msg
	 */
	public static void updateRobot(Robot r, String msg) {
		MainUI.map.updateRobot(r);
		if (msg != null && msg.length() != 0)
			EventHandler.printInfo(msg);
	}
	
	/** Print info text in UI. Text should not contain any new line characters
	 * or unexpected behaviour may happen. */
	public static void printInfo(String info) {
		while (infoLock.isLocked()) { }
		infoLock.lock();
		MainUI.util.getInfo().println(info);
		infoLock.unlock();
	}
	
	public static void setRobotRoot(int[] point) {
		MainUI.map.robot.setRoot(point);
		MainUI.map.repaint();
	} 
	
	public static void setRobotGoal(int[] point) {
		MainUI.map.robot.setGoal(point);
		MainUI.map.repaint();
	} 
	
	public static void startSearch() {
		MainUI.map.robot.startSearch();
	}
	
	public static void clearSearch() {
		MainUI.map.robot.clearSearch();
		MainUI.map.clearClosedImage();
		MainUI.map.repaint();
	}
	
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
