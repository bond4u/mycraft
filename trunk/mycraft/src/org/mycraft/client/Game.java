package org.mycraft.client;

import java.awt.Canvas;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Game extends Thread {
	
	private boolean running;
	
	private int fps;
	
	private Viewport viewport;
	
	private Canvas canvas;
	
	private Textures texs;
	
	private Camera camera;
	
	private List<Shape> shapes;
	
	private Terrain land;
	
	public void setCanvas(Canvas c) {
		canvas = c;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public void initDisplay() {
		// get current desktop
		DisplayMode desktop = Display.getDesktopDisplayMode();
		log("Desktop: " + desktop);
		DisplayMode current = Display.getDisplayMode();
		log("Current: " + current);
		fps = current.getFrequency();
		viewport = new Viewport();
		final int w = viewport.getWidth();
		final int h = viewport.getHeight();
		if (canvas != null) { // applet - browser window
//			log("canvas size: " + canvas.getWidth() + "x" + canvas.getHeight());
			canvas.setSize(w, h);
			log("canvas size: " + canvas.getWidth() + "x" + canvas.getHeight());
			try {
				Display.setParent(canvas); // canvas may be smaller/fixed?
//				logGlErrorIfAny(); // not inited yet
			} catch (LWJGLException e) {
				log("LWJGL.setParent: " + e);
			}
		} else { // desktop window
			DisplayMode dm = new DisplayMode(w, h);
			try {
				Display.setDisplayMode(dm);
//				logGlErrorIfAny(); // not inited yet?
				int left = (current.getWidth() - w) / 2;
				int top = (current.getHeight() - h) / 2;
				Display.setLocation(left, top);
//				logGlErrorIfAny(); // not inited yet?
			} catch (LWJGLException e) {
				log("LWJGL.setDisplayMode: " + e);
			}
		}
		try {
			Display.create();
			logGlErrorIfAny();
		} catch(LWJGLException e) {
			log("LWJGL.create: " + e);
		}
		Mouse.setGrabbed(true);
		logGlErrorIfAny();
	}
	
//	public void start() {
//	}
	
	public void run() {
		running = true;
		initDisplay();
		initGL();
		initWorld();
		gameLoop();
		destroyWorld();
		destroyDisplay();
	}
	
	public void gameLoop() {
		log("gameLoop starting");
		long startTime = System.currentTimeMillis() + 5000;
		long frames = 0;

		while(running) {
			// draw the gears
			drawWorld();
			// measure time
//			long start = System.currentTimeMillis();
			Display.update();
			logGlErrorIfAny();
			Display.sync(fps);
			logGlErrorIfAny();

			if (startTime > System.currentTimeMillis()) {
				frames++;
			} else {
				long timeUsed = 5000 + (startTime - System.currentTimeMillis());
				startTime = System.currentTimeMillis() + 5000;
				log(frames + " frames in " + timeUsed / 1000.0 + " seconds = avg " + (frames / (timeUsed / 1000.0)));
				frames = 0;
			}
			
			pollMouse();
			pollKeyboard();
			
			final boolean doClose = Display.isCloseRequested();
			logGlErrorIfAny();
			if (doClose) {
				log("display.closeRequested");
				running = false;
			}
//			long duration = System.currentTimeMillis() - start;
//			if (duration > 1000 / 60) {
//				log("system " + duration + " ms");
//			}
		}
		log("gameLoop ended");
	}
	
	private void pollMouse() {
		int mdx = Mouse.getDX(); // mouse x is 3d y
		int mdy = Mouse.getDY(); // mouse y is 3d z
		if (mdy != 0) {
			camera.lookUpDown(mdy);
		}
		if (mdx != 0) {
			camera.lookLeftRight(mdx); // mouse goes right, but camera turns right
		}
	}
	
	private void pollKeyboard() {
		// F Key Pressed (i.e. released)
		if (Keyboard.isKeyDown(Keyboard.KEY_F) && !Keyboard.isRepeatEvent()) {
			logGlErrorIfAny();
			try {
				Display.setFullscreen(!Display.isFullscreen());
			} catch (LWJGLException e) {
				log("LWJGL.setFullscreen: " + e);
			}
		}
		int forwardBackward = Keyboard.isKeyDown(Keyboard.KEY_W) ? 1 : (Keyboard.isKeyDown(Keyboard.KEY_S) ? -1 : 0);
		logGlErrorIfAny();
		if (forwardBackward != 0) {
			camera.moveForwardBackward(forwardBackward);
		}
		int leftRight = Keyboard.isKeyDown(Keyboard.KEY_A) ? -1 : (Keyboard.isKeyDown(Keyboard.KEY_D) ? 1 : 0);
		logGlErrorIfAny();
		if (leftRight != 0) {
			camera.moveLeftRight(leftRight);
		}
		int upDown = Keyboard.isKeyDown(Keyboard.KEY_SPACE) ? 1 : (Keyboard.isKeyDown(Keyboard.KEY_C) ? -1 : 0);
		logGlErrorIfAny();
		if (upDown != 0) {
			camera.moveUpDown(upDown);
		}
		boolean esc = Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
		logGlErrorIfAny();
		if (esc) {
			log("escape pressed - stop running");
			running = false;
		}
	}
	
	public void log(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	public void warn(String s) {
		System.out.println(Thread.currentThread().getName() + ": " + s);
	}
	
	public void logGlErrorIfAny() {
		int e = GL11.glGetError();
		if (e != 0) {
			log("glGetError: " + e + ": " + GLU.gluErrorString(e));
		}
	}
	
	protected void initGL() {
			// setup ogl
//			GL11.glEnable(GL11.GL_LIGHT0);
//			logGlErrorIfAny();
//			GL11.glEnable(GL11.GL_LIGHTING);
//			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_CULL_FACE); // dont render hidden/back faces
			logGlErrorIfAny();
			GL11.glDepthFunc(GL11.GL_LEQUAL); // depth test type
			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			logGlErrorIfAny();
//			glEnable(GL_COLOR_MATERIAL);
//			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // avg colors together
//			logGlErrorIfAny();
//			GL11.glEnable(GL11.GL_BLEND); // enable transparency
//			logGlErrorIfAny();
//			glEnable(GL_NORMALIZE); // forces normals to size of 1
//			GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
//			logGlErrorIfAny();
//			GL11.glEnable(GL11.GL_ALPHA_TEST); // enable transparency in textures
//			logGlErrorIfAny();
			GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
			logGlErrorIfAny();
			
			viewport.init();
			
			camera = new Camera();
			camera.update();
	}
	
	private void destroyDisplay() {
		Display.destroy();
	}
	
	private void initWorld() {
		// Y is "up"
		texs = new Textures();
		land = new Terrain(new Random(5432543), viewport, camera);
		shapes = new ArrayList<Shape>();
		land.create();
//		land.init();
		int baseY = 10;
		shapes.add(new Shape(texs, 0, baseY, 0));
		shapes.add(new Shape(texs, 2, baseY, 0));
		shapes.add(new Shape(texs, 0, baseY, 2));
		shapes.add(new Shape(texs, -2, baseY, 0));
		shapes.add(new Shape(texs, 0, baseY, -2));
		shapes.add(new Shape(texs, 0, baseY+3, 0));
		shapes.add(new Shape(texs, 0, baseY-3, 0));
		
		shapes.add(new Shape(texs, 3, baseY+3, 3));
		shapes.add(new Shape(texs, 3, baseY+3, -3));
		shapes.add(new Shape(texs, 3, baseY-3, 3));
		shapes.add(new Shape(texs, 3, baseY-3, -3));
		shapes.add(new Shape(texs, -2, baseY-2, -2));
		shapes.add(new Shape(texs, -2, baseY-2, 2));
		shapes.add(new Shape(texs, -2, baseY+2, 2));
		shapes.add(new Shape(texs, -2, baseY+2, -2));
	}
	
	private void drawWorld() {
//		long start = System.currentTimeMillis();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | /*GL_STENCIL_BUFFER_BIT |*/ GL11.GL_DEPTH_BUFFER_BIT);
		logGlErrorIfAny();
		
		camera.update();
		
		GL11.glPushMatrix();
		logGlErrorIfAny();
		
		land.draw();
		
		FloatBuffer white = BufferUtils.createFloatBuffer(4).put(new float[] { 1f, 1f, 1f, 1f, });
		white.flip();
		GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE, white);
//		long start2 = System.currentTimeMillis();
		for (Shape s : shapes) {
			s.draw();
		}
//		long dura2 = System.currentTimeMillis() - start2;
//		log("shapes.draw " + dura2 + " ms");
		
		GL11.glPopMatrix();
		logGlErrorIfAny();
//		long duration = System.currentTimeMillis() - start;
//		if (duration > 1000 / 60) {
//			log("drawWorld " + duration + "ms");
//		}
	}
	
	private void destroyWorld() {
		for (Shape s : shapes) {
			s.destroy();
		}
		shapes.clear();
		land.destroy();
	}
	
}
