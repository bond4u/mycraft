package org.noise.demo;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public abstract class DemoBase extends JComponent {

	protected JFrame m_f;
	
	public DemoBase(String t) {
		super();
		m_f = new JFrame(t);
		m_f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void addAndShow(JComponent c) {
		m_f.getContentPane().add(c);
		m_f.pack();
		m_f.setVisible(true);
	}

	public abstract int demoWidth();
	public abstract int demoHeight();
	
	public Dimension getPreferredSize() {
		return new Dimension(demoWidth(), demoHeight());
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

}