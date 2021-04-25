package com.hardcoded.compiler.llcode;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Instruction.Type;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.expression.AtomExpr;
import com.hardcoded.compiler.impl.expression.EmptyExpr;
import com.hardcoded.compiler.impl.expression.Expr;
import com.hardcoded.compiler.impl.instruction.*;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.options.Options;
import com.hardcoded.options.Options.Key;

/**
 * A code generator.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
class AmpleCodeGenerator {
	// private static final Log LOGGER = Log.getLogger();
	private int temp_index = -2;
	
	public AmpleCodeGenerator() {
		
	}
	
	// private Options options;
	public ImCode process(Options options, ProgramStat stat) {
		ImCode code = new ImCode();
		processProgramStat(stat, code);
		return code;
	}
	
	private InstList getList(Stat stat) {
		InstList list = InstList.get();
//		if(options.getInt(Key.OPTIMIZATION) > 0) {
//			InstParam start = InstParam.get(stat.getStartOffset());
//			InstParam end = InstParam.get(stat.getEndOffset());
//			list.add(Inst.get(Type.MARKER).addParams(start, end, InstParam.get(stat.toString())));
//		}
		return list;
	}
	
	private InstList getList(Expression expr) {
		InstList list = InstList.get();
//		InstParam start = InstParam.get(expr.getStartOffset());
//		InstParam end = InstParam.get(expr.getEndOffset());
//		list.add(Inst.get(Type.MARKER).addParams(start, end, InstParam.get(expr.toString())));
		return list;
	}
	
	private InstParam getTempParam() {
		return InstParam.get(Reference.get(temp_index--));
	}
	
	private InstParam getNewLabel() {
		return InstParam.get(Reference.get(temp_index--, Reference.Type.LABEL));
	}
	
	
	private void processStatement(Statement stat, ImCode code, Reference ref) {
		if(stat instanceof ImportStat) return;
		
		if(stat instanceof DefineStat) {
			code.push(processDefineStat((DefineStat)stat, null));
			return;
		}
		
		if(stat instanceof ClassStat) return; // TODO:
		if(stat instanceof FuncStat) {
			processFuncStat((FuncStat)stat, code);
			return;
		}
		
		
		throw new RuntimeException("Invalid statement: " + ((stat == null) ? "<null>":(stat.getClass())));
	}
	
	private InstList processStat(Statement stat) { return processStat(stat, InstParam.EMPTY); }
	private InstList processStat(Statement stat, InstParam ref) {
		if(EmptyStat.isEmpty(stat)) return InstList.get();
		
		InstList list = InstList.get();
		
		if(stat instanceof ScopeStat) {
			for(Statement s : stat.getStatements()) {
				list.add(processStat(s, ref));
			}
			
			return list;
		}
		
		if(stat instanceof SwitchStat) {
			list.add(processSwitchStat((SwitchStat)stat, ref));
			return list;
		}
		
		if(stat instanceof ExprStat) {
			list.add(processExprStat((ExprStat)stat, ref));
			return list;
		}
		
		if(stat instanceof ForStat) {
			list.add(processForStat((ForStat)stat, ref));
			return list;
		}
		
		if(stat instanceof DefineStat) {
			list.add(processDefineStat((DefineStat)stat, ref));
			return list;
		}
		
		if(stat instanceof WhileStat) {
			list.add(processWhileStat((WhileStat)stat, ref));
			return list;
		}
		
		if(stat instanceof DoWhileStat) {
			list.add(processDoWhileStat((DoWhileStat)stat, ref));
			return list;
		}
		
		if(stat instanceof GotoStat) {
			list.add(Inst.get(Type.BR).addParam(InstParam.get(((GotoStat)stat).getReference())));
			return list;
		}
		
		if(stat instanceof LabelStat) {
			list.add(Inst.get(Type.LABEL).addParam(InstParam.get(((LabelStat)stat).getReference())));
			return list;
		}
		
		if(stat instanceof ContinueStat) {
			if(continue_label == null) throw_exception(stat, "Invalid placement of continue");
			list.add(Inst.get(Type.BR).addParam(continue_label));
			return list;
		}
		
		if(stat instanceof BreakStat) {
			if(break_label == null) throw_exception(stat, "Invalid placement of break");
			list.add(Inst.get(Type.BR).addParam(break_label));
			return list;
		}
		
		if(stat instanceof ReturnStat) {
			ReturnStat s = (ReturnStat)stat;
			List<Statement> stats = s.getStatements();
			
			if(stats.isEmpty()) {
				list.add(Inst.get(Type.RET).addParam(InstParam.EMPTY));
			} else {
				InstParam temp = getTempParam();
				list.add(processStat(stats.get(0), temp));
				list.add(Inst.get(Type.RET).addParam(temp));
			}
			
			return list;
		}
		
		if(stat instanceof IfStat) {
			list.add(processIfStat((IfStat)stat, ref));
			return list;
		}
		
		throw new RuntimeException("Invalid statement: " + ((stat == null) ? "<null>":(stat.getClass())));
	}
	
	private void processProgramStat(ProgramStat stat, ImCode code) {
		for(Statement s : stat.getStatements()) {
			processStatement(s, code, null);
		}
	}
	
	private InstList processDefineStat(DefineStat stat, InstParam param) {
		InstList list = getList(stat);
		InstParam ref = createParam(stat.getReference());
		
		for(Statement s : stat.getStatements()) {
			list.add(processStat(s, ref));
		}
		
		return list;
	}
	
	private InstList processExprStat(ExprStat stat, InstParam param) {
		InstList list = getList(stat);
		
		Expression expr = stat.getExpression();
		
		if(!param.isEmpty()) {
			if(isAtom(expr)) {
				list.add(Inst.get(Type.SET).addParams(param, createParam(expr)));
			} else {
				list.add(processExpr(expr, param));
			}
		} else {
			if(!isAtom(expr)) {
				list.add(processExpr(expr, InstParam.EMPTY));
			}
		}
		
		return list;
	}
	
	private void processFuncStat(FuncStat stat, ImCode code) {
		InstList list = getList(stat);
		
		for(Statement s : stat.getStatements()) {
			list.add(processStat(s));
		}
		
		list.add(Inst.get(Type.RET).addParam(InstParam.EMPTY));
		code.push(list);
	}
	
	private InstParam continue_label;
	private InstParam break_label;
	private InstList processForStat(ForStat stat, InstParam ref) {
		InstList list = getList(stat);
		
		InstParam label_next = getNewLabel();
		InstParam label_loop = getNewLabel();
		InstParam label_end = getNewLabel();
		
		InstParam old_continue = continue_label;
		InstParam old_break = break_label;
		continue_label = label_next;
		break_label = label_end;
		
		List<Statement> stats = stat.getStatements();
		list.add(processStat(stats.get(0)));
		
		
		System.out.println(stat + ", " + stats + ", " + stat.getStartOffset());
		if(!EmptyStat.isEmpty(stats.get(2))) {
			// ============================== //
			// for(x; y; z) { ... }
			//   ; Define x
			//
			//   check = y;
			//   brz [check], end
			//   br loop
			// next:
			//   ; Calculate action
			//   check = y;
			//   brz [check], end
			// loop:
			//   ; ...
			//   br [next]
			// end:
			
			if(!EmptyStat.isEmpty(stats.get(1))) {
				InstParam temp = getTempParam();
				list.add(processStat(stats.get(1), temp));
				list.add(Inst.get(Type.BRZ).addParams(temp, label_end));
				list.add(Inst.get(Type.BR).addParam(label_loop));
			}
			
			list.add(Inst.get(Type.LABEL).addParam(label_next));
			list.add(processStat(stats.get(2)));
			
			if(!EmptyStat.isEmpty(stats.get(1))) {
				InstParam temp = getTempParam();
				list.add(processStat(stats.get(1), temp));
				list.add(Inst.get(Type.BRZ).addParams(temp, label_end));
			}

			list.add(Inst.get(Type.LABEL).addParam(label_loop));
		}  else {
			// ============================== //
			// for(x; y; ) { ... }
			//   ; Define x
			// next:
			//   check = y;
			//   brz [check], [end]
			//   ; ...
			//   br [next]
			// end:
			
			list.add(Inst.get(Type.LABEL).addParam(label_next));
			
			if(!EmptyStat.isEmpty(stats.get(1))) {
				InstParam temp = getTempParam();
				list.add(processStat(stats.get(1), temp));
				list.add(Inst.get(Type.BRZ).addParams(temp, label_end));
			}
		}

		list.add(processStat(stats.get(3)));
		list.add(Inst.get(Type.BR).addParam(label_next));
		list.add(Inst.get(Type.LABEL).addParam(label_end));
		continue_label = old_continue;
		break_label = old_break;
		return list;
	}
	
	private InstList processIfStat(IfStat stat, InstParam ref) {
		InstList list = getList(stat);

		InstParam label_end = getNewLabel();
		
		List<Statement> stats = stat.getStatements();
		InstParam temp = getTempParam();
		
		if(!stat.hasElse()) {
			// ============================== //
			// if(x) { ... }
			
			// brz [end] [x]				Branch to [end] if [x] is zero
			//		    ...
			// end:
			
			list.add(processStat(stats.get(0), temp));
			list.add(Inst.get(Type.BRZ).addParams(temp, label_end));
			list.add(processStat(stats.get(1)));
			list.add(Inst.get(Type.LABEL).addParam(label_end));
		} else {
			// ============================== //
			// if(x) { ... } else { ... }
			
			// brz [else] [x]				Branch to [else] if [x] is zero
			//    ...
			// br [end]						Branch to [end]
			// else:
			//    ...
			// end:
			
			InstParam label_else = getNewLabel();
			
			list.add(processStat(stats.get(0), temp));
			list.add(Inst.get(Type.BRZ).addParams(temp, label_else));
			list.add(processStat(stats.get(1)));
			list.add(Inst.get(Type.BR).addParam(label_end));
			list.add(Inst.get(Type.LABEL).addParam(label_else));
			list.add(processStat(stats.get(2)));
			list.add(Inst.get(Type.LABEL).addParam(label_end));
		}
		
		return list;
	}
	
	private InstList processWhileStat(WhileStat stat, InstParam ref) {
		InstList list = getList(stat);
		
		// ============================== //
		// while(x) { ... }

		// next:
		//   ; if x has side effects evaluate x here!
		// brz [end] x					Branch to [end] if x is zero
		// loop:
		//   ...
		//   ; if x has side effects jump to next...
		//   br [next]
		// end:
		
		InstParam label_next = getNewLabel();
		InstParam label_loop = getNewLabel();
		InstParam label_end = getNewLabel();

		InstParam temp = getTempParam();
		List<Statement> stats = stat.getStatements();
		
		InstParam old_continue = continue_label;
		InstParam old_break = break_label;
		continue_label = label_next;
		break_label = label_end;
		
		list.add(Inst.get(Type.LABEL).addParam(label_next));
		list.add(processStat(stats.get(0)));
		list.add(Inst.get(Type.BRZ).addParams(temp, label_end));
		list.add(Inst.get(Type.LABEL).addParam(label_loop));
		list.add(processStat(stats.get(1)));
		list.add(Inst.get(Type.BR).addParam(label_next));
		list.add(Inst.get(Type.LABEL).addParam(label_end));
		
		continue_label = old_continue;
		break_label = old_break;
		return list;
	}
	
	private InstList processDoWhileStat(DoWhileStat stat, InstParam ref) {
		InstList list = getList(stat);
		
		// ============================== //
		// do { ... } while(x);
		
		// next:
		//   ; run code
		// 
		// next:
		//   ; if x has side effects evaluate x here!
		// brz [end] x					Branch to [end] if x is zero
		// loop:
		//   ...
		//   ; if x has side effects jump to next...
		//   br [next]
		// end:
		
		InstParam label_next = getNewLabel();
		InstParam label_loop = getNewLabel();
		InstParam label_end = getNewLabel();
		

		InstParam temp = getTempParam();
		List<Statement> stats = stat.getStatements();
		
		InstParam old_continue = continue_label;
		InstParam old_break = break_label;
		continue_label = label_next;
		break_label = label_end;
		
		list.add(Inst.get(Type.BR).addParam(label_loop));
		list.add(Inst.get(Type.LABEL).addParam(label_next));
		list.add(processStat(stats.get(0)));
		list.add(Inst.get(Type.BRZ).addParams(temp, label_end));
		list.add(Inst.get(Type.LABEL).addParam(label_loop));
		list.add(processStat(stats.get(1)));
		list.add(Inst.get(Type.BR).addParam(label_next));
		list.add(Inst.get(Type.LABEL).addParam(label_end));
		
		continue_label = old_continue;
		break_label = old_break;
		return list;
	}
	
	private InstList processSwitchStat(SwitchStat stat, InstParam ref) {
		InstList list = getList(stat);
		
		// ============================== //
		// switch(A) {
		//   case B: ...
		//   case C: ...
		// }
		
		//   set [#0], A
		//   eq [#1], [#0], B
		//   brz [case_B]
		//   eq [#1], [#0], C
		//   brz [case_C]
		//   br [switch_end]
		//   ; for all case statements
		// case_B:
		//   ; code inside B
		// case_C:
		//   ; code inside C
		// switch_end:
		// 
		
		InstParam label_end = getNewLabel();
		
		

		InstParam temp0 = getTempParam();
		InstParam temp1 = getTempParam();
		List<Statement> stats = stat.getStatements();
		List<InstParam> cases = new ArrayList<>();
		
		InstParam old_break = break_label;
		break_label = label_end;
		
		list.add(processStat(stats.get(0), temp0));
		
		for(int i = 1; i < stats.size(); i++) {
			CaseStat s = (CaseStat)stats.get(i);
			Expression e = ((ExprStat)s.getStatements().get(0)).getExpression();
			
			InstParam label = getNewLabel();
			cases.add(label);
			list.add(Inst.get(Type.EQ).addParams(temp1, temp0, resolveParam(e, list)));
			list.add(Inst.get(Type.BRZ).addParams(temp1, label));
		}
		list.add(Inst.get(Type.BR).addParam(label_end));
		for(int i = 1; i < stats.size(); i++) {
			list.add(Inst.get(Type.LABEL).addParam(cases.get(i - 1)));
			CaseStat s = (CaseStat)stats.get(i);
			
			List<Statement> case_stats = s.getStatements();
			for(int j = 1; j < case_stats.size(); j++) {
				list.add(processStat(case_stats.get(j)));
			}
		}
		list.add(Inst.get(Type.LABEL).addParam(label_end));
		
		break_label = old_break;
		return list;
	}
	
	
	// ------
	
	private boolean isAtom(Expression expr) {
		return expr instanceof AtomExpr;
	}
	
	private InstParam createParam(Reference ref) {
		if(ref == null) return InstParam.EMPTY;
		return InstParam.get(ref);
	}
	
	private InstParam createParam(Expression expr) {
		if(expr instanceof AtomExpr) {
			AtomExpr e = (AtomExpr)expr;
			
			if(e.isNumber()) return InstParam.get(e.getNumber());
			if(e.isString()) return InstParam.get(e.getString());
			if(e.isReference()) return InstParam.get(e.getReference());
		}
		
		throw new CodeGenException("Could not create a parameter from: " + expr);
	}
	
	private InstParam resolveParam(Expression expr, InstList list) {
		if(isAtom(expr)) {
			return createParam(expr);
		}
		
		InstParam temp = getTempParam();
		list.add(processExpr(expr, temp));
		return temp;
	}
	
	private InstParam resolveParam(Expression expr, InstParam temp, InstList list) {
		if(isAtom(expr)) {
			return createParam(expr);
		}
		
		list.add(processExpr(expr, temp));
		return temp;
	}
	
	// Expressions
	private InstList processExpr(Expression expr, InstParam ref) {
		if(ref == null) throw new NullPointerException();
		if(EmptyExpr.isEmpty(expr)) return InstList.get();
		
		InstList list = getList(expr);
		Expr ex = (Expr)expr;
		
		switch(ex.getType()) {
			case ADD: case SUB:
			case MUL: case DIV:
			case SHL: case SHR:
			case OR: case XOR:
			case EQ: case NEQ:
			case GT: case GTE:
			case LT: case LTE: {
				InstParam param_0 = ref;
				InstParam param_1 = resolveParam(ex.get(0), list);
				InstParam param_2 = resolveParam(ex.get(1), list);
				list.add(Inst.get(convertBinaryType(ex.getType())).addParams(param_0, param_1, param_2));
				return list;
			}

			case SET: {
				InstParam param_0 = resolveParam(ex.get(0), list);
				InstParam param_1 = resolveParam(ex.get(1), list);
				list.add(Inst.get(Type.SET).addParams(param_0, param_1));
				if(!ref.isEmpty()) list.add(Inst.get(Type.SET).addParams(ref, param_1));
				return list;
			}
			
			case NOT:
			case NOR:
			case NEG: {
				InstParam param_0 = ref;
				InstParam param_1 = resolveParam(ex.get(0), list);
				list.add(Inst.get(convertUnaryType(ex.getType())).addParams(param_0, param_1));
				return list;
			}
			
			case CALL: {
				List<Expression> params = ex.getExpressions();
				Inst inst = Inst.get(Type.CALL);
				
				inst.addParam(ref);
				for(Expression e : params) {
					inst.addParam(resolveParam(e, list));
				}
				
				list.add(inst);
				return list;
			}
			
			case COMMA: {
				List<Expression> params = ex.getExpressions();
				for(int i = 0; i < params.size() - 1; i++) {
					list.add(processExpr(params.get(i), InstParam.EMPTY));
				}
				
				if(!params.isEmpty()) {
					Expression last = params.get(params.size() - 1);
					list.add(processExpr(last, ref));
				}
				
				return list;
			}
			
			case COR: {
				InstParam label_end = getNewLabel();
				// (A || B)
				//   set [ref], [1]
				//   bnz [label_end], [A]
				// ...
				//   set [ref], [0]
				// label_end:
				
				if(!ref.isEmpty()) {
					list.add(Inst.get(Type.SET).addParams(ref, InstParam.get(1)));
				}
				
				InstParam temp = getTempParam();
				List<Expression> params = ex.getExpressions();
				for(int i = 0; i < params.size(); i++) {
					Expression e = params.get(i);
					InstParam param = resolveParam(e, temp, list);
					if(!param.isEmpty()) {
						list.add(Inst.get(Type.BNZ).addParams(param, label_end));
					}
				}
				
				if(!ref.isEmpty()) {
					list.add(Inst.get(Type.SET).addParams(ref, InstParam.get(0)));
				}
				
				list.add(Inst.get(Type.LABEL).addParam(label_end));
				return list;
			}
			
			case CAND: {
				InstParam label_end = getNewLabel();
				// (A && B)
				//   set [ref], [0]
				//   brz [label_end], [A]
				// ...
				//   set [ref], [1]
				// label_end:
				
				if(!ref.isEmpty()) {
					list.add(Inst.get(Type.SET).addParams(ref, InstParam.get(0)));
				}
				
				InstParam temp = getTempParam();
				List<Expression> params = ex.getExpressions();
				for(int i = 0; i < params.size(); i++) {
					Expression e = params.get(i);
					InstParam param = resolveParam(e, temp, list);
					if(!param.isEmpty()) {
						list.add(Inst.get(Type.BRZ).addParams(param, label_end));
					}
				}
				
				if(!ref.isEmpty()) {
					list.add(Inst.get(Type.SET).addParams(ref, InstParam.get(1)));
				}
				
				list.add(Inst.get(Type.LABEL).addParam(label_end));
				return list;
			}
			
			case ATOM: {
				if(!ref.isEmpty()) {
					list.add(Inst.get(Type.SET).addParams(ref, createParam(expr)));
				}
				return list;
			}
			
			case ARRAY: {
				System.out.println("TODO: Implement " + expr.getType());
				return list;
			}
			
			default:
		}
		
		throw new RuntimeException("Invalid expression: [" + expr.getType() + "] " + ((expr == null) ? "<null>":(expr.getClass())));
	}
	
	private Type convertBinaryType(Expression.Type type) {
		return Type.valueOf(type.name());
	}
	
	private Type convertUnaryType(Expression.Type type) {
		return Type.valueOf(type.name());
	}
	
	<T> T throw_exception(Statement stat, String format, Object... args) {
		String extra = String.format("(line: %d, column: %d) ", stat.getStartOffset(), stat.getEndOffset());
		throw new CodeGenException(extra + format, args);
	}
}
