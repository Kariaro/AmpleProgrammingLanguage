package me.hardcoded.compiler.intermediate.inst;

import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.StringUtils;

public interface InstParam {
	class Ref implements InstParam {
		private final InstRef ref;
		
		public Ref(InstRef ref) {
			this.ref = ref;
		}
		
		public InstRef getReference() {
			return ref;
		}
		
		@Override
		public ValueType getSize() {
			return ref.getValueType();
		}
		
		@Override
		public String toString() {
			return ref.toString();
		}
	}
	
	class Num implements InstParam {
		private final ValueType type;
		private final long value;
		
		public Num(ValueType type, long value) {
			this.type = type;
			this.value = value;
		}
		
		@Override
		public ValueType getSize() {
			return type;
		}
		
		public long getValue() {
			return value;
		}
		
		@Override
		@Deprecated
		public String toString() {
			// TODO: Move this into an utility class
			
			int size = type.getSize();
			if (type.isFloating()) {
				// TODO: Print without any scientific notation
				return switch (size) {
					case 64 -> Double.toString(Double.longBitsToDouble(value));
					case 32 -> Float.toString(Float.intBitsToFloat((int) value));
					default -> throw new RuntimeException("Invalid float size %s".formatted(size));
				};
			}
			
			if (type.isUnsigned()) {
				return switch (size) {
					case 64 -> Long.toUnsignedString(value);
					case 32 -> Integer.toUnsignedString((int) value);
					case 16 -> Integer.toString((int) value & 0xffff);
					case 8 -> Integer.toString((int) value & 0xff);
					default -> throw new RuntimeException("Invalid unsigned size %s".formatted(size));
				};
			}
			
			return switch (size) {
				case 64 -> Long.toString(value);
				case 32 -> Integer.toString((int) value);
				case 16 -> Short.toString((short) value);
				case 8 -> Byte.toString((byte) value);
				default -> throw new RuntimeException("Invalid integer size %s".formatted(size));
			};
		}
	}
	
	class Str implements InstParam {
		private final String value;
		
		public Str(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public ValueType getSize() {
			return Primitives.U8.createArray(1);
		}
		
		@Override
		public String toString() {
			return "\"%s\"".formatted(StringUtils.escapeString(value));
		}
	}
	
	/**
	 * Returns the size of this parameter
	 */
	ValueType getSize();
}
