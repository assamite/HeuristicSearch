package searchs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import robot.Robot;

public class ARA extends AbstractSearch {
	/** Current open list, which uses private comparator class. */
	PriorityQueue<Node> open = new PriorityQueue<Node>(6000, new ARAComparator());
	/** Helper data structure for ease of referring nodes in open list. */
	HashMap<Integer, Node> openMap = new HashMap<Integer, Node>();
	/** Closed nodes list. */
	private HashMap<Integer, Node> closed = new HashMap<Integer, Node>();
	/** Current inconsistent states. */
	private HashMap<Integer, Node> inconsistent = new HashMap<Integer, Node>();
	/** Current goal node. */
	protected Node goalNode;
	/** Current root node. */
	protected Node rootNode;
	/** Current epsilon. */
	protected double e = 5;
	/** Special comparator for while loops.*/
	private ARAComparator comp = new ARAComparator();

	public ARA(Robot r) {
		super(r);
		this.rootNode = new Node(this.root, Double.MAX_VALUE / 2, 0);
		this.goalNode = new Node(this.goal, 0, this.calcH(this.root, this.goal));
	}
	
	public ARA(Robot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.rootNode = new Node(this.root, Double.MAX_VALUE / 2, 0);
		this.goalNode = new Node(this.goal, 0, this.calcH(this.root, this.goal));
	}
	
	@Override
	/** SwingWorker's overrided method, called when publish is called for 
	 * interim results. Adds chunks to robot's searched node list. */
	protected void process(List<Object> chunks) {
		LinkedList<ArrayList<Node>> toRemove = new LinkedList<ArrayList<Node>>();
		for (Object o: chunks) {
			if (o instanceof ArrayList<?>) {
				try {
					toRemove.add((ArrayList<Node>)o); 
				}
				catch (ClassCastException e) {
					// Published something else than Node ArrayList!
				}
			}
		}
		if (toRemove.size() > 0) {
			this.robot.setPlannedPath(toRemove.getLast());
		}
		while (toRemove.size() > 0) {
			ArrayList<Node> r = toRemove.remove();
			chunks.remove(r);
		}
		this.robot.updateSearched(chunks);
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
	public synchronized ARA replan(double[][] changed) {
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
		return new ARA(this.robot, this.position, this.goal);
	}
	
	@Override
	/** Execute ARA* search and publish closed list additions and path 
	 * improvements as intermediate results. 
	 */
	protected synchronized void search() {	
		this.open.add(this.goalNode);
		this.openMap.put(this.goalNode.getHashKey(), this.goalNode);
		this.improvePath();
		System.out.println(this.goalNode.prev);
		this.constructPath();
		this.publish(this.path);

		while (this.e > 1) {
			this.robot.clearSearched();
			this.e = this.e - 0.5;
			// Ugh. This is quite stupid.
			PriorityQueue<Node> newPQ = new PriorityQueue<Node>(6000, new ARAComparator());
			newPQ.addAll(this.open);
			this.open = newPQ;
			
			for (int key: this.inconsistent.keySet()) {
				this.open.add(this.inconsistent.get(key));
				this.openMap.put(key, this.inconsistent.get(key));
			}
			this.inconsistent.clear();
			this.closed.clear();
			this.improvePath();
			this.constructPath();
			this.publish(this.path);
		}
		
	}
	
	protected void improvePath() {
		if (this.open.isEmpty()) return;
		
		while (this.comp.compare(this.open.peek(), this.rootNode) < 0) {
			Node r = this.rootNode;
			//System.out.println(r.xy[0] + " " + r.xy[1] + " " + r.getG() + " " + e*r.getH());
			Node node = this.open.remove();
			//System.out.println(node.xy[0] + " " + node.xy[1] + " " + node.getG() + " " + e*node.getH());
			this.closed.put(node.getHashKey(), node);
			this.publish(node);
			for (Node n: this.neighbors(node)) {
				if (n.g > this.getCost(this.map, node.xy) + node.g) {
					n.g = this.getCost(this.map, node.xy) + node.g;
					if (!this.closed.containsKey(n.getHashKey())) {
						this.open.add(n);
						this.openMap.put(n.getHashKey(), n);
					}
					else {
						this.inconsistent.put(n.getHashKey(), n);
					}	
				}
			}
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
	
	/** Construct shortest path for root node to goal node. */
	protected void constructPath() {
		Node gn = this.rootNode;
		if (gn != null) {
			this.path = new ArrayList<Node>();
			this.path.add(gn);
			while (gn.prev != null) {
				this.path.add(gn.prev);
				gn = gn.prev;	
			}
		}
		else {
			this.path = null;	
		}	
	}
	
	/** Create and/or retrieve all neighbors of the node. */
	protected ArrayList<Node> neighbors(Node node) {
		ArrayList<int[]> xys = this.getXYs(node.xy);
		ArrayList<Node> neighbors = new ArrayList<Node>();
		
		for (int[] xy: xys) {
			Node n;
			if (xy[0] == this.position[0] && xy[1] == this.position[1]) {
				n = this.rootNode;
				n.setG(node.getG() + this.getCost(this.map, n.xy));
				n.prev = node;
			}
			if (this.closed.containsKey(Node.getHashKeyFor(xy))) {
				//n = this.closed.get(Node.getHashKeyFor(xy));
				continue;
 			}
			else {
				n = new Node(xy, Double.MAX_VALUE / 2, this.calcH(xy, this.root));
				n.prev = node;
			}
			neighbors.add(n);
		}
		return neighbors;
	} 
	
	protected ArrayList<Node> succ(Node n) {
		ArrayList<Node> succ = this.neighbors(n);

		int i = 0;
		double g = n.getG();
		while (succ.size() > 0 && i < succ.size()) {
			Node s = succ.get(i);
			if (s.getG() >= g) succ.remove(s);
			else i++;
		}
		return succ;
	}
	
	protected ArrayList<Node> pred(Node n) {
		ArrayList<Node> pred = this.neighbors(n);
		int i = 0;
		double g = n.getG();
		while (pred.size() > 0 && i < pred.size()) {
			Node s = pred.get(i);
			if (s.getG() < g) pred.remove(s);
			else i++;
		}
		return pred;
	}
	
	@Override
	protected boolean isGoal(Node n) {
		if (n.xy[0] == this.position[0] && n.xy[1] == this.position[1]) return true;
		return false;
	}
	
	/** Private comparator class to get access to e-epsilon. */
	private class ARAComparator implements Comparator<Node> {
		
		public ARAComparator() {
			super();
		}

		public int compare(Node n1, Node n2) {
			if ((n1.getG() + e * n1.getH()) - (n2.getG() + e * n2.getH()) <= 0) 
				return -1;
			else return 1;
		}
		
		public boolean equals(Node n1, Node n2) {
			if ((n1.getG() + e * n1.getH()) - (n2.getG() + e * n2.getH()) == 0)
				return true;
			return false;
		}
	}
}
