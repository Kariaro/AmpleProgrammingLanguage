package hardcoded;

import hardcoded.exporter.impl.CodeGeneratorImpl;
import hardcoded.exporter.spooky.SpookyCodeGenerator;
import hardcoded.exporter.x86.AssemblyCodeGenerator;
import hardcoded.exporter.ir.IRCodeGenerator;

public enum OutputFormat {
	SPOOKY(".spook", SpookyCodeGenerator.class),
	X86(".bin", AssemblyCodeGenerator.class),
	IR(".lir", IRCodeGenerator.class),
	
	;
	
	public final Class<? extends CodeGeneratorImpl> generator;
	public final String extension;
	
	private OutputFormat(String extension, Class<? extends CodeGeneratorImpl> generator) {
		this.extension = extension;
		this.generator = generator;
	}
	
	public CodeGeneratorImpl createNew() {
		try {
			return generator.newInstance();
		} catch(InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static OutputFormat get(String name) {
		if(name == null) return IR;
		name = name.trim().toLowerCase();
		
		switch(name) {
			case "spooky": return SPOOKY;
			case "x86": return X86;
			case "ir": return IR;
		}
		
		return null;
	}
}
