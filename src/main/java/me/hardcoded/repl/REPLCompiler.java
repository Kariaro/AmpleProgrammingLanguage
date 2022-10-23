package me.hardcoded.repl;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.errors.CompilerException;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.ExportMap;
import me.hardcoded.compiler.intermediate.generator.IntermediateGenerator;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.configuration.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class REPLCompiler {
	private final AmpleConfig ampleConfig;
	private final Set<String> importedPaths;
	private final IntermediateGenerator generator;
	private final IntermediateFile file;
	
	public REPLCompiler(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
		
		// Repl
		this.importedPaths = new HashSet<>();
		this.file = new IntermediateFile();
		this.generator = new IntermediateGenerator(file, new ExportMap());
	}
	
	public void clear() {
		generator.reset();
		importedPaths.clear();
		file.getProcedures().clear();
	}
	
	public IntermediateFile compile(String code) throws CompilerException {
		CompilerConfiguration config = ampleConfig.getConfiguration();
		File workingDir = config.getWorkingDirectory();
		
		List<LinkableObject> objects = new ArrayList<>();
		try {
			LinkableObject first = new AmpleParser(ampleConfig).fromReplBytes("System.in", code.getBytes());
			objects.add(first);
			
			LinkedList<String> importableFiles = new LinkedList<>(first.getImports());
			while (!importableFiles.isEmpty()) {
				File file = new File(workingDir, importableFiles.poll()).getCanonicalFile();
				
				if (importedPaths.add(file.getAbsolutePath())) {
					LinkableObject obj = new AmpleParser(ampleConfig).fromFile(file.getAbsoluteFile());
					importableFiles.addAll(obj.getImports());
					
					objects.add(obj);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Update the export map
		if (!checkImports(generator.getExportMap(), objects)) {
			throw new RuntimeException("Project is not linkable");
		}
		
		for (int i = objects.size() - 1; i >= 0; i--) {
			// Include each linkable object in the intermediate generator
			LinkableObject link = objects.get(i);
			
			//			Path path = link.getFile().toPath();
			//			if (path.isAbsolute()) {
			//				LOGGER.debug(" - {} : {}", link.getChecksum(), ampleConfig.getConfiguration().getWorkingDirectory().toPath().relativize(path));
			//			} else {
			//				LOGGER.debug(" - {} : {}", link.getChecksum(), path);
			//			}
			
			generator.generate(link);
		}
		
		/*
		LOGGER.debug("");
		for (Procedure proc : file.getProcedures()) {
			switch (proc.getType()) {
				case FUNCTION -> LOGGER.debug("# func {}", proc);
				case VARIABLE -> LOGGER.debug("# variable {}", proc);
				default -> LOGGER.debug("# proc = {}", proc.getType());
			}
			
			for (Inst inst : proc.getInstructions()) {
				ISyntaxPos pos = inst.getSyntaxPosition();
				String details = "(line: %3d, column: %3d)".formatted(pos.getStartPosition().line(), pos.getStartPosition().column());
				
				if (inst.getOpcode() == Opcode.LABEL) {
					LOGGER.debug("    {}     {}", details, inst);
				} else {
					LOGGER.debug("        {}     {}", details, inst);
				}
			}
		}
		*/
		
		return file;
	}
	
	private boolean checkImports(ExportMap exportMap, List<LinkableObject> list) throws ParseException {
		for (LinkableObject link : list) {
			if (!exportMap.add(link)) {
				return false;
			}
		}
		
		for (LinkableObject link : list) {
			if (!exportMap.containsThrowErrors(link)) {
				return false;
			}
		}
		
		return true;
	}
}
