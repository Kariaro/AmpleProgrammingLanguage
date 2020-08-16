package hardcoded.compiler;

import java.util.ArrayList;
import java.util.List;

import hardcoded.utils.StringUtils;

public interface Expression extends Stable {
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
	
	public static class ValueExpr implements Expression {
		
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; }
}
