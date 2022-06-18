package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.List;
import java.util.Objects;

public class CallExpr extends Expr {
	private Reference reference;
	private List<Expr> parameters;
	
	public CallExpr(ISyntaxPosition syntaxPosition, Reference reference, List<Expr> parameters) {
		super(syntaxPosition);
		this.reference = Objects.requireNonNull(reference);
		this.parameters = parameters;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<Expr> getParameters() {
		return parameters;
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
		return TreeType.CALL;
	}
	
	@Override
	public String toString() {
		String params = parameters.toString();
		params = params.substring(1, params.length() - 1);
		return "(" + reference.getName() + "(" + params + ")" + ")";
	}
}
