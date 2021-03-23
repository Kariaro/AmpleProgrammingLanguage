package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A abstract statement implementation
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public abstract class Stat implements Statement {
	protected final List<Statement> list;
	protected final Token token;
	protected Token end;
	
	protected Stat(Token token) {
		this(token, false);
	}
	
	protected Stat(Token token, boolean has_list) {
		this.list = has_list ? new ArrayList<>():Collections.emptyList();
		this.token = token;
		this.end = Token.EMPTY;
	}
	
	public Stat add(Statement stat) {
		list.add(stat);
		return this;
	}
	
	public Stat add(Statement... array) {
		for(Statement s : array)
			list.add(s);
		
		return this;
	}
	
	public final Token getToken() {
		return token;
	}
	
	public final Token getEnd() {
		return end;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Stat> T end(Token end) {
		this.end = end;
		return (T)this;
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
	public List<Statement> getStatements() {
		return list;
	}
}
