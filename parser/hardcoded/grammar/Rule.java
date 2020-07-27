package hardcoded.grammar;

import hc.token.Symbol;

public abstract class Rule {
	protected int ruleId;
	
	protected Rule() {
		
	}
	
	protected Rule(int ruleId) {
		this.ruleId = ruleId;
	}
	
	public String getRuleString() {
		return "Rule" + ruleId;
	}
	
	public int getRuleId() {
		return ruleId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Rule) {
			return toString().equals(obj.toString());
		}
		
		return this == obj;
	}
	
	/** Represent this hash with a unique hash that can only be
	 *
	 */
	public String hash() {
		return null;
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
