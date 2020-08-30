package hardcoded.compiler.file;

import java.io.File;
import java.io.IOException;

import hardcoded.utils.FileUtils;

/**
 * This is a wrapper class for a source file.
 * 
 * @author HardCoded
 */
public final class SourceFile {
	private final File file;
	
	/**
	 * Create a new <code>SourceFile</code>.
	 * 
	 * @param	pathname	A pathname string
	 * @throws	NullPointerException
	 * 			If the <code>pathname</code> is <code>null</code>
	 */
	private SourceFile(String pathname) {
		file = new File(pathname);
	}
	
	public File getFile() {
		return file;
	}
	
	public String getPath() {
		return file.getAbsolutePath();
	}
	
	public byte[] getFileContent() throws IOException {
		return FileUtils.readFileBytes(file);
	}
	
	public static SourceFile wrap(String pathname) {
		return new SourceFile(pathname);
	}
	
	@Override
	public String toString() {
		return "SourceFile {'" + getPath() + "'}";
	}
}
