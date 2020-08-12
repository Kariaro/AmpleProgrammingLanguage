package hardcoded.grammar;

import java.io.*;
import java.util.*;

/**
 * This factory class is used to read grammars and to output the grammar as an object.
 * 
 * @author HardCoded
 */
public final class GrammarFactory {
	private static final GrammarFactory factory = new GrammarFactory();
	
	// FIXME: Convert to using a InputStream instead of a Reader!
	
	private final Map<GrammarType, GrammarReaderImpl> grammars;
	private GrammarFactory() {
		Map<GrammarType, GrammarReaderImpl> map = new HashMap<>();
		map.put(GrammarType.BNF, new BNFGrammarReader());
		map.put(GrammarType.HCGR, new HCGRGrammarReader());
		
		grammars = Collections.unmodifiableMap(map);
	}
	
	/**
	 * Parse a grammar from the content of this file.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param filePath the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(GrammarType grammarType, String filePath) throws IOException {
		return load(grammarType, new FileReader(new File(filePath)));
	}
	
	/**
	 * Parse a grammar from the content of this file.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param file the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(GrammarType grammarType, File file) throws IOException {
		return load(grammarType, new FileReader(file));
	}
	
	/**
	 * Parse a grammar from the content of a string.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param content the string containing the grammar data.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromString(GrammarType grammarType, String content) throws IOException {
		return load(grammarType, new StringReader(content));
	}
	
	/**
	 * Parses the input from a reader and returns the parsed grammar.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param reader the reader that contains the data.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(GrammarType grammarType, Reader reader) throws IOException {
		return factory.grammars.get(grammarType).load(reader);
	}
	
	/**
	 * Get all readers that has been initialized by this factory.
	 */
	public static Collection<GrammarReaderImpl> loadedReaders() {
		return factory.grammars.values();
	}
}
