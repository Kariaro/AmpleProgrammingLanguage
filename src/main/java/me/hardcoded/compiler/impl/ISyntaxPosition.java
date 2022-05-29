package me.hardcoded.compiler.impl;

import java.io.File;

import me.hardcoded.utils.ImmutableSyntaxImpl;
import me.hardcoded.utils.Position;

/**
 * @author HardCoded
 */
public interface ISyntaxPosition {
	/**
	 * Returns the start position of this object.
	 */
	Position getStartPosition();
	
	/**
	 * Returns the end position of this object.
	 */
	Position getEndPosition();
	
	static ISyntaxPosition empty() {
		return empty(null);
	}
	
	static ISyntaxPosition empty(File file) {
		return new ImmutableSyntaxImpl(new Position(file, 0, 0, 0), new Position(file, 0, 0, 0));
	}
	
	static ISyntaxPosition of(Position start, Position end) {
		return new ImmutableSyntaxImpl(start, end);
	}
	
	static ISyntaxPosition of(ISyntaxPosition start, ISyntaxPosition end) {
		return new ImmutableSyntaxImpl(start.getStartPosition(), end.getEndPosition());
	}
}
