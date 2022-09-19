package me.hardcoded.utils;

import me.hardcoded.compiler.context.AmpleConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cache class for ample resources
 *
 * @author HardCoded
 */
public class AmpleCache {
	private static final Logger LOGGER = LogManager.getLogger(AmpleCache.class);
	private static final MessageDigest SHA_1_DIGEST;
	
	static {
		MessageDigest shaDigest = null;
		try {
			shaDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Failed to load SHA-1 digest", e);
			System.exit(0);
		}
		
		SHA_1_DIGEST = shaDigest;
	}
	
	/**
	 * Returns the digest used for checksums
	 */
	public static MessageDigest getChecksumDigest() {
		return SHA_1_DIGEST;
	}
	
	/**
	 * Returns the file checksum using the specified message digest
	 *
	 * @param file the file used to calculate the checksum
	 */
	public static String getFileChecksum(File file) throws IOException {
		MessageDigest digest = getChecksumDigest();
		
		try (FileInputStream stream = new FileInputStream(file)) {
			byte[] buffer = new byte[1024];
			int readBytes;
			
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
	
	/**
	 * Returns the file checksum using the specified message digest
	 *
	 * @param bytes the bytes used to calculate the checksum
	 */
	public static String getDataChecksum(byte[] bytes) {
		MessageDigest digest = getChecksumDigest();
		
		digest.update(bytes);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : digest.digest()) {
			sb.append("%02x".formatted(b));
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns the cache path of a file
	 *
	 * @param ampleConfig the ample config
	 * @param file        the file to resolve the cache path from
	 */
	public static String getCacheFileName(AmpleConfig ampleConfig, File file) {
		Path relativePath = ampleConfig.getConfiguration()
			.getWorkingDirectory().toPath().relativize(file.toPath());
		
		return "serial_" + relativePath.toString().replace("_", "__").replaceAll("[\\\\/]", "_") + ".serial";
	}
}
