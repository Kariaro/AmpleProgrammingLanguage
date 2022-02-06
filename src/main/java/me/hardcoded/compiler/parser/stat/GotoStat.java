package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;

public class GotoStat extends Stat {
	private Reference reference;
	
	public GotoStat(Reference reference, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
	}
	
	public String getDestination() {
		return reference.getName();
	}
	
	public void setReference(Reference reference) {
		this.reference = reference;
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
	
	@Override
	public TreeType getTreeType() {
		return TreeType.GOTO;
	}
}
