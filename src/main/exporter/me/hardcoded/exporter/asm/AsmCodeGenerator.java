package me.hardcoded.exporter.asm;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.error.CodeGenException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsmCodeGenerator extends ICodeGenerator {
	private static final Logger LOGGER = LogManager.getLogger(AsmCodeGenerator.class);
	private static final boolean DEBUG = true;
	
	public AsmCodeGenerator(AmpleConfig ampleConfig) {
		super(ampleConfig);
	}
	
	static class AsmContext {
		public final Map<String, byte[]> globalStrings;
		public final Map<String, String> labelStrings;
		public final ElfHeader2 elfHeader;
		
		AsmContext() {
			this.globalStrings = new LinkedHashMap<>();
			this.labelStrings = new LinkedHashMap<>();
			this.elfHeader = new ElfHeader2(DEBUG, "");
		}
		
		public String addGlobalString(byte[] content) {
			int index = globalStrings.size();
			String name = "global_string_" + index;
			globalStrings.put(name, content);
			return name;
		}
		
		public void addLabelString(String label, String name) {
			labelStrings.put(label, name);
		}
	}
	
	@Override
	public byte[] getBytecode(IntermediateFile program) throws CodeGenException {
		byte[] assembler = getAssembler(program);
		return NasmUtils.compile(ampleConfig, assembler);
	}
	
	@Override
	public byte[] getAssembler(IntermediateFile program) throws CodeGenException {
		StringBuilder sb = new StringBuilder();
		
		AsmContext context = new AsmContext();
		InstRef main = null;
		for (Procedure proc : program.getProcedures()) {
			switch (proc.getType()) {
				case FUNCTION -> {
					AsmProcedure asmProc = new AsmProcedure(proc);
					
					InstRef test = proc.getReference();
					sb.append("; %s %s (%s)\n".formatted(test.getValueType(), test, proc.getParameters()));
					
					if (test.getName().equals("main")) {
						main = test;
					}
					
					for (Inst inst : proc.getInstructions()) {
						sb.append(buildInstruction(context, asmProc, inst)).append('\n');
					}
				}
				
				case VARIABLE -> {
					// TODO: Global variables should be saved differently from instructions
					AsmProcedure asmProc = new AsmProcedure(proc);
					
					InstRef test = proc.getReference();
					sb.append("; %s %s\n".formatted(test.getValueType(), test));
					
					for (Inst inst : proc.getInstructions()) {
						sb.append(buildInstruction(context, asmProc, inst)).append('\n');
					}
				}
				
				default -> {
					LOGGER.error("{} procedures has not been implemented", proc.getType());
				}
			}
		}
		
		if (main == null) {
			throw new RuntimeException("Main function was undefined");
		}
		
		{
			StringBuilder header = new StringBuilder();
			context.elfHeader.appendHeader(header);
			context.elfHeader.appendSectionText(header,
				"    call %s\n".formatted(main.toSimpleString()) +
					"    mov rdi, rax\n" +
					"    mov rax, 60\n" +
					"    syscall\n" +
					"\n", sb.toString());
			context.elfHeader.appendSectionData(header, context);
			context.elfHeader.appendSectionSections(header);
			context.elfHeader.appendSectionDebug(header, context);
			
			sb = header;
		}
		
		return sb.toString().trim().getBytes(StandardCharsets.UTF_8);
	}
	
	private String buildInstruction(AsmContext context, AsmProcedure proc, Inst inst) throws CodeGenException {
		if (inst.getOpcode() == Opcode.LABEL) {
			InstRef reference = inst.getRefParam(0).getReference();
			if (reference.isFunction()) {
				List<String> sb = new ArrayList<>();
				sb.add("push RBP");
				sb.add("mov RBP, RSP");
				sb.add("sub RSP, 0x%x".formatted(proc.getStackSize()));
				sb.add("");
				
				int offset = 16;
				for (int i = 0; i < proc.getParamCount(); i++) {
					InstRef param = proc.getParam(i);
					
					int size = AsmUtils.getTypeSize(param.getValueType());
					String regName = AsmUtils.getRegSize("AX", param);
					
					if (param.getValueType().isVarargs()) {
						sb.add("lea %s, [RBP + 0x%x]".formatted(
							regName,
							offset
						));
					} else {
						sb.add("mov %s, %s [RBP + 0x%x]".formatted(
							regName,
							AsmUtils.getPointerName(size),
							offset
						));
					}
					sb.add("mov %s, %s".formatted(
						AsmUtils.getParamValue(param, proc),
						regName
					));
					
					offset += (size >> 3);
				}
				
				context.addLabelString(reference.toSimpleString(), reference.getPath());
				String label = reference.toSimpleString() + ":\n";
				return label + sb.stream().reduce("", (a, b) -> a + '\n' + b).indent(4).stripTrailing().replaceFirst("    \n", "");
			}
			
			context.addLabelString(proc.getName() + "." + reference.toSimpleString(), "." + reference.toSimpleString());
			return "  ." + reference.toSimpleString() + ':';
		}
		
		List<String> sb = new ArrayList<>();
		sb.add("; %s".formatted(inst));
		
		switch (inst.getOpcode()) {
			case STACK_ALLOC -> {
				InstRef dst = inst.getRefParam(0).getReference();
				int size = Integer.parseInt(inst.getNumParam(1).toString());
				
				String regName = AsmUtils.getRegSize("AX", dst);
				sb.add("lea %s, %s".formatted(
					regName,
					AsmUtils.getRawStackPtr(dst, -size, proc)
				));
				sb.add("mov %s, %s".formatted(
					AsmUtils.getStackPtr(dst, proc),
					regName
				));
			}
			case INLINE_ASM -> {
				String targetType = inst.getStrParam(0).getValue();
				
				// We only inline assembly instructions
				if (!targetType.equals("asm")) {
					return "";
				}
				
				String command = inst.getStrParam(1).getValue();
				for (int i = 2; i < inst.getParamCount(); i++) {
					InstRef src = inst.getRefParam(i).getReference();
					command = command.replaceFirst("\\{\\}", AsmUtils.getStackPtr(src, proc));
				}
				
				sb.add(command);
			}
			case MOV -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstParam src = inst.getParam(1);
				
				if (src instanceof InstParam.Num value) {
					long number = value.getValue();
					
					String regName;
					if ((number >>> 32) != 0) {
						sb.add("mov RAX, %s".formatted(value));
						regName = "RAX";
					} else {
						// TODO: FIXME
						regName = value.toString();
					}
					
					sb.add("mov %s, %s".formatted(
						AsmUtils.getStackPtr(dst, proc),
						regName
					));
				} else if (src instanceof InstParam.Ref value) {
					String regName = AsmUtils.getRegSize("AX", value.getReference());
					sb.add("mov %s, %s".formatted(
						regName,
						AsmUtils.getStackPtr(value.getReference(), proc)
					));
					sb.add("mov %s, %s".formatted(
						AsmUtils.getStackPtr(dst, proc),
						regName
					));
				} else if (src instanceof InstParam.Str value) {
					String name = context.addGlobalString(value.getValue().getBytes());
					sb.add("mov %s, %s".formatted(
						AsmUtils.getStackPtr(dst, proc),
						name
					));
				}
			}
			case LOAD -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef src = inst.getRefParam(1).getReference();
				InstParam offset = inst.getParam(2);
				
				String offsetValue;
				if (offset instanceof InstParam.Num value) {
					offsetValue = "0x%x".formatted(value.getValue() * AsmUtils.getLowerTypeByteSize(src.getValueType()));
				} else if (offset instanceof InstParam.Ref value) {
					int offsetSize = AsmUtils.getLowerTypeByteSize(src.getValueType());
					if (offsetSize > 8) {
						throw new RuntimeException();
					}
					
					offsetValue = "RCX * 0x%x".formatted(offsetSize);
					sb.add("xor RCX, RCX");
					sb.add("mov %s, %s".formatted(
						AsmUtils.getRegSize("CX", value.getReference()),
						AsmUtils.getStackPtr(value.getReference(), proc)
					));
				} else {
					throw new RuntimeException();
				}
				
				String regName = AsmUtils.getRegSize("AX", dst);
				sb.add("mov RBX, %s".formatted(
					AsmUtils.getParamValue(src, proc)
				));
				sb.add("mov %s, %s [RBX + %s]".formatted(
					regName,
					AsmUtils.getPointerName(dst),
					offsetValue
				));
				sb.add("mov %s, %s".formatted(
					AsmUtils.getParamValue(dst, proc),
					regName
				));
			}
			case STORE -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstParam offset = inst.getParam(1);
				InstParam src = inst.getParam(2);
				
				String srcValue = AsmUtils.getParamValue(src, proc);
				sb.add("mov RBX, %s".formatted(
					AsmUtils.getStackPtr(dst, proc)
				));
				
				String offsetValue;
				if (offset instanceof InstParam.Num value) {
					offsetValue = "0x%x".formatted(value.getValue() * AsmUtils.getLowerTypeByteSize(dst.getValueType()));
				} else if (offset instanceof InstParam.Ref value) {
					int offsetSize = AsmUtils.getLowerTypeByteSize(dst.getValueType());
					if (offsetSize > 8) {
						System.out.println(offsetSize);
						throw new RuntimeException();
					}
					
					offsetValue = "RCX * 0x%x".formatted(offsetSize);
					sb.add("xor RCX, RCX");
					sb.add("mov %s, %s".formatted(
						AsmUtils.getRegSize("CX", value.getReference()),
						AsmUtils.getStackPtr(value.getReference(), proc)
					));
				} else {
					throw new RuntimeException();
				}
				
				if (src instanceof InstParam.Num) {
					sb.add("mov %s [RBX + %s], %s".formatted(
						AsmUtils.getPointerName(dst.getValueType().getSize()),
						offsetValue,
						srcValue
					));
				} else {
					String regName = AsmUtils.getRegSize("AX", AsmUtils.getLowerTypeSize(dst.getValueType())); // Size of one lower
					sb.add("mov %s, %s".formatted(
						regName,
						srcValue
					));
					sb.add("mov %s [RBX + %s], %s".formatted(
						AsmUtils.getPointerName(dst.getValueType().getSize()),
						offsetValue,
						regName
					));
				}
			}
			case CAST -> {
				InstParam dst = inst.getParam(0);
				InstParam src = inst.getParam(2);
				
				String regSrcName = AsmUtils.getRegSize("AX", src);
				String regDstName = AsmUtils.getRegSize("AX", dst);
				sb.add("xor RAX, RAX");
				sb.add("mov %s, %s".formatted(
					regSrcName,
					AsmUtils.getParamValue(src, proc)
				));
				sb.add("mov %s, %s".formatted(
					AsmUtils.getParamValue(dst, proc),
					regDstName
				));
			}
			case RET -> {
				if (inst.getParamCount() == 1) {
					InstParam param = inst.getParam(0);
					
					if (param instanceof InstParam.Ref src) {
						String regName = AsmUtils.getRegSize("AX", src.getReference());
						sb.add("mov %s, %s".formatted(
							regName,
							AsmUtils.getStackPtr(src.getReference(), proc)
						));
					} else if (param instanceof InstParam.Num num) {
						sb.add("mov RAX, %s".formatted(num.toString()));
					} else {
						throw new RuntimeException();
					}
				}
				
				sb.add("mov RSP, RBP");
				sb.add("pop RBP");
				sb.add("ret");
			}
			case ADD, SUB, AND, XOR, OR -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef a = inst.getRefParam(1).getReference();
				InstParam b = inst.getParam(2);
				
				String regAName = AsmUtils.getRegSize("AX", a);
				sb.add("mov %s, %s".formatted(
					regAName,
					AsmUtils.getStackPtr(a, proc)
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
						AsmUtils.getParamValue(value, proc)
					));
				} else {
					throw new RuntimeException();
				}
				
				sb.add("mov %s, %s".formatted(
					AsmUtils.getStackPtr(dst, proc),
					regAName
				));
			}
			case SHR, SHL -> {
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef a = inst.getRefParam(1).getReference();
				InstParam b = inst.getParam(2);
				
				String regAName = AsmUtils.getRegSize("AX", a);
				String regCName = AsmUtils.getRegSize("CX", b);
				sb.add("xor RCX, RCX");
				sb.add("mov %s, %s".formatted(
					regCName,
					AsmUtils.getParamValue(b, proc)
				));
				sb.add("mov %s, %s".formatted(
					regAName,
					AsmUtils.getParamValue(a, proc)
				));
				
				// TODO: Signed and unsigned shifts
				String type = switch (inst.getOpcode()) {
					case SHR -> "shr";
					case SHL -> "shl";
					default -> throw new RuntimeException();
				};
				sb.add("%s %s, CL".formatted(type, regAName));
				sb.add("mov %s, %s".formatted(
					AsmUtils.getParamValue(dst, proc),
					regAName
				));
			}
			case GT, GTE, LT, LTE, EQ, NEQ -> {
				// GT is required to only have references
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef a = inst.getRefParam(1).getReference();
				InstRef b = inst.getRefParam(2).getReference();
				
				String regAName = AsmUtils.getRegSize("AX", a);
				String regBName = AsmUtils.getRegSize("BX", b);
				String regACmov = AsmUtils.getRegSize("AX", a);
				String regCCmov = AsmUtils.getRegSize("CX", a);
				
				boolean isUnsigned = a.getValueType().isUnsigned();
				if (AsmUtils.getTypeByteSize(a.getValueType()) == 1) {
					regACmov = AsmUtils.getRegSize("AX", 16);
					regCCmov = AsmUtils.getRegSize("CX", 16);
				}
				
				sb.add("mov RCX, 1");
				sb.add("mov RAX, 0");
				sb.add("mov %s, %s".formatted(
					regBName,
					AsmUtils.getParamValue(b, proc)
				));
				sb.add("cmp %s, %s".formatted(
					AsmUtils.getStackPtr(a, proc),
					regBName
				));
				
				String type = switch (inst.getOpcode()) {
					case GT -> isUnsigned ? "a" : "g";
					case LT -> isUnsigned ? "b" : "l";
					case EQ -> "e";
					case GTE -> isUnsigned ? "ae" : "ge";
					case LTE -> isUnsigned ? "be" : "le";
					case NEQ -> "ne";
					default -> throw new RuntimeException();
				};
				sb.add("cmov%s %s, %s".formatted(type, regACmov, regCCmov));
				sb.add("mov %s, %s".formatted(
					AsmUtils.getStackPtr(dst, proc),
					regAName
				));
			}
			case CALL -> {
				// All call follow the 'cdecl' standard
				
				InstRef dst = inst.getRefParam(0).getReference();
				InstRef fun = inst.getRefParam(1).getReference();
				
				int offset = 0;
				for (int i = 2; i < inst.getParamCount(); i++) {
					InstParam param = inst.getParam(i);
					int size = AsmUtils.getTypeSize(param.getSize());
					offset += (size >> 3);
				}
				
				if (offset != 0) {
					sb.add("sub RSP, 0x%x".formatted(offset));
				}
				
				offset = 0;
				for (int i = 2; i < inst.getParamCount(); i++) {
					InstParam param = inst.getParam(i);
					int size = AsmUtils.getTypeSize(param.getSize());
					String regName = AsmUtils.getRegSize("AX", param);
					
					sb.add("mov %s, %s".formatted(
						regName,
						AsmUtils.getParamValue(param, proc)
					));
					sb.add("mov %s [RSP + 0x%x], %s".formatted(
						AsmUtils.getPointerName(size),
						offset,
						regName
					));
					
					offset += (size >> 3);
				}
				sb.add("call %s".formatted(fun.toSimpleString()));
				
				if (offset != 0) {
					sb.add("add RSP, 0x%x".formatted(offset));
				}
				
				if (dst.getValueType().getSize() != 0) {
					String regName = AsmUtils.getRegSize("AX", dst);
					sb.add("mov %s, %s".formatted(
						AsmUtils.getStackPtr(dst, proc),
						regName
					));
				}
			}
			case JNZ -> {
				InstRef src = inst.getRefParam(0).getReference();
				InstRef dst = inst.getRefParam(1).getReference();
				
				String regName = AsmUtils.getRegSize("AX", src);
				sb.add("mov %s, %s".formatted(
					AsmUtils.getStackPtr(src, proc),
					regName
				));
				sb.add("test %s, %s".formatted(regName, regName));
				sb.add("jnz .%s".formatted(dst.toSimpleString()));
			}
			case JZ -> {
				InstRef src = inst.getRefParam(0).getReference();
				InstRef dst = inst.getRefParam(1).getReference();
				
				String regName = AsmUtils.getRegSize("AX", src);
				sb.add("mov %s, %s".formatted(
					AsmUtils.getStackPtr(src, proc),
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
