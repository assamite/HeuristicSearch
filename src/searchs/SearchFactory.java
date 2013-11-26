package searchs;

import robot.SearchBot;

/**
 * Class which creates all the searchs. First time I use factory abstraction. 
 * I feel dirty.
 * @author slinkola
 *
 */
public class SearchFactory {
	/**
	 * Create new search instance.
	 * @param searchType search type from Search enum.
	 * @param r robot, for which this search belongs to
	 * @param root root of the search
	 * @param goal goal of the search
	 * @return new search instance as defined by the parameters.
	 */
	public static AbstractSearch createSearch(SearchType searchType, SearchBot r, int[] root, int[] goal) {
		switch (searchType) {
			case ASTAR: 
				return new AStar(r, root, goal);
			case D_LITE:
				return new DLite(r, root, goal);
			case ARA:
				return new ARA(r, root, goal);
			case NAIVE_ANYTIME:
				return new NaiveAnytime(r, root, goal);
			case ADSTAR:
				return new ADStar(r, root, goal);
			default:
				return null;
		}	
	}
}
