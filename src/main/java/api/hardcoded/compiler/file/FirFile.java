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
public class FirFile extends FileImpl {
	private static final long serialVersionUID = -5144763224012186961L;
	
	// TODO: Types
	public static final String EXTENSION = ".fir";
	
	/**
	 * A list of imported files.
	 */
	private final List<String> imports = new ArrayList<>();
	
	public FirFile(String path) {
		this(new File(path));
	}
	
	public FirFile(File file) {
		super(file);
	}
	
	public void addImport(String name) {
		imports.add(name);
	}
}
