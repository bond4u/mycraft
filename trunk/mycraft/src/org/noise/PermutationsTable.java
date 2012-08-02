package org.noise;

import java.util.Random;

/*
 * Permutations table and filling (generation).
 */
public class PermutationsTable {

	private int[] m_perm = new int[size()];
	
	public PermutationsTable(Random rnd) {
		final int halfSize = size() >> 1;
		// fill half the table with -1
		for (int i = 0; i < halfSize; i++) {
			m_perm[i] = -1;
		}
		// find a spot in table for each byte 0..255
		for (int i = 0; i < halfSize; i++) {
			int p = -1;
			do {
				p = rnd.nextInt(halfSize);
				if (m_perm[p] == -1) {
					// empty sport, fill it
					m_perm[p] = i;
					m_perm[p + halfSize] = i;
				}
			} while (m_perm[p] != i);
		}
	}
	
	public int size() {
		return 512;
	}
	
	public int get(int idx) {
		return m_perm[idx];
	}
}
