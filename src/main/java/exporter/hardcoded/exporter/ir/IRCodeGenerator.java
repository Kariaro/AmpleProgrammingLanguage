package hardcoded.exporter.ir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IRSerializer;
import hardcoded.exporter.impl.CodeGenException;
import hardcoded.exporter.impl.CodeGeneratorImpl;

public class IRCodeGenerator implements CodeGeneratorImpl {
	public byte[] generate(IRProgram program) throws CodeGenException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		
		try {
			IRSerializer.write(program, bs);
		} catch(IOException e) {
			throw new CodeGenException("Failed to write ir code", e.getCause());
		}
		
		return bs.toByteArray();
	}
}
