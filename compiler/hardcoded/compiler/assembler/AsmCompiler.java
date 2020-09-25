package hardcoded.compiler.assembler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			this.flags = parts[flagsIndex];
			
			list.clear();
			for(int i = flagsIndex + 1; i < parts.length; i++) {
				list.add(parts[i]);
			}
			
			this.operands = list.toArray(new String[0]);
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
}
