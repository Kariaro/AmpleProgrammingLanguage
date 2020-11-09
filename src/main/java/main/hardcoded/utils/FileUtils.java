package hardcoded.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
	 * @throws	NullPointerException
	 * 			if the stream was {@code null}
	 * @throws	IOException
	 */
	public static byte[] readInputStream(InputStream stream) throws IOException {
		if(stream == null) throw new NullPointerException("The stream was null");
		
		return stream.readAllBytes();
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		byte[] buffer = new byte[65536];
//		int readBytes = 0;
//		
//		while((readBytes = stream.read(buffer, 0, Math.min(stream.available(), buffer.length))) != -1) {
//			bs.write(buffer, 0, readBytes);
//			if(stream.available() < 1) break;
//		}
//		
//		return bs.toByteArray();
	}
	
	public static byte[] readFileBytes(File parent, String fileName) throws IOException {
		return readFileBytes(new File(parent, fileName));
	}
	
	public static byte[] readFileBytes(String filePath) throws IOException {
		return readFileBytes(new File(filePath));
	}
	
	public static byte[] readFileBytes(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		
		try {
			return readInputStream(stream);
		} finally {
			stream.close();
		}
	}
	
	public static String getAbsolutePathString(String path) {
		if(path == null) return null;
		File file = new File(path);
		file = file.getAbsoluteFile();
		
		try {
			file = file.getCanonicalFile();
		} catch(IOException e) {
		}
		
		return file.getAbsolutePath();
	}
}
