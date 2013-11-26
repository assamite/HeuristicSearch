package searchs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import robot.SearchBot;
import ui.EventHandler;

/**
 * AD* search which uses current knowledge of the nodes in re-planning phase,
 * when new edge costs are observed and speeds up the search by first inflating
 * the heuristic with epsilon (>0) value.
 * 
 * @see http://www.cs.cmu.edu/~maxim/files/ad_icaps05.pdf
 * @author slinkola
 *
 */
public class ADStar extends AbstractSearch {
	/** Current open list, contains exactly the inconsistent states. */
	protected PriorityQueue<ADNode> open = new PriorityQueue<ADNode>();
	/** All the nodes generated in the current search. */
	private HashMap<Integer, ADNode> created = new HashMap<Integer, ADNode>();
	/** Current goal node. */
	protected ADNode goalNode;
	/** Current root node, changed to the position of the travel when edge
	 * changes are detected. */
	protected ADNode rootNode;
	
	/** Current epsilon value. */
	protected double e = 4;
	
	public ADStar(SearchBot r) {
		super(r);
		this.rootNode = new ADNode(
				this.root, Double.MAX_VALUE / 2, 0, Double.MAX_VALUE / 2, this.e);
		this.goalNode = new ADNode(
				this.goal, Double.MAX_VALUE / 2, this.calcH(this.goal, this.root), 0, this.e);
		this.name = "AD*";
	}
	
	public ADStar(SearchBot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.rootNode = new ADNode(
				this.root, Double.MAX_VALUE / 2, 0, Double.MAX_VALUE / 2, this.e);
		this.goalNode = new ADNode(
				this.goal, Double.MAX_VALUE / 2, this.calcH(this.goal, this.root), 0, this.e);
		this.name = "AD*";
		
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
					EventHandler.printInfo("D* Lite found planned path.");
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
	
	
	/** Update state/set membership of the node. */
	protected void updateState(ADNode dn) {
		int dnKey = dn.getHashKey();

		if (dn.getG() != dn.getRhs()) {
			if (!dn.isClosed()) {
				if (dn.isOpen()) {
					this.open.remove(dn);
					this.open.add(dn);	
				}
				else {
					dn.setMembership(Node.OPEN);
					this.open.add(dn);					
				}
			}
			else if (!dn.isInconsistent()) {
				if (dn.isOpen()) this.open.remove(dn);
				dn.setMembership(Node.INCONSISTENT);
			}
		}
		else {
			if (dn.isOpen()) {
				this.open.remove(dnKey);
				dn.setMembership(Node.CLOSED);
			}
			else if (dn.isInconsistent()) {
				dn.setMembership(Node.CLOSED);
			}
		}
	}

	@Override
	protected void search() {
		this.created.put(this.goalNode.getHashKey(), this.goalNode);
		this.created.put(this.rootNode.getHashKey(), this.rootNode);
		this.open.add(this.goalNode);
		this.goalNode.setMembership(Node.OPEN);
			
		while (!this.inGoal()) {
			if (this.isCancelled()) break;
			this.robot.clearSearched();
			print("Epsilon = " + this.e);
			
			synchronized (this.robot.positionLock) {
				int key = Node.getHashKeyFor(this.position);
				if (this.created.containsKey(key)) {
					this.rootNode = this.created.get(key);
					this.rootNode.setH(0.0);
					print("Root: " + this.rootNode.xy[0] +" " + this.rootNode.xy[1]);
				}
			}
			print("Starting to compute shortest path");
			this.computeShortestPath(this.rootNode, this.goalNode);
			print("Shortest path computed");
			
			if (!this.isCancelled()) {
				this.constructPath();
				this.publish(this.path);
			
				if (this.e == 1) {
					// Wait until changes are observed.
					try {
						print("Waiting for changes in search space.");
						synchronized(this) { this.wait(); }
						print("Thread notified. Starting new search iteration.");
					}
					catch (InterruptedException ie) {
						// Do not care about this, user has cancelled the search, etc.
					}
					catch (Exception e) {
						// Houston, we have a problem.
						e.printStackTrace();	
						break;	
					}
				}
				else if (this.e > 1) {
					this.e -= 0.5;	
					
					
					// Ugh. This is quite stupid, but has to be done so that the
					// priorities get updated
					PriorityQueue<ADNode> newPQ = new PriorityQueue<ADNode>();
					for (ADNode n: this.open) { 
						n.setH(this.calcH(n.xy, this.rootNode.xy));
						n.setE(this.e);
						newPQ.add(n); 
					}
					this.open = newPQ;

					for (int key: this.created.keySet()) {
						ADNode an = this.created.get(key);
						an.setH(this.calcH(an.xy, this.rootNode.xy));
						an.setE(this.e);
						if (an.isInconsistent()) { 
							an.setMembership(Node.OPEN);
							this.open.add(an);
						}
						else if (an.isClosed()) {
							an.setMembership(Node.VISITED);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Compute shortest path between ADNodes r and gl. Computation is done 
	 * "backwards", ie. from gl to r.
	 * @param r current position of the robot, ie. root of the search.
	 * @param gl robot's goal.
	 */
	protected void computeShortestPath(ADNode r, ADNode gl) {
		if (this.open.isEmpty()) return;
		
		print(this.open.peek().getRhs() + " " + r.getRhs());
		while ((this.open.peek().compareTo(r) < 0 || r.getRhs() < r.getG())) {
			if (this.isCancelled()) break;
			ADNode dn = this.open.remove();
			this.publish(dn);
			//print(this.open.size() + " " + dn.xy[0] + " " + dn.xy[1]);
			//print(dn.getKey()[0] + " " + dn.getKey()[1] + " " + r.getKey()[0] + " " + r.getKey()[1]);
			
			if (dn.getG() > dn.getRhs()) {
				dn.setG(dn.getRhs());
				dn.setMembership(Node.CLOSED);
				for (ADNode n: this.neighbors(dn)) { 
					if (n.getRhs() > dn.getRhs() + this.getCost(this.map, n.xy)) {
						n.prev = dn;
						n.setRhs(dn.getRhs() + this.getCost(this.map, n.xy));
						this.updateState(n);
					}
					this.updateState(n);
				}
			}
			else {
				dn.setG(Double.MAX_VALUE / 2);
				this.updateState(dn);
				
				for (ADNode n: this.neighbors(dn)) {  
					if (n.prev == dn) {
						double minG = Double.MAX_VALUE / 2;
						Node newPrev = null;
						for (ADNode d: this.neighbors(n)) {
							if (d.getG() < minG) {
								minG = d.getG();
								newPrev = d;
							}
						}
						n.prev = newPrev;
						n.setRhs(minG + this.getCost(this.map, n.xy));
						this.updateState(n);
					}
				}
				
			}
		}
	}
	
	/** Create and/or retrieve all neighbors of the node. */
	protected ArrayList<ADNode> neighbors(ADNode dn) {
		ArrayList<int[]> xys = this.getXYs(dn.xy);
		ArrayList<ADNode> neighbors = new ArrayList<ADNode>();
		
		for (int[] xy: xys) {
			ADNode n;
			if (this.created.containsKey(Node.getHashKeyFor(xy))) {
				n = this.created.get(Node.getHashKeyFor(xy));
 			}
			else {
				n = new ADNode(xy, Double.MAX_VALUE / 2, this.calcH(xy, this.root), Double.MAX_VALUE / 2, this.e);
				this.created.put(n.getHashKey(), n);
			}
			neighbors.add(n);
		}
		return neighbors;
	} 
	
	
	/** Construct shortest path for root node to goal node. */
	protected void constructPath() {
		print("Constructing path");
		try {
			int key = Node.getHashKeyFor(this.position);
			Node gn = this.created.get(key);
			//Node gn = this.rootNode;
			
			if (gn != null) {
				this.path = new ArrayList<Node>();
				this.path.add(gn);
				//print(gn.getHashKey() + "");
				while (gn.prev != null) {
					if (gn == gn.prev.prev) {
						print("argh");
						break;
					}
					this.path.add(gn.prev);
					gn = gn.prev;	
					//print(gn.getHashKey() +"");
				}
				print("Path with length " + this.path.size() + " found.");
			}
			else {
				this.path = null;	
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/** Check is search is currently in goal, i.e. position == goal. */
	public boolean inGoal() {
		synchronized (this.robot.positionLock) {
			if (this.position[0] != this.goal[0]) return false;
			if (this.position[1] != this.goal[1]) return false;
			return true;
		}
	}
	
	/** Replan the current route with the information of the changed pixels. */
	public synchronized ADStar replan(double[][] changed) {
		print("Starting to replan.");
		this.map = this.robot.getMap();
		/*
		while (!this.open.isEmpty()) {
			DNode d = this.open.remove();
			d.setMembership(Node.CLOSED);
		};
		*/
		for (int i: this.created.keySet()) {
			ADNode dn = this.created.get(i);
			dn.setH(this.calcH(dn.xy, this.position));
		}
		
		if (changed != null) {
			for (double[] xyv: changed) {
				int[] xy = new int[] { (int)xyv[0], (int)xyv[1] };
				int hashKey = Node.getHashKeyFor(xy);
				if (this.created.containsKey(hashKey)) {
					ADNode dn = this.created.get(hashKey);
					if (dn != this.goalNode && dn.isVisited()) {
						double minG = Double.MAX_VALUE / 2;
						Node newPrev = null;
						for (ADNode d: this.neighbors(dn)) {
							if (d.getG() < minG) {
								minG = d.getG();
								newPrev = d;
							}
						}
						dn.prev = newPrev;
						dn.setRhs(minG + this.getCost(this.map, dn.xy));
						this.updateState(dn);
					}
				}
			}
			print("New open size " + this.open.size());
		}
		return this;
	}
}
