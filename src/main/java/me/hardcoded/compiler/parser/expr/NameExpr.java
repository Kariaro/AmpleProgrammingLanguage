package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;

public class NameExpr extends Expr {
	private Reference reference;
	
	public NameExpr(Reference reference, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public void setReference(Reference reference) {
		this.reference = reference;
	}
	
	public String geName() {
		return reference.getName();
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
		return TreeType.NAME;
	}
}
