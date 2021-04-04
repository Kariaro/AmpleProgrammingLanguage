package com.hardcoded.compiler.impl.statement;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.Reference;
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
public class FuncStat extends Stat implements IRefContainer {
	protected final List<DefineStat> args;
	protected final Token type;
	protected final Token name;
	protected Reference ref;
	
	private FuncStat(Token type, Token name) {
		super(type, true);
		this.args = new ArrayList<>();
		this.type = type;
		this.name = name;
		this.ref = Reference.get(name.value, Reference.Type.FUN);
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
	
	@Override
	public Reference getReference() {
		return ref;
	}
	
	@Override
	public void setReference(Reference ref) {
		this.ref = ref;
	}
	
	@Override
	public Token getRefToken() {
		return name;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s(%s);", type, name, args);
	}
	
	public static FuncStat get(Token type, Token name) {
		return new FuncStat(type, name);
	}
}
