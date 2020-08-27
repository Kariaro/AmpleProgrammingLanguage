package hardcoded.compiler.expression;

import java.util.Arrays;
import java.util.List;

import hardcoded.compiler.Expression;
import hardcoded.compiler.Expression.*;

public final class ExpressionParser {
	private static final boolean isNumbers(Expression... array) {
		for(Expression e : array) if(!isNumber(e)) return false;
		return true;
	}
	
	private static final boolean isNumber(Expression a) {
		if(!(a instanceof AtomExpr)) return false;
		return ((AtomExpr)a).isNumber();
	}
	
	private static final List<AtomType> types = Arrays.asList(AtomType.float8, AtomType.float4, AtomType.int8, AtomType.int4, AtomType.int2, AtomType.int1);
	private static final AtomType nextAtom(AtomType a, AtomType b) {
		int ax = types.indexOf(a);
		int bx = types.indexOf(b);
		return types.get(Math.min(ax, bx));
	}
	
	private static final boolean isFloating(AtomType a) {
		return a == AtomType.float8 || a == AtomType.float4;
	}
	
	/**
	 *<pre>
	 * 0: float float
	 * 1: float int
	 * 2: int   float
	 * 3: int   int
	 *</pre>
	 * 
	 * @param e0
	 * @param e1
	 * @return
	 */
	private static final int getType(AtomType e0, AtomType e1) {
		boolean a = isFloating(e0);
		boolean b = isFloating(e1);
		return (a ? 0:2) + (b ? 0:1);
	}
	
	private static abstract class BiOpr {
		public AtomExpr run(AtomExpr a, AtomExpr b) {
			AtomExpr c = null;
			switch(getType(a.atomType, b.atomType)) {
				case 0: c = o(a.f_value, b.f_value); break;
				case 1: c = o(a.f_value, b.i_value); break;
				case 2: c = o(a.i_value, b.f_value); break;
				case 3: c = o(a.i_value, b.i_value); break;
				default: throw new RuntimeException("Invalid index");
			}
			
			return c.convert(nextAtom(a.atomType, b.atomType));
		}
		
		public AtomExpr o(long a, long b) { throw new UnsupportedOperationException(error()); }
		public AtomExpr o(long a, double b) { throw new UnsupportedOperationException(error()); }
		public AtomExpr o(double a, long b) { throw new UnsupportedOperationException(error()); }
		public AtomExpr o(double a, double b) { throw new UnsupportedOperationException(error()); }
		public String error() { return "Invalid operation"; }
	 }
	
	private static abstract class UnOpr {
		public AtomExpr run(AtomExpr a) {
			if(isFloating(a.atomType)) {
				return o(a.f_value).convert(a.atomType);
			} else {
				return o(a.i_value).convert(a.atomType);
			}
		}
		
		public AtomExpr o(long a) { throw new UnsupportedOperationException(error()); }
		public AtomExpr o(double a) { throw new UnsupportedOperationException(error()); }
		public String error() { return "Invalid operation"; }
	 }
	
	private static final BiOpr ADD = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a + b); }
		public AtomExpr o(long a, double b) { return new AtomExpr(a + b); }
		public AtomExpr o(double a, long b) { return new AtomExpr(a + b); }
		public AtomExpr o(double a, double b) { return new AtomExpr(a + b); }
	};
	
	private static final BiOpr MUL = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a * b); }
		public AtomExpr o(long a, double b) { return new AtomExpr(a * b); }
		public AtomExpr o(double a, long b) { return new AtomExpr(a * b); }
		public AtomExpr o(double a, double b) { return new AtomExpr(a * b); }
	};
	
	private static final BiOpr DIV = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a / b); }
		public AtomExpr o(long a, double b) { return new AtomExpr(a / b); }
		public AtomExpr o(double a, long b) { return new AtomExpr(a / b); }
		public AtomExpr o(double a, double b) { return new AtomExpr(a / b); }
	};
	
	
	private static final BiOpr XOR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a ^ b); }
		public String error() { return "XOR is not allowed for float values."; }
	};
	
	private static final BiOpr AND = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a & b); }
		public String error() { return "AND is not allowed for float values."; }
	};
	
	private static final BiOpr OR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a | b); }
		public String error() { return "OR is not allowed for float values."; }
	};
	
	private static final BiOpr SHR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a >>> b); }
		public String error() { return "SHR is not allowed for float values."; }
	};
	
	private static final BiOpr SHL = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a << b); }
		public String error() { return "SHL is not allowed for float values."; }
	};
	
	private static final BiOpr EQ = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a == b ? 1:0); }
		public AtomExpr o(long a, double b) { return new AtomExpr(a == b ? 1:0); }
		public AtomExpr o(double a, long b) { return new AtomExpr(a == b ? 1:0); }
		public AtomExpr o(double a, double b) { return new AtomExpr(a == b ? 1:0); }
	};
	
	private static final BiOpr LT = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a < b ? 1:0); }
		public AtomExpr o(long a, double b) { return new AtomExpr(a < b ? 1:0); }
		public AtomExpr o(double a, long b) { return new AtomExpr(a < b ? 1:0); }
		public AtomExpr o(double a, double b) { return new AtomExpr(a < b ? 1:0); }
	};
	
	private static final BiOpr LTE = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a <= b ? 1:0); }
		public AtomExpr o(long a, double b) { return new AtomExpr(a <= b ? 1:0); }
		public AtomExpr o(double a, long b) { return new AtomExpr(a <= b ? 1:0); }
		public AtomExpr o(double a, double b) { return new AtomExpr(a <= b ? 1:0); }
	};
	
	private static final BiOpr CAND = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(((a != 0) && (b != 0)) ? 1:0); }
		public AtomExpr o(long a, double b) { return new AtomExpr(((a != 0) && (b != 0)) ? 1:0); }
		public AtomExpr o(double a, long b) { return new AtomExpr(((a != 0) && (b != 0)) ? 1:0); }
		public AtomExpr o(double a, double b) { return new AtomExpr(((a != 0) && (b != 0)) ? 1:0); }
	};
	
	private static final BiOpr COR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(((a != 0) || (b != 0)) ? 1:0); }
		public AtomExpr o(long a, double b) { return new AtomExpr(((a != 0) || (b != 0)) ? 1:0); }
		public AtomExpr o(double a, long b) { return new AtomExpr(((a != 0) || (b != 0)) ? 1:0); }
		public AtomExpr o(double a, double b) { return new AtomExpr(((a != 0) || (b != 0)) ? 1:0); }
	};
	
	
	
	private static final UnOpr NOR = new UnOpr() {
		public AtomExpr o(long a) { return new AtomExpr(~a); }
		public String error() { return "NOR is not allowed for float values."; }
	};
	
	private static final UnOpr NOT = new UnOpr() {
		public AtomExpr o(long a) { return new AtomExpr(a == 0 ? 1:0); }
		public AtomExpr o(double a) { return new AtomExpr(Double.doubleToLongBits(a) == 0 ? 1:0); }
	};
	
	private static final UnOpr NEG = new UnOpr() {
		public AtomExpr o(long a) { return new AtomExpr(-a); }
		public AtomExpr o(double a) { return new AtomExpr(-a); }
	};
	
	public static Expression add(Expression e0, Expression e1) { return isNumbers(e0, e1) ? ADD.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression mul(Expression e0, Expression e1) { return isNumbers(e0, e1) ? MUL.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression div(Expression e0, Expression e1) { return isNumbers(e0, e1) ? DIV.run((AtomExpr)e0, (AtomExpr)e1):null; }
	
	public static Expression xor(Expression e0, Expression e1) { return isNumbers(e0, e1) ? XOR.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression or(Expression e0, Expression e1) { return isNumbers(e0, e1) ? OR.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression and(Expression e0, Expression e1) { return isNumbers(e0, e1) ? AND.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression shr(Expression e0, Expression e1) { return isNumbers(e0, e1) ? SHR.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression shl(Expression e0, Expression e1) { return isNumbers(e0, e1) ? SHL.run((AtomExpr)e0, (AtomExpr)e1):null; }
	
	public static Expression lt(Expression e0, Expression e1) { return isNumbers(e0, e1) ? LT.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression lte(Expression e0, Expression e1) { return isNumbers(e0, e1) ? LTE.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression eq(Expression e0, Expression e1) { return isNumbers(e0, e1) ? EQ.run((AtomExpr)e0, (AtomExpr)e1):null; }
	

	public static Expression cand(Expression e0, Expression e1) { return isNumbers(e0, e1) ? CAND.run((AtomExpr)e0, (AtomExpr)e1):null; }
	public static Expression cor(Expression e0, Expression e1) { return isNumbers(e0, e1) ? COR.run((AtomExpr)e0, (AtomExpr)e1):null; }
	
	public static Expression neg(Expression e0) { return isNumber(e0) ? NEG.run((AtomExpr)e0):null; }
	public static Expression not(Expression e0) { return isNumber(e0) ? NOT.run((AtomExpr)e0):null; }
	public static Expression nor(Expression e0) { return isNumber(e0) ? NOR.run((AtomExpr)e0):null; }

	public static Expression compute(ExprType type, OpExpr e) {
		switch(type) {
			case neg: return ExpressionParser.neg(e.first());
			case not: return ExpressionParser.not(e.first());
			
			case mul: return ExpressionParser.mul(e.first(), e.last());
			case div: return ExpressionParser.div(e.first(), e.last());
			
			case nor: return ExpressionParser.nor(e.first());
			case xor: return ExpressionParser.xor(e.first(), e.last());
			case or: return ExpressionParser.or(e.first(), e.last());
			case and: return ExpressionParser.and(e.first(), e.last());
			case shr: return ExpressionParser.shr(e.first(), e.last());
			case shl: return ExpressionParser.shl(e.first(), e.last());
			
			case eq: return ExpressionParser.eq(e.first(), e.last());
			case lte: return ExpressionParser.lte(e.first(), e.last());
			case lt: return ExpressionParser.lt(e.first(), e.last());
			default: return null;
		}
	}
	
	// TODO: Create a check type size function that goes throuh a expression tree and get's the largest type that was added in that tree.
	// float + double + byte + byte + byte. would give double
	// add(ident, cast(int, neg(double))) would give either int or the size of the identifier 'ident';
	// User defined types +????????
}
