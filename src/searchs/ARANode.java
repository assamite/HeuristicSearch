package searchs;

public class ARANode extends Node {
	protected boolean visited = false;

	public ARANode(int[] xy, double g, double h) {
		super(xy, g, h);
	}

	public boolean isVisited() {
		return this.visited;
	}
	
	public void visit() {
		this.visited = true;
	}
}
