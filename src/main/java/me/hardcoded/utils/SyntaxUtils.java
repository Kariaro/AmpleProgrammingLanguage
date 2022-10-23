package me.hardcoded.utils;

import me.hardcoded.compiler.impl.ISyntaxPos;

public class SyntaxUtils {
	/**
	 * Returns if the position is inside the specified syntaxPosition
	 *
	 * @param syntaxPosition
	 * @param pos
	 * @return {@code true} if pos is inside the syntaxPosition
	 */
	public static boolean syntaxIntersect(ISyntaxPos syntaxPosition, Position pos) {
		Position s = syntaxPosition.getStartPosition();
		Position e = syntaxPosition.getEndPosition();
		
		return (pos.line() >= s.line() && pos.line() <= e.line())
			&& (pos.line() != s.line() || pos.column() >= s.column())
			&& (pos.line() != e.line() || pos.column() < e.column());
	}
	
	/**
	 * Returns if {@code a} contains {@code b}
	 *
	 * @param a
	 * @param b
	 * @return {@code true} if {@code a} contains {@code b}
	 */
	public static boolean syntaxIntersect(ISyntaxPos a, ISyntaxPos b) {
		return syntaxIntersect(a, b.getStartPosition())
			|| syntaxIntersect(a, b.getEndPosition());
	}
}
