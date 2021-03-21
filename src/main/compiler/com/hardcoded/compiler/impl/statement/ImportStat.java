package com.hardcoded.compiler.impl.statement;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A import statement
 * 
 * <pre>
 * Valid syntax:
 *   'import' [string] ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ImportStat implements Statement {
	protected final Token path;
	
	private ImportStat(Token path) {
		this.path = path;
	}
	
	@Override
	public Type getType() {
		return Type.IMPORT;
	}
	
	@Override
	public List<Statement> getStatements() {
		return Collections.emptyList();
	}
	
	@Override
	public int getLineIndex() {
		return path.line;
	}
	
	@Override
	public int getColumnIndex() {
		return path.column;
	}
	
	public Token getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return String.format("import %s;", path);
	}
	
	public static ImportStat get(Token path) {
		return new ImportStat(path);
	}
}
