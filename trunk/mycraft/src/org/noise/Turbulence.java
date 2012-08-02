package org.noise;

public class Turbulence extends ImprovedPerlinNoise implements ITurbulence {

	private float m_min = 1.0f;
	private float m_max = 1.0f;
	
	public Turbulence(PermutationsTable pTbl) {
		super(pTbl);
	}
	
	public void set(float minFreq, float maxFreq) {
		m_min = minFreq;
		m_max = maxFreq;
	}
	
	public float get(float x, float y, float z) {
		float r = 0.0f;
		for (float freq = m_min; freq <= m_max; freq *= 2.0f) {
			r += Math.abs(super.get(x, y, z) / freq);
		}
		return r;
	}
	
//	public float noise(float x, float y, float z) {
//		return super.get(x, y, z);
//	}
}
