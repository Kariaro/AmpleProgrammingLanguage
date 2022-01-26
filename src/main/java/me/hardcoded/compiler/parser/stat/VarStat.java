package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

public class VarStat extends Stat {
	private ValueType type;
	private Reference reference;
	private Expr value;
	
	public VarStat(ValueType type, Reference reference, Expr value, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.type = type;
		this.reference = reference;
		this.value = value;
	}
	
	public ValueType getType() {
		return type;
	}
	
	public String getName() {
		return reference.getName();
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public Expr getValue() {
		return value;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
}
