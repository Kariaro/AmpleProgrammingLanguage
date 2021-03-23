package com.hardcoded.compiler.impl.statement;

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
public class ImportStat extends Stat {
	protected final Token path;
	
	private ImportStat(Token token, Token path) {
		super(token);
		this.path = path;
	}
	
	@Override
	public Type getType() {
		return Type.IMPORT;
	}
	
	public Token getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return String.format("import %s;", path);
	}
	
	public static ImportStat get(Token token, Token path) {
		return new ImportStat(token, path);
	}
}
