package me.hardcoded.compiler.parser.stat;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.expr.Expr;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;

import java.io.IOException;
import java.util.Objects;

public class WhileStat extends Stat {
	private Expr condition;
	private Stat body;
	
	public WhileStat(ISyntaxPos syntaxPos, Expr condition, Stat body) {
		super(syntaxPos);
		this.condition = Objects.requireNonNull(condition);
		this.body = Objects.requireNonNull(body);
	}
	
	public Expr getCondition() {
		return condition;
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
		return false;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.WHILE;
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		condition.serialize(stream);
		body.serialize(stream);
	}
	
	public static WhileStat deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Expr condition = stream.deserializeExpr();
		Stat body = stream.deserializeStat();
		return new WhileStat(head.syntaxPos(), condition, body);
	}
}
