package hardcoded.utils;

import hardcoded.compiler.impl.ISyntaxPosition;

public class ImmutableSyntaxImpl implements ISyntaxPosition {
	public final Position start;
	public final Position end;
	
	public ImmutableSyntaxImpl(Position start, Position end) {
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
