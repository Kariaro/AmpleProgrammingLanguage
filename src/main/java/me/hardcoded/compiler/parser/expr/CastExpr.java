package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;

public class CastExpr extends Expr {
	private ValueType type;
	private Expr value;
	private Kind kind;
	
	public enum Kind {
		D_TO_F,
		F_TO_D,
		BIT_CAST,
		CAST,
	}
	
	public CastExpr(ISyntaxPos syntaxPos, ValueType type, Expr value, Kind kind) {
		super(syntaxPos);
		this.type = type;
		this.value = value;
		this.kind = kind;
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
		return value.isPure();
	}
	
	public Kind getKind() {
		return kind;
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
		return switch (kind) {
			case BIT_CAST -> "bit_cast<" + type + ">( " + value + " )";
			case CAST -> "cast<" + type + ">( " + value + " )";
			case D_TO_F -> "float_cast<" + type + ">( " + value + " )";
			default -> "unknown_cast<" + type + ">( " + value + " )";
		};
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		stream.serializeValueType(type);
		value.serialize(stream);
		stream.writeVarInt(kind.ordinal());
	}
	
	public static CastExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		ValueType type = stream.deserializeValueType();
		Expr value = stream.deserializeExpr();
		int kind = stream.readVarInt();
		return new CastExpr(head.syntaxPos(), type, value, Kind.values()[kind]);
	}
}
