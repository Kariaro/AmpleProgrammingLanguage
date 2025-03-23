package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;
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
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.writeVarInt(elements.size());
		for (Stat s : elements) {
			s.serialize(stream);
		}
	}
	
	public static ScopeStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		ScopeStat result = new ScopeStat(head.syntaxPos());
		int size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			result.addElement(stream.deserializeStat());
		}
		
		return result;
	}
}
