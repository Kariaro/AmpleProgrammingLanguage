package me.hardcoded.main;

import me.hardcoded.compiler.AmpleCompiler;
import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.configuration.OutputFormat;
import me.hardcoded.configuration.TargetFormat;
import me.hardcoded.utils.DebugUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

/**
 * This is the main entry point for the compiler.<br>
 * This compiler is a multi-stage compiler.<br>
 * <p>
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
	
	public static String getVersion() {
		int MAJOR = 0;
		int MINOR = 1;
		int BUILD = 0;
		return "%d.%d.%d".formatted(MAJOR, MINOR, BUILD);
	}
	
	/**
	 * Only used for debug logging
	 */
	public static Logger getLogger() {
		return LOGGER;
	}
	
	static {
		Locale.setDefault(Locale.US);
	}
	
	private static void printHelpMessage() {
		try (InputStream stream = Main.class.getResourceAsStream("/command/help.txt")) {
			assert stream != null;
			LOGGER.info("\n{}", new String(stream.readAllBytes()));
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
			String file = "test.amp";
			File project = new File("src/main/resources/project");
			
			File workingDirectory = new File(project, "src");
			File outputFolder = new File(project, "bin");
			File inputFile = new File(workingDirectory, file);
			
			args = new String[] {
				"--working-directory", workingDirectory.getAbsolutePath(),
				"--format", OutputFormat.ASM.toString(),
				"--target", TargetFormat.BYTECODE.toString(),
				"--input-file", inputFile.getAbsolutePath(),
				"--output-folder", outputFolder.getAbsolutePath(),
				"--use-cache", "false"
			};
		}
		
		CompilerConfiguration config = CompilerConfiguration.parseArgs(args);
		
		// Print compiler statistics
		LOGGER.info("---------------------------------------------------------");
		LOGGER.info("HardCoded AmpleProgrammingLanguage compiler {} (2021-10-15)", getVersion());
		LOGGER.info("");
		LOGGER.info("WorkingDir   : '{}'", config.getWorkingDirectory());
		LOGGER.info("SourceFile   : '{}'", config.getSourceFile());
		LOGGER.info("OutputFolder : '{}'", config.getOutputFolder());
		LOGGER.info("Format       : {}", Objects.toString(config.getOutputFormat(), "<NONE>"));
		LOGGER.info("Target       : {}", Objects.toString(config.getTargetFormat(), "<NONE>"));
		LOGGER.info("UseCache     : {}", config.useCache() ? "True" : "False");
		LOGGER.info("---------------------------------------------------------");
		
		long start = System.nanoTime();
		try {
			AmpleCompiler compiler = new AmpleCompiler(new AmpleConfig(config));
			
			compiler.compile();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		
		long elapsed = System.nanoTime() - start;
		
		LOGGER.info("");
		LOGGER.info("---------------------------------------------------------");
		LOGGER.info("PROGRAM FINISHED");
		LOGGER.info("");
		LOGGER.info("Took: {} milliseconds", "%.4f".formatted(elapsed / 1000000.0D));
		LOGGER.info("---------------------------------------------------------");
		
		//		switch (mode) {
		//			case COMPILE -> {
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
