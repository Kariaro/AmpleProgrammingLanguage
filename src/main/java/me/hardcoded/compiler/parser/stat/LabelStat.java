package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;

public class LabelStat extends Stat {
	private Reference reference;
	
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
	
	public void setReference(Reference reference) {
		this.reference = reference;
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
		return TreeType.LABEL;
	}
}
