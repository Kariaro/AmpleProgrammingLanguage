package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.ValueType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class BuiltinExpr extends Expr {
	private Expr value;
	private Kind kind;
	
	public BuiltinExpr(ISyntaxPos syntaxPos, Kind kind, Expr value) {
		super(syntaxPos);
		this.kind = kind;
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
	}
	
	public Kind getKind() {
		return kind;
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
		return switch (kind) {
			case PANIC -> Primitives.NONE;
			case ADDRESS -> {
				if (value instanceof BinaryExpr expr) {
					if (expr.getOperation() == Operation.ARRAY
						|| expr.getOperation() == Operation.MEMBER) {
						yield expr.getType().createArray(1); // TODO ?
					}
				}
				yield Primitives.NONE;
			}
		};
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.BUILTIN;
	}
	
	@Override
	public String toString() {
		return "__builtin_%s(%s)".formatted(kind.name().toLowerCase(), value.toString());
	}
	
	public enum Kind {
		PANIC,
		ADDRESS;
		
		public static final Kind[] VALUES = values();
		private static final Map<String, Kind> MAP_VALUES = Arrays.stream(VALUES)
			.collect(Collectors.toMap(
				i -> "__builtin_" + i.name().toLowerCase(),
				i -> i
			));
		
		public static boolean isBuiltin(String name) {
			return MAP_VALUES.containsKey(name);
		}
		
		public static Kind getBuiltin(String name) {
			return MAP_VALUES.get(name);
		}
	}
	
	@Override
	public void serialize(LinkableStream stream) throws IOException {
		stream.writeObjectHeader(this);
		
		value.serialize(stream);
		stream.writeVarInt(kind.ordinal());
	}
	
	public static BuiltinExpr deserialize(LinkableStream stream) throws IOException {
		var head = stream.readObjectHeader();
		
		Expr value = stream.deserializeExpr();
		Kind kind = Kind.VALUES[stream.readVarInt()];
		return new BuiltinExpr(head.syntaxPos(), kind, value);
	}
}
