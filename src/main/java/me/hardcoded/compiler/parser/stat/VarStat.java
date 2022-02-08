package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class VarStat extends Stat {
	private Reference reference;
	private Expr value;
	
	public VarStat(Reference reference, Expr value, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
		this.value = value;
	}
	
	public ValueType getType() {
		return reference.getValueType();
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
	
	@Override
	public TreeType getTreeType() {
		return TreeType.VAR;
	}
}
