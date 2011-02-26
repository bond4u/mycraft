package org.mycraft.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Camera {

	// camera movement/coordinate sensitivity
	private final float sx = 0.25f;
	private final float sy = 0.25f;
	private final float sz = 0.25f;
	// camera turn/angle sensitivity
	private final float sax = 0.25f;
	private final float say = 0.25f;
	private final float saz = 0.25f;
	// camera location
	private float x = 0.0f;
	private float y = 6.0f;
	private float z = 12.0f;
	// camera orientation
	private float ax = 0.0f;
	private float ay = 0.0f;
	private float az = 0.0f;

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
	
	public void update() {
//		long start = System.currentTimeMillis();
		GL11.glLoadIdentity();
		logGlErrorIfAny();
		
		GL11.glRotatef(ax, 1f, 0f, 0f);
		logGlErrorIfAny();
		GL11.glRotatef(ay, 0f, 1f, 0f);
		logGlErrorIfAny();
		GL11.glRotatef(az, 0f, 0f, 1f);
		logGlErrorIfAny();
		GL11.glTranslatef(-x, -y, -z);
		logGlErrorIfAny();
//		log("cam @ " + camPos[0] + "," + camPos[1] + "," + camPos[2] +
//				" > " + camRot[0] + "," + camRot[1] + "," + camRot[2]);
//		log("cam @ " + camera_point[0] + "," + camera_point[1] + "," + camera_point[2] +
//		" -> " + camera_rotation[0] + "," + camera_rotation[1] + "," + camera_rotation[2]);
// inverse camera coordinates - that way we get scene movement
// loading identity - that should be last identity - model view ?
//		long duration = System.currentTimeMillis() - start;
//		if (duration > 1000 / 60) {
//			log("cam update " + duration + " ms");
//		}
	}
	
	private void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	public void logGlErrorIfAny() {
		int e = GL11.glGetError();
		if (e != 0) {
			log("glGetError: " + e + ": " + GLU.gluErrorString(e));
		}
	}
	
}
