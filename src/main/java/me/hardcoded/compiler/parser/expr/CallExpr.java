package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;

import java.util.List;

public class CallExpr extends Expr {
	private Expr caller;
	private List<Expr> parameters;
	
	public CallExpr(Expr caller, List<Expr> parameters, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.caller = caller;
		this.parameters = parameters;
	}
	
	public Expr getCaller() {
		return caller;
	}
	
	public List<Expr> getParameters() {
		return parameters;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		// A call is never pure unless the called function is pure
		return false;
	}
}
