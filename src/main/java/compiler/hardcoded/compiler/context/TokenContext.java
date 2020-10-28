package hardcoded.compiler.context;

public interface TokenContext {
	/**
	 * Returns the location of this object.
	 * @return the location of this object
	 */
	public NamedRange getDefinedRange();
	
	/**
	 * Set the defined range of this object.
	 * @param range a range
	 */
	public void setDefinedRange(NamedRange range);
}
