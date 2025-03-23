package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StructStat extends Stat {
	private Reference reference;
	private List<VarStat> variables;
	private List<FuncStat> functions;
	
	public StructStat(ISyntaxPos syntaxPos, List<VarStat> variables, List<FuncStat> functions, Reference reference) {
		super(syntaxPos);
		this.reference = Objects.requireNonNull(reference);
		this.variables = variables;
		this.functions = functions;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<VarStat> getVariables() {
		return variables;
	}
	
	public List<FuncStat> getFunctions() {
		return functions;
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
		return TreeType.STRUCT;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeReference(reference);
		stream.writeVarInt(variables.size());
		for (Stat s : variables) {
			s.serialize(stream);
		}
		
		stream.writeVarInt(functions.size());
		for (Stat s : functions) {
			s.serialize(stream);
		}
	}
	
	public static StructStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Reference reference = stream.deserializeReference();
		List<VarStat> variables = new ArrayList<>();
		List<FuncStat> functions = new ArrayList<>();
		
		StructStat result = new StructStat(head.syntaxPos(), variables, functions, reference);
		int size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			variables.add(VarStat.deserialize(stream));
		}
		
		size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			functions.add(FuncStat.deserialize(stream));
		}
		
		return result;
	}
}