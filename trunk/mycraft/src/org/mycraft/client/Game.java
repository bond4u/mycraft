package org.mycraft.client;

import java.awt.Canvas;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBTransposeMatrix;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

public class Game extends Thread {

	private static final int HEIGHT = 480;
	private static final int WIDTH = HEIGHT * 3 / 2;
	
	private boolean running;
	
	private int fps;
	
	private Canvas canvas;
	
	private TextureLoader texLoader;
	
	private Camera cam;
	
	private List<Shape> shapes;
	
	private Terrain land;
	
	private float fovy = 75f;
	private float zNear = 0.5f;
	private float zFar = 95.5f;

	public int getWidth() {
		return WIDTH;
	}
	
	public int getHeight() {
		return HEIGHT;
	}
	
	public void setCanvas(Canvas c) {
		canvas = c;
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public void initDisplay() {
		// get current desktop
		DisplayMode desktop = Display.getDesktopDisplayMode();
		System.out.println("Desktop: " + desktop);
		DisplayMode current = Display.getDisplayMode();
		System.out.println("Current: " + current);
		fps = current.getFrequency();
		if (canvas != null) { // applet - browser window
			try {
				Display.setParent(canvas);
//				logGlErrorIfAny();
			} catch (LWJGLException e) {
				log("LWJGL.setParent: " + e);
			}
		} else { // desktop window
			DisplayMode dm = new DisplayMode(getWidth(), getHeight());
			try {
				Display.setDisplayMode(dm);
//				logGlErrorIfAny(); // not inited yet?
				int left = (current.getWidth() - getWidth()) / 2;
				int top = (current.getHeight() - getHeight()) / 2;
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
		texLoader = new TextureLoader();
		cam = new Camera();
		land = new Terrain(new Random(5432543));
		shapes = new ArrayList<Shape>();
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
		}
		log("gameLoop ended");
	}
	
	private void pollMouse() {
		int mdx = Mouse.getDX(); // mouse x is 3d y
		int mdy = Mouse.getDY(); // mouse y is 3d z
		if (mdy != 0) {
			cam.lookUpDown(mdy);
		}
		if (mdx != 0) {
			cam.lookLeftRight(mdx); // mouse goes right, but camera turns right
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
			cam.moveForwardBackward(forwardBackward);
		}
		int leftRight = Keyboard.isKeyDown(Keyboard.KEY_A) ? -1 : (Keyboard.isKeyDown(Keyboard.KEY_D) ? 1 : 0);
		logGlErrorIfAny();
		if (leftRight != 0) {
			cam.moveLeftRight(leftRight);
		}
		int upDown = Keyboard.isKeyDown(Keyboard.KEY_SPACE) ? 1 : (Keyboard.isKeyDown(Keyboard.KEY_C) ? -1 : 0);
		logGlErrorIfAny();
		if (upDown != 0) {
			cam.moveUpDown(upDown);
		}
		boolean esc = Keyboard.isKeyDown(Keyboard.KEY_ESCAPE);
		logGlErrorIfAny();
		if (esc) {
			log("escape pressed - stop running");
			running = false;
		}
	}
	
//	public void stop() {
//	}
//	
//	public void destroy() {
//	}
	
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
//		try {
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
			
			warn("LWJGL: " + Sys.getVersion() + " / " + LWJGLUtil.getPlatformName());
			logGlErrorIfAny();
			warn("GL_VENDOR: " + GL11.glGetString(GL11.GL_VENDOR));
			logGlErrorIfAny();
			warn("GL_RENDERER: " + GL11.glGetString(GL11.GL_RENDERER));
			logGlErrorIfAny();
			warn("GL_VERSION: " + GL11.glGetString(GL11.GL_VERSION));
			logGlErrorIfAny();
//			warn("");
//			warn("glLoadTransposeMatrixfARB() supported: " + GLContext.getCapabilities().GL_ARB_transpose_matrix);
//			logGlErrorIfAny();
			
			// canvas
			GL11.glViewport(0, 0, getWidth(), getHeight());
			logGlErrorIfAny();

			// view mode?
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
			
			// camera?
			double rW = getWidth() / (double) getHeight();
			log("window ratio " + rW);
			GLU.gluPerspective(fovy, (float) rW, zNear, zFar);
			logGlErrorIfAny();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			logGlErrorIfAny();
			GL11.glLoadIdentity();
			logGlErrorIfAny();
			double[] camRot = cam.getRotation();
			GL11.glRotatef((float)camRot[0], 1f, 0f, 0f);
			logGlErrorIfAny();
			GL11.glRotatef((float)camRot[1], 0f, 1f, 0f);
			logGlErrorIfAny();
			GL11.glRotatef((float)camRot[2], 0f, 0f, 1f);
			logGlErrorIfAny();
			double[] camPos = cam.getPosition();
			GL11.glTranslated(-camPos[0], -camPos[1], -camPos[2]);
			logGlErrorIfAny();
			log("cam @ " + camPos[0] + "," + camPos[1] + "," + camPos[2] +
					" > " + camRot[0] + "," + camRot[1] + "," + camRot[2]);
			
//			FloatBuffer red = BufferUtils.createFloatBuffer(4).put(new float[] { 0.8f, 0.1f, 0.0f, 1.0f});
//			FloatBuffer green = BufferUtils.createFloatBuffer(4).put(new float[] { 0.0f, 0.8f, 0.2f, 1.0f});
//			FloatBuffer blue = BufferUtils.createFloatBuffer(4).put(new float[] { 0.2f, 0.2f, 1.0f, 1.0f});
//			red.flip();
//			green.flip();
//			blue.flip();
			
//		} catch (Exception e) {
//			warn("ex: " + e);
//			running = false;
//		}
	}
	
	private void destroyDisplay() {
		Display.destroy();
	}
	
	private void initWorld() {
		// Y is "up"
		land.create();
		land.init();
		int baseY = 10;
		shapes.add(new Shape(texLoader, 0, baseY, 0));
		shapes.add(new Shape(texLoader, 2, baseY, 0));
		shapes.add(new Shape(texLoader, 0, baseY, 2));
		shapes.add(new Shape(texLoader, -2, baseY, 0));
		shapes.add(new Shape(texLoader, 0, baseY, -2));
		shapes.add(new Shape(texLoader, 0, baseY+3, 0));
		shapes.add(new Shape(texLoader, 0, baseY-3, 0));
		
		shapes.add(new Shape(texLoader, 3, baseY+3, 3));
		shapes.add(new Shape(texLoader, 3, baseY+3, -3));
		shapes.add(new Shape(texLoader, 3, baseY-3, 3));
		shapes.add(new Shape(texLoader, 3, baseY-3, -3));
		shapes.add(new Shape(texLoader, -2, baseY-2, -2));
		shapes.add(new Shape(texLoader, -2, baseY-2, 2));
		shapes.add(new Shape(texLoader, -2, baseY+2, 2));
		shapes.add(new Shape(texLoader, -2, baseY+2, -2));
	}
	
	private void drawWorld() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | /*GL_STENCIL_BUFFER_BIT |*/ GL11.GL_DEPTH_BUFFER_BIT);
		logGlErrorIfAny();
//		log("cam @ " + camera_point[0] + "," + camera_point[1] + "," + camera_point[2] +
//				" -> " + camera_rotation[0] + "," + camera_rotation[1] + "," + camera_rotation[2]);
		// inverse camera coordinates - that way we get scene movement
		// loading identity - that should be last identity - model view ?
		GL11.glLoadIdentity();
		logGlErrorIfAny();
		double[] camRot = cam.getRotation();
		GL11.glRotatef((float)camRot[0], 1f, 0f, 0f);
		logGlErrorIfAny();
		GL11.glRotatef((float)camRot[1], 0f, 1f, 0f);
		logGlErrorIfAny();
		GL11.glRotatef((float)camRot[2], 0f, 0f, 1f);
		logGlErrorIfAny();
		double[] camPos = cam.getPosition();
		GL11.glTranslated(-camPos[0], -camPos[1], -camPos[2]);
		logGlErrorIfAny();
		
		GL11.glPushMatrix();
		logGlErrorIfAny();
		
		land.draw();
		
		FloatBuffer white = BufferUtils.createFloatBuffer(4).put(new float[] { 1f, 1f, 1f, 1f, });
		white.flip();
		GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE, white);
		for (Shape s : shapes) {
			s.draw();
		}
		
		GL11.glPopMatrix();
		logGlErrorIfAny();
	}
	
	private void destroyWorld() {
		for (Shape s : shapes) {
			s.destroy();
		}
		shapes.clear();
		land.destroy();
	}
	
}
