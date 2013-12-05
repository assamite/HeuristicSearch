package searchs;

/**
 * Basic node for heuristic searches in 2D -grid world. Has knowledge of its 
 * (x, y) -coordinates and calculates its cost as f(n) = g(n) + h(n). Where g(n) 
 * is the cost via shortest path to this node and h(n) is estimated cost from 
 * this node to goal. 
 * 
 * Also membership between the different sets, ie. closed, open, inconsistent, 
 * is kept inside the node. Users of the node have to change the set membership 
 * appropriately for their needs.
 * @author pihatonttu
 *
 */
public class Node implements Comparable<Node> {
	public int[] xy;
	private double f;
	protected double h;
	protected double g;
	protected int hashKey;
	/** Previous node from which this node was spawned from. Used for 
	 * reconstructing the path after the goal has been found. */
	public Node prev = null;
	
	// Final variables for different set type memberships.
	public static final int NOT_VISITED = 0;
	public static final int VISITED = 1;
	public static final int CLOSED = 2;
	public static final int OPEN = 3;
	public static final int INCONSISTENT = 4;
	
	/** Current membership of the set. Use one of NOT_VISITED, VISITED, CLOSED,
	 * OPEN or INCONSISTENT finals. */
	private int memberOf = 0;
	
	/** Constructor. Node is set to not visited.*/
	public Node(int[] xy, double g, double h) {
		this.xy = xy; 
		this.g = g;
		this.h = h;
		this.f = g + h;
		this.hashKey = this.xy[0] * 10000 + this.xy[1];
	}
	
	/** Constructor with additional option to set membership. Only used via 
	 * clone method. */
	private Node(int[] xy, double g, double h, int setType) {
		this(xy, g, h);
		this.memberOf = setType;
	}
	
	public void setG(double g) {
		this.g = g;
		this.f = this.g + this.h;
	}
	
	public void setH(double h) {
		this.h = h;
		this.f = this.g + this.h;
	}
	
	public double getG() {
		return this.g;
	}
	
	public double getH() {
		return this.h;
	}
	
	public double getF() {
		return this.f;
	}
	
	/** Change the current set membership of this node. Use one of the final 
	 * variables NOT_VISITED, VISITED, CLOSED, OPEN or INCONSISTENT. */
	public void setMembership(int setType) {
		this.memberOf = setType;
	}
	
	/** Node is always visited when it is either closed, open or inconsistent or
	 * deliberately setting it as visited. */
	public boolean isVisited() {
		if (this.memberOf != Node.NOT_VISITED) return true;
		return false;
	}
	
	public boolean isClosed() {
		if (this.memberOf == Node.CLOSED) return true;
		return false;
	}
	
	public boolean isOpen() {
		if (this.memberOf == Node.OPEN) return true;
		return false;		
	}
	
	public boolean isInconsistent() {
		if (this.memberOf == Node.INCONSISTENT) return true;
		return false;		
	}
	
	/** Compares the keys of nodes. Override compareKeys -method for child 
	 * class. */
	public int compareTo(Node n) {
		return this.compareKeys(n);
	}
	
	/** Compares this node agains another node. Child classes should override 
	 * this if the key type changes from f(n) = g(n) + h(n). */
	protected int compareKeys(Node n) {
		double c = this.f - n.getF();
		if (c < 0) return -1;
		if (c > 0) return 1;
		return 0;
	}
	
	/**
	 * @param o Object against which equality is checked.
	 * @return True, if nodes have the same x,y - coordinates.
	 */
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Node) {
			Node n = (Node)o;
			if (n.xy[0] == this.xy[0] && n.xy[1] == this.xy[1]) 
				return true;
		}
		return false;	
	}
	
	/** Returns unique key for this node to use in HashMaps, etc. Uniqueness
	 * is guaranteed only for smaller than 10000 x 10000 pixel maps. */
	public int getHashKey() {
		return this.hashKey;	
	}
	
	/**
	 * Get hash key for node in x, y location.
	 * @param xy coordinates of the node
	 * @return hash key of the node.
	 */
	public static int getHashKeyFor(int[] xy) {
		return xy[0] * 10000 + xy[1];	
	} 
	
	/** Clones the node. Also clones the membership! */
	public Node clone() {
		return new Node(this.xy, this.g, this.h, this.memberOf);
	}
}
