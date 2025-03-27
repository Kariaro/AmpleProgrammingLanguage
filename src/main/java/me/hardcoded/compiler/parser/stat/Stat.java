package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;

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
	 * Returns if this statement does not modify memory
	 */
	public abstract boolean isPure();
	
	/**
	 * Returns the type of this statement
	 */
	public abstract TreeType getTreeType();
	
	/**
	 * Serialize this statement
	 *
	 * @param stream the stream to write to
	 */
	public abstract void serialize(LinkableStream stream) throws IOException;
	
	/**
	 * Deserialize this statement
	 *
	 * @param stream the stream to read from
	 */
	public static Stat deserialize(LinkableStream stream) throws IOException {
		throw new UnsupportedOperationException("Stat::deserialize not implemented");
	}
}
