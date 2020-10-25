package hardcoded;

import java.io.*;
import java.util.*;
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
	
	private static enum ActionType {
		COMPILE,
		RUN,
		NONE
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
		
		ActionType mode = ActionType.NONE;
		
		File working_directory = null;
		String source_file = null;
		String output_file = null;
		String format = null;
		
		for(int i = 0; i < args.length; i++) {
			String str = args[i];
			
			switch(str) {
				case "-f": case "-format": {
					if(i + 1 >= args.length) break;
					format = args[(i++) + 1];
					break;
				}
				
				case "-p": {
					if(i + 1 >= args.length) break;
					working_directory = new File(args[(i++) + 1]);
					if(!working_directory.exists()) {
						System.out.println("Path does not exist");
						printHelpMessage();
						return;
					}
					break;
				}
				
				case "-run": {
					if(i + 1 >= args.length) break;
					source_file = args[(i++) + 1];
					mode = ActionType.RUN;
					break;
				}
				
				case "-compile": {
					if(i + 2 >= args.length) break;
					source_file = args[(i++) + 1];
					output_file = args[(i++) + 1];
					mode = ActionType.COMPILE;
					break;
				}
				
				default: {
					System.out.println("Invalid argument '" + str + "'\n");
				}
				case "-?":
				case "-h":
				case "-help": {
					printHelpMessage();
					return;
				}
			}
		}
		
		if(working_directory == null) {
			// TODO: Check if this is correct
			working_directory = new File("").getAbsoluteFile();
		} else {
			// Make sure that this is a absolute file and not a relative file
			working_directory = working_directory.getAbsoluteFile();
		}
		
		if(isDeveloper()) {
			// Developer variables and test environment
			
			String file = "main.hc";
			// file = "tests/000_pointer.hc";
			file = "prim.hc";
			// file = "tests/001_comma.hc";
			// file = "tests/002_invalid_assign.hc";
			// file = "tests/003_invalid_brackets.hc";
			// file = "tests/004_cor_cand.hc";
			// file = "tests/005_assign_comma.hc";
			// file = "tests/006_cast_test.hc";
			// file = "test_syntax.hc";
			
			// file = "tests_2/000_assign_test.hc";
			
			mode = ActionType.COMPILE;
			format = "ir";
			String file_name = file;
			{
				int index = file.lastIndexOf('.');
				if(index < 0) {
					file_name = file + ".lir";
				} else {
					file_name = file.substring(index) + "lir";
				}
			}
			
			source_file = "res/project/src/" + file;
			output_file = "res/project/bin/" + file_name;
		}
		
		if(mode == ActionType.NONE) {
			printHelpMessage();
			return;
		}
		
		if(mode == ActionType.COMPILE) {
			System.out.println("---------------------------------------------------------");
			System.out.println("HardCoded HCProgrammingLanguage compiler (2020-10-15) (c)");
			System.out.println();
			System.out.printf("WorkingDir  : '%s'\n", working_directory);
			System.out.printf("SourceFile  : '%s'\n", source_file);
			System.out.printf("OutputFile  : '%s'\n", output_file);
			System.out.printf("Format      : %s\n",   Objects.toString(format, "<NONE>"));
			System.out.println("---------------------------------------------------------");
			
			long start = System.nanoTime();
			HCompiler compiler = new HCompiler();
			compiler.setWorkingDirectory(working_directory);
			compiler.setSourceFile(source_file);
			compiler.setOutputFile(output_file);
			compiler.setOutputFormat(format);
			compiler.build();
			
			long time = System.nanoTime() - start;
			
			System.out.println();
			System.out.println("---------------------------------------------------------");
			System.out.println("COMPILE FINISHED");
			System.out.println();
			System.out.printf("Took: %.4f milliseconds\n", time / 1000000D);
			System.out.println("---------------------------------------------------------");
		}
	}
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("hardcoded.developer"))
			&& !DomainUtils.isJarRuntime();
	}
}
