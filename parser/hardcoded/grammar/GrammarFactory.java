package hardcoded.grammar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This factory class is used to read grammars and to output the grammar as an object.
 * 
 * @author HardCoded
 */
public final class GrammarFactory {
	private static final GrammarFactory factory = new GrammarFactory();
	
	private final Map<GrammarType, GrammarParserImpl> grammars;
	private GrammarFactory() {
		Map<GrammarType, GrammarParserImpl> map = new HashMap<>();
		map.put(GrammarType.BNF, new BNFGrammarParser());
		map.put(GrammarType.HCGR, new HCGRGrammarParser());
		
		grammars = Collections.unmodifiableMap(map);
	}
	
	/**
	 * Crate a grammar from the content of a string using the ISO_8859_1 charset.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param content the string containing the grammar data.
	 * @return The parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromString(GrammarType grammarType, String content) {
		return factory.grammars.get(grammarType).loadFromString(content);
	}
	
	/**
	 * Crate a grammar from the content of a string using the specified charset.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param content the string containing the grammar data.
	 * @param charset the charset used to decode the string.
	 * @return The parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromString(GrammarType grammarType, String content, Charset charset) {
		return factory.grammars.get(grammarType).loadFromString(content, charset);
	}
	
	/**
	 * Crate a grammar from the content of a file.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param filePath the path to the grammar file.
	 * @return The parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromFile(GrammarType grammarType, String filePath) throws IOException {
		return factory.grammars.get(grammarType).loadFromFile(filePath);
	}
	
	/**
	 * Crate a grammar from the content of a file.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param parent the parent folder of the file.
	 * @param fileName the name of the grammarFile.
	 * @return The parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromFile(GrammarType grammarType, File parent, String fileName) throws IOException {
		return factory.grammars.get(grammarType).loadFromFile(new File(parent, fileName));
	}
	
	
	/**
	 * Crate a grammar from the content of a file.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param file the grammar file.
	 * @return The parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromFile(GrammarType grammarType, File file) throws IOException {
		return factory.grammars.get(grammarType).loadFromFile(file);
	}
	
	/**
	 * Create a grammar from the input of a inputstream.
	 * 
	 * @param grammarType the type of the grammar.
	 * @param stream a inputstream that contains grammar data.
	 * @return The parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(GrammarType grammarType, InputStream stream) throws IOException {
		return factory.grammars.get(grammarType).load(stream);
	}
	
	/**
	 * Get all readers that has been initialized by this factory.
	 */
	public static Collection<GrammarParserImpl> loadedReaders() {
		return Collections.unmodifiableCollection(factory.grammars.values());
	}
}
