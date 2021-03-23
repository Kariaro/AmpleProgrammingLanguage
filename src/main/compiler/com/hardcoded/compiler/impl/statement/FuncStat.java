package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.lexer.Token;

/**
 * A function statement
 * 
 * <pre>
 * Valid syntax:
 *   [type] [name] '(' [args] ')' ';'
 *   [type] [name] '(' [args] ')' [stat]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class FuncStat extends Stat {
	protected final List<DefineStat> args;
	protected final Token type;
	protected final Token name;
	
	private FuncStat(Token type, Token name) {
		super(type, true);
		this.args = new ArrayList<>();
		this.type = type;
		this.name = name;
	}

	@Override
	public Type getType() {
		return Type.FUNCTION;
	}

	public List<DefineStat> getArguments() {
		return args;
	}
	
	public void addArgument(DefineStat stat) {
		args.add(stat);
	}
	
	public Token getReturnType() {
		return type;
	}
	
	public Token getName() {
		return name;
	}
	
	public void setBody(Statement stat) {
		list.add(stat);
	}
	
	@Override
	public String toString() {
		return String.format("%s %s(%s);", type, name, args);
	}
	
	public static FuncStat get(Token type, Token name) {
		return new FuncStat(type, name);
	}
}
