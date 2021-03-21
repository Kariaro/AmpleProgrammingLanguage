package com.hardcoded.compiler.impl.statement;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A goto statement
 * 
 * <pre>
 * Valid syntax:
 *   'goto' [label] ';'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class GotoStat implements Statement {
	protected final Token label;
	
	private GotoStat(Token label) {
		this.label = label;
	}
	
	@Override
	public Type getType() {
		return Type.GOTO;
	}
	
	@Override
	public List<Statement> getStatements() {
		return Collections.emptyList();
	}
	
	@Override
	public int getLineIndex() {
		return label.line;
	}
	
	@Override
	public int getColumnIndex() {
		return label.column;
	}
	
	public Token getPath() {
		return label;
	}
	
	@Override
	public String toString() {
		return String.format("goto %s;", label);
	}
	
	public static GotoStat get(Token label) {
		return new GotoStat(label);
	}
}
