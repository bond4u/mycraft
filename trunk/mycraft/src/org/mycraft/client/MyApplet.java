package org.mycraft.client;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

public class MyApplet extends Applet {

	private static final long serialVersionUID = 0;
	
	private Game game;
	/** The Canvas where the LWJGL Display is added */
	private Canvas gameCanvas;

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
			game = new Game();
			gameCanvas = new Canvas() {
				private static final long serialVersionUID = 0;
				public void addNotify() {
					super.addNotify();
					// canvas has been added to window - start paint thread
					game.start();
				}
				public void removeNotify() {
					// canvas has been remvoed from window - stop paint thread
					try {
						game.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					super.removeNotify();
				}
			};
			gameCanvas.setSize(game.getWidth(),game.getHeight());
			add(gameCanvas);
			gameCanvas.setFocusable(true);
			gameCanvas.requestFocus();
			gameCanvas.setIgnoreRepaint(true);
			//setResizable(true);
			setVisible(true);
		} catch (Throwable t) {
			warn("ex: " + t);
			throw new RuntimeException("init exception: " + t);
		}
	}
	
}
