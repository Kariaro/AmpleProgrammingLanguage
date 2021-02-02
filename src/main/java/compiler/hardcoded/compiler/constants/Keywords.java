package hardcoded.compiler.constants;

import java.util.*;

/**
 * This class contains all the keywords that are used in the programming language.
 * @author HardCoded
 */
public final class Keywords {
	private static final Set<String> KEYWORDS;
	private Keywords() {}
	
	// TODO: Design feature. A break statement can break out of any nested branch. if, switch, while, for
	//       the keyword is 'lbreak' or 'lbreak <expr>' leaves <expr> branches.
	//       lbreak, break out of a branch directly. Jump to end of branch..
	
	// TODO: This file should contain all other keywords such as
	//       'int', 'long' .. because they represent words not usable in names.
	static {
		KEYWORDS = Set.of(
			"switch", "case", "default",
			"signed", "unsigned",
			"true", "false",
			"while",
			"for",
			"if",
			"return",
			"break",
			"continue",
			"goto",
			"asm"
		);
	}
	
	/**
	 * Checks if the string value is a keyword.
	 * 
	 * @param	value
	 * @return	{@code true} if the string value was a keyword
	 */
	public static boolean contains(String value) {
		return KEYWORDS.contains(value);
	}
	
	/**
	 * Returns a list with all keywords inside of it.
	 * @return a set of keywords
	 */
	public static Set<String> getAllKeywords() {
		return KEYWORDS;
	}
}
