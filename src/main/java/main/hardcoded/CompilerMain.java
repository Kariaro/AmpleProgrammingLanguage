package hardcoded;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.LogManager;

import hardcoded.utils.DomainUtils;
import hardcoded.utils.FileUtils;

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
 * Stage six is to generate the assembly code.<br>
 * Stage seven is to optimize the assembly code.
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
	
	public static void main(String[] args) throws Exception {
		if(!isDeveloper() && args.length < 1) {
			printHelpMessage();
			return;
		}
		
		String source_path = null;
		String binary_path = null;
		String format = null;
		
		for(int i = 0; i < args.length; i++) {
			String str = args[i];
			
			switch(str) {
				
				case "-format":
				case "-f": {
					if(i + 1 >= args.length) break;
					format = args[(i++) + 1];
					continue;
				}
				
				case "-src":
				case "-s": {
					if(i + 1 >= args.length) break;
					source_path = args[(i++) + 1];
					continue;
				}
				
				case "-bin":
				case "-b": {
					if(i + 1 >= args.length) break;
					binary_path = args[(i++) + 1];
					continue;
				}
				
				default: {
					System.out.println("Invalid argument '" + str + "'\n");
				}
				case "?":
				case "-?":
				case "-h": {
					printHelpMessage();
					return;
				}
			}
		}
		
		if(isDeveloper()) {
			// Developer variables and test environment
			
			format = "ir";
			source_path = "res/project/src";
			binary_path = "res/project/bin";
		}
		
		System.out.println("---------------------------------------------------------");
		System.out.println("HardCoded HCProgrammingLanguage compiler (2020-10-15) (c)");
		System.out.println();
		System.out.println("OutputFormat: " + format);
		System.out.println("SourcePath  : '" + source_path + "'");
		System.out.println("BinaryPath  : '" + binary_path + "'");
		System.out.println("---------------------------------------------------------");
		
		long start = System.nanoTime();
		
		HCompiler compiler = new HCompiler();
		compiler.setOutputFormat(format);
		compiler.setProjectPath("res/project/src/");
		compiler.build();
		
		long time = System.nanoTime() - start;
		
		System.out.println();
		System.out.println("---------------------------------------------------------");
		System.out.println("COMPILE FINISHED");
		System.out.println();
		System.out.printf("Took: %.4f milliseconds\n", time / 1000000D);
		System.out.println("---------------------------------------------------------");
	}
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("hardcoded.developer"))
			&& !DomainUtils.isJarRuntime();
	}
}
