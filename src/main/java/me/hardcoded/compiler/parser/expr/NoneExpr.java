package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;

public class NoneExpr extends Expr {
	public NoneExpr(ISyntaxPos syntaxPos) {
		super(syntaxPos);
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public ValueType getType() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.NONE;
	}
	
	@Override
	public String toString() {
		return "(<none>)";
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
	}
	
	public static NoneExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		return new NoneExpr(head.syntaxPos());
	}
}
