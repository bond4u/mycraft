package org.mycraft.client;

import java.util.Comparator;

public class Point2i implements Comparable<Point2i>, Comparator<Point2i> {
	
	private final int x;
	private final int y;
	
	public Point2i(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int compareTo(Point2i p) {
		final boolean lesserX = this.x < p.x;
		final boolean greaterX = this.x > p.x;
		final boolean lesserY = this.y < p.y;
		final boolean greaterY = this.y > p.y;
		return lesserX ? -2 : (greaterX ? +2 : (lesserY ? -1 : (greaterY ? +1 : 0)));
	}
	
	public int compare(Point2i o1, Point2i o2) {
		return o1.compareTo(o2);
	}
	
	public boolean equals(Point2i p) {
		return compareTo(p) == 0;
	}
	
	public String toString() {
		return "Point2i:[x=" + this.x + ";y=" + this.y + "]";
	}
}
