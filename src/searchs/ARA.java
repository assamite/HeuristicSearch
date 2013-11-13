package searchs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import robot.SearchBot;
import ui.EventHandler;

/**
 * Anytime A* (ARA*) search which first plans a fast epsilon-admissible 
 * suboptimal path and iterates better paths by reducing epsilon, finally 
 * creating the same path as A* when epsilon = 1.
 * 
 * @see http://machinelearning.wustl.edu/mlpapers/paper_files/NIPS2003_CN03.pdf
 * @author slinkola
 *
 */
public class ARA extends AbstractSearch {
	/** Current open list, which uses private comparator class. */
	PriorityQueue<ARANode> open = new PriorityQueue<ARANode>();
	/** Helper data structure for ease of referring nodes in open list. */
	//HashMap<Integer, Node> openMap = new HashMap<Integer, Node>();
	/** All extended states. */
	private HashMap<Integer, ARANode> expanded = new HashMap<Integer, ARANode>();
	/** Current goal node. */
	protected ARANode goalNode;
	/** Current root node. */
	protected ARANode rootNode;
	/** Current epsilon. */
	protected double e = 5;

	public ARA(SearchBot r) {
		super(r);
		this.rootNode = new ARANode(this.root, Double.MAX_VALUE / 2, 0, this.e);
		this.goalNode = new ARANode(this.goal, 0, this.calcH(this.root, this.goal), this.e);
	}
	
	public ARA(SearchBot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.rootNode = new ARANode(this.root, Double.MAX_VALUE / 2, 0, this.e);
		this.goalNode = new ARANode(this.goal, 0, this.calcH(this.root, this.goal), this.e);
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
	/** SwingWorker's overrided method. Called when the task is complete. */
	public void done() {
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
		this.open = new PriorityQueue<ARANode>();
		this.open.add(this.goalNode);
		//this.openMap.put(this.goalNode.getHashKey(), this.goalNode);
		this.improvePath();
		this.constructPath();
		this.publish(this.path);

		while (this.e > 1.01) {
			this.robot.clearSearched();
			this.e = this.e - 0.5;
			EventHandler.printInfo("ARA* reducing epsilon to: " + this.e);
			// Ugh. This is quite stupid, but has to be done so that the
			// priorities get updated
			PriorityQueue<ARANode> newPQ = new PriorityQueue<ARANode>();
			for (ARANode n: this.open) { 
				n.setE(this.e);
				newPQ.add(n); 
			}
			this.open = newPQ;
			/*
			ARANode[] test = this.open.toArray(new ARANode[this.open.size()]);
			System.out.println(test.length);
			for (int i = 0; i < test.length -1; i++) {
				ARANode n1 = test[i];
				ARANode n2 = test[i +1];
				if (n1.getKey() > n2.getKey()) {
					System.out.println(i + " " + n1.getKey() + " " + n2.getKey());
				} 
			}
			*/
			
			for (int key: this.expanded.keySet()) {
				ARANode an = this.expanded.get(key);
				if (an.isInconsistent()) { 
					an.setOpen();
					this.open.add(an);
					//this.openMap.put(an.getHashKey(), an);
				}
				else if (an.isClosed()) {
					an.setVisited();
				}
			}
			this.improvePath();
			this.constructPath();
			this.publish(this.path);
		}
		
		EventHandler.printInfo("ARA* stopped search.");
	}
	
	protected void improvePath() {
		if (this.open.isEmpty()) return;
		int i = 0;
		Node[] publishArray = new Node[2000];

		while (this.open.peek().compareTo(this.rootNode) < 0) {
			//ARANode r = this.rootNode;
			//System.out.println(r.xy[0] + " " + r.xy[1] + " " + r.getG() + " " + e*r.getH());
			ARANode node = this.open.remove();
			//System.out.println(node.xy[0] + " " + node.xy[1] + " " + node.getG() + " " + e*node.getH());
			node.setClosed();
			publishArray[i] = node;
			if (i == publishArray.length - 1) {
				this.publish((Object[])publishArray);
				i = 0;
			}
			else {
				i++;
			}

			for (ARANode n: this.pred(node)) {
				if (n.g > this.getCost(this.map, node.xy) + node.g) {
					n.g = this.getCost(this.map, node.xy) + node.g;
					n.prev = node;
					if (!n.isClosed()) {
						n.setOpen();
						n.setE(this.e);
						this.open.add(n);
						//this.openMap.put(n.getHashKey(), n);
					}
					else {
						n.setInconsistent();
					}	
				}
			}
		}
		if (i != 0 && i < publishArray.length - 1) {
			for (int j = i + 1; j < publishArray.length; j++) {
				publishArray[j] = null;
			}
		}
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
	protected ArrayList<ARANode> neighbors(ARANode an) {
		ArrayList<int[]> xys = this.getXYs(an.xy);
		ArrayList<ARANode> neighbors = new ArrayList<ARANode>();
		
		for (int[] xy: xys) {
			ARANode n;
			if (xy[0] == this.position[0] && xy[1] == this.position[1]) {
				n = this.rootNode;
				if (an.getG() + this.getCost(this.map, n.xy) < n.getG()) {
					n.setG(an.getG() + this.getCost(this.map, n.xy));
					n.prev = an;
				}
			}
			if (this.expanded.containsKey(Node.getHashKeyFor(xy))) {
				n = this.expanded.get(Node.getHashKeyFor(xy));
				if (!n.isVisited()) { continue; }
 			} 
			else {
				n = new ARANode(xy, Double.MAX_VALUE / 2, this.calcH(xy, this.root), this.e);
				this.expanded.put(n.getHashKey(), n);
				n.prev = an;
			}
			neighbors.add(n);
		}
		return neighbors;
	} 
	
	protected ArrayList<ARANode> succ(ARANode n) {
		ArrayList<ARANode> succ = this.neighbors(n);

		int i = 0;
		double g = n.getG();
		while (succ.size() > 0 && i < succ.size()) {
			ARANode s = succ.get(i);
			if (s.getG() >= g) succ.remove(s);
			else i++;
		}
		return succ;
	}
	
	protected ArrayList<ARANode> pred(ARANode n) {
		ArrayList<ARANode> pred = this.neighbors(n);
		int i = 0;
		double g = n.getG();
		while (pred.size() > 0 && i < pred.size()) {
			ARANode s = pred.get(i);
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
}
