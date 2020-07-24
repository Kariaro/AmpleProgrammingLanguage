package hc.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hc.errors.SyntaxException;
import hc.parser.syntax.*;
import hc.reflection.ClassFinder;
import hc.token.Symbol;

/**
 * Reads a chain of symbols and generates the syntax tree.
 * 
 * @author HardCoded
 */
// https://www.cs.ru.ac.za/compilers/pdfvers.pdf
public class Syntaxer {
	private static final Logger LOGGER = Logger.getLogger(Syntaxer.class.getSimpleName());
	private final List<SyntaxReader> readers = new ArrayList<>();
	
	public Syntaxer() {
		List<Class<?>> files = ClassFinder.findClasses("hc.parser.syntax");
		
		for(Class<?> clazz : files) {
			if(!SyntaxReader.class.isAssignableFrom(clazz)) continue;
			
			try {
				SyntaxReader reader = clazz.asSubclass(SyntaxReader.class).newInstance();
				readers.add(reader);
				
				LOGGER.log(Level.INFO, "Found SyntaxReader: {0}", reader);
			} catch(Exception e) {
				// This means that the class was not instantiable.
				continue;
			}
		}
	}
	
	public SyntaxTree generate(Symbol symbol) {
		SyntaxTree root = new SyntaxTree();
		
		while(symbol != null) {
			boolean found = false;
			
			for(SyntaxReader reader : readers) {
				Result result = reader.compute(root, symbol);
				
				if(result != null) {
					symbol = symbol.next(result.numUsedSymbols());
					found = true;
					break;
				}
			}
			
			if(!found) {
				LOGGER.log(Level.SEVERE, "Failed to compile for symbol at: line={0} column={1}", new Object[] { symbol.getLineIndex(), symbol.getColumnIndex() });
				LOGGER.log(Level.SEVERE, " ----> ''{0}''", symbol);
				LOGGER.log(Level.SEVERE, "");
				throw new SyntaxException();
			}
			
			if(!found) {
				LOGGER.log(Level.WARNING, "Cound not find SyntaxReader for symbol: {0}", symbol.toString(" ", 4));
				symbol = symbol.next(1);
			}
		}
		
		System.out.println("=======================================================");
		System.out.println(root.toString().replace("\t", "    "));
		
		return root;
	}
}
