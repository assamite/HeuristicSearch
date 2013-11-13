package searchs;

import robot.Robot;

/** Class which creates all the searchs. Ugh, first time I'm using factory
 * abstraction. */
public class SearchFactory {
	/**
	 * Create new search instance.
	 * @param searchType search type from Search enum.
	 * @param r robot, for which this search belongs to
	 * @param root root of the search
	 * @param goal goal of the search
	 * @return new search instance as defined by the parameters.
	 */
	public static AbstractSearch createSearch(SearchType searchType, Robot r, int[] root, int[] goal) {
		switch (searchType) {
			case ASTAR: 
				return new AStar(r, root, goal);
			case DLITE:
				return new DLite(r, root, goal);
			case ARA:
				return new ARA(r, root, goal);
			case ADSTAR:
			default:
				return null;
		}	
	}
}
