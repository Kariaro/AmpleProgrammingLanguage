package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.util.Objects;

public class VarStat extends Stat {
	private Reference reference;
	private Expr value;
	
	public VarStat(ISyntaxPosition syntaxPosition, Reference reference, Expr value) {
		super(syntaxPosition);
		this.reference = Objects.requireNonNull(reference);
		this.value = value;
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
		// Depends on scope
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.VAR;
	}
}
