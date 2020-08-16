package hc.compiler;

import java.io.File;
import java.io.IOException;
import java.util.*;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.GrammarFactory;
import hardcoded.grammar.GrammarType;
import hardcoded.lexer.*;
import hardcoded.parser.GLRParser;
import hardcoded.parser.GLRParserGenerator;
import hardcoded.tree.AbstractSyntaxTree;
import hardcoded.tree.ParseTree;
import hardcoded.tree.ParseTree.PNode;
import hardcoded.utils.FileUtils;
import hardcoded.visualization.ASTVisualization;
import hardcoded.visualization.PTVisualization;
import hc.main.Main;

// TODO: Try multithread this if we find multiple files to create the parse trees and syntax trees.
public class HCompiler {
	@SuppressWarnings("unused")
	private List<HFile> sources = new ArrayList<>();
	private final Map<String, String> options;
	private File projectPath;
	
	/**
	 * The tree of this compile should look something like.
	 * 
	 *<pre>project:
	 *    src:
	 *        &lt;source code files&gt;
	 *    bin:
	 *        &lt;serialized trees from the parser&gt;
	 *        &lt;cached files to make compilation faster&gt;
	 *        &lt;compiled binary files&gt;
	 *        &lt;object files&gt;</pre>
	 * 
	 * Creating that structure we need to create a way to read such a project.
	 * Maybe we need to create our own property file or IDE to create code faster
	 * and more efficient. This might be a very fun project to add quirks to and
	 * to challange me in much more than just making my own compiler.
	 */
	public HCompiler() {
		options = new HashMap<>();
	}
	
	/**
	 * Change a option of this compiler.
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public HCompiler setOption(String name, String value) {
		options.put(name, value);
		return this;
	}
	
	private Grammar grammar;
	private Tokenizer lexer;
	
	// TODO: Serialize this file.
	private GLRParser parser;
	
	public void setProjectPath(String path) {
		projectPath = new File(path);
		
		try {
			grammar = GrammarFactory.loadFromFile(GrammarType.HCGR, projectPath, "grammar.gr").expand();
			// grammar = GrammarFactory.loadFromFile(GrammarType.HCGR, projectPath, "opg.gr").expand();
			lexer = TokenizerFactory.loadFromFile(projectPath, "lexer.lex");
			
			
			// TODO: --> ObjectInputStream; parser
			GLRParserGenerator generator = new GLRParserGenerator();
			parser = generator.generateParser(grammar);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private SyntaxStuff stuff = new SyntaxStuff();
	
	// TODO: Add folders to make it look like the structure we specified..
	// TODO: Make it so that we can change where the source and binary path are on the computer.
	// TODO: This method does not really do much for the compiler. Make a option code instead or something similar to that.
	public void build() throws IOException {
		if(projectPath == null || !projectPath.isDirectory()) {
			throw new NullPointerException("No valid project path has been selected.");
		}
		
		File source = new File(projectPath, options.get("src"));
		File output = new File(projectPath, options.get("bin"));
		
		System.out.println("[Compiler]");
		System.out.println("  proj : " + projectPath);
		System.out.println();
		System.out.println("  src -> " + source);
		System.out.println("  bin -> " + output);
		System.out.println();
		System.out.println("  entry -> " + options.get("entry"));
		System.out.println();
		
		// Change all tokens groups with the correct group after the pre processing..
		
		// Pre processing
		// Build
		// Post processing
		
		System.out.println("Pre processing");
		
		{
			byte[] bytes = FileUtils.readFileBytes(new File(source, options.get("entry")));
			//System.out.printf("Took: %.4f ms\n", (System.nanoTime() - start) / 1000000.0D);
			
			//start = System.nanoTime();
			Token token = TokenizerOld.generateTokenChain(lexer, bytes);
			// System.out.println("  Token: " + token.toString(" ", Integer.MAX_VALUE));
			//System.out.printf("Took: %.4f ms\n", (System.nanoTime() - start) / 1000000.0D);
			
			//start = System.nanoTime();
			//stuff.analyse(token);
			//System.out.printf("Took: %.4f ms\n", (System.nanoTime() - start) / 1000000.0D);
			
			long start = System.nanoTime();
			ParseTree parseTree = parser.parse(token);
			System.out.printf("Took: %.4f ms\n", (System.nanoTime() - start) / 1000000.0D);
			
			System.out.println("ParseTree: " + parseTree);
			
			PTVisualization ptv = new PTVisualization();
			ptv.show(parseTree);
			
			AbstractSyntaxTree ast = stuff.createTree(parseTree);
			ASTVisualization atv = new ASTVisualization();
			atv.show(ast);
			
			// int value = calculate(parseTree.nodes.get(0));
			// System.out.println("value: '" + value + "'");
		}
	}
	
	private int calculate(PNode node) {
		if(node.nodes == null || node.nodes.isEmpty()) return Integer.valueOf(node.value);
		List<PNode> nodes = node.nodes;
		int size = nodes.size();
		
		if(size == 1) return calculate(nodes.get(0));
		else if(size == 3) {
			PNode first = nodes.get(0);
			PNode second = nodes.get(1);
			PNode third = nodes.get(2);
			if(first.value.equals("(")) return calculate(second);
			
			switch(second.value) {
				case "+": return calculate(first) + calculate(third);
				case "-": return calculate(first) - calculate(third);
				case "/": return calculate(first) / calculate(third);
				case "%": return calculate(first) % calculate(third);
				case "*": return calculate(first) * calculate(third);
				case "^": return calculate(first) ^ calculate(third);
				case "&": return calculate(first) & calculate(third);
				case "|": return calculate(first) | calculate(third);
			}
		} else if(size == 2) {
			PNode first = nodes.get(0);
			PNode second = nodes.get(1);
			
			switch(first.value) {
				case "-": return -calculate(first);
				case "~": return ~calculate(second);
			}
		}
		
		return 0;
	}
}
