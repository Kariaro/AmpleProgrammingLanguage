package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.TreeType;

import java.util.ArrayList;
import java.util.List;

public class NamespaceStat extends Stat {
	private Reference reference;
	private List<Stat> elements;
	
	public NamespaceStat(Reference reference, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.reference = reference;
		this.elements = new ArrayList<>();
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public void addElement(Stat stat) {
		elements.add(stat);
	}
	
	public List<Stat> getElements() {
		return elements;
	}
	
	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.NAMESPACE;
	}
}
