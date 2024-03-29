package org.mycraft.client;

import java.awt.Canvas;
import java.awt.Font;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
import org.mycraft.client.terrain.Terrain;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.TextureImpl;
//import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.ResourceLoader;

import sun.nio.ch.DirectBuffer;

public class Game extends Thread {
	
	private boolean running;
	
	private int fps;
	
	private Viewport viewport;
	private Canvas canvas;
	private Textures texs;
	private Cursor cursor;
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
//		log("isClipMouse="+Mouse.isClipMouseCoordinatesToWindow());
//		logGlErrorIfAny();
//		log("isMouseGrabbed="+Mouse.isGrabbed());
//		logGlErrorIfAny();
		Keyboard.enableRepeatEvents(true);
		logGlErrorIfAny();
//		log("areKeyboardRepeatEventsEnabled="+Keyboard.areRepeatEventsEnabled());
//		logGlErrorIfAny();
	}
	
//	public void start() {
//	}
	
	public void run() {
		running = true;
		initDisplay();
		initGL();
		initMenu();
		initWorld();
		gameLoop();
		destroyWorld();
		destroyDisplay();
	}
	
	private TrueTypeFont menuFont = null;
	private boolean isMenuActive = false;
	
	protected void initMenu() {
		final boolean antiAlias = true;
		try {
			InputStream is = ResourceLoader.getResourceAsStream("org/mycraft/client/Comic_Book.ttf");
			Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is);
			awtFont = awtFont.deriveFont(24f);
			menuFont = new TrueTypeFont(awtFont, antiAlias);
		} catch (Exception e) {
			log("initMenu: " + e.getMessage());
		}
	}
	
	protected void activateMenu(boolean activate) {
		if (activate && !isMenuActive) {
			isMenuActive = true;
			Mouse.setGrabbed(false);
		} else if (!activate && isMenuActive) {
			isMenuActive = false;
			Mouse.setGrabbed(true);
		}
	}
	
	protected void drawMenu() {
		if (isMenuActive) {
			if (null == menuFont) {
				log("drawMenu: font is not initialized");
				return;
			}
			viewport.proj2d();
			
			GL11.glPushMatrix();
			logGlErrorIfAny(); // +1=1
			
			GL11.glTranslatef(0f, 0f, -0.1f);
			logGlErrorIfAny();

			GL11.glColor4f(0f, 0f, 1f, 1f); // alpha 0=transparent
			logGlErrorIfAny();
			
			float l1 = 50f;
			float t1 = 50f;
			float w = 255f;
			float h = 24f;
			float p = 5f;
			
			GL11.glBegin(GL11.GL_QUADS);
			
			GL11.glVertex2f(l1-p, t1+h);
			GL11.glVertex2f(l1+w+p, t1+h);
			GL11.glVertex2f(l1+w+p, t1);
			GL11.glVertex2f(l1-p, t1);
			
			GL11.glEnd();
			logGlErrorIfAny();

			float l2 = 50f;
			float t2 = 100f;
			w = 100f;
			
			GL11.glBegin(GL11.GL_QUADS);
			
			GL11.glVertex2f(l2-p, t2+h);
			GL11.glVertex2f(l2+w+p, t2+h);
			GL11.glVertex2f(l2+w+p, t2);
			GL11.glVertex2f(l2-p, t2);
			
			GL11.glEnd();
			logGlErrorIfAny();
			
			GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
			logGlErrorIfAny();			

			menuFont.drawString(l1, t1, "Esc - Pause/Resume", Color.white);
			
			menuFont.drawString(l2, t2, "Q - Quit", Color.white);
			
			GL11.glPopAttrib();
			logGlErrorIfAny();
			// FIXME odd that something goes wrong with drawString() besides texturing
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			logGlErrorIfAny();
			
//			GL11.glColor4f(1f, 1f, 1f, 1f);
//			logGlErrorIfAny();

			GL11.glPopMatrix();
			logGlErrorIfAny(); // -1=0
			
			viewport.proj3d();
		}
	}
	
	public void gameLoop() {
		log("gameLoop starting");
		long startTime = System.currentTimeMillis() + 5000;
		long frames = 0;

		while(running) {
			// draw the gears
			drawWorld();
			cursor.draw();
			drawMenu();
			GL11.glFlush();
			logGlErrorIfAny();
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
				log("display.closeRequested menu=" + isMenuActive);
				if (!isMenuActive) {
					activateMenu(!isMenuActive);
				} else { // do really close when requested 2nd time
					running = false;
				}
			}
//			long duration = System.currentTimeMillis() - start;
//			if (duration > 1000 / 60) {
//				log("system " + duration + " ms");
//			}
		}
		log("gameLoop ended");
	}
	
	private void pollMouse() {
		if (!isMenuActive) {
			int mdx = Mouse.getDX(); // mouse x is 3d y
			int mdy = Mouse.getDY(); // mouse y is 3d z
			if (mdy != 0) {
				camera.lookUpDown(mdy);
			}
			if (mdx != 0) {
				camera.lookLeftRight(mdx); // mouse goes right, but camera turns right
			}
		}
	}
	
	private void pollKeyboard() {
		if (!isMenuActive) {
			int forwardBackward = Keyboard.isKeyDown(Keyboard.KEY_W) ? 1 : (Keyboard.isKeyDown(Keyboard.KEY_S) ? -1 : 0);
			if (0 != forwardBackward) {
				camera.moveForwardBackward(forwardBackward);
			}
			int leftRight = Keyboard.isKeyDown(Keyboard.KEY_A) ? -1 : (Keyboard.isKeyDown(Keyboard.KEY_D) ? 1 : 0);
			if (0 != leftRight) {
				camera.moveLeftRight(leftRight);
			}
			int upDown = Keyboard.isKeyDown(Keyboard.KEY_SPACE) ? 1 : (Keyboard.isKeyDown(Keyboard.KEY_C) ? -1 : 0);
			if (0 != upDown) {
				camera.moveUpDown(upDown);
			}
		}
		while (Keyboard.next()) {
			logGlErrorIfAny();
			boolean pressed = Keyboard.getEventKeyState();
			logGlErrorIfAny();
			int key = Keyboard.getEventKey();
			logGlErrorIfAny();
			if (pressed) {
			} else {
				// released
				if (Keyboard.KEY_F == key) {
					log("F - switching to fullscreen="+Display.isFullscreen());
					try {
						Display.setFullscreen(!Display.isFullscreen());
					} catch (LWJGLException e) {
						log("LWJGL.setFullscreen: " + e);
					}
				} else if (Keyboard.KEY_Q == key) {
					if (isMenuActive) {
						running = false;
						log("Q - running=false");
					}
				} else if (Keyboard.KEY_ESCAPE == key) {
					log("ESC pressed - menu="+isMenuActive);
					activateMenu(!isMenuActive);
				}
			}
		}
		logGlErrorIfAny();
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
//		GL11.glEnable(GL11.GL_LIGHTING);
//		logGlErrorIfAny();
//		boolean haveLighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
//		log("lighting is: " + haveLighting);
//		GL11.glEnable(GL11.GL_LIGHT0);
//		logGlErrorIfAny();
//		boolean haveLight0 = GL11.glIsEnabled(GL11.GL_LIGHT0);
//		log("light 0 is: " + haveLight0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		logGlErrorIfAny();
//		int haveShadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
//		log("shading model is: " + haveShadeModel + " (" + (haveShadeModel==GL11.GL_SMOOTH?"SMOOTH":"n/a") + ")");
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		logGlErrorIfAny();
		GL11.glDepthMask(true);
		logGlErrorIfAny();
		// depthrange is default [0.0..1.0]
		GL11.glEnable(GL11.GL_CULL_FACE); // dont render hidden/back faces
		logGlErrorIfAny();
//		int haveCullMode = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
//		log("cull face mode is: " + haveCullMode + " (" + (haveCullMode==GL11.GL_BACK?"BACK":"n/a") + ")");
//		GL11.glEnable(GL11.GL_TEXTURE_2D);
//		logGlErrorIfAny();
//		boolean haveTexture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
//		log("2d textures are: " + haveTexture);
//			glEnable(GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_BLEND); // enable transparency
		logGlErrorIfAny();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // avg colors together
		logGlErrorIfAny();
//		boolean haveBlend = GL11.glIsEnabled(GL11.GL_BLEND);
//		log("blending is: " + haveBlend);
//		glEnable(GL_NORMALIZE); // forces normals to size of 1
//		GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
//		logGlErrorIfAny();
//		int alphaFunc = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
//		log("alpha test func is: " + alphaFunc + " (" + (alphaFunc==GL11.GL_ALWAYS?"ALWAYS":"n/a") + ")");
//		GL11.glEnable(GL11.GL_ALPHA_TEST); // enable transparency in textures
//		logGlErrorIfAny();
//		boolean haveAlphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
//		log("alpha test is: " + haveAlphaTest);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		logGlErrorIfAny();
			
		viewport.init();
			
		cursor = new Cursor(/*viewport*/);
			
		camera = new Camera();
			
		GL11.glLoadIdentity();
		logGlErrorIfAny();

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
		int baseY = 5;
		shapes.add(new Shape(texs, 0, baseY, 0));
		shapes.add(new Shape(texs, 1, baseY, 0));
		shapes.add(new Shape(texs, 0, baseY, 1));
		shapes.add(new Shape(texs, -1, baseY, 0));
		shapes.add(new Shape(texs, 0, baseY, -1));
		shapes.add(new Shape(texs, 0, baseY+2, 0));
		shapes.add(new Shape(texs, 0, baseY-2, 0));
		
		shapes.add(new Shape(texs, 2, baseY+2, 2));
		shapes.add(new Shape(texs, 2, baseY+2, -2));
		shapes.add(new Shape(texs, 2, baseY-2, 2));
		shapes.add(new Shape(texs, 2, baseY-2, -2));
		shapes.add(new Shape(texs, -1, baseY-1, -1));
		shapes.add(new Shape(texs, -1, baseY-1, 1));
		shapes.add(new Shape(texs, -1, baseY+1, 1));
		shapes.add(new Shape(texs, -1, baseY+1, -1));
	}
	
	// logs contents of FloagBuffer
/*	protected void logBuf(FloatBuffer fb, String s) {
		String z = s;
		while (fb.remaining() > 0) {
			z += " ";
			z += fb.get();
		}
		fb.rewind();
		log(z);
	}*/
	
	private void drawWorld() {
//		long start = System.currentTimeMillis();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | /*GL_STENCIL_BUFFER_BIT |*/ GL11.GL_DEPTH_BUFFER_BIT);
		logGlErrorIfAny();
		
		GL11.glClearDepth(1.0);
		logGlErrorIfAny();
		
		GL11.glLoadIdentity();
		logGlErrorIfAny();
		
		GL11.glPushMatrix(); // +1=1
		logGlErrorIfAny();
		
		camera.update();
		
//		// model matrix = 1 0 0 0; 0 1 0 0; 0 0 1 0; 0 0 0 1
//		FloatBuffer mm = BufferUtils.createFloatBuffer(4*4);
//		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, mm);
//		logGlErrorIfAny();
////		logBuf(mm, "mm0:");

		land.draw();
		
		FloatBuffer white = BufferUtils.createFloatBuffer(4).put(new float[] { 1f, 1f, 1f, 1f, });
		white.flip();
		GL11.glMaterial(GL11.GL_FRONT/*_AND_BACK*/, GL11.GL_AMBIENT_AND_DIFFUSE, white);
//		long start2 = System.currentTimeMillis();
		for (Shape s : shapes) {
			s.draw();
		}
//		long dura2 = System.currentTimeMillis() - start2;
//		log("shapes.draw " + dura2 + " ms");
		
//		float ds = GL11.glGetFloat(GL11.GL_DEPTH_SCALE); //= 1.0
//		logGlErrorIfAny();
//		float db = GL11.glGetFloat(GL11.GL_DEPTH_BIAS); //= 0.0
//		logGlErrorIfAny();
		
//		if (ds != viewport.getFar()) {
//			GL11.glPixelTransferf(GL11.GL_DEPTH_SCALE, viewport.getFar());
//			logGlErrorIfAny();
//		}
//		if (db != viewport.getNear()) {
//			GL11.glPixelTransferf(GL11.GL_DEPTH_BIAS, viewport.getNear());
//			logGlErrorIfAny();
//		}
		
		// viewport & proj matrix are constant
		// we get pixel depth from window buffer & (un)project it to scene space
//		// viewport = 0 0 720 480
//		IntBuffer vp = BufferUtils.createIntBuffer(4*4);
//		GL11.glGetInteger(GL11.GL_VIEWPORT, vp);
//		logGlErrorIfAny();
		int wx = viewport.getWidth() / 2;
		int wy = viewport.getHeight() / 2;
		FloatBuffer px = BufferUtils.createFloatBuffer(1);
		GL11.glReadPixels(wx, wy, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, px);
		logGlErrorIfAny();
		if (px.get(0) <= 0.98f) {
			// projection matrix:
			// 1.0464569 0 0 0
			// 0 1.5696855 0 0
			// 0 0 -1.0021052 -1.0
			// 0 0 0.20021053 0
			FloatBuffer pm = BufferUtils.createFloatBuffer(4*4);
			GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, pm);
			logGlErrorIfAny();
			IntBuffer vp = viewport.getMatrix();
			FloatBuffer mm = camera.getMatrix();
			FloatBuffer op = BufferUtils.createFloatBuffer(4*4);
			GLU.gluUnProject(wx, wy, px.get(0), mm, pm, vp, op);
			logGlErrorIfAny();
			float[] cr = camera.getRotation();
//			log("readpixel(depth)=" + px.get(0) + /*" b=" + db + " s=" + ds +*/
//				/*" z=" + (1f / px.get(0)) +*/ " o=" + op.get(0) + ", " +
//				op.get(1) + ", " + op.get(2) + "; " + /*Math.round(op.get(0)) + ", " +
//				Math.round(op.get(1)) + ", " + Math.round(op.get(2)) + "; " +
//				Math.ceil(op.get(0)) + ", " + Math.ceil(op.get(1)) + ", " +
//				Math.ceil(op.get(2)) + "; " + Math.floor(op.get(0)) + ", " +
//				Math.floor(op.get(1)) + ", " + Math.floor(op.get(2))*/
//				cr[0] + ", " + cr[1] + ", " + cr[2] 
//				);
//			GL11.glPopMatrix(); // -1=0
//			logGlErrorIfAny();
			// fixing cell borders, depending from angle we decide which face it is
			// .. and how to "fix" it
			float x = op.get(0) /*- (float)Math.floor(px.get(0))*/;
			float y = op.get(1) /*- (float)Math.floor(px.get(1))*/;
			float z = op.get(2) /*- (float)Math.floor(px.get(2))*/;
			if ((cr[1] > -360f && cr[1] < -180f) ||
					(cr[1] > 0f && cr[1] < 180f)) {
				x += 0.0062f;
			} else if ((cr[1] > -180f && cr[1] < 0f) ||
					(cr[1] > 180f && cr[1] < 360f)) {
				x -= 0.0062f;
			}
			if ((cr[0] > -360 && cr[0] < -180f) ||
					(cr[0] > 0f && cr[0] < 180f)) {
				y -= 0.0062f;
			} else if ((cr[0] > -180f && cr[0] < 0f) ||
					(cr[0] > 180f && cr[0] < 360f)) {
				y += 0.0062f;
			}
			if ((cr[1] < 90f && cr[1] > -90f) ||
					(cr[1] > 270f) || (cr[1] < -270f)) {
				z -= 0.0062f;
			} else if ((cr[1] < 270f && cr[1] > 90f) ||
					(cr[1] < -90f && cr[1] > -270f)) {
				z += 0.0062f;
			}
			GL11.glTranslatef((float)Math.floor(x),
					(float)Math.floor(y),
					(float)Math.floor(z));
			// black line, but it's too thin
			GL11.glColor4f(0f, 0f, 0f, 1f);
			logGlErrorIfAny();
			// let's draw a wired cube
			float d = 1f;
			// one polyline ..
			GL11.glBegin(GL11.GL_LINE_STRIP);
			GL11.glVertex3f(0f, 0f, 0f); //1
			GL11.glVertex3f( d, 0f, 0f); //2
			GL11.glVertex3f( d,  d, 0f); //3
			GL11.glVertex3f(0f,  d, 0f); //4
			GL11.glVertex3f(0f, 0f, 0f);
			GL11.glVertex3f(0f, 0f,  d); //1
			GL11.glVertex3f( d, 0f,  d); //2
			GL11.glVertex3f( d,  d,  d); //3
			GL11.glVertex3f(0f,  d,  d); //4
			GL11.glVertex3f(0f, 0f,  d);
			GL11.glEnd();
			logGlErrorIfAny();
			// .. and filling the gaps
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex3f( d, 0f, 0f); //2
			GL11.glVertex3f( d, 0f,  d); //2
			GL11.glVertex3f( d,  d, 0f); //3
			GL11.glVertex3f( d,  d,  d); //3
			GL11.glVertex3f(0f,  d, 0f); //4
			GL11.glVertex3f(0f,  d,  d); //4
			GL11.glEnd();
			logGlErrorIfAny();
//			GL11.glPushMatrix(); // +1=1
//			logGlErrorIfAny();
		}
		
		GL11.glPopMatrix(); // -1=0
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
