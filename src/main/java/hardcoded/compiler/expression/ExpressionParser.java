package hardcoded.compiler.expression;

import hardcoded.compiler.constants.ExprType;

public final class ExpressionParser {
	private static final boolean isNumbers(Expression... array) {
		for(Expression e : array) {
			if(!(e instanceof AtomExpr atom) || !atom.isNumber()) {
				return false;
			}
		}
		return true;
	}
	
	@FunctionalInterface
	private static interface BiFnc {
		public default AtomExpr run(AtomExpr a, AtomExpr b) {
			boolean sign = !a.getAtomType().isSigned()
						|| !b.getAtomType().isSigned();
			
			return new AtomExpr(o(sign, a.number(), b.number())).convert(LowType.largest(a.getAtomType(), b.getAtomType()));
		}
		
		long o(boolean unsigned, long a, long b);
	}
	
	@FunctionalInterface
	private static interface UnFnc {
		public default AtomExpr run(AtomExpr a) { return new AtomExpr(o(!a.getAtomType().isSigned(), a.number())).convert(a.getAtomType()); }
		long o(boolean unsigned, long a);
	}
	
	
	// Unsigned > Signed
	
	@UnsignedSafe private static final BiFnc ADD = ($, a, b) -> (a + b);
	@UnsignedSafe private static final BiFnc SUB = ($, a, b) -> (a - b);
	
	@UnsignedSafe private static final BiFnc MUL = ($, a, b) -> (a * b);
	private static final BiFnc DIV = (sign, a, b) -> (a / b);
	private static final BiFnc MOD = (sign, a, b) -> (a % b);
	
	@UnsignedSafe private static final BiFnc XOR = ($, a, b) -> (a ^ b);
	@UnsignedSafe private static final BiFnc AND = ($, a, b) -> (a & b);
	@UnsignedSafe private static final BiFnc OR = ($, a, b) -> (a | b);
	
	private static final BiFnc SHR = (sign, a, b) -> (a >>> b);
	private static final BiFnc SHL = (sign, a, b) -> (a << b);
	
	@UnsignedSafe private static final BiFnc EQ = ($, a, b) -> (a == b ? 1:0);
	@UnsignedSafe private static final BiFnc NEQ = ($, a, b) -> (a != b ? 1:0);
	
	private static final BiFnc LT = (sign, a, b) -> (a < b ? 1:0);
	private static final BiFnc LTE = (sign, a, b) -> (a <= b ? 1:0);
	private static final BiFnc GT = (sign, a, b) -> (a > b ? 1:0);
	private static final BiFnc GTE = (sign, a, b) -> (a >= b ? 1:0);
	
	@UnsignedSafe private static final BiFnc CAND = ($, a, b) -> (((a != 0) && (b != 0)) ? 1:0);
	@UnsignedSafe private static final BiFnc COR = ($, a, b) -> (((a != 0) || (b != 0)) ? 1:0);
	
	@UnsignedSafe private static final UnFnc NOR = ($, a) -> (~a);
	@UnsignedSafe private static final UnFnc NOT = ($, a) -> (a == 0 ? 1:0);
	
	// Does not change unsigned values.
	private static final UnFnc NEG = (sign, a) -> (-a);
	
	
	
	public static Expression compute(ExprType type, OpExpr e) {
		return compute(type, e.first(), e.last());
	}
	
	public static Expression compute(ExprType type, Expression e0, Expression e1) {
//		if(!isAtoms(e0, e1)) return null;
//		
//		if(type == ExprType.add || type == ExprType.sub) {
//			// Convert [ identifier + 0 ], [ identifier - 0 ] -> [ identifier ]
//			if(b.isNumber() && b.isZero() && a.isIdentifier()) {
//				return a;
//			}
//			
//			boolean isAdd = type == ExprType.add;
//			if(a.isNumber() && a.isZero() && b.isIdentifier()) {
//				return isAdd ? b:new OpExpr(ExprType.neg, b);
//			}
//		}
//		
		if(!isNumbers(e0, e1 == null ? e0:e1)) return null;
		AtomExpr a = (AtomExpr)e0;
		AtomExpr b = (AtomExpr)e1;
		
		// TODO: cand, cor
		// TODO: add, sub contains multiple values sometimes.
		switch(type) {
			case neg: return NEG.run(a);
			case nor: return NOR.run(a);
			case not: return NOT.run(a);
			
			case mul: return MUL.run(a, b);
			case div: return DIV.run(a, b);
			case mod: return MOD.run(a, b);

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

/**
 * Temporary interface to tell the dev that this operation is unsigned
 * safe meaning that the operation wont change depending on sign.
 */
@interface UnsignedSafe {}