package hardcoded.grammar;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

interface GrammarParserImpl {
	
	/**
	 * Parse a grammar from the content of a file.
	 * 
	 * @param filePath the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public default Grammar loadFromFile(String filePath) throws IOException {
		return loadFromFile(new File(filePath));
	}
	
	/**
	 * Parse a grammar from the content of a file.
	 * 
	 * @param file the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public default Grammar loadFromFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		
		try {
			return load(new FileInputStream(file));
		} finally {
			stream.close();
		}
	}
	
	/**
	 * Parse a grammar from the content of a inputstream.<br>
	 * This function will not close the stream provided.
	 * 
	 * @param stream the inputstream.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public default Grammar load(InputStream stream) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		byte[] buffer = new byte[65536];
		int readBytes = 0;
		
		while((readBytes = stream.read(buffer)) != -1) {
			bs.write(buffer, 0, readBytes);
		}
		
		return parseGrammar(bs.toByteArray());
	}
	
	/**
	 * Parse a grammar from the content of this string using the ISO_8859_1 charset.
	 * 
	 * @param content the string containing the grammar data.
	 * @return A parsed grammar.
	 * @throws NullPointerException if the string is null:
	 */
	public default Grammar loadFromString(String content) {
		return parseGrammar(content.getBytes(StandardCharsets.ISO_8859_1));
	}
	
	/**
	 * Parse a grammar from the content of this string using the ISO_8859_1 charset.
	 * 
	 * @param content the string containing the grammar data.
	 * @param charset the charset used to decode the string.
	 * @return A parsed grammar.
	 * @throws IOException
	 * @throws NullPointerException if the string is null:
	 */
	public default Grammar loadFromString(String content, Charset charset) {
		return parseGrammar(content.getBytes(charset));
	}
	
	/**
	 * Parses a grammar from a byte array.
	 * 
	 * @param bytes the bytes of a grammar.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public Grammar parseGrammar(byte[] bytes);
}
