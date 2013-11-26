package searchs;

/**
 * Node with added epsilon value. Key is calculated as g + epsilon * h. 
 * 
 * @author slinkola
 *
 */
public class EpsNode extends Node {
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

	@Override
	protected int compareKeys(Node n) {
		double c = this.getKey() - ((EpsNode)n).getKey();
		if (c < 0) return -1;
		if (c > 0) return 1;
		return 0;	
	}
}
