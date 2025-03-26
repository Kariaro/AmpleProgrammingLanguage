package me.hardcoded.compiler;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.errors.CompilerException;
import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.serial.LinkableStream;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.configuration.OutputFormat;
import me.hardcoded.interpreter.AmpleRunner;
import me.hardcoded.lexer.LexerTokenizer;
import me.hardcoded.utils.AmpleCache;
import me.hardcoded.utils.ObjectUtils;
import me.hardcoded.visualization.InstFileVisualization;
import me.hardcoded.visualization.ParseTreeVisualization;
import me.hardcoded.visualization.SourceCodeVisualization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class AmpleCompiler {
	private static final Logger LOGGER = LogManager.getLogger(AmpleCompiler.class);
	
	private final AmpleConfig ampleConfig;
	
	public AmpleCompiler(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	private LinkableObject parseLinkableObject(File file) {
		Exception cacheFailure = null;
		
		try {
			if (ampleConfig.getConfiguration().useCache()) {
				try {
					File cacheFile = new File(ampleConfig.getConfiguration().getOutputFolder(), AmpleCache.getCacheFileName(ampleConfig, file, ""));
					if (cacheFile.exists()) {
						LinkableObject obj = LinkableStream.deserializeLinkable(Files.readAllBytes(cacheFile.toPath()));
						if (obj != null && obj.getChecksum().equals(AmpleCache.getFileChecksum(file))) {
							LOGGER.info(" - [CACHE] {}", file);
							return obj;
						}
					}
				} catch (Exception e) {
					// If the next step does not fail we continue
					cacheFailure = e;
				}
			}
			
			LinkableObject obj = new AmpleParser(ampleConfig).fromFile(file);
			if (cacheFailure != null) {
				LOGGER.warn(" - [SUCCESS - CACHE FAILED] {}", file, cacheFailure);
			} else {
				LOGGER.info(" - [SUCCESS] {}", file);
			}
			
			return obj;
		} catch (Exception e) {
			if (cacheFailure != null) {
				LOGGER.error(" - [FAILURE] {}", file, cacheFailure);
			}
			
			LOGGER.error(" - [FAILURE] {}", file, e);
		}
		
		// Close the application
		throw new RuntimeException("Failed to load file");
	}
	
	public void compile() throws IOException, CompilerException {
		CompilerConfiguration config = ampleConfig.getConfiguration();
		File inputFile = config.getSourceFile();
		File outputFolder = config.getOutputFolder();
		File workingDir = config.getWorkingDirectory();
		
		LinkedList<LinkableObject> list = new LinkedList<>();
		
		LOGGER.info("");
		LOGGER.info("Import Files:");
		{
			LinkedList<String> importableFiles = new LinkedList<>();
			importableFiles.add(workingDir.toPath().relativize(inputFile.toPath()).toString());
			
			Set<String> importedPaths = new HashSet<>();
			
			while (!importableFiles.isEmpty()) {
				File file = new File(workingDir, importableFiles.poll()).getCanonicalFile();
				
				if (importedPaths.add(file.getAbsolutePath())) {
					LinkableObject obj = parseLinkableObject(file);
					list.add(obj);
					importableFiles.addAll(obj.getImports());
				}
			}
		}
		
		// To make sure that each run tests the cache system we should always run this
		// if (ampleConfig.getConfiguration().useCache())
		{
			LOGGER.info("");
			LOGGER.info("Cache Files:");
			
			for (LinkableObject obj : list) {
				try {
					byte[] bytes = LinkableStream.serializeLinkable(obj);
					
					File outputFile = new File(outputFolder, AmpleCache.getCacheFileName(ampleConfig, obj.getFile(), ""));
					Files.write(outputFile.toPath(), bytes);
					
					LOGGER.info(" - [{}] {}", bytes.length == 1 ? "1 byte" : (bytes.length + " bytes"), outputFile);
					
					// Only run this code if serialization validation is enabled
					LinkableObject loaded = LinkableStream.deserializeLinkable(bytes);
					byte[] recombined = LinkableStream.serializeLinkable(loaded);
					
					
					File outputFile2 = new File(outputFolder, AmpleCache.getCacheFileName(ampleConfig, obj.getFile(), "_tmp"));
					Files.write(outputFile2.toPath(), recombined);
					
					if (Arrays.compare(bytes, recombined) != 0) {
						LOGGER.error("Serialized data did not match");
						LOGGER.error("({}) became ({}) bytes", bytes.length, recombined.length);
						
						try {
							final int depth = 5;
							String a = ObjectUtils.deepPrint(obj, depth);
							String b = ObjectUtils.deepPrint(loaded, depth);
							String c = ObjectUtils.diffString(a, b);
							
							LOGGER.info("{}", c);
						} catch (Exception e) {
							LOGGER.error("", e);
						}
						
						throw new RuntimeException("Linkable serializer did not match");
					}
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
		}
		
		// Combine all linkable objects into one instruction file
		AmpleLinker linker = new AmpleLinker(ampleConfig);
		IntermediateFile file = linker.link(list);
		
		try {
			AmpleRunner runner = new AmpleRunner();
			runner.run(file);
			//			interpreter.runBlocking(file);
		} catch (Exception e) {
			LOGGER.error("", e);
		} finally {
			//System.exit(0);
		}
		
		OutputFormat format = ampleConfig.getConfiguration().getOutputFormat();
		
		if (true) {
			return;
		}
		
		ICodeGenerator codeGenerator = format.createNew(ampleConfig);
		
		byte[] bytes = switch (ampleConfig.getConfiguration().getTargetFormat()) {
			case BYTECODE -> codeGenerator.getBytecode(ampleConfig, file);
			case ASSEMBLER -> codeGenerator.getAssembler(ampleConfig, file);
		};
		
		if (bytes == null) {
			throw new RuntimeException("Failed to generate assembler or bytecode");
		}
		
		String path = new File(config.getOutputFolder(), "compile").getAbsolutePath();
		try {
			Files.write(Path.of(path), bytes);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		LOGGER.info("");
		LOGGER.info("{}", path);
		
		ampleConfig.getVisualizationHandler()
			.addVisualization(SourceCodeVisualization::new, LexerTokenizer.parseKeepWhitespace(
				inputFile.getAbsolutePath(),
				Files.readAllBytes(inputFile.toPath())
			))
			.addVisualization(ParseTreeVisualization::new, list.getFirst().getProgram())
			.addVisualization(InstFileVisualization::new, file);
	}
}
