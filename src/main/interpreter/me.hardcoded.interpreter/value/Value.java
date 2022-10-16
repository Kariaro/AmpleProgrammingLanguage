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
	Type getType();
	
	Value getIndex(int index, ValueType type, Function<Long, Value> addressResolver);
	
	void setIndex(int index, Value value, ValueType type);
	
	long getInteger();
	
	double getFloating();
	
	class NumberValue implements Value {
		private final long value;
		private final boolean floating;
		
		public NumberValue(boolean floating, long value) {
			this.floating = floating;
			this.value = value;
		}
		
		public NumberValue(long value) {
			this(false, value);
		}
		
		public NumberValue(double value) {
			this(true, Double.doubleToRawLongBits(value));
		}
		
		public NumberValue(float value) {
			this(true, Float.floatToRawIntBits(value));
		}
		
		@Override
		public Type getType() {
			return floating ? Type.Floating : Type.Integer;
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
			if (floating) {
				throw new UnsupportedOperationException();
			}
			
			return value;
		}
		
		@Override
		public double getFloating() {
			if (!floating) {
				throw new UnsupportedOperationException();
			}
			
			return Double.doubleToRawLongBits(value);
		}
		
		@Override
		public String toString() {
			return floating
				? Double.toString(Double.longBitsToDouble(value))
				: Long.toString(value);
		}
	}
	
	class ArrayValue implements Value {
		protected final Integer[] values;
		protected final long address;
		private final boolean[] pointer;
		
		public ArrayValue(long address, int size) {
			this.address = address;
			this.values = new Integer[size];
			this.pointer = new boolean[size];
		}
		
		protected ArrayValue(long address, Integer[] values, boolean[] pointer) {
			this.address = address;
			this.values = values;
			this.pointer = pointer;
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
			
			boolean isPointer = typeSize == 8 && pointer[index];
			
			if (type.getDepth() > 0) {
				Value value = addressResolver.apply(read);
				if (value == null) {
					throw new RuntimeException("Undefined behavior. Undefined memory pointer");
				}
			}
			
			if (type.isFloating()) {
				if (typeSize == 8) {
					return new NumberValue(Double.longBitsToDouble(read));
				} else if (typeSize == 4) {
					return new NumberValue(Float.intBitsToFloat((int) read));
				} else {
					throw new RuntimeException("Undefined behavior. Unimplemented floating type size");
				}
			}
			
			// Resolve arrays
			if (isPointer) {
				return addressResolver.apply(read);
			}
			
			return new NumberValue(read);
		}
		
		@Override
		public void setIndex(int index, Value value, ValueType type) {
			index = transformIndex(index);
			
			int typeSize = getTypeSize(type);
			if (index < 0 || index >= values.length) {
				throw new RuntimeException("Undefined behavior. Trying to write outside of memory (size=" + values.length + ") (index=" + index + ")");
			}
			
			// Clear pointer
			for (int i = 0; i < typeSize; i++) {
				pointer[index + i] = false;
			}
			
			long result;
			if (value instanceof NumberValue val) {
				result = val.value;
			} else if (value instanceof ArrayValue val) {
				result = val.address;
				pointer[index] = true;
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
			super(value.getInteger(), value.values, value.pointer);
			
			if (value instanceof OffsetArrayValue oav) {
				this.offset = oav.offset + offset;
				this.value = oav.value;
			} else {
				this.offset = offset;
				this.value = value;
			}
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
