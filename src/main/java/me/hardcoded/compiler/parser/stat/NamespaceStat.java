package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NamespaceStat extends Stat {
	private List<Stat> elements;
	private Reference reference;
	
	public NamespaceStat(ISyntaxPos syntaxPos, Reference reference) {
		super(syntaxPos);
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
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeReference(reference);
		stream.writeVarInt(elements.size());
		for (Stat s : elements) {
			s.serialize(stream);
		}
	}
	
	public static NamespaceStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Reference reference = stream.deserializeReference();
		NamespaceStat result = new NamespaceStat(head.syntaxPos(), reference);
		int size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			result.addElement(stream.deserializeStat());
		}
		
		return result;
	}
}
