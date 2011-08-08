package org.mycraft.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Cursor {
	
	public void draw() {
		GL11.glPushMatrix();
		logGlErrorIfAny();
		
		GL11.glTranslatef(0f, 0f, -0.1f);
		logGlErrorIfAny();
		
		float d = 0.00075f;
		
		GL11.glColor3f(1f, 1f, 1f); // too thin line
		logGlErrorIfAny();
		
		// no error-checking between begin & end
		GL11.glBegin(GL11.GL_LINE_STRIP);
		
		GL11.glVertex2f(-d, 0f);
		GL11.glVertex2f(0f, d);
		GL11.glVertex2f(d, 0f);
		
		GL11.glEnd();
		logGlErrorIfAny();
		
		GL11.glColor3f(0f, 0f, 0f);
		logGlErrorIfAny();
		
		GL11.glBegin(GL11.GL_LINE_STRIP);
		
		GL11.glVertex2f(-d, 0f);
		GL11.glVertex2f(0f, -d);
		GL11.glVertex2f(d, 0f);
		
		GL11.glEnd();
		logGlErrorIfAny();
		
		GL11.glPopMatrix();
		logGlErrorIfAny();
	}
	
	public void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	public void logGlErrorIfAny() {
		int e = GL11.glGetError();
		if (e != 0) {
			log("glGetError: " + e + ": " + GLU.gluErrorString(e));
		}
	}
	
}
