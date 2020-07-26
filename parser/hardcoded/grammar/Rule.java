package hardcoded.grammar;

import hc.token.Symbol;

public abstract class Rule {
	protected final int ruleId;
	
	public Rule() { this(0); }
	public Rule(int ruleId) { this.ruleId = ruleId; }
	
	public String getRuleString() { return "Rule" + ruleId; }
	public int getRuleId() { return ruleId; }
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Rule) {
			return toString().equals(obj.toString());
		}
		
		return this == obj;
	}
	
	/**
	 * Returns the amount of symbols that matched this pattern.
	 * @param stack The current symbols used
	 * @param symbol The symbol to check if it matches this pattern.
	 * @return Returns -1 if no match was found.
	 */
	@Deprecated
	protected int match(Symbol symbol) {
		throw new UnsupportedOperationException();
	}
}
