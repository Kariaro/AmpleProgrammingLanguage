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
	
	private static final List<AtomType> types = Arrays.asList(AtomType.i64, AtomType.i32, AtomType.i16, AtomType.i8);
	private static final AtomType nextAtom(AtomType a, AtomType b) {
		int ax = types.indexOf(a);
		int bx = types.indexOf(b);
		return types.get(Math.min(ax, bx));
	}
	
	private static abstract class BiOpr {
		public AtomExpr run(AtomExpr a, AtomExpr b) {
			return o(a.i_value, b.i_value).convert(nextAtom(a.atomType, b.atomType));
		}
		
		public AtomExpr o(long a, long b) { throw new UnsupportedOperationException(error()); }
		public String error() { return "Invalid operation"; }
	 }
	
	private static abstract class UnOpr {
		public AtomExpr run(AtomExpr a) { return o(a.i_value).convert(a.atomType); }
		public AtomExpr o(long a) { throw new UnsupportedOperationException(error()); }
		public String error() { return "Invalid operation"; }
	 }
	
	private static final BiOpr ADD = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a + b); }
	};
	
	private static final BiOpr SUB = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a - b); }
	};
	
	private static final BiOpr MUL = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a * b); }
	};
	
	private static final BiOpr DIV = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a / b); }
	};
	
	
	private static final BiOpr XOR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a ^ b); }
		public String error() { return "XOR is not allowed for float values."; }
	};
	
	private static final BiOpr AND = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a & b); }
	};
	
	private static final BiOpr OR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a | b); }
	};
	
	private static final BiOpr SHR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a >>> b); }
	};
	
	private static final BiOpr SHL = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a << b); }
	};
	
	private static final BiOpr EQ = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a == b ? 1:0); }
	};
	
	private static final BiOpr NEQ = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a != b ? 1:0); }
	};
	
	private static final BiOpr LT = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a < b ? 1:0); }
	};
	
	private static final BiOpr LTE = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a <= b ? 1:0); }
	};
	
	private static final BiOpr GT = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a > b ? 1:0); }
	};
	
	private static final BiOpr GTE = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(a >= b ? 1:0); }
	};
	
	private static final BiOpr CAND = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(((a != 0) && (b != 0)) ? 1:0); }
	};
	
	private static final BiOpr COR = new BiOpr() {
		public AtomExpr o(long a, long b) { return new AtomExpr(((a != 0) || (b != 0)) ? 1:0); }
	};
	
	
	
	private static final UnOpr NOR = new UnOpr() {
		public AtomExpr o(long a) { return new AtomExpr(~a); }
		public String error() { return "NOR is not allowed for float values."; }
	};
	
	private static final UnOpr NOT = new UnOpr() {
		public AtomExpr o(long a) { return new AtomExpr(a == 0 ? 1:0); }
	};
	
	private static final UnOpr NEG = new UnOpr() {
		public AtomExpr o(long a) { return new AtomExpr(-a); }
	};
	
	public static Expression add(AtomExpr a, AtomExpr b) { return ADD.run(a, b); }
	public static Expression mul(AtomExpr a, AtomExpr b) { return MUL.run(a, b); }
	public static Expression div(AtomExpr a, AtomExpr b) { return DIV.run(a, b); }
	
	public static Expression xor(AtomExpr a, AtomExpr b) { return XOR.run(a, b); }
	public static Expression or(AtomExpr a, AtomExpr b) { return OR.run(a, b); }
	public static Expression and(AtomExpr a, AtomExpr b) { return AND.run(a, b); }
	public static Expression shr(AtomExpr a, AtomExpr b) { return SHR.run(a, b); }
	public static Expression shl(AtomExpr a, AtomExpr b) { return SHL.run(a, b); }
	
	public static Expression lt(AtomExpr a, AtomExpr b) { return LT.run(a, b); }
	public static Expression lte(AtomExpr a, AtomExpr b) { return LTE.run(a, b); }
	public static Expression gt(AtomExpr a, AtomExpr b) { return GT.run(a, b); }
	public static Expression gte(AtomExpr a, AtomExpr b) { return GTE.run(a, b); }
	public static Expression eq(AtomExpr a, AtomExpr b) { return EQ.run(a, b); }
	public static Expression neq(AtomExpr a, AtomExpr b) { return NEQ.run(a, b); }
	

	public static Expression cand(AtomExpr a, AtomExpr b) { return CAND.run(a, b); }
	public static Expression cor(AtomExpr a, AtomExpr b) { return COR.run(a, b); }
	
	public static Expression neg(AtomExpr a) { return NEG.run(a); }
	public static Expression not(AtomExpr a) { return NOT.run(a); }
	public static Expression nor(AtomExpr a) { return NOR.run(a); }

	public static Expression compute(ExprType type, OpExpr e) {
		return compute(type, e.first(), e.last());
	}
	
	public static Expression compute(ExprType type, Expression e0, Expression e1) {
		if(!isNumbers(e0, e1)) return null;
		AtomExpr a = (AtomExpr)e0;
		AtomExpr b = (AtomExpr)e1;
		
		switch(type) {
			case neg: return NEG.run(a);
			case nor: return NOR.run(a);
			case not: return NOT.run(a);
			
			case mul: return MUL.run(a, b);
			case div: return DIV.run(a, b);

			case add: return ADD.run(a, b);
			case sub: return SUB.run(a, b);
			
			case xor: return XOR.run(a, b);
			
			case or: return OR.run(a, b);
			case and: return AND.run(a, b);
			case shr: return SHR.run(a, b);
			case shl: return SHL.run(a, b);
			
			case eq: return EQ.run(a, b);
			case neq: return NEQ.run(a, b);
			case lt: return LT.run(a, b);
			case lte: return LTE.run(a, b);
			case gt: return GT.run(a, b);
			case gte: return GTE.run(a, b);
			
			default: return null;
		}
	}
}
