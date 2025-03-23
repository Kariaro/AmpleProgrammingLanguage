package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FuncStat extends Stat {
	private List<Reference> parameters;
	private Reference reference;
	private Stat body;
	
	public FuncStat(ISyntaxPos syntaxPos, List<Reference> parameters, Reference reference) {
		super(syntaxPos);
		this.parameters = parameters;
		this.reference = Objects.requireNonNull(reference);
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<Reference> getParameters() {
		return parameters;
	}
	
	public void setBody(Stat body) {
		this.body = body;
	}
	
	public Stat getBody() {
		return body;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return body.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.FUNC;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeReference(reference);
		stream.writeVarInt(parameters.size());
		for (Reference param : parameters) {
			stream.serializeReference(param);
		}
		body.serialize(stream);
	}
	
	public static FuncStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Reference reference = stream.deserializeReference();
		List<Reference> parameters = new ArrayList<>();
		int size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			parameters.add(stream.deserializeReference());
		}
		
		Stat body = stream.deserializeStat();
		FuncStat result = new FuncStat(head.syntaxPos(), parameters, reference);
		result.setBody(body);
		return result;
	}
}
