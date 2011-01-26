package org.mycraft.client;

public class Camera {

	// camera movement/coordinate sensitivity
	private final double sx = 0.25;
	private final double sy = 0.25;
	private final double sz = 0.25;
	// camera turn/angle sensitivity
	private final double sax = 0.25;
	private final double say = 0.25;
	private final double saz = 0.25;
	// camera location
	private double x = 0.0;
	private double y = 0.0;
	private double z = 12.0;
	// camera orientation
	private double ax = 0.0;
	private double ay = 0.0;
	private double az = 0.0;

	/**
	 * Returns current location.
	 * @return
	 */
	public double[] getPosition() {
		return new double[] { x, y, z, };
	}
	
	/**
	 * Returns current orientation angles - the direction we are looking at.
	 * @return
	 */
	public double[] getRotation() {
		return new double[] { ax, ay, az, };
	}
	
	public void lookUpDown(double a) {
		ax -= a * sax;
//		log("lookUD " + a + "," + ax);
	}
	
	public void lookLeftRight(double a) {
		ay += a * say;
//		log("lookLR " + a + "," + ay);
	}
	
	/**
	 * Move in the direction we are looking.
	 * @param d - delta
	 */
	public void moveForwardBackward(double d) {
		double rad = Math.toRadians(ay); // left/right looking angle
		double dx = d * Math.sin(rad) * sx;
		double dz = d * -Math.cos(rad) * sz; // 0deg -> positive -> move forward
		x += dx;
		z += dz;
//		log("moveFB " + d + "," + ay + ";" + dx + "," + dz + ";" + x + "," + z);
	}
	
	public void moveLeftRight(double d) {
		double rad = Math.toRadians(ay); // left/right looking angle
		double dx = d * Math.cos(rad) * sx; // 0deg -> negative -> move left
		double dz = d * Math.sin(rad) * sz;
		x += dx;
		z += dz;
//		log("moveLR " + d + "," + ay + ";" + dx + "," + dz + ";" + x + "," + z);
	}
	
	public void moveUpDown(double d) {
		double dy = d * sy;
		y += dy;
//		log("moveUD " + d + ";" + dy + ";" + y);
	}
	
	private void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
}
