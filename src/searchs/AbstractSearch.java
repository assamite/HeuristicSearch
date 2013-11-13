package searchs;

import java.awt.image.Raster;
import java.lang.Void;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import robot.Robot;

/**
 * Abstract super class for all the heuristic searchs to obtain coherent 
 * functionality. 
 * @author slinkola
 *
 */
public abstract class AbstractSearch extends SwingWorker<ArrayList<Node>, Object>{
	/** Verbose name of the search algorithm. */
	protected String name = "";
	/** Root, i.e starting position, of the robot. */
	protected int[] root = null;
	/** Goal position of the robot. */
	protected int[] goal = null;
	/** Current position on the path to goal. */
	protected int[] position = null;
	/** Search space as the gray scaled raster image. */
	protected Raster map;
	/** Width of the raster. */
	protected int w = 0;
	/** Height of the raster. */
	protected int h = 0;
	/** Path planned by A*. */
	protected ArrayList<Node> path = null;
	/** Robot which is executing the search. */
	protected Robot robot = null;
	/** Is this search currently running. */
	protected boolean isRunning = false;
	
	public AbstractSearch(Robot r) {
		this.robot = r;
		this.root = this.robot.getRoot();
		this.goal = this.robot.getGoal();
		this.position = this.root;
		this.map = this.robot.getMap();
		this.w = this.map.getWidth();
		this.h = this.map.getHeight();
	}
	
	public AbstractSearch(Robot r, int[] root, int[] goal) {
		this.robot = r;
		this.root = root;
		this.goal = goal;
		this.position = root;
		this.map = this.robot.getMap();
		this.w = this.map.getWidth();
		this.h = this.map.getHeight();
	}
	
	public String getName() {
		return this.name;
	}
	
	public synchronized void setPosition(int[] xy) {
		this.position = xy;
	}
	
	public void setMap(Raster map) {
		this.map = map;
	}
	
	/** Override in subclass for desired functionality. 
	 * 
	 * @params changed All the changed pixels.
	 */
	public synchronized AbstractSearch replan(double[][] changed) {
		return this;
	}
	
	public boolean isRunning() {
		return this.isRunning;
	}
	
	@Override
	/** SwingWorker's overrided method, started when execute() is called from
	 * another thread. */
	public ArrayList<Node> doInBackground() {
		this.isRunning = true;
		this.search();
		return this.path;
	}
	
	@Override
	/** SwingWorker's overrided method. Called when the task is complete. */
	public void done() {
		this.isRunning = false;
	}
	
	@Override
	/** SwingWorker's overrided method, called when publish is called for 
	 * interim results. Adds chunks to robot's searched node list. */
	protected void process(List<Object> chunks) {
		this.robot.updateSearched(chunks);
	}
	
	/** Override in subclass! */
	protected synchronized void search() { }
	
	/**
	 * Returns all possible child pixel coordinates of the x, y pixel in the map.
	 * Currently no diagonal pixels are allowed as childs.
	 * 
	 * @param xy x,y of the pixel in map to get childs from.
	 * @return array of all possible successors.
	 */
	protected ArrayList<int[]> getXYs(int[] xy) {
		int x = xy[0]; int y = xy[1];
		ArrayList<int[]> childs = new ArrayList<int[]>();
		
		if (x > 0) {
			childs.add(new int[] {x - 1, y});
			/*
			if (y > 0) {
				childs.add(new int[] {x - 1, y - 1});
			}
			if (y < this.h) {
				childs.add(new int[] {x - 1, y + 1});
			
			}
			*/
		}
		if (y > 0) childs.add(new int[] {x, y - 1});
		if (x < this.w - 1) {
			childs.add(new int[] {x + 1, y}); 
			/*
			if (y > 0) {
				childs.add(new int[] {x + 1, y - 1});
			}
			if (y < this.h - 1) {
				childs.add(new int[] {x + 1, y + 1});
			}
			*/
		}
			
		if (y < this.h - 1) childs.add(new int[] {x, y + 1});
		
		
		return childs;	
	}
	
	/**
	 * Calculate cost for travelling to xy-pixel from adjacent pixel.
	 * @param r Raster to get cost from.
	 * @param xy
	 * @return Cost for travelling to xy-pixel from adjacent pixel.
	 */
	public double getCost(Raster r, int[] xy) {
		int sample = r.getSample(xy[0], xy[1], 0);
		double cost = Double.MAX_VALUE;
		if (sample == 0) {
			cost = 10000.0;		
		}
		else {
			cost = ((double)(256 - r.getSample(xy[0], xy[1], 0)) / 96) + 1;
		}
		return cost;
	}
	
	/**
	 * @param xys list of pixels to get travel cost to.
	 * @return travel costs to all the pixels in the given ArrayList.
	 */
	protected double[] getCosts(ArrayList<int[]> xys) {
		double[] costs = new double[xys.size()];
		
		for (int i = 0; i < xys.size(); i++) {
			costs[i] = this.getCost(this.map, xys.get(i));
		}
		return costs;
	}
	
	/**
	 * Calculate path cost estimate between points p1 and p2.
	 * @param p1 first point
	 * @param p2 second point
	 * @return heuristic function's cost estimate.
	 */
	protected double calcH(int[] p1, int[] p2) {
		int dx = Math.abs(p1[0] - p2[0]);
		int dy = Math.abs(p1[1] - p2[1]);
		//return Math.sqrt(dx*dx + dy*dy);
		return dx + dy;
	}
	
	/**
	 * Check if given node is goal.
	 * @param n Node to be checked
	 * @return True if the node is goal, false otherwise.
	 */
	protected boolean isGoal(Node n) {
		if (n.xy[0] == this.goal[0] && n.xy[1] == this.goal[1]) return true;
		return false;
	}
}
