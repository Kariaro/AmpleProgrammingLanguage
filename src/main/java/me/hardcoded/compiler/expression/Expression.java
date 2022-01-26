package me.hardcoded.compiler.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.hardcoded.compiler.constants.Identifier;
import me.hardcoded.compiler.impl.IExpression;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.visualization.Printable;

public abstract class Expression implements IExpression, Printable {
	private final List<Expression> list;
	private final boolean hasElements;
	private ExprType type;
	
	protected Expression(ExprType type, boolean hasElements) {
		this.hasElements = hasElements;
		this.list = hasElements ? new ArrayList<>():List.of();
		this.type = Objects.requireNonNull(type, "Expression type must not be null");
	}

	@Override
	public ExprType type() {
		return type;
	}
	
	@Override
	public List<IExpression> getExpressions() {
		return List.copyOf(list);
	}
	
	protected void setType(ExprType type) {
		this.type = Objects.requireNonNull(type, "Expression type must not be null");
	}
	
	public List<Expression> getElements() {
		return list;
	}
	
	public boolean hasElements() {
		return hasElements;
	}
	
	@Override
	public abstract Expression clone();
	
	/**
	 * Returns the number of child nodes in this list.
	 */
	public int length() {
		return list.size();
	}
	
	/**
	 * Returns the first element of this expression or {@code null} if there are no elements.
	 */
	public Expression first() {
		return list.get(0);
	}
	
	/**
	 * Returns the last element of this expression or {@code null} if there are no elements.
	 */
	public Expression last() {
		return list.get(list.size() - 1);
	}
	
	/**
	 * Returns {@code false} if this expression modifies memory otherwise {@code true}.
	 */
	public boolean isPure() {
		if(hasElements) {
			for(Expression expr : list) {
				if(!expr.isPure()) return false;
			}
		}
		
		switch(type) {
			case invalid, call, set, ret -> {
				return false;
			}
			
			default -> {
				return true;
			}
		}
	}
	
	/**
	 * Checks whether or not an expression modifies any values.
	 * A modification can cause unknown side effects and thats
	 * why changing a value can give side effects.
	 */
	public boolean hasSideEffects() {
		if(type == ExprType.set
		|| type == ExprType.call) return true;
		
		if(hasElements) {
			for(Expression expr : list) {
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
	public Expression get(int index) {
		return list.get(index);
	}
	
	/**
	 * Add a new element to this expression.
	 * @param	expr	the expression to add
	 */
	public void add(Expression expr) {
		list.add(expr == null ? EMPTY:expr);
	}
	
	/**
	 * Replaces the element at the specified index with a new expression.
	 * @param	index	the index of the element that should be replaced
	 * @param	expr	the expression to replace with
	 */
	public void set(int index, Expression expr) {
		list.set(index, expr == null ? EMPTY:expr);
	}
	
	/**
	 * Removes the element at the specified index.
	 * @param	index	the index of the element to remove
	 */
	public void remove(int index) {
		list.remove(index);
	}
	
	@Override
	public ISyntaxPosition getSyntaxPosition() {
		// TODO: Implement ISyntaxPosition.
		return ISyntaxPosition.empty();
	}

	@Override
	public String asString() {
		return "Undefined(%s)".formatted(this.getClass());
	}

	@Override
	public Object[] asList() {
		return list.toArray();
	}

	@Override
	public LowType size() {
		if(this == EMPTY) return LowType.INVALID;
		
		LowType curr = null;
		if(hasElements) {
			for(Expression expr : list) {
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
				return a.getAtomType();
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
		@Override
		public LowType size() {
			return LowType.INVALID;
		}

		@Override
		public Expression clone() {
			return this;
		}

		@Override
		public String toString() {
			return "nop";
		}

		@Override
		public String asString() {
			return "nop";
		}
	};
}
