package hc.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// TODO: Try multithread this if we find multiple files to create the parse trees and syntax trees.
public class Compiler {
	@SuppressWarnings("unused")
	private List<HFile> sources = new ArrayList<>();
	private File projectPath;
	private File sourcePath;
	private File binaryPath;
	
	/**
	 * The tree of this compile should look something like.
	 * 
	 *<pre>project:
	 *    src:
	 *        &lt;source code files&gt;
	 *    bin:
	 *        &lt;serialized trees from the parser&gt;
	 *        &lt;cached files to make compilation faster&gt;
	 *        &lt;compiled binary files&gt;
	 *        &lt;object files&gt;</pre>
	 * 
	 * Creating that structure we need to create a way to read such a project.
	 * Maybe we need to create our own property file or IDE to create code faster
	 * and more efficient. This might be a very fun project to add quirks to and
	 * to challange me in much more than just making my own compiler.
	 */
	public Compiler() {
		
	}
	
	public void setProjectPath(String filePath) {
		this.projectPath = new File(filePath);
		// TODO: Add folders to make it look like the structure we specified..
		// TODO: Make it so that we can change where the source and binary path are on the computer.
		// TODO: This method does not really do much for the compiler. Make a option code instead or something similar to that.
	}
	
	public void compile() {
		throw new UnsupportedOperationException("Not implemented");
	}
}
