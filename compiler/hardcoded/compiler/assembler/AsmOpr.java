package hardcoded.compiler.assembler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.assembler.operator.Register;

public class AsmOpr {
	private final List<Part> parts = new ArrayList<>();
	private final boolean isMemory;
	private final int ptr_size;
	
	private AsmOpr(List<Part> parts, boolean isMemory, int ptr_size) {
		this.isMemory = isMemory;
		this.ptr_size = ptr_size;
		this.parts.addAll(parts);
	}
	
	public AsmOpr(Register reg) {
		parts.add(new Part.Reg(reg));
		isMemory = false;
		ptr_size = -1;
	}
	
	public boolean isRegister() {
		if(isMemory || length() != 1) return false;
		return getPart(0) instanceof Part.Reg;
	}
	
	public boolean isImmediate() {
		if(isMemory || length() != 1) return false;
		return getPart(0) instanceof Part.Imm;
	}
	
	public boolean isMemory() {
		return isMemory;
	}
	
	public int length() {
		return parts.size();
	}
	
	public Part getPart(int index) {
		return parts.get(index);
	}
	
	public Object getObject(int index) {
		return parts.get(index).get();
	}
	
	public int getSize() {
		if(isMemory) return ptr_size;
		return getPart(0).size();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(Part p : parts) {
			sb.append(p).append(" ");
		}
		
		String string = sb.toString().trim();
		
		if(isMemory) {
			if(ptr_size == 8) return "byte [" + string + "]";
			if(ptr_size == 16) return "word [" + string + "]";
			if(ptr_size == 32) return "dword [" + string + "]";
			if(ptr_size == 64) return "qword [" + string + "]";
			return "[" + string + "]";
		}
		
		return string;
	}
	
	public static abstract class Part {
		public static class Reg extends Part {
			private final Register reg;
			
			public Reg(Register reg) {
				this.reg = reg;
			}
			
			public Object get() {
				return reg;
			}
			
			public int size() {
				return reg.bits;
			}
			
			public String toString() {
				return String.valueOf(reg);
			}
		}
		
		public static class Disp extends Part {
			public final int bits;
			public final Number value;
			
			public Disp(int bits, Number value) {
				this.bits = bits;
				this.value = value;
			}

			public Object get() {
				return value;
			}
			
			public int size() {
				return bits;
			}
			
			public String toString() {
				return String.format("disp%d=0x%0" + (bits / 4) + "x", bits, value);
			}
		}
		
		public static class Imm extends Part {
			public final int bits;
			public final Number value;
			
			public Imm(int bits, Number value) {
				this.bits = bits;
				this.value = value;
			}
			
			public Object get() {
				return value;
			}
			
			public int size() {
				return bits;
			}
			
			public String toString() {
				return String.format("imm%d=0x%0" + (bits / 4) + "x", bits, value);
			}
		}
		
		public static class Opr extends Part {
			public final int type;
			
			public Opr(int type) {
				this.type = type;
			}
			
			public Object get() {
				return type;
			}
			
			public int size() {
				return -1;
			}
			
			public String toString() {
				switch(type) {
					case 0:	return "+";
					case 1:	return "*";
				}
				
				return "?";
			}
		}
		
		public static class Num extends Part {
			public final long value;
			
			public Num(long value) {
				this.value = value;
			}
			
			public Object get() {
				return value;
			}
			
			public int size() {
				return -1;
			}
			
			public String toString() {
				return "0x" + Long.toHexString(value);
			}
		}
		
		public abstract Object get();
		public abstract int size();
	}
	
	public static class OprBuilder {
		public List<Part> parts = new ArrayList<>();
		
		public OprBuilder reg(Register reg) {
			parts.add(new Part.Reg(reg));
			return this;
		}
		
		public OprBuilder add() {
			parts.add(new Part.Opr(0));
			return this;
		}
		
		public OprBuilder mul() {
			parts.add(new Part.Opr(1));
			return this;
		}
		
		public OprBuilder num(long value) {
			parts.add(new Part.Num(value));
			return this;
		}
		
		public OprBuilder disp8(long value) {
			if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
				throw new RuntimeException("Disp value is outside range for 8 bits. (" + value + ")");
			
			parts.add(new Part.Disp(8, (byte)value));
			return this;
		}
		
		public OprBuilder disp16(long value) {
			if(value < Short.MIN_VALUE || value > Short.MAX_VALUE)
				throw new RuntimeException("Disp value is outside range for 16 bits. (" + value + ")");

			parts.add(new Part.Disp(8, (byte)value));
			return this;
		}
		
		public OprBuilder disp32(long value) {
			if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
				throw new RuntimeException("Disp value is outside range for 32 bits. (" + value + ")");

			parts.add(new Part.Disp(8, (byte)value));
			return this;
		}
		
		public OprBuilder imm8(long value) {
			if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
				throw new RuntimeException("Immediate value is outside range for 8 bits. (" + value + ")");
			
			parts.add(new Part.Imm(8, (byte)value));
			return this;
		}
		
		public OprBuilder imm16(long value) {
			if(value < Short.MIN_VALUE || value > Short.MAX_VALUE)
				throw new RuntimeException("Immediate value is outside range for 16 bits. (" + value + ")");
			
			parts.add(new Part.Imm(16, (short)value));
			return this;
		}
		
		public OprBuilder imm32(long value) {
			if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
				throw new RuntimeException("Immediate value is outside range for 32 bits. (" + value + ")");
			
			parts.add(new Part.Imm(32, (int)value));
			return this;
		}
		
		public AsmOpr ptrByte() { return new AsmOpr(parts, true, 8); }
		public AsmOpr ptrWord() { return new AsmOpr(parts, true, 16); }
		public AsmOpr ptrDword() { return new AsmOpr(parts, true, 32); }
		public AsmOpr ptrQword() { return new AsmOpr(parts, true, 64); }
		public AsmOpr ptr() { return new AsmOpr(parts, true, -1); }
		public AsmOpr get() { return new AsmOpr(parts, false, -1); }
	}
	
//	public static void main(String[] args) {
//		
//	}
//	
//	static {
//		AsmOpr opr = new OprBuilder()
//			.reg(AsmReg.RAX)
//			.add()
//			.num(0x56)
//			.getMemory();
//			
//		
//		System.out.println(opr);
//	}
}
