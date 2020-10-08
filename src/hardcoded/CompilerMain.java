package hardcoded;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.LogManager;

/**
 * This is the main entry point for the compiler.<br>
 * This compiler is a multi stage compiler.<br><br>
 * 
 * 
 * Stage one is to generate a token list from a input.<br><br>
 * 
 * Stage two is to generate a basic parse tree.<br>
 * State three is to optimize the parse tree.<br><br>
 * 
 * Stage four is to generate the reduced instruction language.<br>
 * Stage five is to optimize the reduced instruction language.<br><br>
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
	
	public static void main(String[] args) throws Exception {
		// TODO: Use the arguments to change build options.
		
		HCompiler compiler = new HCompiler();
		compiler.setProjectPath("res/project/src/");
		compiler.build();
		
		// -h help
		// -f format
		//    spooky
		//    x86
		//    ...
		// -d debuging tools
		// -i inputfile
		// -o outputfile
		
		if(args.length > 1) {
			
		}
	}
}
