package hardcoded.compiler.errors;

import java.io.File;

// TODO: Implement for easier debugging outside of this compiler
public class CompilerMarker implements SyntaxMarker {
	private final CompilerError error;
	private final String message;
	
	private final File sourceFile;
	private final int column;
	private final int line;
	
	public CompilerMarker(int line, int column, File sourceFile, CompilerError error, String message) {
		this.sourceFile = sourceFile;
		this.message = message;
		this.column = column;
		this.error = error;
		this.line = line;
	}
	
	public CompilerError getType() {
		return error;
	}
	
	public String getMessage() {
		return message;
	}
	
	public File getSourceFile() {
		return sourceFile;
	}
	
	public int getColumn() {
		return column;
	}
	
	public int getLine() {
		return line;
	}
	
	public String toString() {
		return "(" + sourceFile + ":" + line + "): " + message;
	}
}
