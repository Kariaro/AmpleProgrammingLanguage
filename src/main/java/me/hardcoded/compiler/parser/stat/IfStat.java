package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.TreeType;

public class IfStat extends Stat {
	private Expr value;
	private Stat body;
	private Stat elseBody;
	
	public IfStat(ISyntaxPosition syntaxPosition, Expr value, Stat body, Stat elseBody) {
		super(syntaxPosition);
		this.value = value;
		this.body = body;
		this.elseBody = elseBody;
	}
	
	public Expr getValue() {
		return value;
	}
	
	public Stat getBody() {
		return body;
	}
	
	public Stat getElseBody() {
		return elseBody;
	}
	
	public boolean hasElseBody() {
		return !elseBody.isEmpty();
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return body.isPure() && elseBody.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.IF;
	}
}
