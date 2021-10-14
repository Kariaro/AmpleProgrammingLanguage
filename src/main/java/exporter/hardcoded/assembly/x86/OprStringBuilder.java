package hardcoded.assembly.x86;

import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.StringUtils;

class OprStringBuilder {
	OprStringBuilder() {}
	
	private static final String DELIMITERS = " []()+*";
	
	/**
	 * Split the input string into parts.
	 * 
	 * @param	value	the string to split
	 * @return	an array with parts
	 */
	static String[] splitString(String value) {
		List<String> list = new ArrayList<>();
		String buffer = "";
		
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			
			if(DELIMITERS.indexOf(c) < 0) {
				buffer += c;
			} else {
				if(!buffer.isEmpty())
					list.add(buffer);
				
				if(c != ' ')
					list.add(String.valueOf(c));
				
				buffer = "";
			}
		}
		
		if(!buffer.isEmpty())
			list.add(buffer);
		
		return list.toArray(new String[0]);
	}
	
	static int getPointerSize(String[] parts) {
		switch(parts[0]) {
			case "[": return 0;
			case "byte": return 8;
			case "word": return 16;
			case "dword": return 32;
			case "qword": return 64;
			case "xmmword": return 128;
			case "ymmword": return 256;
			default: return -1;
		}
	}
	
	static AsmOpr fromString(OprBuilder builder, String value) {
		value = value.toLowerCase()
			.replaceAll("\\s+", " ")
			.replaceAll("- ", "+ -").trim();
		String[] parts = splitString(value);
		
		int pointerSize = getPointerSize(parts);
		if(pointerSize < 0) {
			return readRegister(builder, parts);
		} else {
			return readMemory(builder, parts, value, pointerSize);
		}
	}
	
	// Some registers can have segment values ..... AHHHHHHHHHHH
	static int readNextPart(OprBuilder builder, String[] parts, String input, int index) {
		String part = parts[index];
		
		if(isNumber(part)) {
			long value = readNumber(part);
			
			if(parts[index + 1].equals("*")) {
				if(Long.bitCount(value) != 1 || ((value & 15) == 0))
					throw new AssertionError(String.format("Invalid scalar value %s0x%1x. Only 1,2,4,8 allowed. \"%s\"", (value < 0 ? "-":""), (value < 0 ? -value:value), input));
				
				builder.reg(RegisterX86.valueOf(parts[index + 2].toUpperCase())).mul().num(value);
				return index + 3;
			}
			
			builder.num(value);
			return index + 1;
		}
		
		RegisterX86 reg = RegisterX86.valueOf(part.toUpperCase());
		if(parts[index + 1].equals("*")) {
			long value = readNumber(parts[index + 2], "Expected number but got '" + parts[index + 2] + "' \"" + input + "\"");
			if(Long.bitCount(value) != 1 || ((value & 15) == 0))
				throw new AssertionError(String.format("Invalid scalar value %s0x%1x. Only 1,2,4,8 allowed. \"%s\"", (value < 0 ? "-":""), (value < 0 ? -value:value), input));
			
			builder.reg(reg).mul().num(value);
			return index + 3;
		}
		
		builder.reg(reg);
		return index + 1;
	}
	
	static AsmOpr readMemory(OprBuilder builder, String[] parts, String input, int size) {
		int i = (size > 0 ? 2:1);
		
		if(!parts[parts.length - 1].equals("]"))
			throw new AssertionError("Missing address closing bracket ']' \"" + input + "\"");
		
		if(!parts[i - 1].equals("["))
			throw new AssertionError("Missing address opening bracket '[' \"" + input + "\"");
		
		if(parts.length < i + 2)
			throw new AssertionError("Missing address body. \"" + input + "\"");
		
		parts[i - 1] = "+";
		for(; i < parts.length - 1; i++) {
			String part = parts[i - 1];
			
			if(part.equals("+")) {
				builder.add();
				String next = parts[i];
				
				if(next.equals("(")) {
					i = readNextPart(builder, parts, input, i + 1);
					if(!(builder.parts.get(builder.parts.size() - 1) instanceof OprPart.Num)) {
						// TODO: Throw a error because empty parentheses should not be encoded
						//       this way.
						builder.mul().num(0x1);
					}
					
					if(!parts[i].equals(")"))
						throw new AssertionError("Missing closing parenthesis ')' \"" + input + "\"");
					
					i++;
				} else {
					i = readNextPart(builder, parts, input, i);
				}
			} else {
				throw new AssertionError("Invalid syntax '" + part + "' \"" + input + "\"");
			}
		}
		
		builder.parts.remove(0);
		return builder.ptr(size);
	}
	
	static boolean isNumber(String value) {
		if(value.startsWith("-")) value = value.substring(1);
		if(value.startsWith("0x"))
			return value.substring(2).replaceAll("[0-9a-zA-Z]", "").isEmpty();
		
		return value.replaceAll("[0-9]", "").isEmpty();
	}
	
	static long readNumber(String value) {
		return readNumber(value, null);
	}
	
	static long readNumber(String value, String errorMessage) {
		long mul = 1;
		if(value.startsWith("-")) {
			mul = -1;
			value = value.substring(1);
		}
		
		try {
			if(value.startsWith("0x"))
				return Long.parseLong(value.substring(2), 16) * mul;
			
			return Long.parseLong(value) * mul;
		} catch(NumberFormatException exception) {
			throw new NumberFormatException(errorMessage);
		}
	}
	
	static AsmOpr readRegister(OprBuilder builder, String[] parts) {
		if((parts.length != 1))
			throw new AssertionError("Expected a register or immediate value but got \"" + StringUtils.join(" ", parts) + "\"");
		
		if(isNumber(parts[0]))
			return builder.imm(readNumber(parts[0]));
		
		RegisterX86 reg = RegisterX86.valueOf(parts[0].toUpperCase());
		if(reg == null)
			throw new AssertionError("Expected a register but got \"" + StringUtils.join(" ", parts) + "\"");
		
		return builder.reg(reg).get();
	}
}
