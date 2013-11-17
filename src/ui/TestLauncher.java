package ui;

import robot.SearchBot;
import searchs.SearchType;

public class TestLauncher extends UILauncher{
	
	public static void main(String[] args) {
		UILauncher.main(args);
		
		int[] root = {0, 0};
		int[][] goals = new int[10][2];
		SearchType[] st = {SearchType.ASTAR, SearchType.NAIVE_ANYTIME, SearchType.ARA};
		SearchBot sb = MainUI.map.robot;
		int dists[] = new int[goals[0].length];
		
		for (int i = 0; i < goals[0].length; i++) {
			goals[i][0] = (i+1) * 10;
			goals[i][1] = (i+1) * 100;
			dists[i] = Math.abs(goals[i][0] - root[0]) + Math.abs(goals[i][1] - root[1]);
		}
		
		//for (int i = 0; i < ) 
		
	}

}
