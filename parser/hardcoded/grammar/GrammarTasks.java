package hardcoded.grammar;

final class GrammarTasks {
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
	
	/* TODO: Remove duplicate rules that is put inside the same item. OPTIMIZER */
	/* TODO: Include information about how tokenization should happen..
	 *       This could make it possible to create custom comment structures.
	 */
	
}
