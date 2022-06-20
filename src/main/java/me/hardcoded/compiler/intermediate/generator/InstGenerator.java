package me.hardcoded.compiler.intermediate.generator;

import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.AmpleLinker.ExportMap;
import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;
import org.apache.logging.log4j.core.jackson.ContextDataAsEntryListSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstGenerator {
	private static final InstRef NONE = new InstRef("<invalid>", ValueType.UNDEFINED, -1, 0);
	private final InstFile file;
	private final ExportMap exportMap;

	// Used for reference creation
	private final Map<Reference, InstRef> wrappedReferences;
	private int count;
	private int funcCount;
	// private InstRef breakBranch;
	// private InstRef continueBranch;

	public InstGenerator(InstFile file, ExportMap exportMap) {
		this.file = file;
		this.exportMap = exportMap;
		this.wrappedReferences = new HashMap<>();
	}

	public void generate(LinkableObject obj) {
		System.out.println("> Generating '" + obj.getFile().getAbsolutePath() + "'");
		generateProgStat(obj.getProgram());
	}

	private InstRef generateStat(Stat stat, Procedure procedure) {
		return switch (stat.getTreeType()) {
			// Root
			case PROGRAM -> generateProgStat((ProgStat) stat);

			// Statements
//			case BREAK -> generateBreakStat((BreakStat) stat, procedure);
//			case CONTINUE -> generateContinueStat((ContinueStat) stat, procedure);
			case EMPTY -> generateEmptyStat((EmptyStat) stat, procedure);
//			case FOR -> generateForStat((ForStat) stat, procedure);
			case FUNC -> generateFuncStat((FuncStat) stat, procedure);
//			case GOTO -> generateGotoStat((GotoStat) stat, procedure);
			case IF -> generateIfStat((IfStat) stat, procedure);
//			case LABEL -> generateLabelStat((LabelStat) stat, procedure);
			case RETURN -> generateReturnStat((ReturnStat) stat, procedure);
			case SCOPE -> generateScopeStat((ScopeStat) stat, procedure);
			case VAR -> generateVarStat((VarStat) stat, procedure);
//			case WHILE -> generateWhileStat((WhileStat) stat, procedure);
//			case NAMESPACE -> generateNamespaceStat((NamespaceStat) stat, procedure);

			// Expressions
			case BINARY -> generateBinaryExpr((BinaryExpr) stat, procedure);
			case CALL -> generateCallExpr((CallExpr) stat, procedure);
//			case CAST -> generateCastExpr((CastExpr) stat, procedure);
//			case COMMA -> generateCommaExpr((CommaExpr) stat, procedure);
			case NAME -> generateNameExpr((NameExpr) stat, procedure);
			case NONE -> generateNoneExpr((NoneExpr) stat, procedure);
			case NUM -> generateNumExpr((NumExpr) stat, procedure);
//			case STR -> generateStrExpr((StrExpr) stat, procedure);
			case UNARY -> generateUnaryExpr((UnaryExpr) stat, procedure);
//			case CONDITIONAL -> generateConditionalExpr((ConditionalExpr) stat, procedure);
			default -> throw new RuntimeException("Invalid expr %s".formatted(stat.getTreeType()));
		};
	}

	private InstRef generateProgStat(ProgStat stat) {
		for (Stat s : stat.getElements()) {
			count = 0;
			
			Procedure procedure = new Procedure();
			generateStat(s, procedure);
			file.addProcedure(procedure);
		}

		return NONE;
	}

//	private InstRef generateBreakStat(BreakStat stat, Procedure procedure) {
//		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(breakBranch)));
//		return NONE;
//	}
//
//	private InstRef generateContinueStat(ContinueStat stat, Procedure procedure) {
//		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(continueBranch)));
//		return NONE;
//	}

	private InstRef generateEmptyStat(EmptyStat stat, Procedure procedure) {
		return NONE;
	}

//	private InstRef generateForStat(ForStat stat, Procedure procedure) {
//		InstRef nextBranch = createLocalLabel(".for.next");
//		InstRef loopBranch = createLocalLabel(".for.loop");
//		InstRef endBranch = createLocalLabel(".for.end");
//
//		InstRef oldBreakBranch = breakBranch;
//		InstRef oldContinueBranch = continueBranch;
//		breakBranch = endBranch;
//		continueBranch = nextBranch;
//
//		generateStat(stat.getStart(), procedure);
//
//		// start:
//		InstRef check_1 = generateStat(stat.getCondition(), procedure);
//		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(check_1))
//			.addParam(new InstParam.Ref(endBranch)));
//		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(loopBranch)));
//
//		// next:
//		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(nextBranch)));
//		generateStat(stat.getAction(), procedure);
//
//		InstRef check_2 = generateStat(stat.getCondition(), procedure);
//		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(check_2))
//			.addParam(new InstParam.Ref(endBranch)));
//
//		// loop:
//		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(loopBranch)));
//
//		generateStat(stat.getBody(), procedure);
//		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(nextBranch)));
//
//		// end:
//		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(endBranch)));
//
//		breakBranch = oldBreakBranch;
//		continueBranch = oldContinueBranch;
//
//		return NONE;
//	}

	private InstRef generateFuncStat(FuncStat stat, Procedure procedure) {
		InstRef reference = wrapReference(stat.getReference(), funcCount++);
		
		Inst functionLabel = new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(reference));
		procedure.addInst(functionLabel);
		
		// Fill procedure with function data
		List<InstRef> parameters = stat.getParameters().stream().map(this::wrapReference).toList();
		procedure.fillData(reference, parameters);
	
		generateStat(stat.getBody(), procedure);

		return NONE;
	}

//	private InstRef generateGotoStat(GotoStat stat, Procedure procedure) {
//		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(wrapReference(stat.getReference()))));
//		return NONE;
//	}

	private InstRef generateIfStat(IfStat stat, Procedure procedure) {
		InstRef elseBranch = createLocalLabel(".if.else");
		InstRef endBranch = createLocalLabel(".if.end");

		InstRef result = generateStat(stat.getValue(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(result))
			.addParam(new InstParam.Ref(elseBranch)));

		generateStat(stat.getBody(), procedure);
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));

		// else:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(elseBranch)));
		generateStat(stat.getElseBody(), procedure);

		// end:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));

		return NONE;
	}

//	private InstRef generateLabelStat(LabelStat stat, Procedure procedure) {
//		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(wrapReference(stat.getReference()))));
//		return NONE;
//	}

	private InstRef generateReturnStat(ReturnStat stat, Procedure procedure) {
		if (stat.hasValue()) {
			InstRef value = generateStat(stat.getValue(), procedure);
			procedure.addInst(new Inst(Opcode.RET, stat.getSyntaxPosition())
				.addParam(new InstParam.Ref(value)));
		} else {
			procedure.addInst(new Inst(Opcode.RET, stat.getSyntaxPosition())
				.addParam(new InstParam.Num(0)));
		}
		return NONE;
	}

	private InstRef generateScopeStat(ScopeStat stat, Procedure procedure) {
		for (Stat s : stat.getElements()) {
			generateStat(s, procedure);
		}

		return NONE;
	}

	private InstRef generateVarStat(VarStat stat, Procedure procedure) {
		InstRef value = generateStat(stat.getValue(), procedure);
		InstRef holder = wrapReference(stat.getReference());

		procedure.addInst(new Inst(Opcode.MOV, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(value)));

		return NONE;
	}

//	private InstRef generateWhileStat(WhileStat stat, Procedure procedure) {
//		InstRef nextBranch = createLocalLabel(".while.next");
//		InstRef endBranch = createLocalLabel(".while.end");
//
//		InstRef oldBreakBranch = breakBranch;
//		InstRef oldContinueBranch = continueBranch;
//		breakBranch = endBranch;
//		continueBranch = nextBranch;
//
//		// next:
//		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(nextBranch)));
//
//		InstRef check = generateStat(stat.getCondition(), procedure);
//		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(check))
//			.addParam(new InstParam.Ref(endBranch)));
//
//		generateStat(stat.getBody(), procedure);
//		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(nextBranch)));
//
//		// end:
//		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
//			.addParam(new InstParam.Ref(endBranch)));
//
//		breakBranch = oldBreakBranch;
//		continueBranch = oldContinueBranch;
//
//		return NONE;
//	}

//	private InstRef generateNamespaceStat(NamespaceStat stat, Procedure prodedure) {
//		// A namespace is only a container of functions
//
//		for (Stat s : stat.getElements()) {
//			generateStat(s, prodedure);
//		}
//
//		return NONE;
//	}

	// Expressions
	private InstRef generateBinaryExpr(BinaryExpr expr, Procedure procedure) {
//		if (opcode == Opcode.MOV) {
//			if (expr.getLeft() instanceof UnaryExpr e && e.getOperation() == Operation.DEREFERENCE) {
//				InstRef left = generateStat(e.getValue(), procedure);
//				InstRef right = generateStat(expr.getRight(), procedure);
//
//				procedure.addInst(new Inst(Opcode.STORE, expr.getSyntaxPosition())
//					.addParam(new InstParam.Ref(left))
//					.addParam(new InstParam.Ref(right)));
//
//				return right;
//			}
//		}
		
		if (expr.getOperation() == Operation.C_AND
		|| expr.getOperation() == Operation.C_OR) {
			return generateConditionalExpr(expr, procedure);
		}
		
		Opcode opcode = getBinaryOpcode(expr.getOperation());
		InstRef holder;
		
		InstRef left = generateStat(expr.getLeft(), procedure);
		InstRef right = generateStat(expr.getRight(), procedure);
		if (opcode == Opcode.MOV) {
			holder = left;
			procedure.addInst(new Inst(opcode, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(left))
				.addParam(new InstParam.Ref(right)));
		} else {
			holder = createDataReference(".binary", expr.getType());
			procedure.addInst(new Inst(opcode, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(holder))
				.addParam(new InstParam.Ref(left))
				.addParam(new InstParam.Ref(right)));
		}

		return holder;
	}

	private InstRef generateCallExpr(CallExpr expr, Procedure procedure) {
		InstRef caller = wrapReference(expr.getReference());
		InstRef holder = createDataReference(".call", expr.getType());

		Inst callInst = new Inst(Opcode.CALL, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(caller));

		for (Expr parameter : expr.getParameters()) {
			InstRef paramRef = generateStat(parameter, procedure);
			callInst.addParam(new InstParam.Ref(paramRef));
		}

		procedure.addInst(callInst);

		return holder;
	}

//	private InstRef generateCastExpr(CastExpr expr, Procedure procedure) {
//		InstRef holder = createDataReference(".cast");
//		InstRef value = generateStat(expr.getValue(), procedure);
//
//		procedure.addInst(new Inst(Opcode.CAST, expr.getSyntaxPosition())
//			.addParam(new InstParam.Ref(holder))
//			.addParam(new InstParam.Type(expr.getCastType()))
//			.addParam(new InstParam.Ref(value)));
//
//		return holder;
//	}

//	private InstRef generateCommaExpr(CommaExpr expr, Procedure procedure) {
//		InstRef holder = NONE;
//
//		Iterator<Expr> iter = expr.getValues().iterator();
//		if (iter.hasNext()) {
//			holder = generateStat(iter.next(), procedure);
//		}
//
//		while (iter.hasNext()) {
//			generateStat(iter.next(), procedure);
//		}
//
//		return holder;
//	}

	private InstRef generateNameExpr(NameExpr expr, Procedure procedure) {
		return wrapReference(expr.getReference());
	}

	private InstRef generateNoneExpr(NoneExpr expr, Procedure procedure) {
		return createDataReference(".none", Primitives.NONE);
	}

	private InstRef generateNumExpr(NumExpr expr, Procedure procedure) {
		InstRef holder = createDataReference(".number", expr.getType());
		// TODO: Assign number value to ref
		// TODO: Add type size value to ref
		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Num(expr.toString())));

		return holder;
	}

//	private InstRef generateStrExpr(StrExpr expr, Procedure procedure) {
//		InstRef holder = createDataReference(".string");
//		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
//			.addParam(new InstParam.Ref(holder))
//			.addParam(new InstParam.Str(expr.getValue())));
//
//		return holder;
//	}

	private InstRef generateUnaryExpr(UnaryExpr expr, Procedure procedure) {
		InstRef holder = createDataReference(".unary", expr.getType());
		InstRef value = generateStat(expr.getValue(), procedure);

		// TODO: Post and Pre modification of the expression
		procedure.addInst(new Inst(getUnaryOpcode(expr.getOperation()), expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(value)));

		return holder;
	}

	private InstRef generateConditionalExpr(BinaryExpr expr, Procedure procedure) {
		boolean isAnd = expr.getOperation() == Operation.C_AND;

		InstRef jmpBranch = createLocalLabel(isAnd ? ".cand.end" : ".cor.value");
		InstRef holder = createDataReference(isAnd ? ".cand" : ".cor", expr.getType());

		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Num(0)));

		{
			Expr e = expr.getLeft();
			InstRef check = generateStat(e, procedure);
			procedure.addInst(new Inst(isAnd ? Opcode.JZ : Opcode.JNZ, e.getSyntaxPosition())
				.addParam(new InstParam.Ref(check))
				.addParam(new InstParam.Ref(jmpBranch)));
		}
		
		{
			Expr e = expr.getRight();
			InstRef check = generateStat(e, procedure);
			procedure.addInst(new Inst(isAnd ? Opcode.JZ : Opcode.JNZ, e.getSyntaxPosition())
				.addParam(new InstParam.Ref(check))
				.addParam(new InstParam.Ref(jmpBranch)));
		}

		if (isAnd) {
			// :value
			procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(holder))
				.addParam(new InstParam.Num(1)));

			// :end
			procedure.addInst(new Inst(Opcode.LABEL, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(jmpBranch)));
		} else {
			InstRef corEndBranch = createLocalLabel(".cor.end");
			procedure.addInst(new Inst(Opcode.JMP, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(corEndBranch)));

			// :value
			procedure.addInst(new Inst(Opcode.LABEL, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(jmpBranch)));
			procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(holder))
				.addParam(new InstParam.Num(1)));

			// :end
			procedure.addInst(new Inst(Opcode.LABEL, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(corEndBranch)));
		}

		// a && b && c && d && e
		// MOV [tmp], [0]
		// JZ [a], [:end0]
		// JZ [b], [:end0]
		// JZ [c], [:end0]
		// JZ [d], [:end0]
		// JZ [e], [:end0]
		// MOV [tmp], [1]
		// :end0

		// a || b || c || d || e
		// MOV [tmp], [0]
		// JNZ [a], [:val0]
		// JNZ [b], [:val0]
		// JNZ [c], [:val0]
		// JNZ [d], [:val0]
		// JNZ [e], [:val0]
		// JMP [:end0]
		// :val0
		// MOV [tmp], [1]
		// :end0

		return holder;
	}

	// Reference
	private InstRef createLocalLabel(String name) {
		return new InstRef(name, ValueType.UNDEFINED, ++count, Reference.LABEL);
	}

	private InstRef createDataReference(String name, ValueType type) {
		return new InstRef(name, type, ++count, Reference.VARIABLE);
	}

	private InstRef wrapReference(Reference reference) {
		return wrapReference(reference, count++);
	}
	
	private InstRef wrapReference(Reference reference, int id) {
		if (reference.isImported() || reference.isExported()) {
			reference = exportMap.getReference(reference);
		}
		
		InstRef result = wrappedReferences.get(reference);
		if (result != null) {
			return result;
		}
		
		result = new InstRef(reference.getName(), reference.getValueType(), id, 0);
		result.setType(reference.getType());
		wrappedReferences.put(reference, result);
		return result;
	}

	// Type checks
//	private Expr getDereferenceLeaf(Expr expr) {
//		while (true) {
//			if (expr instanceof CommaExpr e) {
//				if (!e.getValues().isEmpty()) {
//					expr = e.getLast();
//					continue;
//				} else {
//					return null;
//				}
//			}
//
//			if (expr instanceof UnaryExpr e) {
//				if (e.getOperation() == Operation.DEREFERENCE) {
//					return e.getValue();
//				}
//			}
//
//			return null;
//		}
//	}

	// Type conversions
	public Opcode getBinaryOpcode(Operation operation) {
		return switch (operation) {
			// Special
//			case ASSIGN -> Opcode.MOV;

			// Binary
			case PLUS -> Opcode.ADD;
			case MINUS -> Opcode.SUB;
			case MULTIPLY -> Opcode.MUL;
			case DIVIDE -> Opcode.DIV;
//			case MODULO -> Opcode.MOD;
			case AND -> Opcode.AND;
//			case XOR -> Opcode.XOR;
			case OR -> Opcode.OR;
//			case SHIFT_RIGHT -> Opcode.SHR;
//			case SHIFT_LEFT -> Opcode.SHL;
			case MORE_EQUAL -> Opcode.GTE;
			case MORE_THAN -> Opcode.GT;
			case LESS_EQUAL -> Opcode.LTE;
			case LESS_THAN -> Opcode.LT;
			case EQUAL -> Opcode.EQ;
			case NOT_EQUAL -> Opcode.NEQ;
			
			default -> throw new ParseException("Unknown binary operation '%s'", operation);
		};
	}

	public Opcode getUnaryOpcode(Operation operation) {
		return switch (operation) {
//			case REFERENCE -> Opcode.REF;
//			case DEREFERENCE -> Opcode.LOAD;

			case NEGATIVE -> Opcode.NEG;
//			case UNARY_PLUS -> Opcode.POS;
			case NOT -> Opcode.NOT;
//			case NOR -> Opcode.NOR;
			
			default -> throw new ParseException("Unknown unary operation '%s'", operation);
		};
	}
}
