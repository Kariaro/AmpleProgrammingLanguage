package me.hardcoded.exporter.ir;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.utils.error.CodeGenException;

public class IRCodeGenerator extends ICodeGenerator {
	
	public IRCodeGenerator(AmpleConfig ampleConfig) {
		super(ampleConfig);
	}
	
	@Override
	public byte[] getBytecode(InstFile program) throws CodeGenException {
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//
//		try {
//			//IRSerializer.write(program, bs);
//		} catch (Exception e) {
//			throw new CodeGenException("Failed to write ir code", e.getCause());
//		}
//
//		return bs.toByteArray();
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getAssembler(InstFile program) throws CodeGenException {
//		return IRPrintUtils.printPretty(program).getBytes();
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {

	}
}
