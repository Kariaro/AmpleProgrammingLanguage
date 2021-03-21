package com.hardcoded.utils;

import java.io.IOException;
import java.io.InputStream;

import com.hardcoded.logger.Log;

/**
 * A utility class for random methods.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public final class Utils {
	private static final Log LOGGER = Log.getLogger(Utils.class);
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("com.hardcoded.developer"));
	}
	
	public static String getResourceFileAsString(String path) {
		try(InputStream stream = Utils.class.getResourceAsStream(path)) {
			return new String(stream.readAllBytes());
		} catch(IOException e) {
			LOGGER.throwing(e);
		} catch(NullPointerException e) {
			LOGGER.error("Could not find the resource '%s'", path);
			LOGGER.throwing(e);
		}
		
		return null;
	}
}
