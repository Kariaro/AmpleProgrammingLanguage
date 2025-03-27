package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;

public class SizeofExpr extends Expr {
	private ValueType type;
	
	public SizeofExpr(ISyntaxPos syntaxPos, ValueType type) {
		super(syntaxPos);
		this.type = type;
	}
	
	public ValueType getCheckedType() {
		return type;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public ValueType getType() {
		return Primitives.USIZE;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.SIZEOF;
	}
	
	@Override
	public String toString() {
		return "sizeof( " + type + " )";
	}
	
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeValueType(type);
	}
	
	public static SizeofExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		ValueType type = stream.deserializeValueType();
		return new SizeofExpr(head.syntaxPos(), type);
	}
}
