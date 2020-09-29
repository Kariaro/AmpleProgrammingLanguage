package hardcoded.compiler.assembler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hardcoded.assembly.x86.AsmOpr;
import hardcoded.assembly.x86.OprBuilder;
import hardcoded.assembly.x86.RegisterX86;
import hardcoded.compiler.assembler.AssemblyConsts.AsmOp;
import hardcoded.utils.FileUtils;
import hardcoded.utils.StringUtils;

public final class AsmCompiler {
	public static void main(String[] args) {
		
//		for(AsmOp asm : Assembly.ALL.items) {
//			if(asm == null) continue;
//			
//			System.out.println(asm.toComplexString());
//		}
		
		try {
			loadInstructions();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void compile(AsmOp asm) {
		// 0x66 - changes the size of register operands
		// 0x67 - changes the size of address operands
		
	}
	
	private static void loadInstructions() throws IOException {
		byte[] bytes = FileUtils.readInputStream(AsmCompiler.class.getResourceAsStream("/assembler/insts.dat"));
		String[] lines = new String(bytes).split("(\r\n|\n|\r)");
		
		for(String line : lines) {
			// Skip comments and empty lines.
			line = line.trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			Pat pat = new Pat(line);
			System.out.println(pat);
			
			// compile(pat, new OperatorBuilder().reg(Register.EDI).get(), new OperatorBuilder().reg(Register.EAX).get());
		}
	}
	
	private static class Pat {
		private final String mnemonic;
		private final String[] opcode;
		private final String flags;
		private final String[] operands;
		
		public Pat(String line) {
			String[] parts = line.split("\\s+");
			
			this.mnemonic = parts[0];
			List<String> list = new ArrayList<>();
			
			int flagsIndex = -1;
			for(int i = 2; i < parts.length; i++) {
				if(parts[i].equals("]")) {
					flagsIndex = i + 1;
					break;
				}
				
				list.add(parts[i]);
			}
			
			this.opcode = list.toArray(new String[0]);
			if(flagsIndex >= parts.length) {
				flags = "";
				operands = new String[0];
				return;
			}
			
			this.flags = parts[flagsIndex];
			
			list.clear();
			for(int i = flagsIndex + 1; i < parts.length; i++) {
				if(parts[i].trim().equals(",")) continue;
				list.add(parts[i].replace(",", ""));
			}
			
			this.operands = list.toArray(new String[0]);
		}
		
		public int getNumOperands() {
			return operands.length;
		}
		
		public String toString() {
			return String.format("%-20s%-20s%-20s%s",
				mnemonic,
				"[ " + StringUtils.join(", ", opcode) + " ]",
				flags,
				StringUtils.join(", ", operands)
			);
		}
	}
	
	private static boolean size(String str, AsmOpr op) {
		char type = str.charAt(0);
		String size = str.substring(1);
		
		/*
		String nmn = null;
		switch(str.charAt(0)) {
			case 'I': nmn = "imm"; break;
			case 'O': nmn = "moffs"; break;
			case 'J': nmn = "rel"; break;
			case 'R':
			case 'F': nmn = "flags"; break;
			case 'A': nmn = "ptr"; break;
			case 'S': nmn = "seg"; break;
			case 'Z':
			case 'G': nmn = "r"; break;
			case 'E': nmn = "r/m"; break;
			
			case 'M':
			case 'X':
			case 'Y': nmn = "m"; break;
		}
		*/
		
		System.out.println("type=" + type + ", size=" + size + "\t" + op);
		if(type == 'G') {
			return op.isRegister()
				&& matches(size, op);
		}
		
		if(type == 'E') {
			return matches(size, op) &&
				(op.isRegister() || op.isMemory());
		}
		
		return false;
	}
	
	private static boolean matches(String size, AsmOpr op) {
		int op_size = op.getSize();
//		if(size.equals("bss")) size = "8"; // T O D O
//		if(size.equals("bs")) size = "8/16";
//		if(size.equals("b")) size = "8";
//		if(size.equals("w")) size = "16";
//		if(size.equals("wo")) size = "16/32";
//		if(size.equals("do")) size = "32/64";
//		if(size.equals("vqp")) size = "16/32/64";
//		if(size.equals("vds")) size = "16/32";
//		if(size.equals("vq")) size = "64/16";
//		if(size.equals("vs")) size = "16/32";
//		if(size.equals("v")) size = "16/32";
//		if(size.equals("p")) size = "32/48";
//		if(size.equals("q")) size = "64";
		
		switch(size) {
			case "vqp": return op_size == 64 || op_size == 32 || op_size == 16;
			case "b": return op_size == 8;
		}
		
		return false;
	}
	
	private static void compile(Pat pat, AsmOpr... ops) {
		String flags = pat.flags;
		int operands = ops.length;
		
		if(flags.contains("r") && pat.getNumOperands() == 2 && operands == 2) {
			// The flag 'r' means that the instructions has a ModR/M
			// byte and that it contains both the 'reg' and 'r/m' part.
			AsmOpr opr0 = ops[0];
			AsmOpr opr1 = ops[1];
			
			boolean enable = size(pat.operands[0], opr0) && size(pat.operands[1], opr1);
			
			if(!enable) return;
			// 01 ADD   EDI, EAX
			// 00000001 10000111
			System.out.println(pat);
			
			// Opr0 and Opr1 should have the same size
			// If '48' [RAX.W] is used we are using 64 bit
			// If '66' is used we change the register to 16 bit
			// If '67' is used we change the sizes of address registers to 32 bit
			// If no prefix is used we use 32 bit on both
			
			// ADD dword [RDI + 0x0], EAX
			// 01 87 00 00 00 00
		}
	}
}
