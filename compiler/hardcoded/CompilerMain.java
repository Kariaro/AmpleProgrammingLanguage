package hardcoded;

import java.util.regex.Pattern;

import hardcoded.utils.StringUtils;

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
	public static void main(String[] args) {
		// TODO: Use the arguments to change build options.
		
		HCompiler compiler = new HCompiler();
		compiler.setProjectPath("res/project/src/");
		compiler.build();
		
	}
}
