package me.hardcoded.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import me.hardcoded.compiler.AmpleCompiler;
import me.hardcoded.compiler.parser.AmpleParser;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.serial.LinkableDeserializer;
import me.hardcoded.compiler.parser.serial.LinkableSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.configuration.CompilerConfiguration.Operation;
import me.hardcoded.configuration.OutputFormat;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.utils.FileUtils;

/**
 * This is the main entry point for the compiler.<br>
 * This compiler is a multi-stage compiler.<br>
 * 
 * 1. Generate a token list from an input.<br>
 * 2. Generate a basic parse tree.<br>
 * 3. Optimize the parse tree.<br>
 * 4. Generate the reduced instruction language.<br>
 * 5. Optimize the reduced instruction language.<br>
 * 6. Export the language with a code generator.<br>
 * 
 * @author HardCoded
 */
public class Main {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	
	static {
		Locale.setDefault(Locale.US);
	}
	
	private static void printHelpMessage() {
		try {
			LOGGER.info("\n{}", new String(FileUtils.readInputStream(Main.class.getResourceAsStream("/command/help.txt"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (!DebugUtils.isDeveloper() && args.length < 1) {
			printHelpMessage();
			return;
		}
		
		if (DebugUtils.isDeveloper()) {
			String file = "link.ample";
			File dir = new File("src/main/resources/test/ample/");
			
			String workingDirectory = new File("src/main/resources/project").getAbsolutePath();
			String inputFile = "src/" + file;
			String outputFile = DebugUtils.getNextFileId(dir, "compiled_%d.txt");
			
			args = new String[] {
				"--working-directory", workingDirectory,
				"--source-folders", "src",
				"--format", OutputFormat.IR.toString(),
				"--input-file", inputFile,
				"--output-file", outputFile,
				"--compile",
				"--assembler"
			};
		}
		
		CompilerConfiguration config = CompilerConfiguration.parseArgs(args);
		
		try {
			AmpleCompiler compiler = new AmpleCompiler(config);
			
			compiler.compile();
//			AmpleParser parser = new AmpleParser();
//			LinkableObject obj = parser.fromFile(config.getSourceFile());
//
//			File wd = config.getWorkingDirectory();
//
//
//			byte[] bytes = LinkableSerializer.serializeLinkable(obj);
//
//			File debugFolder = new File("debug/out.serial");
//			if (debugFolder.exists() || debugFolder.createNewFile()) {
//				try (FileOutputStream out = new FileOutputStream(debugFolder)) {
//					out.write(bytes == null ? new byte[0] : bytes);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//
//			LinkableObject obj2 = LinkableDeserializer.deserializeLinkable(bytes);
//			byte[] bytesAgain = LinkableSerializer.serializeLinkable(obj2);
//
//			System.out.println("Equals: " + Arrays.compare(bytes, bytesAgain));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (true) return;
		
		Operation mode = config.getOperation();
		if (mode == Operation.NONE) {
			printHelpMessage();
			return;
		}
		
//		AmpleCompiler compiler = new AmpleCompiler();
//		compiler.setConfiguration(config);
//
//		switch (mode) {
//			case COMPILE -> {
//				LOGGER.info("---------------------------------------------------------");
//				LOGGER.info("HardCoded AmpleProgrammingLanguage compiler (2021-10-15)");
//				LOGGER.info("");
//				LOGGER.info("WorkingDir  : '{}'", config.getWorkingDirectory());
//				LOGGER.info("SourceFile  : '{}'", config.getSourceFile());
//				LOGGER.info("OutputFile  : '{}'", config.getOutputFile());
//				LOGGER.info("Paths       : {}",   config.getSourceFolders());
//				LOGGER.info("Format      : {}",   Objects.toString(config.getOutputFormat(), "<NONE>"));
//				LOGGER.info("Target      : {}",   Objects.toString(config.getTargetFormat(), "<NONE>"));
//				LOGGER.info("---------------------------------------------------------");
//
//				if(Objects.equals(config.getSourceFile(), config.getOutputFile())) {
//					throw new CompilerException("source and output file cannot be the same file");
//				}
//
//				long start = System.nanoTime();
//				{
//					compiler.build();
//					byte[] bytes = compiler.getBytes();
//					FileOutputStream stream = new FileOutputStream(config.getOutputFile());
//					stream.write(bytes, 0, bytes.length);
//					stream.close();
//				}
//
//				long time = System.nanoTime() - start;
//
//				LOGGER.info("");
//				LOGGER.info("---------------------------------------------------------");
//				LOGGER.info("COMPILE FINISHED");
//				LOGGER.info("");
//				LOGGER.info("Took: {} milliseconds", "%.4f".formatted(time / 1000000.0D));
//				LOGGER.info("---------------------------------------------------------");
//			}
//
//			case RUN -> {
//				compiler.build();
//				AmpleVm.run(compiler.getProgram());
//			}
//		}
	}
}
