package hc.main;

import java.io.*;
import java.util.Locale;
import java.util.logging.LogManager;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.GrammarFactory;
import hardcoded.parser.LR0_ParserGenerator;
import hc.parser.Syntaxer;
import hc.token.Symbol;
import hc.token.Tokenizer;

//https://www.cs.ru.ac.za/compilers/pdfvers.pdf
//http://www.orcca.on.ca/~watt/home/courses/2007-08/cs447a/notes/LR1%20Parsing%20Tables%20Example.pdf

public class Main {
	static {
		try {
			// The ConsoleHandler is initialized once inside LogManager.RootLogger
			// if we change Sytem.err to System.out when the ConsoleHandler is created
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
			
			// Switch back to normal error stream
			System.setErr(error_stream);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static byte[] readFileBytes(File file) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		
		try(DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[4096];
			int readBytes = 0;
			
			while((readBytes = stream.read(buffer)) != -1) {
				bs.write(buffer, 0, readBytes);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return bs.toByteArray();
	}
	
	public static void main(String[] args) {
		if(args.length > 0) {
			new Syntaxer();
			return;
		}
		
		try {
			Grammar grammar = GrammarFactory.load("res/test_wiki.gr");
			grammar = GrammarFactory.load("res/language.gr");
			grammar = GrammarFactory.load("res/test_wiki_2.gr");
			grammar = grammar.expand();
			
			System.out.println();
			
			LR0_ParserGenerator gen = new LR0_ParserGenerator();
			gen.parse(grammar);
			
			//LRParserGenerator generator = new LRParserGenerator();
			//generator.FIRST(grammar);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(true) return;
		}
		
		System.out.println("============================================================================");
		
		File file = new File("res/test.gr.hc");
		byte[] bytes = readFileBytes(file);
		
		// Token token = Tokeniser.generateTokenChain(bytes);
		Symbol symbol = Tokenizer.generateSymbolChain(bytes);
		
		Syntaxer syntaxer = new Syntaxer();
		syntaxer.generate(symbol);
		
		
		/*
		System.out.println("============================================================================");
		
		do {
			token = token.next();
			
			TokenType type = token.getType();
			
			if(type.isComment() || type.isWhitespace()) {
				//continue;
			}
			
			System.out.println("token: " + token.getValue() + ":" + type);
//			if(type.isBracket() && (token instanceof TokenGroup)) {
//				TokenGroup group = token.toGroup();
//				
//				List<Token> tokens = group.getValue();
//				System.out.println("    : " + tokens);
//			}
		} while(token.hasNext());
		*/
	}
}
