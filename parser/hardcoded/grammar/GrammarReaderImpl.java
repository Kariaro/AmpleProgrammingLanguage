package hardcoded.grammar;

import java.io.*;

interface GrammarReaderImpl {
	
	/**
	 * Parse a grammar from the content of this file.
	 * 
	 * @param filePath the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public default Grammar load(String filePath) throws IOException {
		return load(new FileReader(new File(filePath)));
	}
	
	/**
	 * Parse a grammar from the content of this file.
	 * 
	 * @param file the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public default Grammar load(File file) throws IOException {
		return load(new FileReader(file));
	}
	
	/**
	 * Parse a grammar from the content of this string.
	 * 
	 * @param content the string containing the grammar data.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public default Grammar loadFromString(String content) throws IOException {
		return load(new StringReader(content));
	}
	
	/**
	 * Parses the input from a reader and returns the parsed grammar.
	 * 
	 * @param reader the reader that contains the data.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public Grammar load(Reader reader) throws IOException;
}
