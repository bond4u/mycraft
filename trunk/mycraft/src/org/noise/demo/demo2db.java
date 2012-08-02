package org.noise.demo;

import java.awt.Color;
import java.awt.Graphics;

import org.noise.IConv1D;
import org.noise.IFunc2D;
import org.noise.ILimit1D;
import org.noise.Threshold1D;
import org.noise.ILimit1D.Condition;

@SuppressWarnings("serial")
public class demo2db extends DemoBase {

	public demo2db(String t) {
		super(t);
	}
	
	public static void main(String[] args) {
		demo2db demo = new demo2db("demo 2d b");
		demo.addAndShow(demo);
	}

	public int demoWidth() {
		return 400;
	}
	
	public int demoHeight() {
		return 200;
	}
	
	private IFunc2D func = new Grad();
		
	private IConv1D up = new RandomUp();
	
	private IConv1D dn = new RandomDown();
	
	private ILimit1D gnd = new Threshold1D();
	
	private ILimit1D sky = new Threshold1D();

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int w = getWidth();
		int h = getHeight();
			
		float q = h / 4f;
		up.set(q, 2 * q, q, 3 * q); // [0.25..0.5]->[0.25..0.75]
		dn.set(2 * q, 3 * q, q, 3 * q); // [0.5..0.75]->[0.25..0.75]
		
		sky.set(Condition.ABOVE_OR_EQUAL, 0.5f, 1f); // convert to 1f when 0.5f or above
		gnd.set(Condition.BELOW, 0.5f, 0f); // convert to 0f when below 0.5f

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				// transform coords [i,j] to [i,l]
				int l = Math.round(up.get((float)j)); // do we move this point up
				l = Math.round(dn.get((float)l)); // do we move this point down
				// now calc color with transformed point [i,l]
				float f = func.get(i,  l);
				float k = 1 - (f / h);
				k = sky.get(k); // 0.5 and up is sky
				k = gnd.get(k); // below 0.5 is ground
				// paint the point at the original non-transformed location [i,j]
				Color c = new Color(k, k, k);
				g.setColor(c);
				g.drawLine(i, j, i, j);
			}
		}
	}

}
