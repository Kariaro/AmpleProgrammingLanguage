package me.hardcoded.utils;

import me.hardcoded.compiler.impl.ISyntaxPos;

import java.util.Objects;

public class ImmutableSyntaxImpl implements ISyntaxPos {
	public final Position start;
	public final Position end;
	public final String path;
	
	public ImmutableSyntaxImpl(String path, Position start, Position end) {
		this.start = start;
		this.end = end;
		this.path = Objects.requireNonNull(path);
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
	public String getPath() {
		return path;
	}
}
