package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.expr.NoneExpr;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;

public class ReturnStat extends Stat {
	private Expr value;
	
	public ReturnStat(ISyntaxPos syntaxPos, Expr value) {
		super(syntaxPos);
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
	}
	
	public boolean hasValue() {
		return !(value instanceof NoneExpr);
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return value.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.RETURN;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		value.serialize(stream);
	}
	
	public static ReturnStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Expr value = stream.deserializeExpr();
		return new ReturnStat(head.syntaxPos(), value);
	}
}
