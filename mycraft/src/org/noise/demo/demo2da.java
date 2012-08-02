package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;

import org.noise.IFunc2D;

@SuppressWarnings("serial")
public class demo2da extends DemoBase {

	public demo2da(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2da demo = new demo2da("demo 2d a");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private IFunc2D func = new Grad();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int w = getWidth();
		int h = getHeight();
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				float f = func.get(i,  j);
				// convert value from range [0..height] to range [0..1f]
				float k = 1 - (f / h);
				Color c = new Color(k, k, k);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
	}

}
