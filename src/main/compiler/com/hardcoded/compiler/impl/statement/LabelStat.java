package com.hardcoded.compiler.impl.statement;

import com.hardcoded.compiler.impl.context.IRefContainer;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.lexer.Token;

/**
 * A label statement
 * 
 * <pre>
 * Valid syntax:
 *   [name] ':'
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class LabelStat extends Stat implements IRefContainer {
	protected Reference ref;
	protected Token token;
	
	private LabelStat(Token token) {
		super(token);
		this.token = token;
	}
	
	@Override
	public Type getType() {
		return Type.LABEL;
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
		return token;
	}
	
	@Override
	public String toString() {
		return String.format("%s:", token);
	}
	
	public static LabelStat get(Token token) {
		return new LabelStat(token);
	}
}
