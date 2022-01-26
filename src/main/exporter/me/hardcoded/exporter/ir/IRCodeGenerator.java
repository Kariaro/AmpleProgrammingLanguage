package me.hardcoded.exporter.ir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.instruction.IRProgram;
import me.hardcoded.compiler.instruction.IRSerializer;
import me.hardcoded.utils.IRPrintUtils;
import me.hardcoded.utils.error.CodeGenException;

public class IRCodeGenerator implements ICodeGenerator {
	@Override
	public byte[] getBytecode(IRProgram program) throws CodeGenException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		
		try {
			IRSerializer.write(program, bs);
		} catch(IOException e) {
			throw new CodeGenException("Failed to write ir code", e.getCause());
		}
		
		return bs.toByteArray();
	}
	
	@Override
	public byte[] getAssembler(IRProgram program) throws CodeGenException {
		return IRPrintUtils.printPretty(program).getBytes();
	}
	
	@Override
	public void reset() {
		
	}
}
