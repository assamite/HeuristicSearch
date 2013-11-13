package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Date;

import ui.UIScheme;

/**
 * Class for observation points. Class has date, which should usually be the 
 * same as {@link Observation Observation}'s date, but in some cases where 
 * <code>Point</code> does not directly relate to any one observation it might 
 * be useful to add it separately.<br><br>
 * 
 * Probability of the point is used to measure how sure we are about that given
 * point not being noise. Very low probability values should relate to points
 * that have been observed only once and then repeatedly missed when close 
 * enough for ultrasonic sensor to be able to observe them. High values should 
 * correlate with very stable observations around this point.
 * 
 * @author slinkola
 *
 */
public class Point extends java.awt.geom.Point2D implements HasDate, 
Comparable<HasDate>, Serializable {
	
	/** Default serialization UID. */
	private static final long serialVersionUID = 1L;
	private double x = 0;
	private double y = 0;
	private Date date;
	private double p = 0;
	
	/**
	 * Default constructor.
	 * @param x x-coordinate of the observation
	 * @param y y-coordinate of the observation
	 * @param p probability of how sure we are about this observation point, 
	 * should increase when observations near this point are made repeatedly.
	 * @param d time when this observation point was first created
	 */
	public Point(double x, double y, double p, Date d) {
		this.x = x;
		this.y = y; 
		this.p = p;
		this.date = d;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public void setProb(double p) {
		this.p = p;
	}
	
	public double getProb() {
		return p;
	}
	
	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setLocation(double x, double y) {
		this.x = x; 
		this.y = y;
	}

	@Override
	public int compareTo(HasDate d) {
		if (this.date.before(d.getDate())) return -1;
		return 1;
	}
	
	/**
	 * Draw this point in graphics context.
	 * @param gc Graphics context to draw in
	 * @param last is this point part of last observation. 
	 */
	public void draw(Graphics2D gc, boolean last) {
		int px = (int)this.x;
		int py = (int)this.y;
		Color inner = UIScheme.LILA_M;
		Color outer = UIScheme.LILA_VD;
		if (last) { 
			inner = UIScheme.GREEN_L;
			outer = UIScheme.GREEN_D;
		}
		gc.setColor(inner);
		gc.fillArc(px-3, py-3, 6, 6, 0, 360);
		gc.setColor(outer);
		gc.drawArc(px-3, py-3, 6, 6, 0, 360);
	}
}
