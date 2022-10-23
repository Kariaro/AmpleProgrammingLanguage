package me.hardcoded.repl;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.interpreter.AmpleRunner;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Scanner;

/**
 * Read Eval Print Loop
 */
public class ReadEvalPrintLoop {
	private static final Logger LOGGER = LogManager.getLogger(ReadEvalPrintLoop.class);
	private final AmpleConfig ampleConfig;
	
	public ReadEvalPrintLoop(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	private static void printHelp() {
		System.out.println("reset    - reset the state of repl");
		System.out.println("exit     - close the repl");
		System.out.println("help     - show this help message");
		System.out.println();
	}
	
	public void run() {
		Scanner scanner = new Scanner(System.in);
		
		REPLCompiler compiler = new REPLCompiler(ampleConfig);
		
		AmpleRunner runner = new AmpleRunner();
		AmpleRunner.ReplContext context = new AmpleRunner.ReplContext();
		
		// Only info
		Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
		
		printHelp();
		while (true) {
			System.out.println(">> ");
			
			String line = scanner.nextLine();
			switch (line.trim()) {
				case "reset":
					compiler.clear();
					context.clear();
					continue;
				case "exit":
					return;
				case "help":
					printHelp();
					continue;
			}
			
			StringBuilder sb = new StringBuilder();
			do {
				sb.append(line).append('\n');
				line = scanner.nextLine().stripTrailing();
			} while (!line.isBlank());
			
			if (line.isBlank() && sb.length() == 0) {
				continue;
			}
			
			line = sb.toString().trim();
			if (line.isBlank()) {
				continue;
			}
			
			// We want to take the code and compile it to intermediate instructions quietly
			try {
				IntermediateFile code = compiler.compile(line);
				context.setFile(code);
				
				// Run the code repl
				runner.runRepl(context);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
