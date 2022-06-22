package me.hardcoded.exporter.asm;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.error.CodeGenException;

public class AsmCodeGenerator implements ICodeGenerator {
	@Override
	public byte[] getBytecode(InstFile program) throws CodeGenException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getAssembler(InstFile program) throws CodeGenException {
		StringBuilder sb = new StringBuilder();
		
		InstRef main = null;
		for (Procedure proc : program.getProcedures()) {
			AsmProcedure asmProc = new AsmProcedure(proc);
			
			InstRef test = proc.getReference();
			sb.append("; %s %s (%s)\n".formatted(test.getValueType(), test, proc.getParameters()));
			
			if (test.getName().equals("main")) {
				main = test;
			}
			
			for (Inst inst : proc.getInstructions()) {
				sb.append(buildInstruction(asmProc, inst)).append('\n');
			}
		}
		
		if (main == null) {
			throw new RuntimeException("Main function was undefined");
		}
		
		{
			StringBuilder header = new StringBuilder();
			header.append("BITS 64\n\n");
			header.append("section .data\n");
			header.append("    hex_data db \"0123456789abcdef\"\n");
			header.append("    hex_strs db \"................\", 0ah\n\n");
			header.append("section .text\n");
			header.append("    global _start:\n\n");
			header.append("""
printhex:
	mov rcx, 16
	.loop:
		mov rbx, rax
		and rbx, 15
		mov bl, byte [rbx + hex_data]
		mov byte [rcx + hex_strs - 1], bl
		shr rax, 4
		loop .loop
	mov rax, 1
	lea rsi, [hex_strs]
	mov rdi, 1
	mov rdx, 17
	syscall
	ret
""");
			header.append("_start:\n");
			header.append("    call %s\n".formatted(main.toSimpleString()));
			header.append("    call printhex\n");
			header.append("    ret\n");
			header.append("\n");
			sb.insert(0, header);
		}
		
		return sb.toString().trim().getBytes(StandardCharsets.UTF_8);
	}
	
	private String buildInstruction(AsmProcedure proc, Inst inst) throws CodeGenException {
		if (inst.getOpcode() == Opcode.LABEL) {
			InstRef reference = inst.getRefParam(0).getReference();
			if (reference.isFunction()) {
				StringBuilder sb = new StringBuilder();
				sb.append(reference.toSimpleString() + ":\n");
				sb.append("    push RBP\n");
				sb.append("    mov RBP, RSP\n");
				sb.append("    sub RSP, 0x%x\n".formatted(proc.getStackSize()));
				
				return sb.toString().stripTrailing();
			}
			
			return "  ." + reference.toSimpleString() + ':';
		}
		
		List<String> sb = new ArrayList<>();
		sb.add("; %s".formatted(inst));
		
		switch (inst.getOpcode()) {
			case STACK_ALLOC -> {
				InstRef dst = inst.getRefParam(0).getReference();
				
				String regName = getRegSize("AX", dst);
				sb.add("lea %s, %s".formatted(
					regName,
					getRawStackPtr(dst, proc)
				));
				sb.add("mov %s, %s".formatted(
					getStackPtr(dst, proc),
					regName
				));
			}
			case MOV -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstParam src = inst.getParam(1);
				
				if (src instanceof InstParam.Num value) {
					sb.add("mov %s, %s".formatted(
						getStackPtr(dst, proc),
						value
					));
				} else if (src instanceof InstParam.Ref value) {
					String regName = getRegSize("AX", value.getReference());
					sb.add("mov %s, %s".formatted(
						regName,
						getStackPtr(value.getReference(), proc)
					));
					sb.add("mov %s, %s".formatted(
						getStackPtr(dst, proc),
						regName
					));
				}
			}
			case STORE -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstParam offset = inst.getParam(1);
				InstParam src = inst.getParam(2);
				
				String srcValue = getParamValue(src, proc);
				sb.add("mov RBX, %s".formatted(
					getStackPtr(dst, proc)
				));
				
				String offsetValue;
				if (offset instanceof InstParam.Num value) {
					offsetValue = "0x%x".formatted(Integer.parseInt(value.toString()));
				} else if (offset instanceof InstParam.Ref value) {
					offsetValue = "RCX";
					sb.add("xor RCX, RCX");
					sb.add("mov %s, %s".formatted(
						getRegSize("CX", value.getReference()),
						getStackPtr(value.getReference(), proc)
					));
				} else {
					throw new RuntimeException();
				}
				
				if (src instanceof InstParam.Num) {
					sb.add("mov %s [RBX + %s], %s".formatted(
						getPtrName(dst.getValueType().getSize()),
						offsetValue,
						srcValue
					));
				} else {
					String regName = getRegSize("AX", getLowerTypeSize(dst.getValueType())); // Size of one lower
					sb.add("mov %s, %s".formatted(
						regName,
						srcValue
					));
					sb.add("mov %s [RBX + %s], %s".formatted(
						getPtrName(dst.getValueType().getSize()),
						offsetValue,
						regName
					));
				}
			}
			case RET -> {
				InstParam param = inst.getParam(0);
				
				if (param instanceof InstParam.Ref src) {
					String regName = getRegSize("AX", src.getReference());
					sb.add("mov %s, %s".formatted(
						regName,
						getStackPtr(src.getReference(), proc)
					));
				} else if (param instanceof InstParam.Num num) {
					sb.add("mov RAX, %s".formatted(num.toString()));
				} else {
					throw new RuntimeException();
				}
				
				sb.add("mov RSP, RBP");
				sb.add("pop RBP");
				sb.add("ret");
			}
			case ADD, SUB, AND, XOR, OR -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef a = inst.getRefParam(1).getReference();
				InstParam b = inst.getParam(2);
				
				String regAName = getRegSize("AX", a);
				sb.add("mov %s, %s".formatted(
					regAName,
					getStackPtr(a, proc)
				));
				
				String type = switch (inst.getOpcode()) {
					case ADD -> "add";
					case SUB -> "sub";
					case AND -> "and";
					case XOR -> "xor";
					case OR -> "or";
					default -> throw new RuntimeException();
				};
				
				if (b instanceof InstParam.Num value) {
					sb.add("%s %s, %s".formatted(
						type,
						regAName,
						value.toString()
					));
				} else if (b instanceof InstParam.Ref value) {
					// TODO: Make sure b and a have the same size
					sb.add("%s %s, %s".formatted(
						type,
						regAName,
						getParamValue(value, proc)
					));
				} else {
					throw new RuntimeException();
				}
				
				sb.add("mov %s, %s".formatted(
					getStackPtr(dst, proc),
					regAName
				));
			}
			case GT, GTE, LT, LTE, EQ, NEQ -> {
				// GT is required to only have references
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef a = inst.getRefParam(1).getReference();
				InstRef b = inst.getRefParam(2).getReference();
				
				String regAName = getRegSize("AX", a);
				String regBName = getRegSize("BX", b);
				String regCName = getRegSize("CX", a);
				sb.add("mov %s, 0".formatted(regAName));
				sb.add("mov %s, 1".formatted(regCName));
				sb.add("mov %s, %s".formatted(
					regBName,
					getStackPtr(b, proc)
				));
				sb.add("cmp %s, %s".formatted(
					getStackPtr(a, proc),
					regBName
				));
				
				String type = switch (inst.getOpcode()) {
					case GT -> "a";
					case LT -> "b";
					case EQ -> "e";
					case GTE -> "ae";
					case LTE -> "be";
					case NEQ -> "ne";
					default -> throw new RuntimeException();
				};
				sb.add("cmov%s %s, %s".formatted(type, regAName, regCName));
				sb.add("mov %s, %s".formatted(
					getStackPtr(dst, proc),
					regAName
				));
			}
			case CALL -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef reference = inst.getRefParam(1).getReference();
				sb.add("call %s".formatted(reference.toSimpleString()));
				
				if (dst.getValueType().getSize() != 0) {
					String regName = getRegSize("AX", dst);
					sb.add("mov %s, %s".formatted(
						getStackPtr(dst, proc),
						regName
					));
				}
			}
			case JNZ -> {
				InstRef src = inst.getRefParam(0).getReference();
				InstRef dst = inst.getRefParam(1).getReference();
				
				String regName = getRegSize("AX", src);
				sb.add("mov %s, %s".formatted(
					getStackPtr(src, proc),
					regName
				));
				sb.add("test %s, %s".formatted(regName, regName));
				sb.add("jnz .%s".formatted(dst.toSimpleString()));
			}
			case JZ -> {
				InstRef src = inst.getRefParam(0).getReference();
				InstRef dst = inst.getRefParam(1).getReference();
				
				String regName = getRegSize("AX", src);
				sb.add("mov %s, %s".formatted(
					getStackPtr(src, proc),
					regName
				));
				sb.add("test %s, %s".formatted(regName, regName));
				sb.add("jz .%s".formatted(dst.toSimpleString()));
			}
			case JMP -> {
				InstRef dst = inst.getRefParam(0).getReference();
				sb.add("jmp .%s".formatted(dst.toSimpleString()));
			}
			default -> {
				sb.add("; NOT IMPLEMENTED");
				sb.add("ud2");
			}
		}
		
		return sb.stream().reduce("", (a, b) -> a + '\n' + b).indent(4).stripTrailing();
	}

	@Override
	public void reset() {

	}
	
	public String getParamValue(InstParam param, AsmProcedure proc) {
		if (param instanceof InstParam.Ref value) {
			return getStackPtr(value.getReference(), proc);
		} else if (param instanceof InstParam.Num value) {
			return value.toString();
		}
		
		throw new RuntimeException();
	}
	
	public String getRawStackPtr(InstRef ref, AsmProcedure proc) {
		return "[RBP - 0x%x]".formatted(
			proc.getStackOffset(ref)
		);
	}
	
	public String getStackPtr(InstRef ref, AsmProcedure proc) {
		return "%s [RBP - 0x%x]".formatted(
			getPtrName(ref),
			proc.getStackOffset(ref)
		);
	}
	
	public String getStackPtrPointer(InstRef ref, AsmProcedure proc) {
		return "%s [RBP - 0x%x]".formatted(
			getPtrName(ref),
			proc.getStackOffset(ref)
		);
	}
	
	public String getPtrName(InstRef ref) {
		return getPtrName(getTypeSize(ref.getValueType()));
	}
	
	public String getPtrName(int size) {
		return switch (size) {
			case 8 -> "byte";
			case 16 -> "word";
			case 32 -> "dword";
			case 64 -> "qword";
			default -> throw new RuntimeException();
		};
	}
	
	private static String getRegSize(String name, InstRef ref) {
		return getRegSize(name, getTypeSize(ref.getValueType()));
	}
	
	private static String getRegSize(String name, int size) {
		return switch (size) {
			case 8 -> name.charAt(0) + "L";
			case 16 -> name;
			case 32 -> "E" + name;
			case 64 -> "R" + name;
			default -> throw new RuntimeException();
		};
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
