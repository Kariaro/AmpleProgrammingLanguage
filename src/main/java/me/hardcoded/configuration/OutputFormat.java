package me.hardcoded.configuration;

import java.util.function.Supplier;

import me.hardcoded.compiler.impl.ICodeGenerator;

public enum OutputFormat {
	IR(".lir", null), //, IRCodeGenerator::new),
	;
	
	private final Supplier<? extends ICodeGenerator> generator;
	private final String extension;
	
	OutputFormat(String extension, Supplier<? extends ICodeGenerator> generator) {
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
