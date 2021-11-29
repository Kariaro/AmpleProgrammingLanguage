package hardcoded.compiler.errors;

import java.io.File;

import hardcoded.compiler.context.LangContext;
import hardcoded.compiler.impl.ISyntaxPosition;
import hardcoded.lexer.Token;

public class CompilerMarker implements SyntaxMarker {
	private final ISyntaxPosition syntaxPosition;
	private final CompilerError error;
	private final String compilerMessage;
	private final String message;
	private final int severity;
	
	public CompilerMarker(File file, LangContext reader, int offs, int count, int severity, String compilerMessage, String message, CompilerError error) {
		this.compilerMessage = compilerMessage;
		this.message = message;
		this.severity = severity;
		this.error = error;
		
		if(reader == null) {
			syntaxPosition = ISyntaxPosition.empty(file);
		} else {
			Token t0 = reader.peak(offs);
			Token t1 = reader.peak(offs + count - 1);
			syntaxPosition = ISyntaxPosition.of(t0.syntaxPosition.getStartPosition(), t1.syntaxPosition.getEndPosition());
		}
	}
	
	public CompilerMarker(File file, LangContext reader, int severity, String compilerMessage, String message, CompilerError error) {
		this(file, reader, 0, 1, severity, compilerMessage, message, error);
	}
	
	public CompilerMarker(Throwable t) {
		this(new File(""), null, 0, 0, SyntaxMarker.ERROR, "", t.getMessage(), CompilerError.INTERNAL_ERROR);
	}
	
	@Override
	public CompilerError getCompilerError() {
		return error;
	}

	@Override
	public String getCompilerMessage() {
		return compilerMessage;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	@Override
	public int getSeverity() {
		return severity;
	}
	
	@Override
	public ISyntaxPosition getSyntaxPosition() {
		return syntaxPosition;
	}

	@Override
	public String toString() {
		return message;
	}
}
