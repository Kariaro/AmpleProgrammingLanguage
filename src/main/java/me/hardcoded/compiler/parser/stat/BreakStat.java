package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;

public class BreakStat extends Stat {
	public BreakStat(ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.BREAK;
	}
}
