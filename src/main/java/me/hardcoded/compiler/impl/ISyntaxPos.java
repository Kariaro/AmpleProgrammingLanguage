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
	 * Returns the path this object belongs to
	 */
	String getPath();
	
	static ISyntaxPos empty(String path) {
		return new ImmutableSyntaxImpl(path, new Position(0, 0), new Position(0, 0));
	}
	
	static ISyntaxPos of(String path, Position start, Position end) {
		return new ImmutableSyntaxImpl(path, start, end);
	}
	
	static ISyntaxPos of(File file, Position start, Position end) {
		return new ImmutableSyntaxImpl(file.getAbsolutePath(), start, end);
	}
}
