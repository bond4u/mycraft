package org.noise;

/**
 * Converts one range to another.
 * For example from range [0..1] to [-1..+1].
 */
public interface IConv1D extends IFunc1D {
	
	/**
	 * Set from range and to range
	 * @param fromx1
	 * @param fromx2
	 * @param tox1
	 * @param tox2
	 */
	void set(float fromx1, float fromx2, float tox1, float tox2);
	
	/**
	 * Convert point from "from" range to "to" range
	 * @param x
	 * @return float
	 */
	float get(float x);
	
}
