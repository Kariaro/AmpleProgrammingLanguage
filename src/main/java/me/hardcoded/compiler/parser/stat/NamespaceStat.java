package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.ArrayList;
import java.util.List;

public class NamespaceStat extends Stat {
	private List<Stat> elements;
	private Reference reference;
	
	public NamespaceStat(ISyntaxPosition syntaxPosition, Reference reference) {
		super(syntaxPosition);
		this.elements = new ArrayList<>();
		this.reference = reference;
	}
	
	public void addElement(Stat stat) {
		elements.add(stat);
	}
	
	public List<Stat> getElements() {
		return elements;
	}
	
	public Reference getReference() {
		return reference;
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
