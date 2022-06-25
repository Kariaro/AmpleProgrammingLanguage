package me.hardcoded.compiler;

//import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.serial.LinkableDeserializer;
import me.hardcoded.compiler.parser.serial.LinkableSerializer;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.exporter.asm.AsmCodeGenerator;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.visualization.InstFileVisualization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
		
		{
			List<LinkableObject> allObjects = new ArrayList<>();
			allObjects.add(main);
			allObjects.addAll(list);
			
			try {
				for (LinkableObject obj : allObjects) {
					byte[] bytes = LinkableSerializer.serializeLinkable(obj);
					
					File debugFolder = new File("debug/out_" + obj.getFile().getName() + ".serial");
					if (debugFolder.exists() || debugFolder.createNewFile()) {
						try (FileOutputStream out = new FileOutputStream(debugFolder)) {
							out.write(bytes == null ? new byte[0] : bytes);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					byte[] readBytes = Files.readAllBytes(debugFolder.toPath());
					LinkableObject loaded = LinkableDeserializer.deserializeLinkable(readBytes);
					byte[] recombined = LinkableSerializer.serializeLinkable(loaded);
					
					if (Arrays.compare(bytes, recombined) != 0) {
						throw new ParseException("Linkable serializer did not match");
					}
					
					// System.out.println(ParseUtil.stat(loaded.getProgram()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		AmpleLinker linker = new AmpleLinker();
		InstFile file = linker.link(main, list);
		
		AsmCodeGenerator asmCodeGenerator = new AsmCodeGenerator();
		byte[] bytes = asmCodeGenerator.getAssembler(file);
		
		String path = DebugUtils.getNextFileId(new File("debug"), "out_%d.asm");
		Files.write(Path.of(path), bytes);
		
		
		System.out.println("=".repeat(100));
		System.out.println(new String(bytes));
		System.out.println(path);
		
		new InstFileVisualization().show(file);
	}
}
