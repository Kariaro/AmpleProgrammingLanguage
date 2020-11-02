package hardcoded.compiler.errors;

import java.io.File;

import hardcoded.compiler.context.Lang;
import hardcoded.lexer.Token;

// TODO: Implement for easier debugging outside of this compiler
public class CompilerMarker implements SyntaxMarker {
	private final CompilerError error;
	
	private final String compilerMessage;
	private final String message;
	
	private final File sourceFile;
	private final int severity;
	private final int offset;
	private final int column;
	private final int length;
	private final int line;
	
	public CompilerMarker(File sourceFile, Lang reader, int severity, String compilerMessage, String message, CompilerError error) {
		this.sourceFile = sourceFile;
		this.compilerMessage = compilerMessage;
		this.severity = severity;
		this.message = message;
		this.error = error;
		
		if(reader == null) {
			offset = INVALID;
			column = INVALID;
			line = INVALID;
			length = 10;
		} else {
			offset = reader.fileOffset();
			column = reader.column();
			line = reader.line();
			length = reader.value().length();
		}
	}
	
	public CompilerMarker(File sourceFile, Token token, int severity, String compilerMessage, String message, CompilerError error) {
		this.sourceFile = sourceFile;
		this.compilerMessage = compilerMessage;
		this.severity = severity;
		this.message = message;
		this.error = error;
		
		if(token == null) {
			offset = INVALID;
			column = INVALID;
			line = INVALID;
			length = 10;
		} else {
			offset = token.offset;
			column = token.column;
			line = token.line;
			length = token.value.length();
		}
	}
	
	public CompilerMarker(File sf, Lang reader, int offs, int count, int severity, String msg, String cm, CompilerError error) {
		this.sourceFile = sf;
		this.compilerMessage = cm;
		this.message = msg;
		this.severity = severity;
		this.error = error;
		
		Token t0 = reader.peak(offs);
		Token t1 = reader.peak(offs + count - 1);
		this.offset = t0.offset;
		this.length = t1.offset - t0.offset + t1.value.length();
		this.column = t0.column;
		this.line = t0.line;
	}

	public CompilerError getCompilerError() {
		return error;
	}
	
	public File getDeclaringFile() {
		return sourceFile;
	}
	
	public String getCompilerMessage() {
		return compilerMessage;
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getFileOffset() {
		return offset;
	}
	
	public int getColumnIndex() {
		return column;
	}
	
	public int getLineIndex() {
		return line;
	}
	
	public int getLocationLength() {
		return length;
	}
	
	public int getSeverity() {
		return severity;
	}
	
	public String toString() {
		return message;
	}
}
