package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CallExpr extends Expr {
	private Reference reference;
	private List<Expr> parameters;
	
	public CallExpr(ISyntaxPos syntaxPos, Reference reference, List<Expr> parameters) {
		super(syntaxPos);
		this.reference = Objects.requireNonNull(reference);
		this.parameters = parameters;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public List<Expr> getParameters() {
		return parameters;
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
	public ValueType getType() {
		return reference.getValueType();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.CALL;
	}
	
	@Override
	public String toString() {
		String params = parameters.toString();
		params = params.substring(1, params.length() - 1);
		return "(" + reference.getName() + "(" + params + ")" + ")";
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeReference(reference);
		stream.writeVarInt(parameters.size());
		for (Expr e : parameters) {
			e.serialize(stream);
		}
	}
	
	public static CallExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Reference reference = stream.deserializeReference();
		List<Expr> parameters = new ArrayList<>();
		int size = stream.readVarInt();
		for (int i = 0; i < size; i++) {
			parameters.add(stream.deserializeExpr());
		}
		
		return new CallExpr(head.syntaxPos(), reference, parameters);
	}
}
