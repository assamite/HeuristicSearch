package searchs;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import robot.SearchBot;

/**
 * Basic A* search.
 * 
 * @see https://en.wikipedia.org/wiki/A*
 * @author slinkola
 *
 */
public class AStar extends AbstractSearch {
	/** Current open list. */
	PriorityQueue<Node> open = new PriorityQueue<Node>();
	/** All created nodes. */
	HashMap<Integer, Node> created = new HashMap<Integer, Node>();
	/** Closed nodes list. */
	private HashMap<Integer, Node> closed = new HashMap<Integer, Node>();

	public AStar(SearchBot r) {
		super(r);
		this.name = "A*";
	}
	
	public AStar(SearchBot r, int[] root, int[] goal) {
		// Search from goal to root.
		super(r, goal, root);
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
		return new AStar(this.robot, this.position, this.root);
	}
	
	@Override
	/** Execute A* search and publish closed list additions as intermediate 
	 * results. 
	 */
	protected synchronized void search() {	
		Node r = new Node(this.root, 0, this.calcH(this.root, this.goal));
		open.add(r);
		this.created.put(r.getHashKey(), r);
		boolean found = false;
		Node gn = null;	// goal node.
		
		while (!found && !this.open.isEmpty() && !this.isCancelled()) {
			Node n = this.open.remove();
			if (this.isGoal(n)) {
				found = true;
				this.constructPath(n);
				continue;
			}
			this.closed.put(n.getHashKey(), n);
			n.setMembership(Node.CLOSED);
			Node[] childs = this.childs(n);
			for (Node c: childs) {
				int key = c.getHashKey();
				if (this.created.containsKey(key)) {
					double cost = this.getCost(this.map, c.xy);
					if (c.isClosed()) {
						if (n.getG() + cost >= c.getG()) { 
							continue;
						}
						else {
							c.setG(n.getG() + cost);
							c.setMembership(Node.OPEN);
							this.open.add(c);
						}
					}
					else if (c.isOpen()) {
						if (c.getG() < n.getG() + cost) {
							c.setG(c.getG() + cost);
							this.open.remove(c);
							this.open.add(c);
						}
						
					}
					
				}
				else {
					this.open.add(c);	
					this.created.put(key, c);
				}
			}
			publish(n);
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
			if (this.created.containsKey(Node.getHashKeyFor(xy))) {
				childs[i] = this.created.get(Node.getHashKeyFor(xy));
			}
			else {
				childs[i] = new Node(xy, ncost + costs[i], this.calcH(xy, this.goal));
				childs[i].prev = n;
			}
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
		}
		else {
			this.path = null;	
		}
	}
}