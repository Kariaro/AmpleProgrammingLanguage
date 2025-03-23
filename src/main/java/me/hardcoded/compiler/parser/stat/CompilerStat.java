package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompilerStat extends Stat {
	private List<Part> parts;
	private String targetType;
	
	public CompilerStat(ISyntaxPos syntaxPos, String targetType, List<Part> parts) {
		super(syntaxPos);
		this.targetType = targetType;
		this.parts = parts;
	}
	
	public List<Part> getParts() {
		return parts;
	}
	
	public String getTargetType() {
		return targetType;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.COMPILER;
	}
	
	/**
	 * Compiler expression part
	 *
	 * <code>"mov RAX, {}" : reference</code>
	 */
	public static record Part(ISyntaxPos syntaxPosition, String command, List<Reference> references) {
		
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeString(targetType);
		stream.writeVarInt(parts.size());
		for (CompilerStat.Part part : parts) {
			stream.serializeISyntaxPosition(part.syntaxPosition());
			stream.serializeString(part.command());
			List<Reference> references = part.references();
			stream.writeVarInt(references.size());
			for (Reference reference : references) {
				stream.serializeReference(reference);
			}
		}
	}
	
	public static CompilerStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		String targetType = stream.deserializeString();
		List<CompilerStat.Part> parts = new ArrayList<>();
		int size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			ISyntaxPos partSyntaxPosition = stream.deserializeISyntaxPosition();
			String command = stream.deserializeString();
			int count = stream.readVarInt();
			List<Reference> references = new ArrayList<>();
			for (int j = 0; j < count; j++) {
				references.add(stream.deserializeReference());
			}
			parts.add(new CompilerStat.Part(partSyntaxPosition, command, references));
		}
		
		return new CompilerStat(head.syntaxPos(), targetType, parts);
	}
}
