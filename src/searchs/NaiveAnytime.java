package searchs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

import robot.SearchBot;

/** Class that implements naive anytime search by inflating A* heuristic with
 * epsilon modified. */
public class NaiveAnytime extends AbstractSearch {
	/** Current open list. */
	PriorityQueue<EpsNode> open = new PriorityQueue<EpsNode>();
	/** Helper data structure for ease of referring nodes in open list. */
	HashMap<Integer, EpsNode> openMap = new HashMap<Integer, EpsNode>();
	/** Closed nodes list. */
	private HashMap<Integer, EpsNode> closed = new HashMap<Integer, EpsNode>();
	
	protected double e = 4;

	public NaiveAnytime(SearchBot r) {
		super(r);
		this.name = "Naive Anytime";
	}
	
	public NaiveAnytime(SearchBot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.name = "Naive Anytime";
	}
	
	public NaiveAnytime(SearchBot r, int[] root, int[] goal, double eps) {
		super(r, root, goal);
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
		return new NaiveAnytime(this.robot, this.position, this.goal);
	}
	
	@Override
	/** Execute A* search and publish closed list additions as intermediate 
	 * results. 
	 */
	protected synchronized void search() {	
		while (this.e >= 1.0) {
			this.open.clear();
			this.closed.clear();
			EpsNode r = new EpsNode(this.root, 0, this.calcH(this.root, this.goal), this.e);
			open.add(r);
			this.openMap.put(r.getHashKey(), r);
			boolean found = false;
			
			while (!found && !this.open.isEmpty() && !this.isCancelled()) {
				EpsNode n = this.open.remove();
				if (this.isGoal(n)) {
					found = true;
					this.constructPath(n);
					continue;
				}
				this.closed.put(n.getHashKey(), n);
				EpsNode[] childs = this.childs(n);
				for (EpsNode c: childs) {
					int key = c.getHashKey();
					if (this.closed.containsKey(key)) {
						EpsNode c1 = this.closed.get(key);
						if (c.getG() >= c1.getG()) { 
							continue;
						}
					}
					if (this.openMap.containsKey(key)) {
						EpsNode c1 = this.openMap.get(key);
						if (c.getG() < c1.getG()) {
							c1.setG(c.getG());
						}
					}
					else {
						this.open.add(c);	
						this.openMap.put(key, c);
					}
				}
				publish(n);
			}
			this.e = this.e - 0.5;
			System.out.println(this.e);
			// testing
			try {
				Thread.sleep(150);
			}
			catch (InterruptedException ex) { }
		}
		
	}
	
	/**
	 * Create child nodes for given node.
	 * 
	 * @param n Node whose childs are created.
	 * @return
	 */
	protected EpsNode[] childs(Node n) {
		ArrayList<int[]> xys = this.getXYs(n.xy);
		double[] costs = this.getCosts(xys);
		double ncost = n.getG();
		EpsNode[] childs = new EpsNode[costs.length];
		for (int i = 0; i < costs.length; i++) {
			int[] xy = xys.get(i);
			childs[i] = new EpsNode(xy, ncost + costs[i], this.calcH(xy, this.goal), this.e);
			childs[i].prev = n;
		}
		return childs;
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
			Collections.reverse(this.path);
		}
		else {
			this.path = null;	
		}
	}
	
	
}
