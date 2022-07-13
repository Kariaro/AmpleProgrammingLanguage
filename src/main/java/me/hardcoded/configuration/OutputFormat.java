package me.hardcoded.configuration;

import java.util.function.Function;
import java.util.function.Supplier;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.exporter.asm.AsmCodeGenerator;
import me.hardcoded.exporter.ir.IRCodeGenerator;

public enum OutputFormat {
	IR(".lir", IRCodeGenerator::new),
	ASM(".asm", AsmCodeGenerator::new)
	;
	
	private final Function<AmpleConfig, ? extends ICodeGenerator> generator;
	private final String extension;
	
	OutputFormat(String extension, Function<AmpleConfig, ? extends ICodeGenerator> generator) {
		this.extension = extension;
		this.generator = generator;
	}
	
	public ICodeGenerator createNew(AmpleConfig config) {
		return generator.apply(config);
	}
	
	public String getExtension() {
		return extension;
	}
	
	public static OutputFormat get(String name) {
		return OutputFormat.valueOf(name.toUpperCase());
	}
}
