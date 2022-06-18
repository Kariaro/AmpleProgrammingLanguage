package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.util.ArrayList;
import java.util.List;

public class ProgStat extends Stat {
	private List<Stat> elements;
	
	public ProgStat(ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
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
		return TreeType.PROGRAM;
	}
}
