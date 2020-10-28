package hardcoded;

import java.io.*;
import java.util.*;
import java.util.logging.LogManager;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.utils.DomainUtils;
import hardcoded.utils.FileUtils;
import hardcoded.vm.HcVm;

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
		
		// Default working directory
		File working_directory = new File("").getAbsoluteFile();
		String sourcePath = null;
		String outputPath = null;
		String format = null;
		
		// TODO: Make sure that the command line is correctly typed
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
					
					// Make sure that the working directory has the absolute path to the file and not a relative path
					working_directory = working_directory.getAbsoluteFile();
					
					break;
				}
				
				case "-run": {
					if(i + 1 >= args.length) break;
					sourcePath = args[(i++) + 1];
					mode = ActionType.RUN;
					break;
				}
				
				case "-compile": {
					if(i + 2 >= args.length) break;
					sourcePath = args[(i++) + 1];
					outputPath = args[(i++) + 1];
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
			
			if(mode != ActionType.NONE) break;
		}

		File sourceFile = null;
		File outputFile = null;
		
		if(isDeveloper()) {
			// Developer variables and test environment
			
			String file = "main.hc";
			// file = "tests/000_pointer.hc";
			// file = "prim.hc";
			// file = "tests/001_comma.hc";
			// file = "tests/002_invalid_assign.hc";
			// file = "tests/003_invalid_brackets.hc";
			// file = "tests/004_cor_cand.hc";
			// file = "tests/005_assign_comma.hc";
			// file = "tests/006_cast_test.hc";
			// file = "test_syntax.hc";
			// file = "prog.hc";
			
			// file = "tests_2/000_assign_test.hc";
			
			mode = ActionType.COMPILE;
			format = "ir";
			String file_name = file;
			{
				int index = file.lastIndexOf('.');
				if(index < 0) {
					file_name = file + ".lir";
				} else {
					file_name = file.substring(0, index) + ".lir";
				}
			}
			
			working_directory = new File("res/project/src").getAbsoluteFile();
			sourcePath = file;
			outputPath = "../bin/" + file_name;
		}
		
		if(mode == ActionType.NONE) {
			printHelpMessage();
			return;
		}
		
		sourceFile = new File(working_directory, sourcePath).getCanonicalFile();
		HCompiler compiler = new HCompiler();
		compiler.setSourceFile(sourceFile);
		compiler.setOutputFormat(format);
		
		if(mode == ActionType.COMPILE) {
			outputFile = new File(working_directory, outputPath).getCanonicalFile();
			
			System.out.println("---------------------------------------------------------");
			System.out.println("HardCoded HCProgrammingLanguage compiler (2020-10-15) (c)");
			System.out.println();
			System.out.printf("WorkingDir  : '%s'\n", working_directory);
			System.out.printf("SourceFile  : '%s'\n", sourcePath);
			System.out.printf("OutputFile  : '%s'\n", outputPath);
			System.out.printf("Format      : %s\n",   Objects.toString(format, "<NONE>"));
			System.out.println("---------------------------------------------------------");
			
			if(sourceFile.equals(outputFile))
				throw new CompilerException("source and output file cannot be the same file");
			
			compiler.build();
			long start = System.nanoTime(); {
				// TODO: Is this a safe operation?
				byte[] bytes = compiler.getBytes();
				FileOutputStream stream = new FileOutputStream(outputFile);
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
		
		if(mode == ActionType.RUN) {
			compiler.build();
			HcVm.run(compiler.getProgram());
		}
	}
	
	public static boolean isDeveloper() {
		return "true".equalsIgnoreCase(System.getProperty("hardcoded.developer"))
			&& !DomainUtils.isJarRuntime();
	}
}
