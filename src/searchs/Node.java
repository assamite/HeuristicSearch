package searchs;

public class Node implements Comparable<Node> {
	public int[] xy;
	private double f;
	protected double h;
	protected double g;
	/** Previous node from which this node was spawned from. Can be either 
	 * predecessor (A*) or successor (D* Lite, ARA*, AD*). */
	public Node prev = null;
	protected int hashKey;
	
	public Node(int[] xy, double g, double h) {
		this.xy = xy; 
		this.g = g;
		this.h = h;
		this.f = g + h;
		this.hashKey = this.xy[0] * 10000 + this.xy[1];
	}
	
	public void setG(double g) {
		this.g = g;
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
	
	public int compareTo(Node n) {
		return this.compareKeys(n);
	}
	
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
	
	public Node clone() {
		return new Node(this.xy, this.g, this.h);
	}
}
