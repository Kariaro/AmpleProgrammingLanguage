package me.hardcoded.configuration;

import java.util.function.Supplier;

import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.exporter.chockintosh.ChockintoshCodeGenerator;
import me.hardcoded.exporter.ir.IRCodeGenerator;
import me.hardcoded.exporter.spooky.SpookyCodeGenerator;
import me.hardcoded.exporter.x86.AssemblyCodeGenerator;

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
