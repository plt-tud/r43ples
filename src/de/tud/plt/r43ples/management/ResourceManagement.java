package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class ResourceManagement {
	public static String getContentFromResource(String resourceName) throws IOException{
		InputStream is = ClassLoader.getSystemResourceAsStream(resourceName);
		StringWriter sw = new StringWriter();
		try {
			IOUtils.copy(is, sw, "UTF-8");
			return sw.toString();
		} catch (IOException e) {
			return "";
		}
		finally {
			is.close();
			sw.close();
		}
		
	}
}
