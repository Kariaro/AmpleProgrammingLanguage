package hardcoded.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is a utility class used to read files and inputstreams.
 * 
 * @author HardCoded
 */
public final class FileUtils {
	/**
	 * This method will read all bytes available inside a inputstream
	 * and return them as a byte array. This function does not close
	 * the input stream.
	 * 
	 * @param	stream	the inputstream to read
	 * @return	the content of the inputstream
	 * @throws	IOException
	 */
	public static byte[] readInputStream(InputStream stream) throws IOException {
		return stream == null ? new byte[0]:stream.readAllBytes();
	}
	
	public static byte[] readFileBytes(File parent, String fileName) throws IOException {
		return Files.readAllBytes(new File(parent, fileName).toPath());
	}
	
	public static byte[] readFileBytes(String filePath) throws IOException {
		return Files.readAllBytes(Path.of(filePath));
	}
	
	public static byte[] readFileBytes(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
	
	public static String getAbsolutePathString(String path) {
		if(path == null) return null;
		File file = new File(path);
		file = file.getAbsoluteFile();
		
		try {
			file = file.getCanonicalFile();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return file.getAbsolutePath();
	}
	
	public static File makeAbsolute(File file) {
		if(file == null) return null;
		file = file.getAbsoluteFile();
		
		try {
			return file.getCanonicalFile();
		} catch(IOException e) {
			return file;
		}
	}

	public static boolean isValidPath(File file) {
		try {
			file.toPath();
			return true;
		} catch(Throwable e) {
			return false;
		}
	}
}
