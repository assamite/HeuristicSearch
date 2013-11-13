package searchs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import robot.SearchBot;

/**
 * D* Lite search which uses current knowledge of the nodes in replanning phase,
 * when new edge costs are observed.
 * @author slinkola
 *
 */
public class DLite extends AbstractSearch {
	/** Current open list, contains exactly the inconsistent states. */
	protected PriorityQueue<DNode> open = new PriorityQueue<DNode>();
	/** Helper data structure for fast reference to nodes in open list. */
	protected HashMap<Integer, DNode> openMap = new HashMap<Integer, DNode>();
	/** All the nodes generated in the current search. */
	private HashMap<Integer, DNode> expanded = new HashMap<Integer, DNode>();
	/** Current goal node. */
	protected DNode goalNode;
	/** Current root node, changed to the position of the travel when edge
	 * changes are detected. */
	protected DNode rootNode;
	
	public DLite(SearchBot r) {
		super(r);
		this.rootNode = new DNode(
				this.root, Double.MAX_VALUE / 2, 0, Double.MAX_VALUE / 2);
		this.goalNode = new DNode(
				this.goal, Double.MAX_VALUE / 2, this.calcH(this.goal, this.root), 0);
		this.name = "D* Lite";
	}
	
	public DLite(SearchBot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.rootNode = new DNode(
				this.root, Double.MAX_VALUE / 2, 0, Double.MAX_VALUE / 2);
		this.goalNode = new DNode(
				this.goal, Double.MAX_VALUE / 2, this.calcH(this.goal, this.root), 0);
		this.name = "D* Lite";
		
	}
	
	@Override
	/** SwingWorker's overrided method, called when publish is called for 
	 * interim results. Adds chunks to robot's searched node list. */
	protected void process(List<Object> chunks) {
		Object toRemove = null;
		for (Object o: chunks)
			if (o instanceof ArrayList<?>) {
				try {
					ArrayList<Node> n = (ArrayList<Node>)o;
					toRemove = o; 
					this.robot.setPlannedPath(n);
					//this.path = new ArrayList<Node>();
				}
				catch (ClassCastException e) {
					// Published something else than Node ArrayList!
				}
			}
		if (toRemove != null) chunks.remove(toRemove);
		System.out.println("Publishing " + chunks.size());
		synchronized (this.robot) { this.robot.updateSearched(chunks); }
	}
	
	/** Update state of the node. */
	protected void updateState(DNode dn) {
		int dnKey = dn.getHashKey();
		if (!dn.equals(this.goalNode)) {
			double minG = dn.prev.getG();
			Node newPrev = dn.prev;
			for (DNode d: this.succ(dn)) {
				if (d.getG() < minG) {
					minG = d.getG();
					newPrev = d;
				}
			}
			dn.setRhs(minG + this.getCost(this.map, dn.xy));
			dn.prev = newPrev;
			//dn.setRhs(dn.prev.getG() + this.getCost(this.map, dn.xy));
		}
		if (dn.getG() != dn.getRhs()) {
			if (this.openMap.containsKey(dnKey)) {
				//System.out.println("opened again " + dnKey);
				this.openMap.get(dnKey).setValues(dn);
			}
			else {
				this.openMap.put(dnKey, dn);
				this.open.add(dn);
				this.expanded.put(dnKey, dn);
			}
		}
		else if (this.openMap.containsKey(dnKey)) {
			this.openMap.remove(dnKey);
			this.open.remove(dnKey);
		}
	}

	@Override
	protected synchronized void search() {
		this.open.add(this.goalNode);
		this.openMap.put(this.goalNode.getHashKey(), this.goalNode);
		this.expanded.put(this.goalNode.getHashKey(), this.goalNode);
		this.expanded.put(this.rootNode.getHashKey(), this.rootNode);
			
		while (!this.isCancelled() || !(this.position[0] == this.goal[0] && this.position[1] == this.goal[1])) {
			this.robot.clearSearched();
			int key = Node.getHashKeyFor(this.position);
			if (this.expanded.containsKey(key)) {
				this.rootNode = this.expanded.get(key);
				this.rootNode.setG(Double.MAX_VALUE / 2);
				this.rootNode.setRhs(Double.MAX_VALUE / 2);
				System.out.println("Root: " + this.rootNode.xy[0] +" " + this.rootNode.xy[1]);
			}
			this.computeShortestPath(this.rootNode, this.goalNode);
			System.out.println("computed path");
			this.constructPath();
			this.publish(this.path);
			// Wait until changes are observed.
			try {
				this.wait();
			}
			catch (InterruptedException e) {
				// TODO: notify something about this
			}	
		}
	}
	
	/**
	 * Compute shortest path between DNodes r and gl. Computation is done 
	 * "backwards", ie. from gl to r.
	 * @param r current position of the robot, ie. root.
	 * @param gl robot's goal.
	 */
	protected void computeShortestPath(DNode r, DNode gl) {
		if (this.open.isEmpty()) return;
		
		System.out.println(this.open.peek().getRhs() + " " + r.getRhs());
		while (this.open.peek().compareTo(r) < 0 ||
				r.getRhs() != r.getG()) {
			DNode dn = this.open.remove();
			System.out.println(this.open.size() + " " + dn.xy[0] + " " + dn.xy[1] + " " + dn.getRhs() + " " + r.getRhs());
			this.expanded.put(dn.getHashKey(), dn);
			this.publish(dn);
			if (dn.getG() > dn.getRhs()) {
				dn.setG(dn.getRhs());
				for (DNode pred: this.pred(dn)) { 
					this.updateState(pred);
				}
			}
			else {
				dn.setG(Double.MAX_VALUE / 2);
				for (DNode pred: this.pred(dn)) { this.updateState(pred); }
				this.updateState(dn);
			}
		}
	}
	
	/** Create and/or retrieve all neighbors of the node. */
	protected ArrayList<DNode> neighbors(DNode dn) {
		ArrayList<int[]> xys = this.getXYs(dn.xy);
		ArrayList<DNode> neighbors = new ArrayList<DNode>();
		
		for (int[] xy: xys) {
			DNode n;
			if (xy[0] == this.position[0] && xy[1] == this.position[1]) {
				n = this.rootNode;
				if (n.prev == null || n.prev.compareTo(dn) > 0)
					n.prev = dn;
			}
			
			else if (this.expanded.containsKey(Node.getHashKeyFor(xy))) {
				n = this.expanded.get(Node.getHashKeyFor(xy));
				/*
				if (n.prev.compareTo(dn) > 0) {
					n.prev = dn;
					n.setRhs(dn.getG() + this.getCost(this.map, n.xy));
					this.updateState(n);
				}
				*/
 			}
			else {
				n = new DNode(xy, Double.MAX_VALUE / 2, this.calcH(xy, this.root), Double.MAX_VALUE / 2);
				n.prev = dn;
				this.expanded.put(n.getHashKey(), n);
				//this.open.add(n);
				//this.openMap.put(n.getHashKey(), n);
			}
			neighbors.add(n);
		}
		return neighbors;
	} 
	
	protected ArrayList<DNode> succ(DNode dn) {
		ArrayList<DNode> succ = this.neighbors(dn);

		int i = 0;
		double g = dn.getG();
		while (succ.size() > 0 && i < succ.size()) {
			DNode s = succ.get(i);
			if (s.getG() >= g) succ.remove(s);
			else i++;
		}
		return succ;
	}
	
	protected ArrayList<DNode> pred(DNode dn) {
		ArrayList<DNode> pred = this.neighbors(dn);
		int i = 0;
		double g = dn.getG();
		while (pred.size() > 0 && i < pred.size()) {
			DNode s = pred.get(i);
			if (s.getG() < g) pred.remove(s);
			else i++;
		}
		return pred;
	}
	
	/** Construct shortest path for root node to goal node. */
	protected void constructPath() {
		System.out.println("Computing Path");
		Node gn = this.rootNode;
		if (gn != null) {
			this.path = new ArrayList<Node>();
			this.path.add(gn);
			while (gn.prev != null) {
				this.path.add(gn.prev);
				gn = gn.prev;	
			}
			System.out.println("Path computed " + this.path.size());
		}
		else {
			this.path = null;	
		}	
	}
	
	/** Replan the current route with the information of the changed pixels. */
	public synchronized DLite replan(double[][] changed) {
		this.map = this.robot.getMap();
		this.open.clear();
		this.openMap.clear();
		if (changed != null) {
			for (double[] xyv: changed) {
				int[] xy = new int[] {(int)xyv[0], (int)xyv[1]};
				int hashKey = Node.getHashKeyFor(xy);
				int idx = -1;
				for (int i = 0; i < this.path.size(); i++) {
					if (this.path.get(i).getHashKey() == hashKey) {
						idx = i;
						DNode n = this.expanded.get(this.path.get(idx).getHashKey());
						n.setRhs(n.getRhs() + xyv[2]);
					}
				}
				if (idx > -1) {
					for (int i = idx -1; i >= 0; i--) {
					DNode n = this.expanded.get(this.path.get(i).getHashKey());
					System.out.println(n.xy[0] + " " + n.xy[1] + " " + n.getG() + " " + xyv[2]);
					//System.out.println("propagating the path");
					this.updateState(n);	
					//for (DNode d: this.neighbors(n)) this.updateState(d);
					}
				}
				
				else if (this.expanded.containsKey(hashKey)) {
					DNode dn = this.expanded.get(hashKey);
					//System.out.println(dn.xy[0] + " " + dn.xy[1] + " " + dn.getG() + " " + xyv[2]);
					dn.setRhs(dn.getRhs() + xyv[2]);
					for (DNode d: this.neighbors(dn)) this.updateState(d);
				}

			}
			System.out.println("New open size " + this.open.size());
		}
		System.out.println("Expsize " + this.expanded.size());
		//this.expanded.clear();
		//this.expanded.putAll(this.openMap);
		this.path.clear();
		return this;
	}
}
