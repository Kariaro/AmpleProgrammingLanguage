package hardcoded.grammar;

public class GrammarTest {
	/* TODO: There is some problems with the grammar class.
	 *       roundbrackets are optional and should be expanded
	 *       into multiple rules....
	 *       
	 *       The grammar
	 *         S > ( "test" ) S
	 *       
	 *       Should become
	 *         S > S
	 *         S > "test" S
	 *       
	 *       Optimized to
	 *         S > "test" S
	 */
	/* TODO: The grammar is not checking for unused tokens.
	 *       If a token T is not used by any other rule it
	 *       should be optimized away.
	 *       
	 *       Same goes for unused items but that requires a
	 *       entry set to be defined.
	 */
	/* TODO: Nesting brackets inside brackets should not be
	 *       allowed.
	 *       
	 *       There is a problem where if you were to simplify
	 *       a roundbracket inside a squarebracket you would
	 *       get the wrong rule set after.
	 *       
	 *       The only way to fix this is by enforcing a rule
	 *       that you cannot nest brackets..
	 */
	/* TODO: The square bracket allows for ambiguity when written
	 *       in certain ways.
	 *       
	 *       A way to fix this is by adding another item that is
	 *       in the form.
	 *       
	 *         S > name ( "=" n) [ "," name ( "=" n ) ] ";"
	 *         
	 *       to
	 *       
	 *         S > S0 [ "," S0 ] ";"
	 *         S0 > name "=" n
	 *            > name
	 *            
	 *       to
	 *       
	 *         S > S1 ";"
	 *         S1 > S0
	 *            > S1 "," S0
	 *         S0 > name "=" n
	 *            > name
	 *            
	 *       By doing this we remove any ambiguity that was present
	 *       in the later form...
	 */
}
