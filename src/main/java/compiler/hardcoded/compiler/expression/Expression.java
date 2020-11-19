package hardcoded.compiler.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hardcoded.compiler.Identifier;
import hardcoded.compiler.constants.ExprType;
import hardcoded.compiler.impl.IExpression;
import hardcoded.visualization.Printable;

public abstract class Expression implements IExpression, Printable {
	protected final List<Expression> list;
	private ExprType type;
	
	protected Expression(ExprType type, boolean hasElements) {
		this.list = hasElements ? new ArrayList<>():null;
		this.type = Objects.requireNonNull(type, "Expression type must not be null");
	}
	
	public final ExprType type() {
		return type;
	}
	
	protected final void setType(ExprType type) {
		this.type = Objects.requireNonNull(type, "Expression type must not be null");
	}
	
	public final List<IExpression> getExpressions() {
		return List.copyOf(list);
	}
	
	public final boolean hasExpressions() {
		return list != null;
	}
	
	public final List<Expression> getElements() {
		return list;
	}
	
	public final boolean hasElements() {
		return list != null;
	}
	
	public abstract Expression clone();
	
	/**
	 * Returns the number of child nodes in this list.
	 * @return the number of child nodes in this list
	 */
	public final int length() {
		if(list != null)
			return list.size();
		return 0;
	}
	
	/**
	 * Returns the first element of this expression or {@code null} if there are no elements.
	 * @return the first element of this expression or {@code null} if there are no elements
	 */
	public final Expression first() {
		if(length() > 0)
			return list.get(0);
		return null;
	}
	
	/**
	 * Returns the last element of this expression or {@code null} if there are no elements.
	 * @return the last element of this expression or {@code null} if there are no elements
	 */
	public final Expression last() {
		int length = length();
		if(length > 0)
			return list.get(length - 1);
		
		return null;
	}
	
	/**
	 * Returns {@code false} if this expression modifies memory otherwise {@code true}.
	 * @return {@code false} if this expression modifies memory otherwise {@code true}
	 */
	public boolean isPure() {
		if(list != null) {
			for(Expression expr : list) {
				if(!expr.isPure()) return false;
			}
		}
		
		switch(type) {
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
	public final boolean hasSideEffects() {
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
	 * Returns the element at the specified index.
	 * @param	index	the index of the element in this list
	 * @return	the element at the specified index
	 */
	public final Expression get(int index) {
		if(list != null)
			return list.get(index);
		return null;
	}
	
	/**
	 * Add a new element to this expression.
	 * @param	expr	the expression to add
	 */
	public final void add(Expression expr) {
		if(list == null) throw new UnsupportedOperationException();
		list.add(expr == null ? EMPTY:expr);
	}
	
	/**
	 * Replaces the element at the specified index with a new expression.
	 * @param	index	the index of the element that should be replaced
	 * @param	expr	the expression to replace with
	 */
	public final void set(int index, Expression expr) {
		if(list == null) throw new UnsupportedOperationException();
		list.set(index, expr == null ? EMPTY:expr);
	}
	
	/**
	 * Removes the element at the specified index.
	 * @param	index	the index of the element to remove
	 */
	public void remove(int index) {
		if(list == null) throw new UnsupportedOperationException();
		list.remove(index);
	}
	
	public String asString() { return "Undefined(" + this.getClass() + ")"; }
	
	public final Object[] asList() {
		return list == null ? new Object[0]:list.toArray();
	}
	
	public LowType size() {
		if(this == EMPTY) return LowType.INVALID;
		
		LowType curr = null;
		if(hasElements()) {
			for(Expression expr : getElements()) {
				LowType type = expr.size();
				if(type == null) continue;
				
				curr = (curr == null) ? type:LowType.largest(curr, type);
			}
		} else {
			AtomExpr a = (AtomExpr)this;
			
			if(a.isIdentifier()) {
				Identifier ident = a.identifier();
				if(ident == null) return LowType.INVALID;
				
				if(ident.hasType()) {
					return ident.getLowType();
				}
			}
			
			if(a.isNumber()) {
				return a.atomType;
			}
			
			// FIXME: Ternary operations does not work sometimes because size is a group and not a primitive type!
			// curr = a.atomType();
		}
		
		if(curr == null) curr = LowType.INVALID;
		return curr;
	}
	
	
	/**
	 * An empty expression class used to indicate that an expression was absent or invalid.
	 */
	public static final Expression EMPTY = new Expression(ExprType.nop, false) {
		public LowType size() {
			return LowType.INVALID;
		}
		
		public Expression clone() {
			return this;
		}
		
		public String toString() {
			return "nop";
		}
		
		public String asString() {
			return "nop";
		}
	};
}
