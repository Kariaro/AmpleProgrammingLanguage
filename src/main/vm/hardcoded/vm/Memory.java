package hardcoded.vm;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.numbers.Value;

public class Memory {
	private byte[] memory;
	
	public Memory() {
		this.memory = new byte[0xffffff];
	}
	
	public void write(int offset, Value value, LowType type) {
		write(offset, value, type.isPointer() ? Atom.i64:type.type());
	}
	
	public void write(int offset, Value value, Atom type) {
		int size = value.size();
		long val;
		if(value.isFloating()) {
			if(size == 8) {
				val = Double.doubleToRawLongBits(value.doubleValue());
			} else {
				val = Float.floatToRawIntBits((float)value.doubleValue());
			}
		} else {
			val = value.longValue();
		}
		
		for(int i = size - 1; i >= 0; i--) {
			memory[offset + i] = (byte)(val & 0xff);
			val >>>= 8L;
		}
	}
	
	public Value read(int offset, LowType type) {
		return read(offset, type.isPointer() ? Atom.i64:type.type());
	}
	
	public Value read(int offset, Atom type) {
		long val = 0;
		
		for(int i = 0; i < type.size(); i++) {
			long read = ((int)memory[offset + i]) & 0xff;
			val <<= 8L;
			val |= read;
		}
		
		if(type.isFloating()) {
			if(type.size() == 8) return Value.idouble(Double.longBitsToDouble(val));
			return Value.ifloat(Float.intBitsToFloat((int)val));
		}
		
		return Value.get(val, type);
	}
	
	public void write(int offset, int value) {
		memory[offset] = (byte)(value & 0xff);
	}
	
	public int read(int offset) {
		return ((int)memory[offset]) & 0xff;
	}
}
