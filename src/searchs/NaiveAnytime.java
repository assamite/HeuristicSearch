package searchs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import robot.SearchBot;
import ui.EventHandler;

/** Class that implements naive anytime search by inflating A* heuristic with
 * epsilon modifier. */
public class NaiveAnytime extends AbstractSearch {
	/** Current open list. */
	PriorityQueue<EpsNode> open = new PriorityQueue<EpsNode>();
	/** Helper data structure for ease of referring nodes in open list. */
	HashMap<Integer, EpsNode> openMap = new HashMap<Integer, EpsNode>();
	/** Closed nodes list. */
	private HashMap<Integer, EpsNode> created = new HashMap<Integer, EpsNode>();
	
	protected double e = 4;

	public NaiveAnytime(SearchBot r) {
		super(r);
		this.name = "Naive Anytime";
	}
	
	public NaiveAnytime(SearchBot r, int[] root, int[] goal) {
		// Search from goal to root.
		super(r, goal, root);
		this.name = "Naive Anytime";
	}
	
	public NaiveAnytime(SearchBot r, int[] root, int[] goal, double eps) {
		super(r, goal, root);
		this.e = eps;
		this.name = "Naive Anytime";
	}
	
	@Override
	/** SwingWorker's overrided method. Called when the task is complete. */
	public void done() {
		this.robot.setPlannedPath(this.path);
		this.isRunning = false;
	}
	
	@Override
	/** SwingWorker's overrided method, called when publish is called for 
	 * interim results. Adds chunks to robot's searched node list. */
	protected void process(List<Object> chunks) {
		LinkedList<ArrayList<Node>> paths = new LinkedList<ArrayList<Node>>();
		for (Object o: chunks) {
			if (o instanceof ArrayList<?>) {
				try {
					paths.add((ArrayList<Node>)o); 
					EventHandler.printInfo("NAA* reducing epsilon.");
				}
				catch (ClassCastException e) {
					// Published something else than Node ArrayList!
				}
			}
		}
		if (paths.size() > 0) {
			ArrayList<Node> p = paths.getLast();
			
			while (paths.size() > 0) {
				ArrayList<Node> r = paths.remove();
				chunks.remove(r);
			}
			this.robot.updateSearched(chunks);
			this.robot.setPlannedPath(p);
		}
		else {
			this.robot.updateSearched(chunks);
		}
	}
	
	@Override
	/**
	 * Replan the route
	 * @params changed omitted, since A* does not remember anything and thus
	 * cannot copy with changed information.
	 */
	public synchronized NaiveAnytime replan(double[][] changed) {
		if (!this.isDone()) 
			this.cancel(true);
		while (!this.isDone()) { 
			try {
				Thread.sleep(10); 
			}
			catch (Exception ex) {
				// TODO: do something with this.
			}
		}	
		return new NaiveAnytime(this.robot, this.position, this.root);
	}
	
	@Override
	/** Execute search and publish closed list additions as intermediate 
	 * results. 
	 */
	protected synchronized void search() {	
		while (this.e >= 1.0) {
			this.open = new PriorityQueue<EpsNode>();
			this.created = new HashMap<Integer, EpsNode>();
			EpsNode r = new EpsNode(this.root, 0, this.calcH(this.root, this.goal), this.e);
			r.setMembership(Node.OPEN);
			open.add(r);
			boolean found = false;
			print(this.e + "");
			
			while (!found && !this.open.isEmpty() && !this.isCancelled()) {
				EpsNode n = this.open.remove();
				if (this.isGoal(n)) {
					found = true;
					this.constructPath(n);
					if (this.e == 0) this.publish(this.path);
					continue;
				}
				n.setMembership(Node.CLOSED);
				for (EpsNode c: this.neighbors(n)) {
					int key = c.getHashKey();
					if (this.created.containsKey(key)) {
						double cost = this.getCost(this.map, c.xy);
						if (c.isClosed()) {
							if (n.getG() + cost > c.getG()) { 
								continue;
							}
							else {
								c.setG(n.getG() + cost);
								c.setMembership(Node.OPEN);
								this.open.add(c);
							}
						}
						else if (c.isOpen()) {
							if (c.getG() > n.getG() + cost) {
								c.setG(n.getG() + cost);
								this.open.remove(c);
								this.open.add(c);
							}	
						}
					}
					else {
						c.setMembership(Node.OPEN);
						this.open.add(c);	
						this.created.put(key, c);
					}
				}
				publish(n);
			}
			this.e = this.e - 0.5;
//			 testing
//			try {
//				Thread.sleep(150);
//			}
//			catch (InterruptedException ex) { }
		}
		
	}
	
	/**
	 * Create neighbor nodes for given node.
	 * 
	 * @param n Node whose neighbors are created.
	 * @return
	 */
	protected EpsNode[] neighbors(Node n) {
		ArrayList<int[]> xys = this.getXYs(n.xy);
		double[] costs = this.getCosts(xys);
		double ncost = n.getG();
		EpsNode[] neighbors = new EpsNode[costs.length];
		for (int i = 0; i < costs.length; i++) {
			int[] xy = xys.get(i);
			if (this.created.containsKey(Node.getHashKeyFor(xy))) {
				EpsNode nn = this.created.get(Node.getHashKeyFor(xy));
				neighbors[i] = nn;
			}
			else {
				EpsNode nn = new EpsNode(xy, ncost + costs[i], this.calcH(xy, this.goal), this.e);
				nn.prev = n;
				neighbors[i] = nn;
			}
		}
		return neighbors;
	}
	
	/**
	 * Construct path from root to goal. Is no goal node is given, current
	 * path is set to null;
	 * @param goalNode goal node from which the path is started to construct.
	 */
	protected void constructPath(Node goalNode) {
		if (goalNode != null) {
			//EventHandler.printInfo(String.format("Goal node found with %d expansions", expanded));
			this.path = new ArrayList<Node>();
			this.path.add(goalNode);
			while (goalNode.prev != null) {
				this.path.add(goalNode.prev);
				goalNode = goalNode.prev;
			}
		}
		else {
			this.path = null;	
		}
	}
	
	
}
