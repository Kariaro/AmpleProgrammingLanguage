package me.hardcoded.exporter.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.utils.error.CodeGenException;
import org.apache.logging.log4j.core.jackson.ContextDataAsEntryListDeserializer;

public class AsmCodeGenerator implements ICodeGenerator {
	@Override
	public byte[] getBytecode(InstFile program) throws CodeGenException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getAssembler(InstFile program) throws CodeGenException {
		StringBuilder sb = new StringBuilder();
		
		for (Procedure proc : program.getProcedures()) {
			// sb.append("; PROC = ").append(proc).append('\n');
			
			for (Inst inst : proc.getInstructions()) {
				sb.append(buildInstruction(inst)).append('\n');
			}
		}
		
		return sb.toString().trim().getBytes(StandardCharsets.UTF_8);
	}
	
	private String buildInstruction(Inst inst) throws CodeGenException {
		if (inst.getOpcode() == Opcode.LABLE) {
			InstRef reference = inst.getRefParam(0).getReference();
			if (reference.isFunction()) {
				return reference.toSimpleString() + ':';
			}
			
			return "  ." + reference.toSimpleString() + ':';
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("; ").append(inst).append('\n');
		
		switch (inst.getOpcode()) {
			case MOV -> {
				InstRef dst = inst.getRefParam(0).getReference();
				
				if (inst.getParam(1) instanceof InstParam.Num numRef) {
					sb.append("mov qword ptr [RSP - 0x%s], ".formatted(dst.getId() * 8)).append(numRef);
				}
			}
			case RET -> {
				InstRef reference = inst.getRefParam(0).getReference();
				sb.append("mov RAX, qword ptr [RSP - 0x%x]\n".formatted(reference.getId() * 8));
				sb.append("ret");
			}
			case CALL -> {
				InstRef reference = inst.getRefParam(0).getReference();
				sb.append("call ").append(reference.toSimpleString());
			}
			case JNZ -> {
				InstRef a = inst.getRefParam(0).getReference();
				InstRef b = inst.getRefParam(0).getReference();
				InstRef dst = inst.getRefParam(0).getReference();
				sb.append("test " + a.toSimpleString() + ", " + b.toSimpleString() + "\n");
				sb.append("jnz ").append(dst.toSimpleString());
			}
			case JZ -> {
				InstRef a = inst.getRefParam(0).getReference();
				InstRef b = inst.getRefParam(0).getReference();
				InstRef dst = inst.getRefParam(0).getReference();
				sb.append("test " + a.toSimpleString() + ", " + b.toSimpleString() + "\n");
				sb.append("jz ").append(dst.toSimpleString());
			}
			case JMP -> {
				InstRef dst = inst.getRefParam(0).getReference();
				sb.append("jmp ").append(dst.toSimpleString());
			}
			default -> {
				sb.append("; NOT IMPLEMENTED");
			}
		}
		
		return sb.toString().indent(4).stripTrailing();
	}

	@Override
	public void reset() {

	}
}
