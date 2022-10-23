package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.util.ArrayList;
import java.util.List;

public class ScopeStat extends Stat {
	private List<Stat> elements;
	
	public ScopeStat(ISyntaxPos syntaxPos) {
		super(syntaxPos);
		this.elements = new ArrayList<>();
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
		for (Stat stat : elements) {
			if (!stat.isPure()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.SCOPE;
	}
}
