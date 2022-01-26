package unit.parsetree;

import java.util.List;

import org.junit.Test;

import me.hardcoded.compiler.impl.*;
import me.hardcoded.compiler.parsetree.ParseTreeGenerator;
import me.hardcoded.compiler.statement.Statement;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.StatementUtils;

public class ParseTreeTest {
	private static byte[] generate(String code) {
		StringBuilder sb = new StringBuilder();
		sb.append("long* fla();\n")
		  .append("int* fia();\n")
		  .append("short* fsa();\n")
		  .append("char* fba();\n")
		  .append("long fl();\n")
		  .append("int fi();\n")
		  .append("short fs();\n")
		  .append("char fb();\n\n")
		  .append("@set GLOBAL 3;\n\n")
		  .append("void main() {\n")
		  .append("\t").append("long* lp = 0;\n")
		  .append("\t").append("int* ip = 0;\n")
		  .append("\t").append("short* sp = 0;\n")
		  .append("\t").append("char* bp = 0;\n")
		  .append("\t").append("long lv = 0;\n")
		  .append("\t").append("int iv = 0;\n")
		  .append("\t").append("short sv = 0;\n")
		  .append("\t").append("byte bv = 0;\n")
		  .append("\t").append(code).append("\n\t;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n") // Make sure we get all the errors
		  .append("}\n");
		
		return sb.toString().getBytes();
	}
	
	private static String _caller() {
		StackTraceElement element = Thread.currentThread().getStackTrace()[2];
		return "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
	}
	
	private static void dump(IProgram program) {
		for(ISyntaxMarker marker : program.getSyntaxMarkers()) {
			Position position = marker.getSyntaxPosition().getStartPosition();
			
			System.err.printf("%s(%s:%s) : %s",
				marker.getCompilerMessage(),
				position.line + 1,
				position.column + 1,
				marker.getMessage()
			);
		}
	}
	
	private static String dumpCustom(IProgram program) {
		StringBuilder sb = new StringBuilder();
		
		List<IFunction> functions = program.getFunctions();
		IFunction func = null;
		
		for(IFunction f : functions) {
			if(f.getName().equals("main")) {
				func = f;
				break;
			}
		}
		
		if(func == null) return "";
		
		List<IStatement> func_body = func.getStatements();
		if(!func_body.isEmpty()) {
			List<IStatement> statements = func_body.get(0).getStatements();
			for(int i = 8; i < statements.size(); i++) {
				Statement stat = (Statement)statements.get(i);
				sb.append(StatementUtils.printPretty(stat).strip()).append("\n");
			}
		}
		
		return sb.toString().trim();
	}
	
	
	private static void check(String code, int flags) {
		byte[] bytes = generate(code);
		
		IProgram program = ParseTreeGenerator.loadParseTreeFromBytes(bytes);
		
		boolean valid = program.hasErrors() != (flags == ERROR);
		if(valid || (flags == PASS && program.hasErrors())) {
			System.out.println(program.getSyntaxMarkers() + ": " + program.hasErrors() + " != " + (flags == ERROR) + ", " + code);
			System.out.println("=".repeat(100));
			System.out.println(new String(bytes));
			System.out.println("=".repeat(100));
			dump(program);
			throw new AssertionError(_caller() + ": Expected ");
		} else {
			// System.out.printf("%s\n%s: %s\n\n", "=".repeat(100), dumpCustom(program), code);
		}
	}
	
	private static void check(String code, int flags, String content) {
		byte[] bytes = generate(code);
		
		IProgram program = ParseTreeGenerator.loadParseTreeFromBytes(bytes);
		
		String check = dumpCustom(program);
		boolean valid = program.hasErrors() != (flags == ERROR);
		boolean nonContent = !check.equals(content);
		
		if(valid || (flags == PASS && program.hasErrors()) || nonContent) {
			System.out.println(program.getSyntaxMarkers() + ": " + program.hasErrors() + " != " + (flags == ERROR) + ", " + code);
			System.out.println("=".repeat(100));
			System.out.println(new String(bytes));
			System.out.println("=".repeat(100));
			
			if(nonContent) {
				System.out.println("Bad Content:");
				System.out.printf("Got:  [%s]\n", check);
				System.out.printf("Want: [%s]\n", content);
				System.out.println("==============================");
			}
			dump(program);
			throw new AssertionError(_caller() + ": Expected ");
		} else {
			// System.out.printf("%s\n%s: %s\n\n", "=".repeat(100), check, code);
		}
	}
	
	private static final int PASS = 0;
	private static final int ERROR = 1;
	
	// f [lisb] [a ]
	// [lisb] [pv]
	
	@Test
	public void test_comma() {
		check("iv;", ERROR);
		check("*iv;", ERROR);
		check("*iv += 3;", ERROR);
		check("iv[3];", ERROR);
		check("iv[3]++;", ERROR);
		check("ip[3]++;", PASS);
		check("iv += 3;", PASS);
		check("(0, iv += 3);", PASS);
		check("(0, iv) += 3;", PASS);
		check("(0, iv += 3) += 3;", ERROR);
		check("(0, iv) ++;", PASS);
		check("++(0, iv);", PASS);
		check("++(0, *iv);", ERROR);
		check("(0, iv)[3];", ERROR);
		check("(0, (int*)iv)[3];", ERROR);
		check("(0, (int*)iv)[3] += 3;", PASS);
		check("(*(0, (int*)iv))++;", PASS);
		

		check("fia();", PASS);
		check("fia(1);", ERROR);
		check("(0, fia());", PASS);
		check("(0, fia(0));", ERROR);
		check("(0, fia())++;", ERROR);
		check("(0, *(fia()));", PASS);
		check("(0, *(fia()))++;", PASS);
		check("(0, *(fia())) += 3;", PASS);
		check("(0, (fia())[3]);", PASS);
		check("(0, (fia())[3])++;", PASS);
		check("(0, (fia())[3]) += 3;", PASS);
		
		check("(0, GLOBAL[3])++;", ERROR);
		check("(0, GLOBAL[3]) += 3;", ERROR);
		check("(0, GLOBAL) += 3;", ERROR);
		check("(0, *GLOBAL) += 3;", ERROR);
		check("(0, *fia) += 3;", ERROR);
		check("(0, *fia)++;", ERROR);
		check("(0, *fia)[3]++;", ERROR);
		check("(0, *(fia()))[3]++;", PASS);
		check("fia[3] += 3;", ERROR);
		check("fia[3];", ERROR);
		check("(0, fia[3]);", ERROR);
		check("(0, fia[3]) += 3;", ERROR);
	}
	
	@Test
	public void test_assign_operator() {
//		check("ip *= 3;", ERROR);
//		check("ip >>= 3;", ERROR);
//		check("ip <<= 3;", ERROR);
//		check("ip %= 3;", ERROR);
//		check("ip /= 3;", ERROR);
//		check("ip |= 3;", ERROR);
		check("ip += 3;", PASS);
		check("ip -= 3;", PASS);

		check("void _ = 3;", ERROR);
		check("void* _ = 3;", PASS);
	}
	
	@Test
	public void test_pointers() {
		check("int _ = (int)&fia;", PASS);
		check("int _ = *fia;", ERROR);
		check("int _ = fla();", ERROR);
		check("int _ = fia();", ERROR);
		check("int _ = fsa();", ERROR);
		check("int _ = fba();", ERROR);
		
		check("long _ = fl();", PASS, "set(_, call(fl))");
		check("long _ = fi();", PASS, "set(_, cast(call(fi), i64))");
		check("long _ = fs();", PASS, "set(_, cast(call(fs), i64))");
		check("long _ = fb();", PASS, "set(_, cast(call(fb), i64))");
		
		check("int _ = fl();", PASS, "set(_, cast(call(fl), i32))");
		check("int _ = fi();", PASS, "set(_, call(fi))");
		check("int _ = fs();", PASS, "set(_, cast(call(fs), i32))");
		check("int _ = fb();", PASS, "set(_, cast(call(fb), i32))");
		
		check("short _ = fl();", PASS, "set(_, cast(call(fl), i16))");
		check("short _ = fi();", PASS, "set(_, cast(call(fi), i16))");
		check("short _ = fs();", PASS, "set(_, call(fs))");
		check("short _ = fb();", PASS, "set(_, cast(call(fb), i16))");
		
		check("char _ = fl();", PASS, "set(_, cast(call(fl), i8))");
		check("char _ = fi();", PASS, "set(_, cast(call(fi), i8))");
		check("char _ = fs();", PASS, "set(_, cast(call(fs), i8))");
		check("char _ = fb();", PASS, "set(_, call(fb))");
	}
	
	@Test
	public void test_labels() {
		check("goto Label0;\nLabel0:", PASS);
		check("goto Label1;\nLabel0:", ERROR);
	}
	
}
