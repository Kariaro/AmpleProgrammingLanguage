package me.hardcoded.utils;

import me.hardcoded.compiler.impl.ISyntaxPos;

import java.io.File;

public class ImmutableSyntaxImpl implements ISyntaxPos {
	public final Position start;
	public final Position end;
	public final File file;
	
	public ImmutableSyntaxImpl(File file, Position start, Position end) {
		this.start = start;
		this.end = end;
		this.file = file;
	}
	
	@Override
	public Position getStartPosition() {
		return start;
	}
	
	@Override
	public Position getEndPosition() {
		return end;
	}
	
	@Override
	public File getFile() {
		return file;
	}
}
