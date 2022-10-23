package me.hardcoded.repl;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.interpreter.AmpleRunner;

import java.util.Scanner;

/**
 * Read Eval Print Loop
 */
public class ReadEvalPrintLoop {
	private final AmpleConfig ampleConfig;
	
	public ReadEvalPrintLoop(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		
		REPLCompiler compiler = new REPLCompiler(ampleConfig);
		
		StringBuilder sb = new StringBuilder();
		while (true) {
			System.out.println(">>>");
			
			String line = scanner.nextLine();
			switch (line.trim()) {
				case "reset" -> {
					compiler.clear();
					continue;
				}
				case "exit" -> {
					return;
				}
				case "help" -> {
					// TODO: Implement help
					continue;
				}
			}
			
			do {
				sb.append(line).append('\n');
				line = scanner.nextLine().stripTrailing();
			} while (!line.isBlank());
			
			if (line.isBlank() && sb.length() == 0) {
				continue;
			}
			
			line = sb.toString();
			sb.delete(0, sb.length());
			
			// We want to take the code and compile it to intermediate instructions quietly
			System.out.println("You wrote: " + line);
			
			try {
				IntermediateFile code = compiler.compile(line);
				
				AmpleRunner runner = new AmpleRunner();
				runner.runCodeBlock(code);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
