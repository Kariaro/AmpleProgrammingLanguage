package hardcoded.parser;

import hardcoded.grammar.Grammar;

/**
 * Sources:<br>
 *  - https://www.cs.ru.ac.za/compilers/pdfvers.pdf<br>
 *  - http://www.orcca.on.ca/~watt/home/courses/2007-08/cs447a/notes/LR1%20Parsing%20Tables%20Example.pdf<br>
 *  - https://en.wikipedia.org/wiki/Canonical_LR_parser<br><br><br>
 * 
 * This is a LR(k) parser generator.
 * 
 * @author HardCoded
 */
public class LRParserGenerator {
	public LRParserGenerator() {
		
	}
	
	/**
	 * Generate a LR(k) parse from a given grammar.<br>
	 * 
	 * A higher k value can drastically increase the amount
	 * of states that the parser needs to check.
	 * 
	 * @param k the amount of tokens to look ahead
	 * @param grammar the grammar to generate a parser to
	 * @return a LR(k) parser generated from the grammar.
	 */
	public LRParser generateParser(int k, Grammar grammar) {
		
		
		return null;
	}
	
	/**
	 * We need to calculate the FOLLOW(b) and FIRST(a, b) for
	 * all sets 'a' and production rule 'b'<br><br>
	 * 
	 * For this grammar:<br>
	 * 
	 *<pre># $ Is the terminal of the file EOF
	 *# n is a token
	 *
	 *S -> E $
	 *
	 *E -> T
	 *  -> ( E )
	 *
	 *T -> n
	 *  -> + T
	 *  -> T + n</pre>
	 *
	 * The result of calling FIRST on a production rule should give all tokens
	 * that could be infront of that production rule legaly in this grammar.<br>
	 * 
	 * The result of calling FOLLOW on a production rule should give all tokens
	 * where this production rule could legaly be proceded by that token.<br>
	 *
	 *<pre>FIRST(S) = { E>T>n, E>T>+, E>( }
	 *FIRST(E) = {   T>n,   T>+,   ( }
	 *FIRST(T) = {     n,     + }
	 *
	 *FOLLOW(S) = { $, }
	 *FOLLOW(E) = { $, +, ), n }
	 *FOLLOW(T) = { $, +, ), }</pre>
	 *
	 *Sources: <a href="https://en.wikipedia.org/wiki/Canonical_LR_parser#FIRST_and_FOLLOW_sets">Wikipedia Canonical LR parser (FIRST and FOLLOW sets)</a>
	 */
	public void generateTable() {
		
	}
}
