package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.TreeType;

public abstract class Stat {
	private final ISyntaxPosition syntaxPosition;
	
	public Stat(ISyntaxPosition syntaxPosition) {
		this.syntaxPosition = syntaxPosition;
	}
	
	public final ISyntaxPosition getSyntaxPosition() {
		return this.syntaxPosition;
	}
	
	/**
	 * Returns if this statement is empty and {#isPure}
	 */
	public abstract boolean isEmpty();
	
	/**
	 * Returns if this statement modifies memory in some way
	 */
	public abstract boolean isPure();
	
	/**
	 * Returns the type of this statement
	 */
	public abstract TreeType getTreeType();
}
