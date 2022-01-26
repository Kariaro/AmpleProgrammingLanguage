package me.hardcoded.utils;

import me.hardcoded.compiler.impl.ISyntaxPosition;

public class MutableSyntaxImpl implements ISyntaxPosition {
	public Position start;
	public Position end;
	
	public MutableSyntaxImpl(Position start, Position end) {
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
}
