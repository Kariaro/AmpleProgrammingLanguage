package hardcoded.exporter.ir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import hardcoded.compiler.impl.ICodeGenerator;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IRSerializer;
import hardcoded.utils.IRPrintUtils;
import hardcoded.utils.error.CodeGenException;

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
