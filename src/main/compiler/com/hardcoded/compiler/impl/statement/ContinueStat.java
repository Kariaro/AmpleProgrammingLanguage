package com.hardcoded.compiler.impl.statement;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A continue statement
 * 
 * <pre>
 * Valid syntax:
 *   'continue' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ContinueStat implements Statement {
	protected final Token token;
	
	private ContinueStat(Token token) {
		this.token = token;
	}
	
	@Override
	public Type getType() {
		return Type.CONTINUE;
	}

	@Override
	public List<Statement> getStatements() {
		return Collections.emptyList();
	}

	@Override
	public int getLineIndex() {
		return token.line;
	}

	@Override
	public int getColumnIndex() {
		return token.column;
	}
	
	@Override
	public String toString() {
		return "continue;";
	}
	
	public static ContinueStat get(Token token) {
		return new ContinueStat(token);
	}
}
