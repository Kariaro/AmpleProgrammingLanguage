package me.hardcoded.interpreter.value;

import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.StringUtils;

import java.util.function.Function;

/**
 * Value implementation for the interpreter
 *
 * @author HardCoded
 */
public interface Value {
	boolean isUnsigned();
	
	Type getType();
	
	Value getIndex(int index, ValueType type, Function<Long, Value> addressResolver);
	
	void setIndex(int index, Value value, ValueType type);
	
	long getInteger();
	
	double getFloating();
	
	class IntegerValue implements Value {
		private final boolean unsigned;
		private final long value;
		
		public IntegerValue(boolean unsigned, long value) {
			this.unsigned = unsigned;
			this.value = value;
		}
		
		@Override
		public boolean isUnsigned() {
			return unsigned;
		}
		
		@Override
		public Type getType() {
			return Type.Integer;
		}
		
		@Override
		public Value getIndex(int index, ValueType type, Function<Long, Value> addressResolver) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void setIndex(int index, Value value, ValueType type) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public long getInteger() {
			return value;
		}
		
		@Override
		public double getFloating() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return unsigned
				? Long.toUnsignedString(value)
				: Long.toString(value);
		}
	}
	
	class FloatingValue implements Value {
		private final double value;
		
		public FloatingValue(double value) {
			this.value = value;
		}
		
		@Override
		public boolean isUnsigned() {
			return false;
		}
		
		@Override
		public Type getType() {
			return Type.Floating;
		}
		
		@Override
		public Value getIndex(int index, ValueType type, Function<Long, Value> addressResolver) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void setIndex(int index, Value value, ValueType type) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public long getInteger() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public double getFloating() {
			return value;
		}
		
		@Override
		public String toString() {
			return Double.toString(value);
		}
	}
	
	class ArrayValue implements Value {
		protected final Integer[] values;
		protected final long address;
		
		public ArrayValue(long address, int size) {
			this.address = address;
			this.values = new Integer[size];
		}
		
		protected ArrayValue(long address, Integer[] values) {
			this.address = address;
			this.values = values;
		}
		
		@Override
		public boolean isUnsigned() {
			return false;
		}
		
		@Override
		public Type getType() {
			return Type.Array;
		}
		
		private int getTypeSize(ValueType type) {
			return (type.getDepth() > 0) ? ValueType.getPointerSize() : (type.getSize() >> 3);
		}
		
		private long read(int index, int size) {
			long result = 0;
			for (int i = 0; i < size; i++) {
				Integer read = values[index + i];
				if (read == null) {
					throw new RuntimeException("Undefined behavior. Trying to read uninitialized array value");
				}
				result |= (read & 0xffL) << (i * 8);
			}
			
			return result;
		}
		
		private void write(int index, long value, int size) {
			for (int i = 0; i < size; i++) {
				values[index + i] = (int) (value >> (i * 8)) & 0xff;
			}
		}
		
		protected int transformIndex(int index) {
			return index;
		}
		
		@Override
		public Value getIndex(int index, ValueType type, Function<Long, Value> addressResolver) {
			index = transformIndex(index);
			
			int typeSize = getTypeSize(type);
			if (index < 0 || index + typeSize > values.length) {
				throw new RuntimeException("Undefined behavior. Trying to read outside of memory");
			}
			
			long read = switch (typeSize) {
				case 8, 4, 2, 1 -> read(index, typeSize);
				default -> throw new RuntimeException("Undefined behavior. Trying to read undefined sized value");
			};
			
			if (type.getDepth() > 0) {
				Value value = addressResolver.apply(read);
				if (value == null) {
					throw new RuntimeException("Undefined behavior. Undefined memory pointer");
				}
			}
			
			if (type.isFloating()) {
				if (typeSize == 8) {
					return new FloatingValue(Double.longBitsToDouble(read));
				} else if (typeSize == 4) {
					return new FloatingValue(Float.intBitsToFloat((int) read));
				} else {
					throw new RuntimeException("Undefined behavior. Unimplemented floating type size");
				}
			}
			
			return new IntegerValue(type.isUnsigned(), read);
		}
		
		@Override
		public void setIndex(int index, Value value, ValueType type) {
			index = transformIndex(index);
			
			int typeSize = getTypeSize(type);
			if (index < 0 || index >= values.length) {
				throw new RuntimeException("Undefined behavior. Trying to write outside of memory");
			}
			
			long result;
			if (value instanceof IntegerValue val) {
				result = val.value;
			} else if (value instanceof FloatingValue val) {
				if (typeSize == 8) {
					result = Double.doubleToRawLongBits(val.value);
				} else if (typeSize == 4) {
					result = Float.floatToRawIntBits((float) val.value);
				} else {
					throw new RuntimeException("Floating point of size '" + typeSize + "' is undefined");
				}
			} else if (value instanceof ArrayValue val) {
				result = val.address;
			} else {
				throw new RuntimeException("Unknown value type '" + (value == null ? null : value.getClass()) + "'");
			}
			
			write(index, result, typeSize);
		}
		
		@Override
		public long getInteger() {
			return address;
		}
		
		@Override
		public double getFloating() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return "Array<" + values.length + "> " + String.format("0x%016x", address);
		}
	}
	
	class OffsetArrayValue extends ArrayValue {
		private final ArrayValue value;
		private final int offset;
		
		public OffsetArrayValue(ArrayValue value, int offset) {
			super(value.address, value.values);
			this.value = value;
			this.offset = offset;
		}
		
		@Override
		protected int transformIndex(int index) {
			return index + offset;
		}
		
		@Override
		public long getInteger() {
			return address + (offset & 0xffL);
		}
		
		@Override
		public String toString() {
			return "Array<" + values.length + "> " + String.format("0x%016x", getInteger());
		}
	}
	
	class StringValue extends ArrayValue {
		public StringValue(long address, String string) {
			super(address, string.length() + 1);
			
			for (int i = 0; i < string.length(); i++) {
				this.values[i] = ((int) string.charAt(i)) & 0xff;
			}
			
			// Null termination
			this.values[string.length()] = 0;
		}
		
		@Override
		public String toString() {
			StringBuilder value = new StringBuilder();
			for (Integer item : values) {
				value.append((char) (int) item);
			}
			return '"' + StringUtils.escapeString(value.toString()) + "\" " + String.format("0x%016x", address);
		}
	}
	
	enum Type {
		Integer,
		Floating,
		Array
	}
}
