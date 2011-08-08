package org.noise;

public interface IConv1D extends IFunc1D {
	
	/**
	 * Set from range
	 * @param x1
	 * @param x2
	 */
	void setFrom(float x1, float x2);
	
	/**
	 * Set to range
	 * @param x1
	 * @param x2
	 */
	void setTo(float x1, float x2);
	
//	/**
//	 * Convert point from "from" range [0..+1] to "to" range [-1..+1]
//	 * @param x
//	 * @return
//	 */
//	float get(float x);
	
}
