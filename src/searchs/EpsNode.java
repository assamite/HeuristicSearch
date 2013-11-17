package searchs;

/**
 * Node with added epsilon value. Key is calculated as g + epsilon * h. Also 
 * membership between the different sets, ie. closed, open, inconsistent, is 
 * kept inside the node. All nodes are created as "not visited". Users of the 
 * node have to change the set membership appropriately for their needs.
 * 
 * @author slinkola
 *
 */
public class EpsNode extends Node {
	/** 0 = not visited, 1 = visited, 2 = closed, 3 = open, 4 = inconsistent. */
	private int memberOfSet = 0;
	private double e;

	public EpsNode(int[] xy, double g, double h, double e) {
		super(xy, g, h);
		this.e = e;
	}
	
	/** Set the node's epsilon value. */
	public void setE(double e) {
		this.e = e;
	}
	
	/** Get the nodes current epsilon value.*/
	public double getE() {
		return this.e;
	}
	
	/** Key = g + e * h. */
	public double getKey() {
		return this.g + (this.h * this.e);
	}

	/** Node is always visited when it is either closed, open or inconsistent or
	 * deliberately setting it as visited. */
	public boolean isVisited() {
		if (memberOfSet == 0) return false;
		return true;
	}
	
	public void setVisited() {
		this.memberOfSet = 1;
	}
	
	public void setClosed() {
		this.memberOfSet = 2;
	}
	
	public void setOpen() {
		this.memberOfSet = 3;
	}
	
	public void setInconsistent() {
		this.memberOfSet = 4;
	}
	
	public boolean isClosed() {
		if (this.memberOfSet == 2) return true;
		return false;
	}
	
	public boolean isOpen() {
		if (this.memberOfSet == 3) return true;
		return false;		
	}
	
	public boolean isInconsistent() {
		if (this.memberOfSet == 4) return true;
		return false;		
	}
	
	@Override
	protected int compareKeys(Node n) {
		double c = this.getKey() - ((EpsNode)n).getKey();
		if (c < 0) return -1;
		if (c > 0) return 1;
		return 0;	
	}
}
