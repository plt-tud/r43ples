package de.tud.plt.r43ples.management;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class ResourceManagement {
	public static String getContentFromResource(String resourceName) {
		try {
			return FileUtils.readFileToString(new File(ResourceManagement.class.getClassLoader().getResource(resourceName).getFile()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}	
	}
}