package hardcoded.compiler.file;

import java.io.File;
import java.io.IOException;

/**
 * This class is used for handling compiler managed files linking code files internally.
 * 
 * @author HardCoded
 * @since v0.2
 */
public interface CmFile {
	/**
	 * Returns the name of this file.
	 * @return the name of this file
	 */
	String getName();
	
	/**
	 * Returns the path of this file.
	 * @return the path of this file
	 */
	String getPath();
	
	/**
	 * Returns the file.
	 * @return the file
	 */
	File toFile();
	
	/**
	 * Returns {@code true} if this file exists.
	 * @return {@code true} if this file exists
	 */
	boolean exists();
	
	/**
	 * Returns {@code true} if this file exists and is not a directory.
	 * @return {@code true} if this file exists and is not a directory
	 */
	boolean isFile();
	
	/**
	 * Returns {@code true} if this file exists and is a directory.
	 * @return {@code true} if this file exists and is a directory
	 */
	boolean isDirectory();
	
	/**
	 * Returns all bytes inside of this file.
	 * @return all bytes inside of this file
	 * @throws IOException
	 */
	byte[] getContent() throws IOException;
	
	/**
	 * Returns the computed checksum of this file or {@code -1}
	 * if the file does not exist or is a directory.
	 * @return the computed checksum of this file
	 * @throws IOException
	 */
	long getChecksum() throws IOException;
}
