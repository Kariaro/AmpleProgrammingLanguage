package me.hardcoded.repl;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.errors.CompilerException;
import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.configuration.CompilerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

// TODO: Implement REPL (Read Eval Print Loop)
public class REPLCompiler {
	private static final Logger LOGGER = LogManager.getLogger(REPLCompiler.class);
	
	private final AmpleConfig ampleConfig;
	private final List<LinkableObject> previous;
	private final Set<String> importedPaths;
	
	public REPLCompiler(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
		
		// Repl
		this.previous = new ArrayList<>();
		this.importedPaths = new HashSet<>();
	}
	
	public void clear() {
		previous.clear();
		importedPaths.clear();
	}
	
	public IntermediateFile compile(String code) throws CompilerException {
		CompilerConfiguration config = ampleConfig.getConfiguration();
		File workingDir = config.getWorkingDirectory();
		
		// Compile the code
		LinkedList<LinkableObject> list = new LinkedList<>(previous);
		
		try {
			LinkableObject first = new AmpleParser(ampleConfig).fromBytes(new File("", "System.in"), code.getBytes());
			list.add(first);
			
			LinkedList<String> importableFiles = new LinkedList<>(first.getImports());
			
			
			while (!importableFiles.isEmpty()) {
				File file = new File(workingDir, importableFiles.poll()).getCanonicalFile();
				
				if (importedPaths.add(file.getAbsolutePath())) {
					LinkableObject obj = new AmpleParser(ampleConfig).fromFile(file.getAbsoluteFile());
					list.add(obj);
					importableFiles.addAll(obj.getImports());
					
					previous.add(obj);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Combine all linkable objects into one instruction file
		// Link the code
		AmpleLinker linker = new AmpleLinker(ampleConfig);
		IntermediateFile file = linker.link(list);
		
		//		OutputFormat format = ampleConfig.getConfiguration().getOutputFormat();
		//		ICodeGenerator codeGenerator = format.createNew(ampleConfig);
		//
		//		byte[] bytes = switch (ampleConfig.getConfiguration().getTargetFormat()) {
		//			case BYTECODE -> codeGenerator.getBytecode(ampleConfig, file);
		//			case ASSEMBLER -> codeGenerator.getAssembler(ampleConfig, file);
		//		};
		//
		//		if (bytes == null) {
		//			throw new RuntimeException("Failed to generate assembler or bytecode");
		//		}
		//
		//		String path = new File(config.getOutputFolder(), "compile").getAbsolutePath();
		//		try {
		//			Files.write(Path.of(path), bytes);
		//		} catch (IOException e) {
		//			LOGGER.error("", e);
		//		}
		//		LOGGER.info("");
		//		LOGGER.info("{}", path);
		
		return file;
	}
}
