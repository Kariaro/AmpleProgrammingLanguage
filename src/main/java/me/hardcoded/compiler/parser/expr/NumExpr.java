package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Atom;
import me.hardcoded.compiler.parser.type.TreeType;

public class NumExpr extends Expr {
	private double f_value;
	private long   i_value;
	private Atom atom;
	
	public NumExpr(double value, Atom atom, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.f_value = value;
		this.atom = atom;
	}
	
	public NumExpr(long value, Atom atom, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.i_value = value;
		this.atom = atom;
	}
	
	public NumExpr(boolean value, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.i_value = value ? 1 : 0;
		this.atom = Atom.int_8;
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
		return TreeType.NUM;
	}
	
	public Atom getAtom() {
		return atom;
	}
	
	public double getFloatingValue() {
		return f_value;
	}
	
	public long getIntegerValue() {
		return i_value;
	}
	
	@Override
	public String toString() {
		// TODO: Correct formatting
		if (atom.isFloating()) {
			return "" + getFloatingValue();
		}
		
		return "" + getIntegerValue();
	}
}
