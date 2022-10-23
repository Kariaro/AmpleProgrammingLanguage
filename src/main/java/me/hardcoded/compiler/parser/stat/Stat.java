package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;

public abstract class Stat {
	private final ISyntaxPos syntaxPos;
	
	public Stat(ISyntaxPos syntaxPos) {
		this.syntaxPos = syntaxPos;
	}
	
	public final ISyntaxPos getSyntaxPosition() {
		return this.syntaxPos;
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
