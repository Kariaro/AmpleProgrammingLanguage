package hardcoded.assembly.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.FileUtils;
import hardcoded.utils.StringUtils;
import hardcoded.utils.buffer.IntBuffer;

public final class AsmLoader {
	private AsmLoader() {}
	
	// "/assembler/insts.dat"
	public static Unit[] load(String path) throws IOException {
		byte[] bytes = FileUtils.readInputStream(AsmLoader.class.getResourceAsStream(path));
		String[] lines = new String(bytes).split("(\r\n|\n|\r)");
		
		List<Unit> list = new ArrayList<>();
		for(String line : lines) {
			// Skip comments and empty lines.
			line = line.trim();
			if(line.isEmpty() || line.startsWith("#")) continue;
			
			list.add(new Unit(line));
		}
		
		return list.toArray(new Unit[0]);
	}
	
	public static class Unit {
		private final String mnemonic;
		private final int[] opcode;
		private final String flags;
		private final String[] operands;
		
		public Unit(String line) {
			String[] parts = line.split("\\s+");
			
			this.mnemonic = parts[0];
			IntBuffer buffer = new IntBuffer(32);
			
			int flagsIndex = -1;
			for(int i = 2; i < parts.length; i++) {
				if(parts[i].equals("]")) {
					flagsIndex = i + 1;
					break;
				}
				
				buffer.write(Integer.parseInt(parts[i], 16));
			}
			
			this.opcode = buffer.toArray();
			if(flagsIndex >= parts.length) {
				flags = "";
				operands = new String[0];
				return;
			}
			
			this.flags = parts[flagsIndex];
			
			List<String> list = new ArrayList<>();
			for(int i = flagsIndex + 1; i < parts.length; i++) {
				if(parts[i].trim().equals(",")) continue;
				list.add(parts[i].replace(",", ""));
			}
			
			this.operands = list.toArray(new String[0]);
		}
		
		public String getMnemonic() {
			return mnemonic;
		}
		
		public int[] getOpcode() {
			return opcode.clone();
		}
		
		public String getFlags() {
			return flags;
		}
		
		public String[] getOperands() {
			return operands.clone();
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
