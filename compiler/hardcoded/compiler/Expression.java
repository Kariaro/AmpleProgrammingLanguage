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
			return "(" + type.type() + ")" + a;
		}
		
		public String listnm() { return "CAST"; }
		public Object[] listme() { return new Object[] { type, a }; };
	}
	
	public static class NumberExpr implements Expression {
		public Number value;
		
		public NumberExpr(Number value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return Objects.toString(value);
		}
		
		// TODO: Signed unsigned...
		public boolean isLong() { return value instanceof Long; }
		public boolean isInteger() { return value instanceof Integer; }
		public boolean isShort() { return value instanceof Short; }
		public boolean isByte() { return value instanceof Byte; }
		public boolean isDouble() { return value instanceof Double; }
		public boolean isFloat() { return value instanceof Float; }
		public boolean isFloating() { return isDouble() || isFloat(); }
		
		public boolean isCompilerExpr() { return true; }
		
		public String listnm() { return Objects.toString(value); }
	}
	
	public static class StringExpr implements Expression {
		public String value;
		
		public StringExpr(String value) {
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
	
	
	
	
	
	
	
	
	
	
	
	
	public static Expression optimize(Expression expr) {
		if(expr instanceof BiExpr) {
			return optimizeBiExpr((BiExpr)expr);
		}
		
		if(expr instanceof TeExpr) {
			return optimizeTeExpr((TeExpr)expr);
		}
		
		if(expr instanceof CastExpr) {
			return optimizeCastExpr((CastExpr)expr);
		}
		
		return expr;
	}
	
	public static Expression optimizeBiExpr(BiExpr expr) {
		expr.a = optimize(expr.a);
		expr.b = optimize(expr.b);
		if(canReduce(expr)) return calulateBiExpr(expr);
		if(canHalfReduce(expr)) return calulateHalfBiExpr(expr);
		
		return expr;
	}
	
	public static Expression optimizeCastExpr(CastExpr expr) {
		if(expr.type.size() == 0) throw new RuntimeException("Invalid type cast. Cannot cast into a zero byte type '" + expr.type.name() + "'");
		expr.a = optimize(expr.a);
		
		if(expr.a instanceof NumberExpr) {
			NumberExpr a = (NumberExpr)expr.a;
			Type type = expr.type;
			
			switch(type.size()) {
				case 1: {
					if(type.isFloating()) throw new RuntimeException("Invalid");
					return new NumberExpr(a.value.byteValue());
				}
				case 2: {
					if(type.isFloating()) throw new RuntimeException("Invalid");
					return new NumberExpr(a.value.shortValue());
				}
				case 4: {
					if(type.isFloating()) return new NumberExpr(a.value.floatValue());
					return new NumberExpr(a.value.intValue());
				}
				case 8: {
					if(type.isFloating()) return new NumberExpr(a.value.doubleValue());
					return new NumberExpr(a.value.longValue());
				}
				default: {
					throw new RuntimeException("Invalid type size. (" + type.size() + ")");
				}
			}
		}
		
		return expr;
	}
	
	public static Expression optimizeTeExpr(TeExpr expr) {
		expr.a = optimize(expr.a);
		expr.b = optimize(expr.b);
		expr.c = optimize(expr.c);
		
		if(expr.a instanceof NumberExpr) { // a?b:c
			NumberExpr a = (NumberExpr)expr.a;
			if(a.isDouble()) return (a.value.doubleValue() == 0 ? expr.c:expr.b);
			if(a.isFloat()) return (a.value.floatValue() == 0 ? expr.c:expr.b);
			if(a.isLong()) return (a.value.longValue() == 0 ? expr.c:expr.b);
			if(a.isInteger()) return (a.value.intValue() == 0 ? expr.c:expr.b);
		}
		
		return expr;
	}
	
	public static Expression calulateBiExpr(BiExpr expr) {
		NumberExpr a = (NumberExpr)expr.a;
		NumberExpr b = (NumberExpr)expr.b;
		
		boolean isDouble = a.isDouble() || b.isDouble();
		boolean isFloat = a.isFloat() || b.isFloat();
		boolean isLong = a.isLong() || b.isLong();
		boolean isInteger = a.isInteger() || b.isInteger();
		boolean isShort = a.isShort() || b.isShort();
		boolean isByte = a.isByte() || b.isByte();
		
		switch(expr.operation) {
			case "+": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() + b.value.doubleValue());
				if(isFloat) return new NumberExpr(a.value.floatValue() + b.value.floatValue());
				if(isLong) return new NumberExpr(a.value.longValue() + b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() + b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() + b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() + b.value.byteValue());
			}
			case "-": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() - b.value.doubleValue());
				if(isFloat) return new NumberExpr(a.value.floatValue() - b.value.floatValue());
				if(isLong) return new NumberExpr(a.value.longValue() - b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() - b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() - b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() - b.value.byteValue());
			}
			case "*": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() * b.value.doubleValue());
				if(isFloat) return new NumberExpr(a.value.floatValue() * b.value.floatValue());
				if(isLong) return new NumberExpr(a.value.longValue() * b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() * b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() * b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() * b.value.byteValue());
			}
			case "/": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() / b.value.doubleValue());
				if(isFloat) return new NumberExpr(a.value.floatValue() / b.value.floatValue());
				if(isLong) return new NumberExpr(a.value.longValue() / b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() / b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() / b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() / b.value.byteValue());
			}
			case "%": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() % b.value.doubleValue());
				if(isFloat) return new NumberExpr(a.value.floatValue() % b.value.floatValue());
				if(isLong) return new NumberExpr(a.value.longValue() % b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() % b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() % b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() % b.value.byteValue());
			}
			case "^": {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply XOR between floating point values!");
				if(isLong) return new NumberExpr(a.value.longValue() ^ b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() ^ b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() ^ b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() ^ b.value.byteValue());
			}
			case "&": {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply AND between floating point values!");
				if(isLong) return new NumberExpr(a.value.longValue() & b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() & b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() & b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() & b.value.byteValue());
			}
			case "|": {
				if(isDouble || isFloat) throw new RuntimeException("You cannot apply OR between floating point values!");
				if(isLong) return new NumberExpr(a.value.longValue() | b.value.longValue());
				if(isInteger) return new NumberExpr(a.value.intValue() | b.value.intValue());
				if(isShort) return new NumberExpr(a.value.shortValue() | b.value.shortValue());
				if(isByte) return new NumberExpr(a.value.byteValue() | b.value.byteValue());
			}
			case ">": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() > b.value.doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value.floatValue() > b.value.floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value.longValue() > b.value.longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value.intValue() > b.value.intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value.shortValue() > b.value.shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value.byteValue() > b.value.byteValue() ? 1:0);
			}
			case ">=": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() >= b.value.doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value.floatValue() >= b.value.floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value.longValue() >= b.value.longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value.intValue() >= b.value.intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value.shortValue() >= b.value.shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value.byteValue() >= b.value.byteValue() ? 1:0);
			}
			case "<": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() < b.value.doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value.floatValue() < b.value.floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value.longValue() < b.value.longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value.intValue() < b.value.intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value.shortValue() < b.value.shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value.byteValue() < b.value.byteValue() ? 1:0);
			}
			case "<=": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() <= b.value.doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value.floatValue() <= b.value.floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value.longValue() <= b.value.longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value.intValue() <= b.value.intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value.shortValue() <= b.value.shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value.byteValue() <= b.value.byteValue() ? 1:0);
			}
			
			case "==": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() == b.value.doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value.floatValue() == b.value.floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value.longValue() == b.value.longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value.intValue() == b.value.intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value.shortValue() == b.value.shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value.byteValue() == b.value.byteValue() ? 1:0);
			}
			case "!=": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() != b.value.doubleValue() ? 1:0);
				if(isFloat) return new NumberExpr(a.value.floatValue() != b.value.floatValue() ? 1:0);
				if(isLong) return new NumberExpr(a.value.longValue() != b.value.longValue() ? 1:0);
				if(isInteger) return new NumberExpr(a.value.intValue() != b.value.intValue() ? 1:0);
				if(isShort) return new NumberExpr(a.value.shortValue() != b.value.shortValue() ? 1:0);
				if(isByte) return new NumberExpr(a.value.byteValue() != b.value.byteValue() ? 1:0);
			}
			
			case "||": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() != 0 ? 1:(b.value.doubleValue() != 0 ? 1:0));
				if(isFloat) return new NumberExpr(a.value.floatValue() != 0 ? 1:(b.value.floatValue() != 0 ? 1:0));
				if(isLong) return new NumberExpr(a.value.longValue() != 0 ? 1:(b.value.longValue() != 0 ? 1:0));
				if(isInteger) return new NumberExpr(a.value.intValue() != 0 ? 1:(b.value.intValue() != 0 ? 1:0));
				if(isShort) return new NumberExpr(a.value.shortValue() != 0 ? 1:(b.value.shortValue() != 0 ? 1:0));
				if(isByte) return new NumberExpr(a.value.byteValue() != 0 ? 1:(b.value.byteValue() != 0 ? 1:0));
			}
			case "&&": {
				if(isDouble) return new NumberExpr(a.value.doubleValue() != 0 ? (b.value.doubleValue() != 0 ? 1:0):0);
				if(isFloat) return new NumberExpr(a.value.floatValue() != 0 ? (b.value.floatValue() != 0 ? 1:0):0);
				if(isLong) return new NumberExpr(a.value.longValue() != 0 ? (b.value.longValue() != 0 ? 1:0):0);
				if(isInteger) return new NumberExpr(a.value.intValue() != 0 ? (b.value.intValue() != 0 ? 1:0):0);
				if(isShort) return new NumberExpr(a.value.shortValue() != 0 ? (b.value.shortValue() != 0 ? 1:0):0);
				if(isByte) return new NumberExpr(a.value.byteValue() != 0 ? (b.value.byteValue() != 0 ? 1:0):0);
			}
		}
		
		return expr;
	}
	
	public static Expression calulateHalfBiExpr(BiExpr expr) {
		NumberExpr a = (NumberExpr)expr.a;
		
		switch(expr.operation) {
			case "||": {
				if(a.isDouble()) return (a.value.doubleValue() != 0 ? new NumberExpr(1):expr.b);
				if(a.isFloat()) return (a.value.floatValue() != 0 ? new NumberExpr(1):expr.b);
				if(a.isLong()) return (a.value.longValue() != 0 ? new NumberExpr(1):expr.b);
				if(a.isInteger()) return (a.value.intValue() != 0 ? new NumberExpr(1):expr.b);
				if(a.isShort()) return (a.value.shortValue() != 0 ? new NumberExpr(1):expr.b);
				if(a.isByte()) return (a.value.byteValue() != 0 ? new NumberExpr(1):expr.b);
			}
			case "&&": {
				if(a.isDouble()) return new NumberExpr(a.value.doubleValue() != 0 ? 1:0);
				if(a.isFloat()) return new NumberExpr(a.value.floatValue() != 0 ? 1:0);
				if(a.isLong()) return new NumberExpr(a.value.longValue() != 0 ? 1:0);
				if(a.isInteger()) return new NumberExpr(a.value.intValue() != 0 ? 1:0);
				if(a.isShort()) return new NumberExpr(a.value.shortValue() != 0 ? 1:0);
				if(a.isByte()) return new NumberExpr(a.value.byteValue() != 0 ? 1:0);
			}
		}
		
		return expr;
	}
	
	public static boolean canReduce(Expression expr) {
		if(expr instanceof BiExpr) {
			BiExpr b_expr = (BiExpr)expr;
			return (b_expr.a instanceof NumberExpr) && (b_expr.b instanceof NumberExpr);
		}
		
		return false;
	}
	
	public static boolean canHalfReduce(Expression expr) {
		if(expr instanceof BiExpr) {
			BiExpr b_expr = (BiExpr)expr;
			return (b_expr.a instanceof NumberExpr);
		}
		
		return false;
	}
	
	public default String listnm() { return "Undefined(" + this.getClass() + ")"; }
	public default Object[] listme() { return new Object[] {}; }
}
