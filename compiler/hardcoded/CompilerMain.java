package hardcoded;

/**
 * This is the main entry point for the compiler.
 * 
 * @author HardCoded
 */
public class CompilerMain {
	public static void main(String[] args) {
		// TODO: Use the arguments to change build options.
		
		//int val = 204 ^ 229 * 119 ^ 75 ^ (5) - 163 + 69 + 10 * 30 * 191 | 145 * 139 + 183 * 165 * 248 * 6 & 183 * 243 ^ 11;
		//System.out.println("val -> " + val);
		
		HCompiler compiler = new HCompiler();
		compiler.setProjectPath("res/project/src/");
		compiler.build();
	}
}
