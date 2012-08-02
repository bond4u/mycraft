package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import org.noise.Convert1D;
import org.noise.FBM2;
import org.noise.IConv1D;
import org.noise.IFbm2D;
import org.noise.IFunc2D;
import org.noise.ILimit1D;
import org.noise.PermutationsTable;
import org.noise.Threshold1D;
import org.noise.ILimit1D.Condition;

@SuppressWarnings("serial")
public class demo2dd extends DemoBase {

	public demo2dd(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2dd demo = new demo2dd("demo 2d d");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private PermutationsTable tbl = new PermutationsTable(new Random());
	
	private IFbm2D dist = new FBM2(tbl);
	
	private ILimit1D top = new Threshold1D();	
	private ILimit1D bot = new Threshold1D();

	private IConv1D conv = new Convert1D();
	
	private ILimit1D tp2 = new Threshold1D();
	private ILimit1D bt2 = new Threshold1D();
	
	private IFunc2D grad = new Grad();
	
	private ILimit1D tp3 = new Threshold1D();
	private ILimit1D bt3 = new Threshold1D();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (null == dist) {
			System.out.println("not initialized yet; not ready to paint");
			return;
		}
		
		int w = getWidth();
		int h = getHeight();
		
		dist.setOctaves(4);
		dist.setLacunarity(2f);
		dist.setGain(0.85f);
		
		top.set(Condition.ABOVE, 1f, 1f);
		bot.set(Condition.BELOW, -1f, -1f);

		conv.set(-1.0f, +1.0f, -0.25f, 0.25f);
		
		tp2.set(Condition.ABOVE, 0.25f, 0.25f);
		bt2.set(Condition.BELOW, -0.25f, -0.25f);

		tp3.set(Condition.ABOVE, 0.5f, 1f);
		bt3.set(Condition.BELOW, 0.5f, 0f);
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				// get disturbance for point [i,j]
				float d = dist.get(i/80f, j/80f);
				if (d < -1f || 1f < d) {
			System.out.println("i: " + i + " j: " + j + " d: " + d);
				}
				// make sure dist is in range
				float e = bot.get(top.get(d));
				// convert from range [-1..+1] to range [-0.25..+0.25f]
				float f = conv.get(d);
				if (f < -0.25f || 0.25f < f) {
			System.out.println("i: " + j + " j: " + j + " d: " + d + " e: " + e + " f: " + f);
				}
				float b = bt2.get(tp2.get(f));
				// disturb the point
				float x = i;
				float y = j + (h * b);
				// get the gradient at disturbed point [x,y]
				float a = grad.get(x, y);
				// convert to color
				float k = 1f - (a / h);
				if (k < 0f || 1f < k) {
			System.out.println("i: " + i + " j: " + j + " k: " + k);
				}
				// limit to black below 0.5 and white above 0.5
				float l = bt3.get(tp3.get(k));
				// paint the point at the original non-transformed location [i,j]
				Color c = new Color(l, l, l);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
	}

}
