package searchs;

import java.util.ArrayList;

/**
 * Node for D* Lite.
 * @author slinkola
 *
 */
public class DNode extends Node {
	protected double rhs = 0.0;
	protected double[] key = {0.0, 0.0};
	
	public DNode(int[] xy, double g, double h) {
		super(xy, g, h);
		this.setKey();
	}
	
	public DNode(int[] xy, double g, double h, double rhs) {
		this(xy, g, h);
		this.rhs = rhs;
		this.setKey();
	}
	
	@Override
	public void setH(double h) {
		this.h = h;
		this.setKey();
	}
	
	@Override
	public void setG(double g) {
		this.g = g;
		this.setKey();
	}
	
	public double getRhs() { return this.rhs; }
	public void setRhs(double rhs) {
		this.rhs = rhs; 
		this.setKey();
		}
	
	/**
	 * Set key to represent current node values.
	 */
	protected void setKey() {
		this.key[0] = Math.min(this.g, this.rhs) + this.h;
		this.key[1] = Math.min(this.g, this.rhs);
	}

	/**
	 * Return this node's key which should be used to compare this node to other
	 * nodes. Key is [k1, k2] -pair and node n1 is before n2 iff. n1(k1) < n2(k1)
	 * or n1(k1) == n2(k1) and n1(k2) < n2(k2).
	 * 
	 * @return Key for this node as a [k1, k2] -pair where k1 = min{g, rhs + h}
	 * and k2 = min(g, rhs).
	 */
	public double[] getKey() {
		return this.key;
	}
	
	/**
	 * Change values to represent other node's values. prev and predecessors are
	 * only changed if given node compares before this node.
	 * 
	 * @param dn DNode, to which this node's values are changed to.
	 */
	public void setValues(DNode dn) {
		this.rhs = dn.rhs;
		this.g = dn.g;
		this.h = dn.h;
		this.setKey();
		if (dn.compareKeys(this) < 0) {
			this.prev = dn.prev;
		}
	}
	
	@Override
	protected int compareKeys(Node n) {
		DNode dn = (DNode)n;
		double[] dnkey = dn.getKey();
		if (this.key[0] < dnkey[0])
			return -1;
		if (this.key[0] == dnkey[0] && this.key[1] < dnkey[1])
			return -1;
		return 1;
	}
	
}
