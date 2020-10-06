package hardcoded.compiler.expression;

import java.util.List;

import hardcoded.compiler.Identifier;
import hardcoded.compiler.constants.ExprType;
import hardcoded.visualization.Printable;

public interface Expression extends Printable {
	// FIXME: Maybe remove this 'EMPTY' expression constant.
	public static final Expression EMPTY = new Expression() {
		public String toString() { return "nop"; }
		public String asString() { return "nop"; }
		
		public ExprType type() { return ExprType.nop; }
		public boolean hasElements() { return false; }
		public List<Expression> getElements() { return null; }
		public Expression get(int index) { return null; }
		public void set(int index, Expression e) {}
		public void remove(int index) {}
		public Expression clone() { return this; }
		public int length() { return 0; }
	};
	
	/**
	 * Returns the type of this expression.
	 * @return the type of this expression
	 */
	public ExprType type();
	
	/**
	 * Returns {@code true} if this expression contains child nodes.
	 * @return {@code true} if this expression contains child nodes
	 */
	public boolean hasElements();
	
	/**
	 * Returns a list containing all child nodes.
	 * @return a list containing all child nodes
	 */
	public List<Expression> getElements();
	
	/**
	 * Retruns the child node at the specified position.
	 * @param index the index of the child node in this list
	 * @return the child node at the specified position
	 */
	public Expression get(int index);
	
	/**
	 * Replaces the child node at the specified position with a new node.
	 * @param	index	the index of the child node to be replaced
	 * @param	expr	the child node to replace with
	 */
	public void set(int index, Expression expr);
	
	/**
	 * Removes the child node at the specified position.
	 * @param	index	the index of the child node to be removed
	 */
	public void remove(int index);
	
	/**
	 * Returns the number of child nodes in this list.
	 * @return the number of child nodes in this list
	 */
	public int length();
	
	
	public default Expression clone() {
		return null;
	}
	
	public default Expression first() {
		if(!hasElements() || length() < 1) return null;
		return getElements().get(0);
	}
	
	public default Expression last() {
		if(!hasElements() || length() < 1) return null;
		return getElements().get(length() - 1);
	}
	
	/** This is true if the expression can be reduced while compiling. */
	public default boolean isPure() {
		List<Expression> list = getElements();
		if(list != null) {
			for(Expression expr : list) {
				if(!expr.isPure()) return false;
			}
		}
		
		if(type() == null) return false;
		
		switch(type()) {
			case invalid:
			case call:
			case set:
			case ret:
				return false;
			default: return true;
		}
	}
	
	/**
	 * Checks whether or not an expression modifies any values.
	 * A modification can cause unknown side effects and thats
	 * why changing a value can give side effects.
	 * 
	 * @return 
	 */
	public default boolean hasSideEffects() {
		ExprType type = type();
		if(type == ExprType.set
		|| type == ExprType.call) return true;
		
		if(hasElements()) {
			for(Expression expr : getElements()) {
				if(expr.hasSideEffects()) return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Calculate the size of the nummerical size that this expression might hold.<br>
	 * <code>1L + 3 = (long)1 + (int)3 = (long)</code>
	 * 
	 * @return
	 */
	public default LowType size() {
		if(this == EMPTY) return null;
		
		LowType curr = null;
		
		if(hasElements()) {
			for(Expression expr : getElements()) {
				LowType type = expr.size();
				if(type == null) continue;
				
				curr = curr == null ? type:LowType.largest(curr, type);
			}
		} else {
			AtomExpr a = (AtomExpr)this;
			
			if(a.isIdentifier()) {
				Identifier ident = a.d_value;
				
				if(ident.hasType()) {
					return ident.low_type();
				}
			}
			
			if(a.isNumber()) {
				return a.atomType;
			}
		}
		
		return curr;
	}
	
	public default String asString() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] asList() { return new Object[] {}; }
}
