package de.tud.plt.r43ples.iohelper;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class ResourceManagement {
	public static String getContentFromResource(String resourceName) {
		try {
			return IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}
}