package searchs;

public class ARANode extends Node {
	/** 0 = not visited, 1 = visited, 2 = closed, 3 = open, 4 = inconsistent. */
	private int memberOfSet = 0;
	private double e;

	public ARANode(int[] xy, double g, double h, double e) {
		super(xy, g, h);
		this.e = e;
	}
	
	public void setE(double e) {
		this.e = e;
	}
	
	public double getE() {
		return this.e;
	}
	
	public double getKey() {
		return this.g + (this.h * this.e);
	}

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
	public int compareKeys(Node n) {
		double c = this.getKey() - ((ARANode)n).getKey();
		if (c < 0) return -1;
		if (c > 0) return 1;
		return 0;	
	}
}
