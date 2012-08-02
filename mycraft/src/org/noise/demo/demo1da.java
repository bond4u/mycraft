package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;

import org.noise.IFunc1D;

@SuppressWarnings("serial")
public class demo1da extends DemoBase {

	public demo1da(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo1da demo = new demo1da("demo 1d a");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private IFunc1D func = new Sine();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		final int len = getWidth();
		int[] xP = new int[len];
		int[] yP = new int[len];
		final int delta = getHeight() / 2;
		
		for (int i = 0; i < len; i++) {
			xP[i] = i;
			double f = (xP[i] * Math.PI) / 180.0;
			int r = Math.round(delta * func.get((float)f));			
			yP[i] = delta + r;
		}
		
		g.setColor(Color.black);
		g.drawPolyline(xP, yP, len);
	}
	
}
