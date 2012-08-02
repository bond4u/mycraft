package org.noise;

public class ImprovedPerlinNoise implements IFunc3D {
	
	private PermutationsTable m_tbl;
	
	public ImprovedPerlinNoise(PermutationsTable pTbl) {
		m_tbl = pTbl;
	}
	
	public float get(float x, float y, float z) {
		int X = (int)Math.floor(x) & 255;
		int Y = (int)Math.floor(y) & 255;
		int Z = (int)Math.floor(z) & 255;
		x -= Math.floor(x);
		y -= Math.floor(y);
		z -= Math.floor(z);
		float u = fade(x);
		float v = fade(y);
		float w = fade(z);
		int A = m_tbl.get(X) + Y;
		int AA = m_tbl.get(A) + Z;
		int AB = m_tbl.get(A + 1) + Z;
		int B = m_tbl.get(X + 1) + Y;
		int BA = m_tbl.get(B) + Z;
		int BB = m_tbl.get(B + 1) + Z;
		float gAA = grad(m_tbl.get(AA), x, y, z);
		float gAA1 = grad(m_tbl.get(AA+1), x, y, z-1);
		float gBA = grad(m_tbl.get(BA), x-1, y, z);
		float gBA1 = grad(m_tbl.get(BA+1), x-1, y, z-1);
		float gAB = grad(m_tbl.get(AB), x, y-1, z);
		float gAB1 = grad(m_tbl.get(AB+1), x, y-1, z-1);
		float gBB = grad(m_tbl.get(BB), x-1, y-1, z);
		float gBB1 = grad(m_tbl.get(BB+1), x-1, y-1, z-1);
		float luAABA = lerp(u, gAA, gBA);
		float luABBB = lerp(u, gAB, gBB);
		float luAA1BA1 = lerp(u, gAA1, gBA1);
		float luAB1BB1 = lerp(u, gAB1, gBB1);
		float lvAB = lerp(v, luAABA, luABBB);
		float lvAB1 = lerp(v, luAA1BA1, luAB1BB1);
		return lerp(w, lvAB, lvAB1);
	}
	
	protected float fade(float t) {
		float r = (t * t * t * (t * (t * 6 - 15) + 10));
		return r;
	}
	
	protected float lerp(float t, float a, float b) {
		float d = b - a;
		float r = (a + t * d);
		return r;
    }
	
	protected float grad(int hash, float x, float y, float z) {
		int h = hash & 15;
		float u = (h < 8) ? x : y;
		float v = (h < 4) ? y : (h == 12 || h == 14 ? x : z);
		return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
