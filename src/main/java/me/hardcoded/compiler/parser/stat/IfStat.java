package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;

public class IfStat extends Stat {
	private Expr value;
	private Stat body;
	private Stat elseBody;
	
	public IfStat(ISyntaxPos syntaxPos, Expr value, Stat body, Stat elseBody) {
		super(syntaxPos);
		this.value = value;
		this.body = body;
		this.elseBody = elseBody;
	}
	
	public Expr getValue() {
		return value;
	}
	
	public Stat getBody() {
		return body;
	}
	
	public Stat getElseBody() {
		return elseBody;
	}
	
	public boolean hasElseBody() {
		return !elseBody.isEmpty();
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return body.isPure() && elseBody.isPure();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.IF;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		value.serialize(stream);
		body.serialize(stream);
		elseBody.serialize(stream);
	}
	
	public static IfStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Expr condition = stream.deserializeExpr();
		Stat body = stream.deserializeStat();
		Stat elseBody = stream.deserializeStat();
		return new IfStat(head.syntaxPos(), condition, body, elseBody);
	}
}
