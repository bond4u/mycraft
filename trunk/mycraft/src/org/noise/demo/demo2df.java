package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import org.noise.Convert1D;
import org.noise.IConv1D;
import org.noise.ILimit1D;
import org.noise.ImprovedPerlinNoise;
import org.noise.PermutationsTable;
import org.noise.Threshold1D;

@SuppressWarnings("serial")
public class demo2df extends DemoBase {

	public demo2df(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2df demo = new demo2df("demo 2d f");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private PermutationsTable tbl = new PermutationsTable(new Random());
	
	private ImprovedPerlinNoise noise = new ImprovedPerlinNoise(tbl);
	
	private IConv1D conv = new Convert1D();
	
	private ILimit1D top = new Threshold1D();	
	private ILimit1D bot = new Threshold1D();
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (null == noise) {
			System.out.println("not initialized yet; not ready to paint");
			return;
		}
		
		int w = getWidth();
		int h = getHeight();

		conv.set(-1.0f, 1.0f, 0.0f, 1.0f); // [-1..+1]->[0..1]
		
		top.set(ILimit1D.Condition.ABOVE, 1f, 1f);
		bot.set(ILimit1D.Condition.BELOW, 0f, 0f);

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				float x = i / 50.0f; // frequency=1/5=is sharp, like tv white noise
				float y = j / 50.0f; // 1/50 is smoother
				// get noise for point
				float a = noise.get(x, y, 0.0f);
				// convert to color
				float k = conv.get(a);
				if (k < 0f || 1f < k) {
			System.out.println("i: " + i + " j: " + j + " k: " + k);
				}
				float l = bot.get(top.get(k));
				// paint the point at the original non-transformed location [i,j]
				Color c = new Color(l, l, l);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
	}

}
