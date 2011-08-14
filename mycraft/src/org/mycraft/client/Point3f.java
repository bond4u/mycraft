package org.mycraft.client;

import java.util.Comparator;

public class Point3f implements Comparable<Point3f>, Comparator<Point3f> {
	
	private final float x;
	private final float y;
	private final float z;
	
	public Point3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	public int compareTo(Point3f p) {
		if (null == p) {
			return -4;
		}
		// FIXME allow little error? ~0.0001
		final boolean lesserX = this.x < p.x;
		final boolean greaterX = this.x > p.x;
		if (lesserX || greaterX) {
			return lesserX ? -3 : +3; // great difference
		}
		final boolean lesserY = this.y < p.y;
		final boolean greaterY = this.y > p.y;
		if (lesserY || greaterY) {
			return lesserY ? -2 : +2; // middle difference
		}
		final boolean lesserZ = this.z < p.z;
		final boolean greaterZ = this.z > p.z;
		if (lesserZ || greaterZ) {
			return lesserZ ? -1 : +1; // little difference
		}
		return 0; // equal
	}
	
	public int compare(Point3f o1, Point3f o2) {
		return o1.compareTo(o2);
	}
	
	public boolean equals(Point3f p) {
		return compareTo(p) == 0;
	}
	
	public String toString() {
		return "Point3f:[x=" + this.x + ";y=" + this.y + ";z=" + this.z + "]";
	}
	
}
