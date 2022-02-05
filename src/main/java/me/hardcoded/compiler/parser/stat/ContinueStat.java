package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.TreeType;

public class ContinueStat extends Stat {
	public ContinueStat(ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.CONTINUE;
	}
}
