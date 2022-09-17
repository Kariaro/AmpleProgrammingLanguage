package me.hardcoded.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

/**
 * This is a utility class used to read files and inputstreams.
 *
 * @author HardCoded
 */
public final class FileUtils {
	/**
	 * This method will read all bytes available inside an inputStream
	 * and return them as a byte array. This function does not close
	 * the input stream.
	 *
	 * @param stream the inputStream to read
	 * @return the content of the inputStream
	 */
	public static byte[] readInputStream(InputStream stream) throws IOException {
		return stream == null ? new byte[0] : stream.readAllBytes();
	}
	
	public static byte[] readFileBytes(File file) throws IOException {
		return Files.readAllBytes(file.toPath());
	}
	
	/**
	 * Returns the file checksum using the specified message digest
	 *
	 * @param digest the used message digest
	 * @param file   the file to calculate the checksum from
	 */
	public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			byte[] buffer = new byte[1024];
			int readBytes = 0;
			
			while ((readBytes = stream.read(buffer)) != -1) {
				digest.update(buffer, 0, readBytes);
			}
			
			StringBuilder sb = new StringBuilder();
			for (byte b : digest.digest()) {
				sb.append("%02x".formatted(b));
			}
			
			return sb.toString();
		}
	}
	
	public static String getFileChecksum(MessageDigest digest, byte[] bytes) {
		digest.update(bytes);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : digest.digest()) {
			sb.append("%02x".formatted(b));
		}
		
		return sb.toString();
	}
	
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
