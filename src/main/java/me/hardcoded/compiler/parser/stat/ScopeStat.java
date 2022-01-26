package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;

import java.util.ArrayList;
import java.util.List;

public class ScopeStat extends Stat {
	private final List<Stat> elements;
	
	public ScopeStat(ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		
		// Create a new list for all elements in this scope
		elements = new ArrayList<>();
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
		// This statement is only pure if all sub elements are pure
		for (Stat stat : elements) {
			if (!stat.isPure()) {
				return false;
			}
		}
		
		return true;
	}
}
