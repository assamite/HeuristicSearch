package searchs;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import robot.Robot;

public class AStar extends AbstractSearch {
	/** Current open list. */
	PriorityQueue<Node> open = new PriorityQueue<Node>();
	/** Helper data structure for ease of referring nodes in open list. */
	HashMap<Integer, Node> openMap = new HashMap<Integer, Node>();
	/** Closed nodes list. */
	private HashMap<Integer, Node> closed = new HashMap<Integer, Node>();

	public AStar(Robot r) {
		super(r);
		this.name = "A*";
	}
	
	public AStar(Robot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.name = "A*";
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
	public synchronized AStar replan(double[][] changed) {
		if (!this.isDone()) 
			this.cancel(true);
		while (!this.isDone()) { 
			try {
				Thread.sleep(10); 
			}
			catch (Exception e) {
				// TODO: do something with this.
			}
		}	
		return new AStar(this.robot, this.position, this.goal);
	}
	
	@Override
	/** Execute A* search and publish closed list additions as intermediate 
	 * results. 
	 */
	protected synchronized void search() {	
		Node r = new Node(this.root, 0, this.calcH(this.root, this.goal));
		open.add(r);
		this.openMap.put(r.getHashKey(), r);
		boolean found = false;
		Node gn = null;	// goal node.
		
		while (!found && !this.open.isEmpty() && !this.isCancelled()) {
			Node n = this.open.remove();
			if (this.isGoal(n)) {
				found = true;
				gn = n;
				continue;
			}
			this.closed.put(n.getHashKey(), n);
			Node[] childs = this.childs(n);
			for (Node c: childs) {
				int key = c.getHashKey();
				if (this.closed.containsKey(key)) {
					Node c1 = this.closed.get(key);
					if (c.getG() >= c1.getG()) { 
						continue;
					}
				}
				if (this.openMap.containsKey(key)) {
					Node c1 = this.openMap.get(key);
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
		
		if (gn != null) {
			//EventHandler.printInfo(String.format("Goal node found with %d expansions", expanded));
			this.path = new ArrayList<Node>();
			this.path.add(gn);
			while (gn.prev != null) {
				this.path.add(gn.prev);
				gn = gn.prev;
			}
			Collections.reverse(this.path);
		}
		else {
			this.path = null;	
		}
	}
	
	/**
	 * Create child nodes for given node.
	 * 
	 * @param n Node whose childs are created.
	 * @return
	 */
	protected Node[] childs(Node n) {
		ArrayList<int[]> xys = this.getXYs(n.xy);
		double[] costs = this.getCosts(xys);
		double ncost = n.getG();
		Node[] childs = new Node[costs.length];
		for (int i = 0; i < costs.length; i++) {
			int[] xy = xys.get(i);
			childs[i] = new Node(xy, ncost + costs[i], this.calcH(xy, this.goal));
			childs[i].prev = n;
		}
		return childs;
	}
}