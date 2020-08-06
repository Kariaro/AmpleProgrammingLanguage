package hardcoded.grammar;

// https://www.masswerk.at/algol60/syntax.txt
public enum GrammarType {
	/**
	 * This is the default HardCoded grammar file syntax.<br>
	 * 
	 * This grammar allows for complicated capture groups including regex and repeated patterns.
	 */
	HCGR,
	
	/**
	 * Backus-Naur form<br><br>
	 * 
	 * This grammar syntax was made by <i>John Backus</i> in the middle of the
	 * 20th century. His grammar <i>BNF</i> was made to read the syntax of the new
	 * programming language IAL <i>(ALGOL 58)</i> and was first seen in the ALGOL
	 * 60 report.<br><br>
	 * 
	 * Since then the grammar has been implemented by many compiler compilers such as
	 * <a href="https://en.wikipedia.org/wiki/ANTLR">ANTLR</a>, 
	 * <a href="https://en.wikipedia.org/wiki/Yacc">Yacc</a>, 
	 * JavaCC.<br><br>
	 * 
	 * Wikipedia page: <a href="https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form">https://en.wikipedia.org/wiki/Backus-Naur_form</a>
	 */
	BNF,
}
