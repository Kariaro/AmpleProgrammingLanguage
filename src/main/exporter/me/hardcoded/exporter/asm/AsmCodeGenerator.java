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
	push rax
	push rbx
	push rcx
	push rdx
	mov rcx, 16
	.loop:
		sub rcx, 1
		mov rbx, rax
		and rbx, 15
		mov bl, byte [rbx + hex_data]
		mov byte [rcx + hex_strs], bl
		shr rax, 4
		cmp rcx, 0
		jg .loop
	mov rax, 1
	lea rsi, [hex_strs]
	mov rdi, 1
	mov rdx, 17
	syscall
	pop rdx
	pop rcx
	pop rbx
	pop rax
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
			case RET -> {
				InstRef src = inst.getRefParam(0).getReference();
				String regName = getRegSize("AX", src);
				sb.add("mov %s, %s".formatted(
					regName,
					getStackPtr(src, proc)
				));
				sb.add("mov RSP, RBP");
				sb.add("pop RBP");
				sb.add("ret");
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
			}
		}
		
		return sb.stream().reduce("", (a, b) -> a + '\n' + b).indent(4).stripTrailing();
	}

	@Override
	public void reset() {

	}
	
	public String getStackPtr(InstRef ref, AsmProcedure proc) {
		return "%s [RBP - 0x%x]".formatted(
			getPtrName(ref),
			proc.getStackOffset(ref)
		);
	}
	
	public String getPtrName(InstRef ref) {
		return switch (ref.getValueType().getSize()) {
			case 8 -> "byte";
			case 16 -> "word";
			case 32 -> "dword";
			case 64 -> "qword";
			default -> throw new RuntimeException();
		};
	}
	
	private static String getRegSize(String name, InstRef ref) {
		return switch (ref.getValueType().getSize()) {
			case 8 -> name.charAt(0) + "L";
			case 16 -> name;
			case 32 -> "E" + name;
			case 64 -> "R" + name;
			default -> throw new RuntimeException();
		};
	}
	public static int getTypeSize(ValueType type) {
		return (type.getDepth() > 0) ? getPointerSize() : (type.getSize() >> 3);
	}
	
	// TODO: Read this from some config
	public static int getPointerSize() {
		return 8;
	}
}
