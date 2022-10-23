package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.Objects;

public class NameExpr extends Expr {
	private Reference reference;
	
	public NameExpr(ISyntaxPos syntaxPos, Reference reference) {
		super(syntaxPos);
		this.reference = Objects.requireNonNull(reference);
	}
	
	public Reference getReference() {
		return reference;
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public ValueType getType() {
		return reference.getValueType();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.NAME;
	}
	
	@Override
	public String toString() {
		return "(" + reference.getName() + ")";
	}
}
