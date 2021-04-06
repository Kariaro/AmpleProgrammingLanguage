package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;

/**
 * A program statment. This is the top level statement that contains the syntax tree
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ProgramStat implements Statement {
	protected final List<Statement> list;
	
	private ProgramStat() {
		this.list = new ArrayList<>();
	}
	
	@Override
	public Type getType() {
		return Type.PROGRAM;
	}

	@Override
	public List<Statement> getStatements() {
		return list;
	}
	
	@Override
	public int getStartOffset() {
		return 0;
	}
	
	@Override
	public int getEndOffset() {
		return 0;
	}
	
	public void add(Statement stat) {
		list.add(stat);
	}
	
	public Statement last() {
		if(list.isEmpty()) return EmptyStat.get();
		
		return list.get(list.size() - 1);
	}
	
	@Override
	public String toString() {
		return "root";
	}
	
	public static ProgramStat get() {
		return new ProgramStat();
	}
}
