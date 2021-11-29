package hardcoded.compiler.impl;

import java.io.File;

import hardcoded.utils.ImmutableSyntaxImpl;
import hardcoded.utils.Position;

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
		return new ImmutableSyntaxImpl(new Position(0, 0, 0), new Position(0, 0, 0));
	}
	
	static ISyntaxPosition empty(File file) {
		return new ImmutableSyntaxImpl(new Position(file, 0, 0, 0), new Position(file, 0, 0, 0));
	}
	
	static ISyntaxPosition of(Position start, Position end) {
		return new ImmutableSyntaxImpl(start, end);
	}
}
