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
	/** All created nodes. */
	private HashMap<Integer, EpsNode> created = new HashMap<Integer, EpsNode>();
	
	private EpsNode rootNode = null;
	private EpsNode goalNode = null;
	
	protected double e = 4;

	public NaiveAnytime(SearchBot r) {
		super(r);
		this.name = "NAA*";
	}
	
	public NaiveAnytime(SearchBot r, int[] root, int[] goal) {
		// Search from goal to root.
		super(r, root, goal);
		this.rootNode = new EpsNode(this.root, Double.MAX_VALUE / 2, 0, this.e);
		this.goalNode = new EpsNode(this.goal, 0, this.calcH(this.goal, this.root), this.e);
		this.name = "NAA*";
	}
	
	public NaiveAnytime(SearchBot r, int[] root, int[] goal, double eps) {
		this(r, root, goal);
		this.rootNode = new EpsNode(this.root, Double.MAX_VALUE / 2, 0, this.e);
		this.goalNode = new EpsNode(this.goal, 0, this.calcH(this.goal, this.root), this.e);
		this.e = eps;
	}
	
	@Override
	/** SwingWorker's overrided method, called when publish is called for 
	 * interim results. Adds chunks to robot's searched node list. */
	protected void process(List<Object> chunks) {
		try {
			LinkedList<ArrayList<Node>> paths = new LinkedList<ArrayList<Node>>();
			for (Object o: chunks) {
				if (o instanceof ArrayList<?>) {
					try {
						paths.add((ArrayList<Node>)o); 
					}
					catch (ClassCastException e) {
						e.printStackTrace();
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				ex.printStackTrace();
				// TODO: do something with this.
			}
		}	
		return new NaiveAnytime(this.robot, this.position, this.goal);
	}
	
	@Override
	/** Execute search and publish closed list additions as intermediate 
	 * results. 
	 */
	protected synchronized void search() {	
		Node[] publishArray = new Node[2000];
		
		while (this.e >= 1.0) {
			int i = 0;
			this.robot.clearSearched();
			try {
				Thread.sleep(150);
			}
			catch (Exception e) {}
			this.open = null;
			this.open = new PriorityQueue<EpsNode>();
			this.created = null;
			this.created = new HashMap<Integer, EpsNode>();
			 
			this.rootNode = new EpsNode(this.position, Double.MAX_VALUE / 2, 0, this.e);
			this.rootNode.setMembership(Node.CLOSED);
			this.created.put(this.rootNode.getHashKey(), this.rootNode);
			print("Root: " + this.rootNode.xy[0] +" " + this.rootNode.xy[1]);

			this.goalNode = new EpsNode(this.goal, 0, this.calcH(this.goal, this.rootNode.xy), this.e);
			this.created.put(this.goalNode.getHashKey(), this.goalNode);
			this.open.add(this.goalNode);
			boolean found = false;
			print("Epsilon = " + this.e);
			print("Starting to compute path.");
			
			while (!found && !this.open.isEmpty() && !this.isCancelled()) {
				EpsNode n = this.open.remove();
				n.setMembership(Node.CLOSED);
				
				if (n.equals(this.rootNode)) { // Searching backwards
					print("Path computed.");
					found = true;
					this.constructPath();
					//if (this.e == 0) 
						this.publish(this.path);
					break;
				}
				
				for (EpsNode c: this.neighbors(n)) {
					int key = c.getHashKey();
					if (this.created.containsKey(key)) {
						double cost = this.getCost(this.map, c.xy);
						if (c.isClosed()) {
							if (n.getG() + cost >= c.getG()) { 
								continue;
							}
							else {
								c.setG(n.getG() + cost);
								c.prev = n;
								c.setMembership(Node.OPEN);
								this.open.add(c);
							}
						}
						else if (c.isOpen()) {
							if (c.getG() > n.getG() + cost) {
								c.setG(n.getG() + cost);
								c.prev = n;
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
				publishArray[i] = n;
				if (i == publishArray.length - 1) {
					this.publish((Object[])publishArray);
					i = 0;
				}
				else {
					i++;
				}
			}
			if (found) {
				if (i != 0 && i < publishArray.length - 1) {
					Node[] a = new Node[i];
					for (int j = 0; j < i; j++) a[j] = publishArray[j];
					this.publish((Object[])a);
				}
			}
			else {
				print("Path not found.");
				break;
			}
			
			this.e -= 0.5;
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
	/** Create and/or retrieve all neighbors of the node. */
	protected ArrayList<EpsNode> neighbors(EpsNode an) {
		ArrayList<int[]> xys = this.getXYs(an.xy);
		ArrayList<EpsNode> neighbors = new ArrayList<EpsNode>();
		
		for (int[] xy: xys) {
			EpsNode n;

			if (xy[0] == this.rootNode.xy[0] && xy[1] == this.rootNode.xy[1]) {
				print("Root node!");
				n = this.rootNode;
				if (an.getG() + this.getCost(this.map, n.xy) < n.getG()) {
					n.setG(an.getG() + this.getCost(this.map, n.xy));
					n.prev = an;
				}
			}

			else if (this.created.containsKey(Node.getHashKeyFor(xy))) {
				n = this.created.get(Node.getHashKeyFor(xy));
 			} 
			else {
				n = new EpsNode(xy, an.getG() + this.getCost(this.map, xy) , this.calcH(xy, this.root), this.e);
				n.prev = an;
			}
			neighbors.add(n);
		}
		return neighbors;
	} 
	
	/** Construct shortest path for root node to goal node. */
	protected void constructPath() {
		print("Constructing path.");
		Node gn = this.rootNode;
		if (gn != null) {
			//EventHandler.printInfo(String.format("Goal node found with %d expansions", expanded));
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
	
	
}
