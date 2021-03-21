package com.hardcoded.utils;

import java.io.*;

/**
 * This is a utility class used to read files and inputstreams.
 * 
 * @author HardCoded
 * @since 0.2.0
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
}
