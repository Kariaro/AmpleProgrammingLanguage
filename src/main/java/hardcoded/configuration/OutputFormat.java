package hardcoded.configuration;

import java.util.function.Supplier;

import hardcoded.compiler.impl.ICodeGenerator;
import hardcoded.exporter.chockintosh.ChockintoshCodeGenerator;
import hardcoded.exporter.ir.IRCodeGenerator;
import hardcoded.exporter.spooky.SpookyCodeGenerator;
import hardcoded.exporter.x86.AssemblyCodeGenerator;

public enum OutputFormat {
	SPOOKY(".spook", SpookyCodeGenerator::new),
	X86(".bin", AssemblyCodeGenerator::new),
	IR(".lir", IRCodeGenerator::new),
	CHOCKINTOSH(".chock", ChockintoshCodeGenerator::new)
	;
	
	private final Supplier<? extends ICodeGenerator> generator;
	private final String extension;
	
	private OutputFormat(String extension, Supplier<? extends ICodeGenerator> generator) {
		this.extension = extension;
		this.generator = generator;
	}
	
	public ICodeGenerator createNew() {
		return generator.get();
	}
	
	public String getExtension() {
		return extension;
	}
	
	public static OutputFormat get(String name) {
		return OutputFormat.valueOf(name.toUpperCase());
	}
}
