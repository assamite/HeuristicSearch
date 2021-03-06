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
	PriorityQueue<EpsNode> open = new PriorityQueue<EpsNode>();
	/** All extended states. */
	private HashMap<Integer, EpsNode> created = new HashMap<Integer, EpsNode>();
	/** Current goal node. */
	protected EpsNode goalNode;
	/** Current root node. */
	protected EpsNode rootNode;
	/** Current epsilon. */
	protected double e = 4;

	public ARA(SearchBot r) {
		super(r);
		this.rootNode = new EpsNode(this.root, Double.MAX_VALUE / 2, 0, this.e);
		this.goalNode = new EpsNode(this.goal, 0, this.calcH(this.root, this.goal), this.e);
		this.name = "ARA*";
	}
	
	public ARA(SearchBot r, int[] root, int[] goal) {
		super(r, root, goal);
		this.rootNode = new EpsNode(this.root, Double.MAX_VALUE / 2, 0, this.e);
		this.goalNode = new EpsNode(this.goal, 0, this.calcH(this.root, this.goal), this.e);
		this.name = "ARA*";
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
	protected void search() {
		this.open = new PriorityQueue<EpsNode>();
		this.open.add(this.goalNode);
		this.improvePath();
		this.constructPath();
		this.publish(this.path);

		while (this.e > 1.0) {
			try {
				Thread.sleep(150);	
			}
			catch (Exception e) { }
			
			this.robot.clearSearched();
			this.e = this.e - 0.5;
			print("Epsilon = " + this.e);
			
			int hashKey = Node.getHashKeyFor(this.getPosition());
			if (this.created.containsKey(hashKey)) {
				this.rootNode = this.created.get(hashKey);
				this.rootNode.setH(0.0);
				print("Root: " + this.rootNode.xy[0] +" " + this.rootNode.xy[1]);
			}
			
			// Ugh. This is quite stupid, but has to be done so that the
			// priorities get updated
			PriorityQueue<EpsNode> newPQ = new PriorityQueue<EpsNode>();
			for (EpsNode n: this.open) { 
				n.setH(this.calcH(n.xy, this.rootNode.xy));
				n.setE(this.e);
				newPQ.add(n); 
			}
			this.open = newPQ;

			for (int key: this.created.keySet()) {
				EpsNode an = this.created.get(key);
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
			EpsNode node = this.open.remove();
			//System.out.println(node.xy[0] + " " + node.xy[1] + " " + node.getG() + " " + e*node.getH());
			node.setMembership(Node.CLOSED);
			// Publish only in batches.
			this.publish(node);
			/*
			publishArray[i] = node;
			if (i == publishArray.length - 1) {
				this.publish((Object[])publishArray);
				i = 0;
			}
			else {
				i++;
			}
			*/
			for (EpsNode n: this.neighbors(node)) {
				if (n.g > this.getCost(this.map, n.xy) + node.g) {
					n.g = this.getCost(this.map, n.xy) + node.g;
					n.prev = node;
					if (!n.isClosed()) {
						if (n.isOpen()) {
							this.open.remove(n);
						}
						n.setMembership(Node.OPEN);
						n.setE(this.e);
						this.open.add(n);
					}
					else {
						n.setMembership(Node.INCONSISTENT);
					}	
				}
			}
		}
		/*
		if (i != 0 && i < publishArray.length - 1) {
			Node[] a = new Node[i];
			for (int j = 0; j < i; j++) a[j] = publishArray[j];
			this.publish((Object[])a);
		}
		*/
		
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
	protected ArrayList<EpsNode> neighbors(EpsNode an) {
		ArrayList<int[]> xys = this.getXYs(an.xy);
		ArrayList<EpsNode> neighbors = new ArrayList<EpsNode>();
		
		for (int[] xy: xys) {
			EpsNode n;
			if (xy[0] == this.rootNode.xy[0] && xy[1] == this.rootNode.xy[1]) {
				n = this.rootNode;
				/*
				if (an.getG() + this.getCost(this.map, n.xy) < n.getG()) {
					n.setG(an.getG() + this.getCost(this.map, n.xy));
					n.prev = an;
				}
				*/
			}
			else if (this.created.containsKey(Node.getHashKeyFor(xy))) {
				n = this.created.get(Node.getHashKeyFor(xy));
 			} 
			else {
				n = new EpsNode(xy, Double.MAX_VALUE / 2, this.calcH(xy, this.root), this.e);
				this.created.put(n.getHashKey(), n);
				n.prev = an;
			}
			neighbors.add(n);
		}
		return neighbors;
	} 
	
	@Override
	protected boolean isGoal(Node n) {
		if (n.xy[0] == this.position[0] && n.xy[1] == this.position[1]) return true;
		return false;
	}
}
