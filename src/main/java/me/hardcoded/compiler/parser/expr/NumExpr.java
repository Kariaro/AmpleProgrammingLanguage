package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class NumExpr extends Expr {
	private long value;
	private ValueType type;
	
	public NumExpr(ISyntaxPosition syntaxPosition, ValueType type, long value) {
		super(syntaxPosition);
		this.type = type;
		this.value = value;
	}
	
	public NumExpr(ISyntaxPosition syntaxPosition, ValueType type, int value) {
		super(syntaxPosition);
		this.type = type;
		this.value = Integer.toUnsignedLong(value);
	}
	
	public NumExpr(ISyntaxPosition syntaxPosition, ValueType type, double value) {
		super(syntaxPosition);
		this.type = type;
		this.value = Double.doubleToRawLongBits(value);
	}
	
	public NumExpr(ISyntaxPosition syntaxPosition, ValueType type, float value) {
		super(syntaxPosition);
		this.type = type;
		this.value = Integer.toUnsignedLong(Float.floatToRawIntBits(value));
	}
	
	public long getValue() {
		return value;
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
		return type;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.NUM;
	}
	
	@Override
	public String toString() {
		int size = type.getSize();
		
		if (type.isFloating()) {
			// TODO: Print without any scientific notation
			return switch (size) {
				case 64 -> Double.toString(Double.longBitsToDouble(value));
				case 32 -> Float.toString(Float.intBitsToFloat((int) value));
				default -> throw new RuntimeException("Invalid float size %s".formatted(size));
			};
		} else if (type.isUnsigned()) {
			return switch (size) {
				case 64 -> Long.toUnsignedString(value);
				case 32 -> Integer.toUnsignedString((int) value);
				case 16 -> Integer.toString((int) value & 0xffff);
				case 8 -> Integer.toString((int) value & 0xff);
				default -> throw new RuntimeException("Invalid unsigned size %s".formatted(size));
			};
		} else if (type.isSigned()) {
			return switch (size) {
				case 64 -> Long.toString(value);
				case 32 -> Integer.toString((int) value);
				case 16 -> Short.toString((short) value);
				case 8 -> Byte.toString((byte) value);
				default -> throw new RuntimeException("Invalid integer size %s".formatted(size));
			};
		} else {
			throw new RuntimeException("Invalid type '%x'".formatted(type.getFlags()));
		}
	}
}
