package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;

import org.noise.IFunc1D;

@SuppressWarnings("serial")
public class demo1db extends DemoBase {

	public demo1db(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo1db demo = new demo1db("demo 1d b");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private IFunc1D func = new Sine();
	
	private int x = -1;
	
	private Thread timer = null;
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		x += 1;
		int delta = getWidth() / 2;
		double r = (x * Math.PI) / 180.0;
		float f = func.get((float)r);
		int y = Math.round(delta * f);
		
		g.setColor(Color.black);
		g.fillOval(demoWidth() / 2 + y, demoHeight() / 2, 25, 25);
		
		if (null == timer) {
			timer = startTimerThread();
		}
	}

	protected Thread startTimerThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				timerLoop();
			}
		});
		t.start();
		return t;
	}
	
	protected void timerLoop() {
		while (isShowing() && isVisible()) {
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
			}
			repaint();
		}
	}
	
}
