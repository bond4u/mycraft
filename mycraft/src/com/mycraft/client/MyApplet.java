package com.mycraft.client;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.opengl.ARBTransposeMatrix;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

public class MyApplet extends Applet {

	private static final long serialVersionUID = 0;
	
	/** The Canvas where the LWJGL Display is added */
	private Canvas gameCanvas;

	/** Thread which runs the main game loop */
	private Thread gameThread;

	/** is the game loop running */
	private boolean running;

	private int fps = 60;
	
	private TextureLoader textureLoader;
	
	private Camera cam;
	
//	private final float[] light_point = { 5f, 5f, 10f, };
	
	private int gear1;
	private int	gear2;
	private int	gear3;
	private float angle;

	private List<Shape> shapes;
	
	private float fovy = 60f;
	private float zNear = 1f;
	private float zFar = 26f;
	
	/**
	 * Once the Canvas is created its add notify method will call this method to
	 * start the LWJGL Display and game loop in another thread.
	 */
	public void startLWJGL() {
		log("gameThread starting");
		gameThread = new Thread() {
			public void run() {
				running = true;
				try {
					DisplayMode dm = Display.getDisplayMode();
//					logGlErrorIfAny();
					fps = dm.getFrequency();
//					logGlErrorIfAny();
					Display.setParent(gameCanvas);
//					logGlErrorIfAny();
					//Display.setVSyncEnabled(true);
					Display.create();
					logGlErrorIfAny();
					Mouse.setGrabbed(true);
					logGlErrorIfAny();
					textureLoader = new TextureLoader();
					cam = new Camera();
					shapes = new ArrayList<Shape>();
					initGL();
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				gameLoop();
				destroyGL();
				Display.destroy();
//				logGlErrorIfAny();
			}
		};
		gameThread.start();
		log("gameThread started");
	}


	/**
	 * Tell game loop to stop running, after which the LWJGL Display will be destoryed.
	 * The main thread will wait for the Display.destroy() to complete
	 */
	private void stopLWJGL() {
		log("gameThread stopping");
		// set the flag and wait for thread to die
		running = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cam = null;
		textureLoader = null;
		shapes = null;
		log("gameThread stopped");
	}

	public void start() {
		log("starting");
	}

	public void stop() {
		log("stopping");
	}

	/**
	 * Applet Destroy method will remove the canvas, before canvas is destroyed it will notify
	 * stopLWJGL() to stop main game loop and to destroy the Display
	 */
	public void destroy() {
		log("destroying");
		remove(gameCanvas);
		super.destroy();
	}

	private void log(String s) {
		System.out.println(s);
	}
	
	private void warn(String s) {
		System.err.println(s);
	}
	
	/**
	 * initialise applet by adding a canvas to it, this canvas will start the LWJGL Display and game loop
	 * in another thread. It will also stop the game loop and destroy the display on canvas removal when
	 * applet is destroyed.
	 */
	public void init() {
		log("initializing");
		setLayout(new BorderLayout());
		try {
			gameCanvas = new Canvas() {
				private static final long serialVersionUID = 0;
				public void addNotify() {
					super.addNotify();
					startLWJGL();
				}
				public void removeNotify() {
					stopLWJGL();
					super.removeNotify();
				}
			};
			gameCanvas.setSize(getWidth(),getHeight());
			add(gameCanvas);
			gameCanvas.setFocusable(true);
			gameCanvas.requestFocus();
			gameCanvas.setIgnoreRepaint(true);
			//setResizable(true);
			setVisible(true);
		} catch (Exception e) {
			warn("ex: " + e);
			throw new RuntimeException("init exception: " + e);
		}
	}

	public void gameLoop() {
		log("gameLoop starting");
		long startTime = System.currentTimeMillis() + 5000;
		long frames = 0;

		while(running) {
			angle += 0.1;
			
			// draw the gears
			drawScene();

			Display.update();
			logGlErrorIfAny();
			Display.sync(fps);
			logGlErrorIfAny();

			if (startTime > System.currentTimeMillis()) {
				frames++;
			} else {
				long timeUsed = 5000 + (startTime - System.currentTimeMillis());
				startTime = System.currentTimeMillis() + 5000;
				log(frames + " frames 2 in " + timeUsed / 1000f + " seconds = " + (frames / (timeUsed / 1000f)));
				frames = 0;
			}
			
			pollMouse();
			pollKeyboard();
			
//			showOrigin(1);
//			triangles();
			
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
//		if (Keyboard.isKeyDown(Keyboard.KEY_F) && !Keyboard.isRepeatEvent()) {
//			try {
//				log("toggling fullscreen");
//				Display.setFullscreen(!Display.isFullscreen());
//			} catch (LWJGLException e) {
//				warn("ex: " + e);
//			}
//		}
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

//	protected void showOrigin(float length) {
//		glPushMatrix();				
//		glBegin(GL_LINES);				
//		glColor3f(0, 0, 0);
//		glVertex3f(-length, 0, 0);
//		glColor3f(1, 0, 0);
//		glVertex3f(length, 0, 0);
//		glColor3f(0, 0, 0);
//		glVertex3f(0, -length, 0);
//		glColor3f(1, 1, 0);
//		glVertex3f(0, length, 0);				
//		glColor3f(0, 0, 0);
//		glVertex3f(0, 0, -length);
//		glColor3f(0, 0, 1);
//		glVertex3f(0, 0, length);				
//		glEnd();
//		glPopMatrix();	
//	}
//
//	public void triangles() {
//		glPushMatrix();				
//		glBegin(GL_TRIANGLES);	
//		glColor3d(0, 0, 1);
//		glVertex3f(-0.2f, -0.2f, 0.0f);
//		glColor3d(0, 1, 0);
//		glVertex3f(0.0f, 0.2f, 0.0f);
//		glColor3d(1, 0, 0);
//		glVertex3f(0.2f, -0.2f, 0.0f);
//		glEnd();
//		glPopMatrix();	
//	}
	
	public void drawScene() {
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
		
		GL11.glPushMatrix();
		logGlErrorIfAny();
		GL11.glTranslatef(-3.0f, -2.0f, 0.0f);
		logGlErrorIfAny();
		GL11.glRotatef(angle, 0.0f, 0.0f, 1.0f);
		logGlErrorIfAny();
		GL11.glCallList(gear1);
		logGlErrorIfAny();
		GL11.glPopMatrix();
		logGlErrorIfAny();

		GL11.glPushMatrix();
		logGlErrorIfAny();
		GL11.glTranslatef(3.1f, -2.0f, 0.0f);
		logGlErrorIfAny();
		GL11.glRotatef(-2.0f * angle - 9.0f, 0.0f, 0.0f, 1.0f);
		logGlErrorIfAny();
		GL11.glCallList(gear2);
		logGlErrorIfAny();
		GL11.glPopMatrix();
		logGlErrorIfAny();

		GL11.glPushMatrix();
		logGlErrorIfAny();
		GL11.glTranslatef(-3.1f, 4.2f, 0.0f);
		logGlErrorIfAny();
		GL11.glRotatef(-2.0f * angle - 25.0f, 0.0f, 0.0f, 1.0f);
		logGlErrorIfAny();
		GL11.glCallList(gear3);
		logGlErrorIfAny();
		GL11.glPopMatrix();
		logGlErrorIfAny();
		
		FloatBuffer white = BufferUtils.createFloatBuffer(4).put(new float[] { 1f, 1f, 1f, 1f, });
		white.flip();
		GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE, white);
		for (Shape s : shapes) {
			s.draw();
		}
		
		GL11.glPopMatrix();
		logGlErrorIfAny();
	}

	protected void initGL() {
		try {
			// setup ogl
//			FloatBuffer pos = BufferUtils.createFloatBuffer(4).put(new float[] {
//					light_point[0], light_point[1], light_point[2], 0.0f, });
//			pos.flip();
//			glLight(GL_LIGHT0, GL_POSITION, pos);
			
			GL11.glEnable(GL11.GL_LIGHT0);
			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_LIGHTING);
			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_CULL_FACE); // dont render hidden/back faces
			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			logGlErrorIfAny();
			GL11.glDepthFunc(GL11.GL_LEQUAL); // depth test type
			logGlErrorIfAny();
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			logGlErrorIfAny();
//			glEnable(GL_COLOR_MATERIAL);
			GL11.glEnable(GL11.GL_BLEND); // enable transparency
			logGlErrorIfAny();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // avg colors together
			logGlErrorIfAny();
//			glEnable(GL_NORMALIZE); // forces normals to size of 1
			GL11.glEnable(GL11.GL_ALPHA_TEST); // enable transparency in textures
			logGlErrorIfAny();
			GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
			logGlErrorIfAny();
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
			warn("");
			warn("glLoadTransposeMatrixfARB() supported: " + GLContext.getCapabilities().GL_ARB_transpose_matrix);
			logGlErrorIfAny();
			
			// canvas
			int wW = gameCanvas.getWidth();
			int hW = gameCanvas.getHeight();
			GL11.glViewport(0, 0, wW, hW);
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
			double rW = wW / (double) hW;
			log("window ratio " + rW);
//			GL11.glFrustum(-rW, rW, -1.0, 1.0, 1.0, 11.0);
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
			
//			GL11.glPushMatrix();
//			logGlErrorIfAny();
			
			FloatBuffer red = BufferUtils.createFloatBuffer(4).put(new float[] { 0.8f, 0.1f, 0.0f, 1.0f});
			FloatBuffer green = BufferUtils.createFloatBuffer(4).put(new float[] { 0.0f, 0.8f, 0.2f, 1.0f});
			FloatBuffer blue = BufferUtils.createFloatBuffer(4).put(new float[] { 0.2f, 0.2f, 1.0f, 1.0f});
			red.flip();
			green.flip();
			blue.flip();
			/* make the gears */
			gear1 = GL11.glGenLists(1);
			logGlErrorIfAny();
			GL11.glNewList(gear1, GL11.GL_COMPILE);
			logGlErrorIfAny();
			GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, red);
			logGlErrorIfAny();
			gear(1.0f, 4.0f, 1.0f, 20, 0.7f);
			GL11.glEndList();
			logGlErrorIfAny();

			gear2 = GL11.glGenLists(1);
			logGlErrorIfAny();
			GL11.glNewList(gear2, GL11.GL_COMPILE);
			logGlErrorIfAny();
			GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, green);
			logGlErrorIfAny();
			gear(0.5f, 2.0f, 2.0f, 10, 0.7f);
			GL11.glEndList();
			logGlErrorIfAny();

			gear3 = GL11.glGenLists(1);
			logGlErrorIfAny();
			GL11.glNewList(gear3, GL11.GL_COMPILE);
			logGlErrorIfAny();
			GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE, blue);
			logGlErrorIfAny();
			gear(1.3f, 2.0f, 0.5f, 10, 0.7f);
			GL11.glEndList();
			logGlErrorIfAny();
			
			// Y is "up"
			shapes.add(new Shape(textureLoader, 0, 0, 0));
			shapes.add(new Shape(textureLoader, 4, 0, 0));
			shapes.add(new Shape(textureLoader, 0, 0, 4));
			shapes.add(new Shape(textureLoader, -4, 0, 0));
			shapes.add(new Shape(textureLoader, 0, 0, -4));
			shapes.add(new Shape(textureLoader, 0, 4, 0));
			shapes.add(new Shape(textureLoader, 0, -4, 0));
			
			shapes.add(new Shape(textureLoader, 4, 4, 4));
			shapes.add(new Shape(textureLoader, 4, 4, -4));
			shapes.add(new Shape(textureLoader, 4, -4, 4));
			shapes.add(new Shape(textureLoader, 4, -4, -4));
			shapes.add(new Shape(textureLoader, -4, -4, -4));
			shapes.add(new Shape(textureLoader, -4, -4, 4));
			shapes.add(new Shape(textureLoader, -4, 4, 4));
			shapes.add(new Shape(textureLoader, -4, 4, -4));
			
//			GL11.glPopMatrix();
//			logGlErrorIfAny();

		} catch (Exception e) {
			warn("ex: " + e);
			running = false;
		}
	}

	/**
	 * Draw a gear wheel.  You'll probably want to call this function when
	 * building a display list since we do a lot of trig here.
	 *
	 * @param inner_radius radius of hole at center
	 * @param outer_radius radius at center of teeth
	 * @param width width of gear
	 * @param teeth number of teeth
	 * @param tooth_depth depth of tooth
	 */
	private void gear(float inner_radius, float outer_radius, float width, int teeth, float tooth_depth) {
		int i;
		float r0, r1, r2;
		float angle, da;
		float u, v, len;

		r0 = inner_radius;
		r1 = outer_radius - tooth_depth / 2.0f;
		r2 = outer_radius + tooth_depth / 2.0f;
		da = 2.0f * (float) Math.PI / teeth / 4.0f;
		GL11.glShadeModel(GL11.GL_SMOOTH);
		logGlErrorIfAny();
		GL11.glNormal3f(0.0f, 0.0f, 1.0f);
		logGlErrorIfAny();
		/* draw front face */
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		logGlErrorIfAny();
		for (i = 0; i <= teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			GL11.glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), width * 0.5f);
			logGlErrorIfAny();
			if (i < teeth) {
				GL11.glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), width * 0.5f);
				logGlErrorIfAny();
				GL11.glVertex3f(r1 * (float) Math.cos(angle + 3.0f * da), r1 * (float) Math.sin(angle + 3.0f * da),
						width * 0.5f);
				logGlErrorIfAny();
			}
		}
		GL11.glEnd();
		logGlErrorIfAny();

		/* draw front sides of teeth */
		GL11.glBegin(GL11.GL_QUADS);
		logGlErrorIfAny();
		for (i = 0; i < teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			GL11.glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + 2.0f * da), r2 * (float) Math.sin(angle + 2.0f * da), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle + 3.0f * da), r1 * (float) Math.sin(angle + 3.0f * da), width * 0.5f);
			logGlErrorIfAny();
		}
		GL11.glEnd();
		logGlErrorIfAny();

		/* draw back face */
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		logGlErrorIfAny();
		for (i = 0; i <= teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			GL11.glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), -width * 0.5f);
			logGlErrorIfAny();
		}
		GL11.glEnd();
		logGlErrorIfAny();

		/* draw back sides of teeth */
		GL11.glBegin(GL11.GL_QUADS);
		logGlErrorIfAny();
		for (i = 0; i < teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			GL11.glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + 2 * da), r2 * (float) Math.sin(angle + 2 * da), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), -width * 0.5f);
			logGlErrorIfAny();
		}
		GL11.glEnd();
		logGlErrorIfAny();

		/* draw outward faces of teeth */
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		logGlErrorIfAny();
		for (i = 0; i < teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			GL11.glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle), r1 * (float) Math.sin(angle), -width * 0.5f);
			logGlErrorIfAny();
			u = r2 * (float) Math.cos(angle + da) - r1 * (float) Math.cos(angle);
			v = r2 * (float) Math.sin(angle + da) - r1 * (float) Math.sin(angle);
			len = (float) Math.sqrt(u * u + v * v);
			u /= len;
			v /= len;
			GL11.glNormal3f(v, -u, 0.0f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + da), r2 * (float) Math.sin(angle + da), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glNormal3f((float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + 2 * da), r2 * (float) Math.sin(angle + 2 * da), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r2 * (float) Math.cos(angle + 2 * da), r2 * (float) Math.sin(angle + 2 * da), -width * 0.5f);
			logGlErrorIfAny();
			u = r1 * (float) Math.cos(angle + 3 * da) - r2 * (float) Math.cos(angle + 2 * da);
			v = r1 * (float) Math.sin(angle + 3 * da) - r2 * (float) Math.sin(angle + 2 * da);
			GL11.glNormal3f(v, -u, 0.0f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r1 * (float) Math.cos(angle + 3 * da), r1 * (float) Math.sin(angle + 3 * da), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glNormal3f((float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
			logGlErrorIfAny();
		}
		GL11.glVertex3f(r1 * (float) Math.cos(0), r1 * (float) Math.sin(0), width * 0.5f);
		logGlErrorIfAny();
		GL11.glVertex3f(r1 * (float) Math.cos(0), r1 * (float) Math.sin(0), -width * 0.5f);
		logGlErrorIfAny();
		GL11.glEnd();
		logGlErrorIfAny();

//		GL11.glShadeModel(GL11.GL_SMOOTH);
		logGlErrorIfAny();
		/* draw inside radius cylinder */
		GL11.glBegin(GL11.GL_QUAD_STRIP);
		logGlErrorIfAny();
		for (i = 0; i <= teeth; i++) {
			angle = i * 2.0f * (float) Math.PI / teeth;
			GL11.glNormal3f(-(float) Math.cos(angle), -(float) Math.sin(angle), 0.0f);
			logGlErrorIfAny();
			GL11.glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), -width * 0.5f);
			logGlErrorIfAny();
			GL11.glVertex3f(r0 * (float) Math.cos(angle), r0 * (float) Math.sin(angle), width * 0.5f);
			logGlErrorIfAny();
		}
		GL11.glEnd();
		logGlErrorIfAny();
	}
	
	private void destroyGL() {
		for (Shape s : shapes) {
			s.destroy();
		}
		shapes.clear();
	}
	
	private void logGlErrorIfAny() {
		int e = GL11.glGetError();
		if (e != 0) {
			log("MyApplet openGL error " + e + ": " + GLU.gluErrorString(e));
		}
	}
	
}
