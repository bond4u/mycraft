package org.mycraft.client;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ARBTransposeMatrix;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

public class Viewport {
	
	private static final int HEIGHT = 480;
	private static final int WIDTH = HEIGHT * 3 / 2;
	
	private static final float fovy = 75f;
	private static final float zNear = 0.1f;
	private static final float zFar = 95.1f;

	public int getWidth() {
		return WIDTH;
	}
	
	public int getHeight() {
		return HEIGHT;
	}
	
	public void init() {
		warn("LWJGL: " + Sys.getVersion() + " / " + LWJGLUtil.getPlatformName());
		logGlErrorIfAny();
		warn("GL_VENDOR: " + GL11.glGetString(GL11.GL_VENDOR));
		logGlErrorIfAny();
		warn("GL_RENDERER: " + GL11.glGetString(GL11.GL_RENDERER));
		logGlErrorIfAny();
		warn("GL_VERSION: " + GL11.glGetString(GL11.GL_VERSION));
		logGlErrorIfAny();
		warn("glLoadTransposeMatrixfARB() supported: " + GLContext.getCapabilities().GL_ARB_transpose_matrix);
		logGlErrorIfAny();

		GL11.glViewport(0, 0, getWidth(), getHeight());
		logGlErrorIfAny();
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		
		final boolean canTranspose = GLContext.getCapabilities().GL_ARB_transpose_matrix;
		logGlErrorIfAny();
		if (canTranspose) {
			// --- using extensions
			final FloatBuffer identityTranspose = BufferUtils.createFloatBuffer(16).put(
					new float[] {
							1, 0, 0, 0,
							0, 1, 0, 0,
							0, 0, 1, 0,
							0, 0, 0, 1,
					}
			);
			identityTranspose.flip();
			ARBTransposeMatrix.glLoadTransposeMatrixARB(identityTranspose);
			logGlErrorIfAny();
		} else {
			// --- not using extensions
			GL11.glLoadIdentity();
			logGlErrorIfAny();
		}
		
		double r = getWidth() / (double) getHeight();
		log("window ratio " + r);
		GLU.gluPerspective(fovy, (float) r, zNear, zFar);
		logGlErrorIfAny();
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		logGlErrorIfAny();
	}
	
	private void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	private void logGlErrorIfAny() {
		final int e = GL11.glGetError();
		if (e != 0) {
			log("err=" + e + ": " + GLU.gluErrorString(e));
		}
	}
	
	public void warn(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
}
