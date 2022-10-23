package me.hardcoded.compiler.impl;

import me.hardcoded.utils.ImmutableSyntaxImpl;
import me.hardcoded.utils.Position;

import java.io.File;

/**
 * Syntax position interface
 *
 * @author HardCoded
 */
public interface ISyntaxPos {
	/**
	 * Returns the start position of this object
	 */
	Position getStartPosition();
	
	/**
	 * Returns the end position of this object
	 */
	Position getEndPosition();
	
	/**
	 * Returns the file this object belongs to
	 */
	File getFile();
	
	static ISyntaxPos empty(File file) {
		return new ImmutableSyntaxImpl(file, new Position(0, 0), new Position(0, 0));
	}
	
	static ISyntaxPos of(File file, Position start, Position end) {
		return new ImmutableSyntaxImpl(file, start, end);
	}
}
