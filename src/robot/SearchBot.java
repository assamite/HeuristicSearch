package robot;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import searchs.AStar;
import searchs.DLite;
import searchs.Node;
import searchs.SearchType;
import searchs.AbstractSearch;
import searchs.SearchFactory;
import ui.EventHandler;
import ui.UIScheme;
import util.Calc;
import util.Point;
/**
 * Search robot, which owns the search algorithm and communicates with the 
 * search worker while it is running.
 * 
 * @author slinkola
 *
 */
public class SearchBot {	
	/** Serialization UID. */
	private static final long serialVersionUID = 1L;
	/** Currently traveled path as list of x,y pairs. */
	private ArrayList<int[]> traveledPath = new ArrayList<int[]>();
	/** Current planned path as list of x,y pairs. */
	private ArrayList<Node> plannedPath = new ArrayList<Node>(); 
	/** Root, i.e starting position, of the robot. */
	private int[] root = null;
	/** Goal position of the robot. */
	private int[] goal = null;
	/** Current position of the robot. */
	private int[] position = null;
	/** Current map of the robot as a greyscale image's raster. Darker shade 
	 *  means more time spent on traveling to the pixel. */
	private Raster map;
	/** Current heuristic search type. */
	private SearchType searchType = SearchType.ASTAR;
	/** Current heuristic search algorithm instance for the robot. */
	private AbstractSearch search = null;
	/** Currenly searched nodes. Maintained here for thread safe UI updating. */
	private ArrayList<int[]> searched = new ArrayList<int[]>();
	private ArrayList<int[]> undrawn = new ArrayList<int[]>();
	/** Timer for traveling the planned path after it has been set. Replanning
	 * and clearing the search will cancel the timer and setting the planned 
	 * path (to something else than null) will create new timer and start it. */
	private Timer travelTimer = new Timer();
	/** Is robot currently travelling along a planned path. */
	private boolean isTravelling = false;
	/** Has searhing for path started. At some point after search is started 
	 * robot may be also travelling. */
	public boolean isSearchStarted = false;
	/** Color of the drawn searched nodes. */
	private int[] searchedColor = {255, 0, 255, 60};
	
	private ArrayList<Node> storedPath = new ArrayList<Node>(); 
	/** Object which can be used in the synchronized -block when updating 
	 * position status.*/
	public Object positionLock = new Object();
	/** Object which can be used in the synchronized -block when updating 
	 * or clearing searched list.*/
	public Object searchedLock = new Object();
	
	public SearchBot(BufferedImage map, SearchType searchType) {
		this.map = map.getData();	
		this.searchType = searchType;
		//EventHandler.printInfo(this.map.getMinX() + " " + this.map.getMinY());
	}
	
	/**
	 * Constructor for starting robot with root and goal set.
	 * @param root starting position of the robot; x,y -pair
	 * @param goal goal position of the robot; x,y -pair
	 * @param map map for the robot as greyscale image.
	 * @param searchType Type of heuristic search from Search enum.
	 */
	public SearchBot(int[] root, int[] goal, BufferedImage map, SearchType searchType) {
		this.root = root;
		this.goal = goal;
		this.position = root;
		this.map = map.getData();	
		this.searchType = searchType;
		this.search = SearchFactory.createSearch(searchType, this, root, goal);
	}
	
	// Some setters and getters
 	public int[] getRoot() { return this.root; }
 	public int[] getGoal() { return this.goal; }
	public int[] getPosition() { return this.position; }
	public ArrayList<int[]> getTraveledPath() { return this.traveledPath; }
	public ArrayList<Node> getPlannedPath() { return this.plannedPath; }
	public Raster getMap() { return this.map; }
	
	public void setRoot(int[] root) { 
		this.root = root; 
		this.position = root;
		if (this.goal != null) {
			this.search = SearchFactory.createSearch(this.searchType, this, this.root, this.goal);
			EventHandler.printInfo("Robot set up " + this.search.getName() +".");
		}
	}
	
 	public void setGoal(int[] goal) { 
 		this.goal = goal; 
 		if (this.root != null) {
 			this.search = SearchFactory.createSearch(this.searchType, this, this.root, this.goal);
 			EventHandler.printInfo("Robot set up " + this.search.getName() +".");
 		}
 	}
	
	/** 
	 * Set updated map of the robot's surroundings and replan the route from 
	 * current travel position to the goal. 
	 * 
	 * @params map Updated map.
	 * @param drawn possible 4-length int array consisting of drawn line's end
	 * points.
	 * */
	public void setMap(BufferedImage map, int[] drawn) {
		double[][] changed = null;
		Raster newMap = map.getData();
 		if (drawn != null) {
 			changed = this.observeChanges(newMap, drawn);
		}
		this.map = newMap;
		if (this.isSearchStarted) this.replan(changed);
	}
	
 	/** Callback for searches to set new planned path, either in the middle 
 	 * of the search or at the end. */
 	public void setPlannedPath(ArrayList<Node> path) {
 		if (path != null && path.size() > 0) {
 			this.plannedPath = path;
 			/*
 			try {
 			this.storedPath = new ArrayList<Node>();
 			for (Node n: this.plannedPath) { this.storedPath.add(n.clone()); }
 			this.plannedPath = path;
 			System.out.println(this.storedPath.size());
 			if (this.storedPath.size() != 0) {
 				if (this.storedPath.size() != this.plannedPath.size()) 
 					System.out.println(this.storedPath.size() + " != " + this.plannedPath.size());
 				else {
 					for (int i = 0; i < path.size(); i++) {
 						Node n1 = this.plannedPath.get(i);
 						Node n2 = this.storedPath.get(i);
 						System.out.println(n1.xy[0] + " " + n1.xy[1] + " " + 
 						n1.getG() + " - " + n2.xy[0] + " " + n2.xy[1] + " " + n2.getG() + " = " + (n1.getG() - n2.getG()));
 						//if (n1.getG() != n2.getG()) System.out.println("OOOOOOOPS!");
 					}
 				}
 			}
 			}
 			catch (Exception e) {
 				e.printStackTrace();
 			}
			*/
 			String msg = String.format(this.search.getName() + 
 					" found path, searched: " + this.searched.size());
 			EventHandler.printInfo(msg);
 			//msg = String.format("Path length %d, cost %.5f", 
 			//		path.size(), this.getPathCost());
 			//EventHandler.updateRobot(this, msg);
 			this.startTravel(100);
 		}
 		else {
 			EventHandler.updateRobot(this, "No path found!");
 		}
 	}
 	
 	/** Get cost of currently planned path */
 	public double getPathCost() {
 		double c = 0.0;
 		for (Node n: this.plannedPath) {
 			c += this.search.getCost(this.search.getMap(), n.xy);
 		}
 		return c;
 	}
	
	/**
	 * Update currently searched nodes.
	 * @param chunks chunks of Node objects send by process()-method of current
	 * search.
	 */
	public void updateSearched(List<Object> chunks) {
		synchronized (this.searchedLock) {
			for (Object o: chunks) {
				int[] xy = ((Node)o).xy;
				this.searched.add(xy);
				this.undrawn.add(xy);
			}
		}
		EventHandler.updateRobot(this, null);
	}
	
	public synchronized void clearSearched() {
		System.out.println("Robot: Clearing search.");
		this.searched.clear();
		this.undrawn.clear();
		EventHandler.updateRobot(this, null);	
	}
	
	/** Start searching for path plan. Replanning is done automatically when
	 * setMap is called. */
	public void startSearch() {	
		if (this.search != null) {
			this.isSearchStarted = true;
			EventHandler.printInfo("Starting " + this.search.getName() + " search with:");
			EventHandler.printInfo(String.format("Root: (%d, %d), Goal: (%d, %d)", this.root[0], this.root[1], this.goal[0], this.goal[1]));
			this.search.execute();
		}
		else {
			EventHandler.printInfo("Robot has no search set!");
		}
	}
	
	/** Clear robot's search, and create new search algorithm instance. */
	public void clearSearch() {
		System.out.println("Robot: Clearing search.");
		if (this.search != null) {
			if (!this.search.isDone()) 
				this.search.cancel(true);
			while (!this.search.isDone()) { 
				try {
					Thread.sleep(10); 
				}
				catch (Exception e) {
					// TODO: do something with this.
				}
			}
 			this.search = SearchFactory.createSearch(this.searchType, this, this.root, this.goal);
		}
		this.traveledPath.clear();
		this.plannedPath.clear();
		this.travelTimer.cancel();
		this.travelTimer.purge();
		synchronized (this.searchedLock) { 
			this.searched.clear(); 
			this.undrawn.clear();
		}
		this.isSearchStarted = false;
	}
	
	/**
	 * Start traveling planned path by setting up a timer to call travel() 
	 * method with fixed intervals.
	 * 
	 * @params interval Travel interval in milliseconds
	 */
	public void startTravel(int interval) {
		
		this.travelTimer.cancel();
		this.travelTimer.purge();
		this.travelTimer = null;
		TimerTask traveler = new TimerTask() {
			public void run() {
				travel();
			}
		};
		this.travelTimer = new Timer();
		this.travelTimer.schedule(traveler, 0, interval);
	}
	
	/**
	 * Remove one node from planned path and put it's location to traveled path.
	 */
	public void travel() {
		if (this.plannedPath != null && !this.plannedPath.isEmpty()) {
			int[] xy = this.plannedPath.remove(0).xy;
			this.traveledPath.add(xy);
			//synchronized (this.positionLock) 
			this.position = xy;
			this.search.setPosition(xy);
			EventHandler.repaintMap();
		}
		else {
			this.travelTimer.cancel();
			this.travelTimer.purge();
		}
		if (this.position[0] == this.goal[0] && this.position[1] == this.goal[1]) {
			this.search.cancel(true);
		}
	}
	
	/** 
	 * Replan the planned path from current position to goal. Called when the 
	 * new map is set.
	 */ 
	private void replan(double[][] changed) {
		this.travelTimer.cancel();
		this.travelTimer.purge();
		synchronized (this.searchedLock) { this.searched.clear(); }
		this.plannedPath.clear();
		
		this.search = this.search.replan(changed);
		synchronized(this.search) { 
			System.out.println("Robot: Notifying search.");
			this.search.notify();
			System.out.println("Robot: Search notified.");		
		}
		if (this.isSearchStarted && !this.search.isRunning())
			this.search.execute();
	}
	
	/**
	 * Observe which pixels (nodes) were changed during the drawing.
	 * @param newMap map to check changes against current map.
	 * @param drawn 4-length array {x1, y1, x2, y2} of the drawn line's end 
	 * ponts.
	 * 
	 * @return All changed pixel's coordinates and value changes int[3][n] array.
	 */
	private double[][] observeChanges(Raster newMap, int[] drawn) {
		int[] r = Calc.getRectangle(drawn);
		int x = r[0] - 5 < 0 ? 0 : r[0] - 5; 
		int y = r[1] - 5 < 0 ? 0 : r[1] - 5;
		int w = x + r[2] + 5 > newMap.getWidth() ? newMap.getWidth() - x : r[2] + 5; 
		int h = y + r[3] + 5 > newMap.getHeight() ? newMap.getHeight() - y : r[3] + 5;
		int[] xy = new int[2];
		ArrayList<double[]> changes = new ArrayList<double[]>();
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				if (this.map.getSample(i, j, 0) != newMap.getSample(i, j, 0)) {
					xy[0] = i; xy[1] = j;
					double c1 = this.search.getCost(this.map, xy);
					double c2 = this.search.getCost(newMap, xy);
					changes.add(new double[] {i, j, c2 - c1});
				}
			}
		}
		return changes.toArray(new double[3][changes.size()]);
	}
	
	/**
	 * Draw searched node set to raster by coloring pixels one by one.
	 * @param r Raster to draw closed set.
	 */
	public synchronized void drawSearched(WritableRaster r) {
		if (this.undrawn != null && this.undrawn.size() > 0) {
			for (int[] xy: this.undrawn) { 
				r.setPixel(xy[0], xy[1], this.searchedColor);
			}
			this.undrawn.clear();
		}
	}
	
	/** 
	 * Draw robot on given graphic context.
	 * 
	 * @param gc Graphics context to draw robot in.
	 */
	public void draw(Graphics2D gc) {
		if (this.root != null) {
			gc.setColor(UIScheme.RED);
			gc.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			gc.drawArc(this.root[0]-6, this.root[1]-6, 13, 13, 0, 360);
		}
		if (this.goal != null) {
			gc.setColor(UIScheme.BLUE);
			gc.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			gc.drawArc(this.goal[0]-6, this.goal[1]-6, 13, 13, 0, 360);
		}	
		this.drawPath(gc);
	}

	/**
	 * Draw robot's path.
	 * 
	 * @param gc Graphics context to draw path in.
	 */
	private void drawPath(Graphics2D gc) {
		gc.setColor(UIScheme.BLUE_D);
		gc.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL));
		for (int i = 0; i < this.storedPath.size(); i++) {
			int[] p1 = this.storedPath.get(i).xy;
			int x = (int)Math.floor(p1[0]);
			int y = (int)Math.floor(p1[1]);
			gc.drawRect(x, y, 1, 1);
		}
		
		gc.setColor(UIScheme.MAGENTA);			
		for (int i = 0; i < this.traveledPath.size(); i++) {
			int[] p1 = this.traveledPath.get(i);
			int x = (int)Math.floor(p1[0]);
			int y = (int)Math.floor(p1[1]);
			gc.drawRect(x, y, 1, 1);
		}

		gc.setColor(UIScheme.CYAN);
		for (int i = 0; i < this.plannedPath.size(); i++) {
			int[] p1 = this.plannedPath.get(i).xy;
			int x = (int)Math.floor(p1[0]);
			int y = (int)Math.floor(p1[1]);
			gc.drawRect(x, y, 1, 1);
		}
	}

	public void changeSearchType(SearchType s) {
		this.searchType = s;
		if (!this.isSearchStarted && this.root != null && this.goal != null)
			this.search = SearchFactory.createSearch(s, this, this.root, this.goal);
	}
}
