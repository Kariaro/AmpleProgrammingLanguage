package me.hardcoded.utils;

import me.hardcoded.compiler.impl.ISyntaxPos;

import java.io.File;

public class MutableSyntaxImpl implements ISyntaxPos {
	public Position start;
	public Position end;
	public final File file;
	
	public MutableSyntaxImpl(File file, Position start, Position end) {
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
