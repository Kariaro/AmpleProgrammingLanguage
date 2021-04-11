package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.lexer.Token;

/**
 * A class statement
 * 
 * <pre>
 * Valid syntax:
 *   'class' [name] '{' [class-body] '}'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ClassStat extends Stat implements IRefContainer {
	protected final Token name;
	protected Reference ref;
	
	private ClassStat(Token token, Token name) {
		super(token, true);
		this.name = name;
		this.ref = Reference.get(token.value, Reference.Type.CLASS);
	}
	
	@Override
	public Type getType() {
		return Type.CLASS;
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
		return String.format("class %s;", name);
	}
	
	public static ClassStat get(Token token, Token name) {
		return new ClassStat(token, name);
	}
}
