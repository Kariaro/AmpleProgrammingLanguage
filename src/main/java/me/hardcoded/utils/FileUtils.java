package me.hardcoded.utils;

import java.io.File;
import java.io.IOException;

/**
 * This is a utility class used to read files and inputStream
 *
 * @author HardCoded
 */
public final class FileUtils {
	@Deprecated
	public static File makeAbsolute(File file) {
		if (file == null) {
			return null;
		}
		file = file.getAbsoluteFile();
		
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			return file;
		}
	}
}
