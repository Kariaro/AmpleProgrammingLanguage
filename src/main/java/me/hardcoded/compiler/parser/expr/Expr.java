package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.stat.Stat;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;

public abstract class Expr extends Stat {
	public Expr(ISyntaxPos syntaxPos) {
		super(syntaxPos);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	/**
	 * Returns the type of this expression
	 */
	public abstract ValueType getType();
	
	@Override
	public abstract TreeType getTreeType();
	
	/**
	 * Deserialize this statement
	 *
	 * @param stream the stream to read from
	 */
	public static Expr deserialize(LinkableStream stream) throws IOException {
		throw new UnsupportedOperationException("Expr::deserialize not implemented");
	}
}
