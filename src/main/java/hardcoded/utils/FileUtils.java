package hardcoded.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

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
	
	public static byte[] readFileBytes(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
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
}
