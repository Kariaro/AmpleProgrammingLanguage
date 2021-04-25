package com.hardcoded.compiler.impl.statement;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.NonNullList;
import com.hardcoded.compiler.lexer.Token;

/**
 * A abstract statement implementation
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public abstract class Stat implements Statement {
	protected final List<Statement> list;
	protected int start_offset;
	protected int end_offset;
	
	protected Stat(Token token) {
		this(token, false);
	}
	
	protected Stat(Token start, boolean has_list) {
		this.list = has_list ? new NonNullList<>(EmptyStat.get()):Collections.emptyList();
		this.start_offset = start.offset;
		this.end_offset = start.offset;
	}
	
	public Stat add(Statement stat) {
		list.add(stat);
		return this;
	}
	
	public int getNumElements() {
		return list.size();
	}
	
	public Statement get(int index) {
		return list.get(index);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Stat> T end(Token end) {
		this.end_offset = end.offset + end.value.length();
		return (T)this;
	}
	
	@Override
	public int getStartOffset() {
		return start_offset;
	}

	@Override
	public int getEndOffset() {
		return end_offset;
	}
	
	public void setLocation(int start, int end) {
		this.start_offset = start;
		this.end_offset = end;
	}
	
	@Override
	public List<Statement> getStatements() {
		return list;
	}
}
