package me.hardcoded.compiler;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.serial.LinkableDeserializer;
import me.hardcoded.compiler.parser.serial.LinkableSerializer;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.exporter.asm.AsmCodeGenerator;
import me.hardcoded.lexer.LexerTokenizer;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.visualization.InstFileVisualization;
import me.hardcoded.visualization.ParseTreeVisualization;
import me.hardcoded.visualization.SourceCodeVisualization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// TODO: Implement REPL (Read Eval Print Loop)
public class AmpleCompiler {
	private final AmpleConfig ampleConfig;
	
	public AmpleCompiler(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	public void compile() throws IOException {
		CompilerConfiguration config = ampleConfig.getConfiguration();
		File inputFile = config.getSourceFile();
		File workingDir = config.getWorkingDirectory();
		
		Set<String> importedPaths = new HashSet<>();
		importedPaths.add(inputFile.getAbsolutePath());
		LinkableObject main = new AmpleParser(ampleConfig).fromFile(inputFile);
		
		LinkedList<String> importableFiles = new LinkedList<>(main.getImports());
		List<LinkableObject> list = new ArrayList<>();
		
		while (!importableFiles.isEmpty()) {
			File file = new File(workingDir, importableFiles.poll());
			
			if (importedPaths.add(file.getAbsolutePath())) {
				list.add(new AmpleParser(ampleConfig).fromFile(file));
			}
		}
		
		{
			List<LinkableObject> allObjects = new ArrayList<>();
			allObjects.add(main);
			allObjects.addAll(list);
			
			try {
				for (LinkableObject obj : allObjects) {
					byte[] bytes = LinkableSerializer.serializeLinkable(obj);
					
					File debugFolder = new File(config.getOutputFolder(), "out_" + obj.getFile().getName() + ".serial");
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
		
		AsmCodeGenerator asmCodeGenerator = new AsmCodeGenerator(ampleConfig);
		byte[] bytes = asmCodeGenerator.getAssembler(file);
		
		String path = DebugUtils.getNextFileId(config.getOutputFolder(), "out_%d.asm");
		Files.write(Path.of(path), bytes);
		
		System.out.println("=".repeat(100));
		System.out.println(new String(bytes));
		System.out.println(path);
		
		ampleConfig.getVisualizationHandler()
			.addVisualization(SourceCodeVisualization::new, LexerTokenizer.parseKeepWhitespace(
				inputFile.getAbsoluteFile(),
				Files.readAllBytes(inputFile.toPath())
			))
			.addVisualization(ParseTreeVisualization::new, main.getProgram())
			.addVisualization(InstFileVisualization::new, file);
	}
}
