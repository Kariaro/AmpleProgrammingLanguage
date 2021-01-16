package hardcoded.compiler.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for fragmented ir code.
 * 
 * @author HardCoded
 * @since v0.2
 */
public class FirFile implements IFile {
	public static final String EXTENSION = ".fir";
	
	private final File file;
	public final List<String> imports = new ArrayList<>();
	
	public FirFile(String path) {
		file = new File(path);
	}
	
	public String getName() {
		return file.getName();
	}
	
	@Override
	public String getPath() {
		return null;
	}
	
	@Override
	public File toFile() {
		return file;
	}
	
	@Override
	public boolean exists() {
		return file.exists();
	}
	
	@Override
	public boolean isFile() {
		return file.isFile();
	}
	
	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}
}
