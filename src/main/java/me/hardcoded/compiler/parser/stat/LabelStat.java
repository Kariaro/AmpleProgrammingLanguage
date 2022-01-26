package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Reference;

public class LabelStat extends Stat {
	private final Reference reference;
	
	public LabelStat(Reference reference, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
	}
	
	public String getLocation() {
		return reference.getName();
	}
	
	public Reference getReference() {
		return reference;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
}
