package hardcoded.configuration;

import java.lang.reflect.InvocationTargetException;

import hardcoded.compiler.impl.ICodeGenerator;
import hardcoded.exporter.chockintosh.ChockintoshCodeGenerator;
import hardcoded.exporter.spooky.SpookyCodeGenerator;
import hardcoded.exporter.x86.AssemblyCodeGenerator;
import hardcoded.exporter.ir.IRCodeGenerator;

public enum OutputFormat {
	SPOOKY(".spook", SpookyCodeGenerator.class),
	X86(".bin", AssemblyCodeGenerator.class),
	IR(".lir", IRCodeGenerator.class),
	CHOCKINTOSH(".chock", ChockintoshCodeGenerator.class)
	
	;
	
	public final Class<? extends ICodeGenerator> generator;
	public final String extension;
	
	private OutputFormat(String extension, Class<? extends ICodeGenerator> generator) {
		this.extension = extension;
		this.generator = generator;
	}
	
	public ICodeGenerator createNew() {
		try {
			return generator.getDeclaredConstructor().newInstance();
		} catch(InstantiationException | IllegalAccessException
			| IllegalArgumentException | InvocationTargetException
			| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static OutputFormat get(String name) {
		return OutputFormat.valueOf(name.toUpperCase());
	}
}
