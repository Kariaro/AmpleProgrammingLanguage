package hc.main;

import java.io.*;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.LogManager;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.GrammarFactory;
import hardcoded.grammar.GrammarType;
import hardcoded.parser.GLRParser;
import hardcoded.parser.GLRParserGenerator;
import hardcoded.tree.AbstractSyntaxTree;
import hc.parser.Syntaxer;
import hc.token.Symbol;
import hc.token.Tokenizer;

//https://www.cs.ru.ac.za/compilers/pdfvers.pdf
//http://www.orcca.on.ca/~watt/home/courses/2007-08/cs447a/notes/LR1%20Parsing%20Tables%20Example.pdf

// TODO: Remove parser syntax classes because they are not usefull anymore..
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
//		if(true) {
//			hardcoded.lexer.Tokenizer tk = TokenizerFactory.create()
//				.setLinefeedCharacter("\n")
//				
//				.addDelimiterGroup("multiline_comment", "/*", null, "*/")
//				.addDelimiterGroup("singleline_comment", "//", null, "\n")
//					.discardGroups("multiline_comment", "singleline_comment")
//				
//				.addDelimiterGroup("build", "%", null, "\n")
//				
//				.addDelimiterGroup("string_literal", "\"", "\\", "\"")
//				.addDelimiterGroup("char_literal", "\'", "\\", "\'")
//				
//				.addRegexGroup("identifier", "[a-zA-Z][a-zA-Z0-9_]")
//				
//				// TODO: These cannot be put directly after a identifier and needs a delimiter
//				.addRegexGroup("decimal_literal", "0.[0-9]+")
//				.addRegexGroup("integer_literal", "0x[0-9a-fA-F]+", "[0-9]+")
//				.defaultGroup("token")
//				.build();
//			
//			File file = new File("res/test.hc");
//			byte[] bytes = readFileBytes(file);
//			tk.parse(new String(bytes));
//			
//			return;
//		}
		
		try {
			Grammar grammar = GrammarFactory.load(GrammarType.HCGR, "res/test_wiki.gr");
			//grammar = GrammarFactory.load(GrammarType.HCGR, "res/language.gr");
			//grammar = GrammarFactory.load(GrammarType.HCGR, "res/test_wiki_2.gr");
			grammar = GrammarFactory.load(GrammarType.HCGR, "res/language_2.gr");
			grammar = grammar.expand();
			
			//LR0_ParserGenerator generator = new LR0_ParserGenerator();
			//LR0_Parser parser = generator.generateParser(grammar);
			//GLRParser glpars = parser.testingHACK();
			
			GLRParserGenerator generator = new GLRParserGenerator();
			GLRParser parser = generator.generateParser(grammar);
			// a a a a b a b
			
			try(Scanner input = new Scanner(System.in)) {
				while(true) {
					String line;
					// line = input.nextLine();
					line = "uint_64* A(){}";
					// line = "void TEST(uin, , , ) {}";
					// line = "uint_64******* TESTING_FUNCTION (uint_64 ABCD, uint_64*** CDEB, uint_64*************** WHY_) { { { { { { 132323232 + 0x32 ; } } } } } }";
					if(line == null || line.isEmpty()) break;
					
					Symbol symbol = Tokenizer.generateSymbolChain(line.getBytes());
					AbstractSyntaxTree ast = parser.parse(symbol);
					
					System.out.println("SyntaxTree: " + ast);
					
					System.out.println();
					System.out.println();
					System.out.println();
					break;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			
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
