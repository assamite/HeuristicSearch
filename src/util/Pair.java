package util;

import java.io.Serializable;

/**
 * Light wrapper to make "generic" pairs in java.
 * 
 * @author slinkola
 *
 * @param <T> Type of first element of pair
 * @param <S> Type of second element of pair.
 */
public class Pair<T, S> implements Serializable{ 
	  /** Default serialization UID. */
	private static final long serialVersionUID = 1L;
	public final T first; 
	  public final S second; 
	  
	  /**
	   * Default constructor for Pair instance. Nothing fancy here.
	   * 
	   * @param t first element of the pair. Instance of given type T
	   * @param s second element of the pair. Instance of given type S
	   */
	  public Pair(T t, S s) { 
		  this.first = t; 
		  this.second = s; 
	  }	  
}
