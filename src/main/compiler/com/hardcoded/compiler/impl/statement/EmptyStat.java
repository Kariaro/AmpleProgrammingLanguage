package com.hardcoded.compiler.impl.statement;

import java.util.Collections;
import java.util.List;

import com.hardcoded.compiler.api.Statement;

/**
 * An empty statement
 * 
 * <pre>
 * Valid syntax:
 *   ';'
 *   $
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class EmptyStat implements Statement {
	private EmptyStat() {
		
	}
	
	@Override
	public Type getType() {
		return Type.NONE;
	}

	@Override
	public List<Statement> getStatements() {
		return Collections.emptyList();
	}

	@Override
	public int getLineIndex() {
		return 0;
	}

	@Override
	public int getColumnIndex() {
		return 0;
	}
	
	@Override
	public String toString() {
		return ";";
	}
	
	private static final EmptyStat EMPTY = new EmptyStat();
	
	/**
	 * Returns a singleton {@code EMPTY} statement.
	 * @return a singleton {@code EMPTY} statement
	 */
	public static EmptyStat get() {
		return EMPTY;
	}
	
	public static boolean isEmpty(Statement part) {
		return part == EMPTY;
	}
}
