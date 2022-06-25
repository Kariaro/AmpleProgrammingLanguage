package me.hardcoded.exporter.asm;

import me.hardcoded.compiler.intermediate.inst.InstParam;
import me.hardcoded.compiler.intermediate.inst.InstRef;
import me.hardcoded.compiler.parser.type.ValueType;

class AsmUtils {
	public static String getPointerName(int size) {
		return switch (size) {
			case 8 -> "byte";
			case 16 -> "word";
			case 32 -> "dword";
			case 64 -> "qword";
			default -> throw new RuntimeException();
		};
	}
	
	public static String getRegSize(String name, InstParam param) {
		if (param instanceof InstParam.Ref ref) {
			return getRegSize(name, ref.getReference());
		} else {
			return getRegSize(name, param.getSize().getSize());
		}
	}
	
	public static String getRegSize(String name, InstRef ref) {
		return getRegSize(name, getTypeSize(ref.getValueType()));
	}
	
	public static String getRegSize(String name, int size) {
		return switch (size) {
			case 8 -> name.charAt(0) + "L";
			case 16 -> name;
			case 32 -> "E" + name;
			case 64 -> "R" + name;
			default -> throw new RuntimeException();
		};
	}
	
	public static String getParamValue(InstRef ref, AsmProcedure proc) {
		return getStackPtr(ref, proc);
	}
	
	public static String getParamValue(InstParam param, AsmProcedure proc) {
		if (param instanceof InstParam.Ref value) {
			return getStackPtr(value.getReference(), proc);
		} else if (param instanceof InstParam.Num value) {
			return value.toString();
		}
		
		throw new RuntimeException();
	}
	
	public static String getRawStackPtr(InstRef ref, int offset, AsmProcedure proc) {
		return "[RBP - 0x%x]".formatted(
			proc.getStackOffset(ref) - offset
		);
	}
	
	public static String getStackPtr(InstRef ref, AsmProcedure proc) {
		return "%s [RBP - 0x%x]".formatted(
			getPointerName(ref),
			proc.getStackOffset(ref)
		);
	}
	
	public static String getPointerName(InstRef ref) {
		return getPointerName(getTypeSize(ref.getValueType()));
	}
	
	public static int getLowerTypeSize(ValueType type) {
		return ((type.getDepth() > 1) ? getPointerSize() : (type.getSize() >> 3)) << 3;
	}
	
	public static int getTypeSize(ValueType type) {
		return getTypeByteSize(type) << 3;
	}
	
	public static int getTypeByteSize(ValueType type) {
		return (type.getDepth() > 0) ? getPointerSize() : (type.getSize() >> 3);
	}
	
	// TODO: Read this from some config
	public static int getPointerSize() {
		return 8;
	}
}
