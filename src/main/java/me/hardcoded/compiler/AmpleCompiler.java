package me.hardcoded.compiler;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.impl.ICodeGenerator;
import me.hardcoded.compiler.intermediate.AmpleLinker;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.serial.LinkableDeserializer;
import me.hardcoded.compiler.parser.serial.LinkableSerializer;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.configuration.OutputFormat;
import me.hardcoded.utils.FileUtils;
import me.hardcoded.utils.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

// TODO: Implement REPL (Read Eval Print Loop)
public class AmpleCompiler {
	private static final Logger LOGGER = LogManager.getLogger(AmpleCompiler.class);
	public static final MessageDigest SHA_1_DIGEST;
	
	static {
		MessageDigest shaDigest = null;
		try {
			shaDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Failed to load SHA-1 digest");
			System.exit(0);
		}
		
		SHA_1_DIGEST = shaDigest;
	}
	
	private final AmpleConfig ampleConfig;
	
	public AmpleCompiler(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	private String getCacheFileName(File file) {
		Path relativePath = ampleConfig.getConfiguration()
			.getWorkingDirectory().toPath().relativize(file.toPath());
		
		return "serial_" + relativePath.toString().replaceAll("[\\\\/]", "_") + ".serial";
	}
	
	private LinkableObject parseLinkableObject(File file) {
		Exception cacheFailure = null;
		
		try {
			if (ampleConfig.getConfiguration().useCache()) {
				try {
					File cacheFile = new File(ampleConfig.getConfiguration().getOutputFolder(), getCacheFileName(file));
					if (cacheFile.exists()) {
						LinkableObject obj = LinkableDeserializer.deserializeLinkable(Files.readAllBytes(cacheFile.toPath()));
						if (obj != null && obj.getChecksum().equals(FileUtils.getFileChecksum(SHA_1_DIGEST, file))) {
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
		throw new RuntimeException();
	}
	
	public void compile() throws IOException {
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
		
		if (ampleConfig.getConfiguration().useCache()) {
			LOGGER.info("");
			LOGGER.info("Cache Files:");
			
			for (LinkableObject obj : list) {
				try {
					byte[] bytes = LinkableSerializer.serializeLinkable(obj);
					
					File outputFile = new File(outputFolder, getCacheFileName(obj.getFile()));
					Files.write(outputFile.toPath(), bytes);
					
					LOGGER.info(" - [{}] {}", bytes.length == 1 ? "1 byte" : (bytes.length + " bytes"), outputFile);
					
					// Only run this code if serialization validation is enabled
					LinkableObject loaded = LinkableDeserializer.deserializeLinkable(bytes);
					byte[] recombined = LinkableSerializer.serializeLinkable(loaded);
					
					if (Arrays.compare(bytes, recombined) != 0) {
						LOGGER.error("Serialized data did not match");
						LOGGER.error("({}) became ({}) bytes", bytes.length, recombined.length);
						
						try {
							String a = ObjectUtils.deepPrint(obj, 5);
							String b = ObjectUtils.deepPrint(loaded, 5);
							
							LOGGER.info("{}", a);
							LOGGER.info("{}", b);
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
		//		AmpleInterpreter interpreter = new AmpleInterpreter();
		//		try {
		//			interpreter.runBlocking(file);
		//		} catch (InterruptedException e) {
		//			LOGGER.error("", e);
		//		}
		
		OutputFormat format = ampleConfig.getConfiguration().getOutputFormat();
		ICodeGenerator codeGenerator = format.createNew(ampleConfig);
		
		byte[] bytes = switch (ampleConfig.getConfiguration().getTargetFormat()) {
			case BYTECODE -> codeGenerator.getBytecode(file);
			case ASSEMBLER -> codeGenerator.getAssembler(file);
		};
		
		String path = new File(config.getOutputFolder(), "compile").getAbsolutePath(); //DebugUtils.getNextFileId(config.getOutputFolder(), "compile_%d");
		Files.write(Path.of(path), bytes);
		LOGGER.info("");
		LOGGER.info("{}", path);
		
		//		ampleConfig.getVisualizationHandler()
		//			.addVisualization(SourceCodeVisualization::new, LexerTokenizer.parseKeepWhitespace(
		//				inputFile.getAbsoluteFile(),
		//				Files.readAllBytes(inputFile.toPath())
		//			))
		//			.addVisualization(ParseTreeVisualization::new, list.getFirst().getProgram())
		//			.addVisualization(InstFileVisualization::new, file)
		;
	}
}
