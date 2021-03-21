package com.hardcoded.compiler.impl.statement;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A break statement
 * 
 * <pre>
 * Valid syntax:
 *   'break' ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class BreakStat implements Statement {
	protected final Token token;
	
	private BreakStat(Token token) {
		this.token = token;
	}
	
	@Override
	public Type getType() {
		return Type.BREAK;
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
		return "break;";
	}
	
	public static BreakStat get(Token token) {
		return new BreakStat(token);
	}
}
