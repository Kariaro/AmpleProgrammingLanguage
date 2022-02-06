package me.hardcoded.compiler;

import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.configuration.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AmpleCompiler {
	private final CompilerConfiguration config;
	
	public AmpleCompiler(CompilerConfiguration config) {
		this.config = config;
	}
	
	public void compile() throws IOException {
		File inputFile = config.getSourceFile();
		File workingDir = config.getWorkingDirectory();
		
		Set<String> importedPaths = new HashSet<>();
		importedPaths.add(inputFile.getAbsolutePath());
		LinkableObject main = new AmpleParser().fromFile(inputFile);
		
		LinkedList<String> importableFiles = new LinkedList<>(main.getImports());
		List<LinkableObject> list = new ArrayList<>();
		
		while (!importableFiles.isEmpty()) {
			File file = new File(workingDir, importableFiles.poll());
			
			if (importedPaths.add(file.getAbsolutePath())) {
				list.add(new AmpleParser().fromFile(file));
			}
		}
		
		AmpleLinker linker = new AmpleLinker();
		linker.link(main, list);
	}
}
