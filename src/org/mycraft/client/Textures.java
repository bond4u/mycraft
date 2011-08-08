package org.mycraft.client;

import java.io.IOException;
import java.io.InputStream;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Textures {

	public Texture get(String n) {
		String f = n.substring(n.lastIndexOf(".") + 1);
		ClassLoader cl = this.getClass().getClassLoader();
		InputStream is = cl.getResourceAsStream(n);
		Texture t = null;
		try {
			t = TextureLoader.getTexture(f, is);
		} catch (IOException e) {
			log("Texture load error: " + e);
		}
		return t;
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
}
