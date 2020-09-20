package hardcoded.compiler.assembler;

import java.util.ArrayList;
import java.util.List;

public class AsmOpr {
	private final List<Part> parts = new ArrayList<>();
	private final boolean isMemory; // TODO get size of the memory ptr
	
	
	private AsmOpr(List<Part> parts, boolean isMemory) {
		this.isMemory = isMemory;
		this.parts.addAll(parts);
	}
	
	public AsmOpr(AsmReg reg) {
		parts.add(new Part.Reg(reg));
		isMemory = false;
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
	
	
	@Override
	public String toString() {
		// TODO: ModR/M
		
		StringBuilder sb = new StringBuilder();
		
		for(Part p : parts) {
			sb.append(p).append(" ");
		}
		
		String string = sb.toString().trim();
		
		if(isMemory) {
			return "[" + string + "]";
		}
		
		return string;
	}
	
	public static AsmOpr plus(AsmReg r0, AsmReg r1) {
		return null;
	}
	
	public static AsmOpr plus(AsmReg r0, AsmReg r1, Object disp) {
		return null;
	}
	
	public static abstract class Part {
		public static class Reg extends Part {
			private final AsmReg reg;
			
			public Reg(AsmReg reg) {
				this.reg = reg;
			}
			
			public Object get() {
				return reg;
			}
			
			public String toString() {
				return String.valueOf(reg);
			}
		}
		
		public static class Disp extends Part {
			public final int bits;
			public final long value;
			
			public Disp(int bits, long value) {
				this.bits = bits;
				this.value = value;
			}

			public Object get() {
				return value;
			}
			
			public String toString() {
				return "disp" + bits + "=0x" + Long.toHexString(value);
			}
		}
		
		public static class Imm extends Part {
			public final int bits;
			public final long value;
			
			public Imm(int bits, long value) {
				this.bits = bits;
				this.value = value;
			}
			
			public Object get() {
				return null;
			}
			
			public String toString() {
				return "imm" + bits + "=0x" + Long.toHexString(value);
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
			
			public String toString() {
				return "0x" + Long.toHexString(value);
			}
		}
		
		public abstract Object get();
	}
	
	public static class OprBuilder {
		public List<Part> parts = new ArrayList<>();
		
		public OprBuilder reg(AsmReg reg) {
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
			parts.add(new Part.Disp(8, value));
			return this;
		}
		
		public OprBuilder disp16(long value) {
			parts.add(new Part.Disp(16, value));
			return this;
		}
		
		public OprBuilder disp32(long value) {
			parts.add(new Part.Disp(32, value));
			return this;
		}
		
		public OprBuilder imm8(long value) {
			parts.add(new Part.Imm(8, value));
			return this;
		}
		
		public OprBuilder imm16(long value) {
			parts.add(new Part.Imm(16, value));
			return this;
		}
		
		public OprBuilder imm32(long value) {
			parts.add(new Part.Imm(32, value));
			return this;
		}
		
		public AsmOpr getMemory() {
			return new AsmOpr(parts, true);
		}
		
		public AsmOpr get() {
			return new AsmOpr(parts, false);
		}
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
