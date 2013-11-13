package util;

import java.util.ArrayList;

public class Calc {

	/**
	 * Calculates distance from point (x, y) to line represented by y = ax + b.
	 * @param x x-coordinate of the point
	 * @param y y-coordinate of the point
	 * @param a slope of the line
	 * @param b constant of the line equation
	 * @return distance to line
	 */
	public static double distanceToLine(double x, double y, double a, double b) {
		double[] p = Calc.closestPointInLine(x, y, a, b);
		return ed(x, y, p[0], p[1]);
	}
	
	/**
	 * Calculates closest point in line represented by y = ax + b to point (x, y).
	 * @param x x-coordinate of the point
	 * @param y y-coordinate of the point
	 * @param a slope of the line
	 * @param b constant of the line equation
	 * @return [x, y] of the closest point
	 */
	public static double[] closestPointInLine(double x, double y, double a, double b) {
		double ao = -1.0/a;
		if (a == 0) ao = 0;
		double bo = y - ao*x;
		double px = ao == 0 ? 0 : (b - bo)/(ao - a);
		double py = ao == 0 ? Math.abs(b - y) : (ao*(b - bo))/(ao - a) + bo;
		return new double[] {px, py};
	}
	
	/**
	 * Calculate euclidean distance between two points (x1, y1) and (x2, y2)
	 * @param x1 x-coordinate of the first point
	 * @param y1 y-coordinate of the first point
	 * @param x2 x-coordinate of the second point
	 * @param y2 y-coordinate of the second point
	 * @return distance between points.
	 */
	public static double ed(double x1, double y1, double x2, double y2) {
		double dx = Math.abs(x1 - x2);
		double dy = Math.abs(y1 - y2);
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	/**
	 * Return the upper left corner and width and height of the rectangle 
	 * defined by points {x1, y1, x2, y2}.
	 * 
	 * @return rectangle as a int array {x, y, w, h}
	 */
	public static int[] getRectangle(int[] xys) {
		int x1 = xys[0]; int y1 = xys[1]; int x2 = xys[2]; int y2 = xys[3];
		int x = (int)Math.floor(x1 < x2 ? x1 : x2);
		int y = (int)Math.floor(y1 < y2 ? y1 : y2);
		int mx = (int)Math.ceil(x1 > x2 ? x1 : x2);
		int my = (int)Math.ceil(y1 > y2 ? y1 : y2);
		int w = mx - x;
		int h = my - y;
		return new int[] {x, y, w, h};
	} 
	
	
	/**
	 * Linear least squares solver. Converted to Java (and my needs) from C# 
	 * code in "SLAM for Dummies".
	 * @param points observation points as (x,y)-pairs of (absolute) coordinates
	 * @param selected currently selected points which are are used to solve the
	 * linear least squares
	 * @return [a, b] in y = ax + b line.
	 */
	public static double[] lsqSolver(ArrayList<Point> points, int[] selected) {
		int slen = selected.length;
		
		double y; //y coordinate
		double x; //x coordinate
		double sumY=0; //sum of y coordinates
		double sumYY=0; //sum of y^2 for each coordinate
		double sumX=0; //sum of x coordinates
		double sumXX=0; //sum of x^2 for each coordinate
		double sumYX=0; //sum of y*x for each point
		
		for(int i = 0; i < slen; i++) {
			x = points.get(i).getX();
			y = points.get(i).getY();
			sumY += y;
			sumYY += y*y; 
			sumX += x;
			sumXX += x*x; 
			sumYX += y*x;
		}
		double b = (sumY*sumXX - sumX*sumYX)/(slen*sumXX - sumX*sumX);
		double a = (slen*sumYX - sumX*sumY)/(slen*sumXX - sumX*sumX);
		
		return new double[] {a, b};
	}
}
