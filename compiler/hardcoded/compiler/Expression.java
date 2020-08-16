package hardcoded.compiler;

import java.util.*;

import hardcoded.utils.StringUtils;

// TODO: Evaluate expression at compiler time.
public interface Expression extends Stable {
	public static enum ExpressionType {
		CALL,
	}
	
	/** This is true if the expression can be reduced while compiling. */
	public default boolean isCompilerExpr() {
		return false;
	}
	
	// TODO: Changes value...
	// TODO: ....
	// TODO: Always contain children.
	
	public static class StatementExpression implements Expression {}
	
	// Unary
	public static class UnExpr implements Expression {
		public Expression a;
		public String operation;
		public boolean postfix;
		
		public UnExpr(Expression a, String operation) {
			this.a = a;
			this.operation = operation;
		}
		
		public UnExpr(Expression a, String operation, boolean postfix) {
			this.a = a;
			this.operation = operation;
			this.postfix = postfix;
		}
		
		@Override
		public String toString() {
			if(postfix) return a + "" + operation;
			return operation + "" + a;
		}
		
		public String listnm() { return operation; }
		public Object[] listme() { return new Object[] { a }; }
	}
	
	// Binary
	public static class BiExpr implements Expression {
		public Expression a;
		public String operation;
		public Expression b;
		
		public BiExpr(Expression a, String operation, Expression b) {
			this.a = a;
			this.b = b;
			this.operation = operation;
		}
		
		public String toString() {
			return a + " " + operation + " " + b;
		}
		
		public String listnm() { return operation; }
		public Object[] listme() { return new Object[] { a, b }; }
	}
	
	// BinaryV2
	// TODO: Implement
	public static class BiExprList implements Expression {
		public String operation;
		public List<Expression> list;
		
		public BiExprList(String operation, Expression a, Expression b) {
			this.operation = operation;
			this.list = new ArrayList<>(Arrays.asList(a, b));
		}
		
		public String toString() {
			return StringUtils.join(" " + operation + " ", list);
		}
		
		public String listnm() { return operation; }
		public Object[] listme() { return list.toArray(); }
	}
	
	// Ternary
	public static class TeExpr implements Expression {
		public Expression a;
		public String op1;
		public Expression b;
		public String op2;
		public Expression c;
		
		public TeExpr(Expression a, String op1, Expression b, String op2, Expression c) {
			this.a = a;
			this.op1 = op1;
			this.b = b;
			this.op2 = op2;
			this.c = c;
		}
		
		@Override
		public String toString() {
			return a + " " + op1 + " " + b + " " + op2 + " " + c;
		}
	}
	
	public static class CallExpr implements Expression {
		public List<Expression> args;
		public Expression pointer;
		
		public CallExpr() {
			args = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return pointer + "(" + StringUtils.join(", ", args) + ")";
		}
		
		public ExpressionType getType() {
			return ExpressionType.CALL;
		}
		
		public String listnm() { return "CALL [" + pointer + "]"; }
		public Object[] listme() { return args.toArray(); }
	}
	
	public static class CastExpr implements Expression {
		public Type type;
		public Expression a;
		
		public CastExpr(Type type, Expression a) {
			this.type = type;
			this.a = a;
		}
		
		public String toString() {
			return "(" + type + ")" + a;
		}
		
		public String listnm() { return "CAST"; }
		public Object[] listme() { return new Object[] { type, a }; };
	}
	
	public static class VarExpr implements Expression {
		public String name;
		public String toString() { return name; }
		
		public String listnm() { return name; }
		public Object[] listme() { return new Object[] {}; }
	}
	
	public static class ValExpr implements Expression {
		// TODO: This should only be for numbers or strings.
		public Object value;
		
		public ValExpr(Object value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return Objects.toString(value);
		}
		
		public String listnm() { return Objects.toString(value); }
	}
	
	public static class IdentifierExpr implements Expression {
		public String name;
		
		public IdentifierExpr(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public String listnm() { return name; }
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; }
}
