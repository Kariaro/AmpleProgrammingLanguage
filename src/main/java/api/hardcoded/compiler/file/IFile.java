package hardcoded.compiler.file;

import java.io.File;

public interface IFile {
	String getName();
	String getPath();
	File toFile();
	
	boolean exists();
	boolean isFile();
	boolean isDirectory();
}
