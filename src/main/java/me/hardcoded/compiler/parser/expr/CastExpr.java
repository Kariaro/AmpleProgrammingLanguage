package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;

public class CastExpr extends Expr {
	private ValueType type;
	private Expr value;
	
	public CastExpr(ISyntaxPos syntaxPos, ValueType type, Expr value) {
		super(syntaxPos);
		this.type = type;
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
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
		return type;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.CAST;
	}
	
	@Override
	public String toString() {
		return "cast<" + type + ">( " + value + " )";
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeValueType(type);
		value.serialize(stream);
	}
	
	public static CastExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		ValueType type = stream.deserializeValueType();
		Expr value = stream.deserializeExpr();
		return new CastExpr(head.syntaxPos(), type, value);
	}
}
