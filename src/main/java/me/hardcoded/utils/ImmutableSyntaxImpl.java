package me.hardcoded.utils;

import me.hardcoded.compiler.impl.ISyntaxPos;

import java.util.Objects;

public class ImmutableSyntaxImpl implements ISyntaxPos {
	public final String path;
	public final Position start;
	public final Position end;
	
	public ImmutableSyntaxImpl(String path, Position start, Position end) {
		this.path = Objects.requireNonNull(path);
		this.start = start;
		this.end = end;
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
