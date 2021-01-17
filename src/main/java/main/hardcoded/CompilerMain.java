package hardcoded;

import java.io.*;
import java.util.*;
import java.util.logging.LogManager;

import hardcoded.compiler.BuildConfiguration;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IRSerializer;
import hardcoded.configuration.AmpleOptions;
import hardcoded.configuration.Config;
import hardcoded.utils.DomainUtils;
import hardcoded.utils.FileUtils;
import hardcoded.utils.PathUtils;
import hardcoded.vm.AmpleVm;

/**
 * This is the main entry point for the compiler.<br>
 * This compiler is a multi stage compiler.<p>
 * 
 * 
 * Stage one is to generate a token list from a input.<p>
 * 
 * 
 * Stage two is to generate a basic parse tree.<br>
 * State three is to optimize the parse tree.<p>
 * 
 * 
 * Stage four is to generate the reduced instruction language.<br>
 * Stage five is to optimize the reduced instruction language.<p>
 * 
 * 
 * Stage six is optional and requires a code generator.
 * 
 * @author HardCoded
 */
public class CompilerMain {
	static {
		try {
			// The ConsoleHandler is initialized once inside LogManager.RootLogger.
			// If we change System.err to System.out when the ConsoleHandler is created
			// we change it's output stream to System.out.
			
			PrintStream error_stream = System.err;
			System.setErr(System.out);
			
			Locale.setDefault(Locale.ENGLISH);
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream((
				"handlers=java.util.logging.ConsoleHandler\r\n" + 
				".level=INFO\r\n" + 
				"java.util.logging.ConsoleHandler.level=ALL\r\n" + 
				"java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter\r\n" + 
				"java.util.logging.SimpleFormatter.format=%1$tF %1$tT [%4$s] %3$s - %5$s%n"
			).getBytes()));
			
			// Interact with the RootLogger so that it calls LogManager.initializeGlobalHandlers();
			LogManager.getLogManager().getLogger("").removeHandler(null);
			
			// Switch back to the normal error stream.
			System.setErr(error_stream);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printHelpMessage() {
		try {
			System.out.println(new String(FileUtils.readInputStream(
				CompilerMain.class.getResourceAsStream("/command/help.txt")
			)));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("hardcoded.developer"))
			&& !DomainUtils.isJarRuntime();
	}
	
//	final Thread mainThread = Thread.currentThread();
//	Thread thread = new Thread(() -> {
//		String last = "";
//		try {
//			while(true) {
//				StackTraceElement[] array = Thread.getAllStackTraces().get(mainThread);
//				String curr = Arrays.deepToString(array);
//				
//				if(!last.equals(curr)) {
//					last = curr;
//					
//					System.out.println("=".repeat(100));
//					System.out.println(curr.replace(", ", "\n"));
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	});
//	thread.setDaemon(true);
//	thread.start();
	
	private static String[] getDevArgs() {
		String file = "main.hc";
		file = "crash.ample";
		file = "main.ample";
		
		String file_name = PathUtils.getFileName(file) + ".lir";
		
		String[] array = new String[] {
			"--compile", file,
			"--dir", new File("res_project/test0").getAbsolutePath(),
			"--paths", "src/;incl/",
			"--format", "ir",
			"--output", "bin/" + file_name
		};
		
		return array;
	}
	
	public static void main(String[] args) throws Exception {
		if(isDeveloper()) {
			args = getDevArgs();
		} else if(args.length < 1) {
			printHelpMessage();
			return;
		}
		
		Config config = CommandLine.load(args);
		System.out.println(config);
		
		String mode = config.get(AmpleOptions.COMPILER_MODE);
		
		// Stop the compiler if no mode was specified
		if(mode.equals("none")) return;
		
		if(mode.equals("run")) {
			String inputfile = config.get(AmpleOptions.COMPILER_INPUT_FILE);
			File file = new File(inputfile);
			
			try(FileInputStream stream = new FileInputStream(file)) {
				IRProgram program = IRSerializer.read(stream);
				AmpleVm.run(program);
			} catch(IOException e) {
				System.err.println("Failed to read program '" + file + "'");
			}
		} else if(mode.equals("compile")) {
			BuildConfiguration build_config = new BuildConfiguration();
			build_config.setOutputFormat(OutputFormat.get(config.get(AmpleOptions.COMPILER_FORMAT)));
			build_config.setWorkingDirectory((String)config.get(AmpleOptions.COMPILER_DIRECTORY));
			Set<String> paths = config.get(AmpleOptions.COMPILER_SOURCE_PATHS);
			for(String path : paths) {
				build_config.addSourceFolder(path);
			}
			
			String sourceName = config.get(AmpleOptions.COMPILER_INPUT_FILE);
			List<File> startFile = build_config.lookupFile(sourceName);
			if(startFile.isEmpty()) {
				System.err.println("The file '" + sourceName + "' does not exist in paths");
				return;
			}
			
			build_config.setStartFile(startFile.get(0));
			build_config.setOutputFile((String)config.get(AmpleOptions.COMPILER_OUTPUT_FILE));
			
			HCompiler compiler = new HCompiler();
			compiler.setConfiguration(build_config);
			
			{
				System.out.println("---------------------------------------------------------");
				System.out.println("HardCoded AmpleProgrammingLanguage compiler (2021-01-17) (c)");
				System.out.println();
				System.out.printf("WorkingDir  : '%s'\n", config.<Object>get(AmpleOptions.COMPILER_DIRECTORY));
				System.out.printf("SourceFile  : '%s'\n", config.<Object>get(AmpleOptions.COMPILER_INPUT_FILE));
				System.out.printf("OutputFile  : '%s'\n", config.<Object>get(AmpleOptions.COMPILER_OUTPUT_FILE));
				System.out.printf("Paths       : '%s'\n", config.<Object>get(AmpleOptions.COMPILER_SOURCE_PATHS));
				System.out.printf("Format      : %s\n",   config.<Object>get(AmpleOptions.COMPILER_FORMAT));
				System.out.println("---------------------------------------------------------");
				
				if(Objects.equals(build_config.getStartFile(), build_config.getOutputFile()))
					throw new CompilerException("source and output file cannot be the same file");
				
				
				long start = System.nanoTime();
				{
					compiler.build();
					
					// TODO: Is this a safe operation?
					byte[] bytes = compiler.getBytes();
					FileOutputStream stream = new FileOutputStream(build_config.getOutputFile());
					stream.write(bytes, 0, bytes.length);
					stream.close();
				}
				
				long time = System.nanoTime() - start;
				
				System.out.println();
				System.out.println("---------------------------------------------------------");
				System.out.println("COMPILE FINISHED");
				System.out.println();
				System.out.printf("Took: %.4f milliseconds\n", time / 1000000D);
				System.out.println("---------------------------------------------------------");
			}
		}
	}
}
