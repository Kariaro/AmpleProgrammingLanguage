package me.hardcoded.compiler;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.serial.LinkableDeserializer;
import me.hardcoded.compiler.parser.serial.LinkableSerializer;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.configuration.OutputFormat;
import me.hardcoded.interpreter.AmpleInterpreter;
import me.hardcoded.lexer.LexerTokenizer;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.visualization.InstFileVisualization;
import me.hardcoded.visualization.ParseTreeVisualization;
import me.hardcoded.visualization.SourceCodeVisualization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

// TODO: Implement REPL (Read Eval Print Loop)
public class AmpleCompiler {
	private static final Logger LOGGER = LogManager.getLogger(AmpleCompiler.class);
	private final AmpleConfig ampleConfig;
	
	public AmpleCompiler(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	public void compile() throws IOException {
		CompilerConfiguration config = ampleConfig.getConfiguration();
		File inputFile = config.getSourceFile();
		File outputFolder = config.getOutputFolder();
		File workingDir = config.getWorkingDirectory();
		
		//		// Clean the output folder
		//		{
		//			// TODO: Make sure the output folder is not the same as the source folder
		//			File[] files = outputFolder.listFiles((dir, name) -> !name.startsWith("."));
		//			if (files != null) {
		//				Arrays.stream(files).forEach(File::delete);
		//			}
		//		}
		
		LinkedList<String> importableFiles = new LinkedList<>();
		importableFiles.add(workingDir.toPath().relativize(inputFile.toPath()).toString());
		
		LinkedList<LinkableObject> list = new LinkedList<>();
		{
			Set<String> importedPaths = new HashSet<>();
			
			while (!importableFiles.isEmpty()) {
				File file = new File(workingDir, importableFiles.poll());
				
				if (importedPaths.add(file.getAbsolutePath())) {
					list.add(new AmpleParser(ampleConfig).fromFile(file));
				}
			}
		}
		
		try {
			for (LinkableObject obj : list) {
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
		
		// Combine all linkable objects into one instruction file
		AmpleLinker linker = new AmpleLinker();
		IntermediateFile file = linker.link(list);
		
		AmpleInterpreter interpreter = new AmpleInterpreter();
		try {
			interpreter.runBlocking(file);
			System.exit(0);
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		OutputFormat format = ampleConfig.getConfiguration().getOutputFormat();
		ICodeGenerator codeGenerator = format.createNew(ampleConfig);
		
		byte[] bytes = switch (ampleConfig.getConfiguration().getTargetFormat()) {
			case BYTECODE -> codeGenerator.getBytecode(file);
			case ASSEMBLER -> codeGenerator.getAssembler(file);
		};
		
		String path = DebugUtils.getNextFileId(config.getOutputFolder(), "compile_%d");
		Files.write(Path.of(path), bytes);
		LOGGER.info("=".repeat(100));
		LOGGER.info("{}", path);
		
		ampleConfig.getVisualizationHandler()
			.addVisualization(SourceCodeVisualization::new, LexerTokenizer.parseKeepWhitespace(
				inputFile.getAbsoluteFile(),
				Files.readAllBytes(inputFile.toPath())
			))
			.addVisualization(ParseTreeVisualization::new, list.getFirst().getProgram())
			.addVisualization(InstFileVisualization::new, file);
	}
}
