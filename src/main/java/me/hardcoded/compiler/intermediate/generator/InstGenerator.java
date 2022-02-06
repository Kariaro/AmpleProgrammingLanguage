package me.hardcoded.compiler.intermediate.generator;

import me.hardcoded.compiler.errors.ParseException;
import me.hardcoded.compiler.intermediate.AmpleLinker.ExportMap;
import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Operation;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InstGenerator {
	private static final InstRef NONE = new InstRef("<invalid>", -1, 0);
	private final InstFile file;
	private final ExportMap exportMap;
	
	// Used for reference creation
	private final Map<Reference, InstRef> wrappedReferences;
	private int count;
	private InstRef breakBranch;
	private InstRef continueBranch;
	
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
			case PROG -> generateProgStat((ProgStat)stat);
			
			// Statements
			case BREAK -> generateBreakStat((BreakStat)stat, procedure);
			case CONTINUE -> generateContinueStat((ContinueStat)stat, procedure);
			case EMPTY -> generateEmptyStat((EmptyStat)stat, procedure);
			case FOR -> generateForStat((ForStat)stat, procedure);
			case FUNC -> generateFuncStat((FuncStat)stat, procedure);
			case GOTO -> generateGotoStat((GotoStat)stat, procedure);
			case IF -> generateIfStat((IfStat)stat, procedure);
			case LABEL -> generateLabelStat((LabelStat)stat, procedure);
			case RETURN -> generateReturnStat((ReturnStat)stat, procedure);
			case SCOPE -> generateScopeStat((ScopeStat)stat, procedure);
			case VAR -> generateVarStat((VarStat)stat, procedure);
			case WHILE -> generateWhileStat((WhileStat)stat, procedure);
			
			// Expressions
			case BINARY -> generateBinaryExpr((BinaryExpr)stat, procedure);
			case CALL -> generateCallExpr((CallExpr)stat, procedure);
			case CAST -> generateCastExpr((CastExpr)stat, procedure);
			case COMMA -> generateCommaExpr((CommaExpr)stat, procedure);
			case NAME -> generateNameExpr((NameExpr)stat, procedure);
			case NULL -> generateNullExpr((NullExpr)stat, procedure);
			case NUM -> generateNumExpr((NumExpr)stat, procedure);
			case STR -> generateStrExpr((StrExpr)stat, procedure);
			case UNARY -> generateUnaryExpr((UnaryExpr)stat, procedure);
		};
	}
	
	private InstRef generateProgStat(ProgStat stat) {
		for (Stat s : stat.getElements()) {
			Procedure procedure = new Procedure();
			generateStat(s, procedure);
			file.addProcedure(procedure);
		}
		
		return NONE;
	}
	
	private InstRef generateBreakStat(BreakStat stat, Procedure procedure) {
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(breakBranch)));
		return NONE;
	}
	
	private InstRef generateContinueStat(ContinueStat stat, Procedure procedure) {
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(continueBranch)));
		return NONE;
	}
	
	private InstRef generateEmptyStat(EmptyStat stat, Procedure procedure) {
		return NONE;
	}
	
	private InstRef generateForStat(ForStat stat, Procedure procedure) {
		InstRef nextBranch = createLocalLabel(".for.next");
		InstRef loopBranch = createLocalLabel(".for.loop");
		InstRef endBranch = createLocalLabel(".for.end");
		
		InstRef oldBreakBranch = breakBranch;
		InstRef oldContinueBrach = continueBranch;
		breakBranch = endBranch;
		continueBranch = nextBranch;
		
		generateStat(stat.getStart(), procedure);
		
		// start:
		InstRef check_1 = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(check_1))
			.addParam(new InstParam.Ref(endBranch)));
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(loopBranch)));
		
		// next:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		generateStat(stat.getAction(), procedure);
		
		InstRef check_2 = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(check_2))
			.addParam(new InstParam.Ref(endBranch)));
		
		// loop:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(loopBranch)));
		
		generateStat(stat.getBody(), procedure);
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		
		// end:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));
		
		breakBranch = oldBreakBranch;
		continueBranch = oldContinueBrach;
		
		return NONE;
	}
	
	private InstRef generateFuncStat(FuncStat stat, Procedure procedure) {
		Inst functionLabel = new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(wrapReference(stat.getReference())));
		procedure.addInst(functionLabel);
		
		generateStat(stat.getBody(), procedure);
		
		return NONE;
	}
	
	private InstRef generateGotoStat(GotoStat stat, Procedure procedure) {
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(wrapReference(stat.getReference()))));
		return NONE;
	}
	
	private InstRef generateIfStat(IfStat stat, Procedure procedure) {
		InstRef elseBranch = createLocalLabel(".if.else");
		InstRef endBranch = createLocalLabel(".if.end");
		
		InstRef result = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(elseBranch))
			.addParam(new InstParam.Ref(result)));
		
		generateStat(stat.getBody(), procedure);
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));
		
		// else:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(elseBranch)));
		generateStat(stat.getElseBody(), procedure);
		
		// end:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));
		
		return NONE;
	}
	
	private InstRef generateLabelStat(LabelStat stat, Procedure procedure) {
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(wrapReference(stat.getReference()))));
		return NONE;
	}
	
	private InstRef generateReturnStat(ReturnStat stat, Procedure procedure) {
		InstRef value = generateStat(stat.getValue(), procedure);
		procedure.addInst(new Inst(Opcode.RET, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(value)));
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
	
	private InstRef generateWhileStat(WhileStat stat, Procedure procedure) {
		InstRef nextBranch = createLocalLabel(".while.next");
		InstRef endBranch = createLocalLabel(".while.end");
		
		InstRef oldBreakBranch = breakBranch;
		InstRef oldContinueBranch = continueBranch;
		breakBranch = endBranch;
		continueBranch = nextBranch;
		
		// next:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		
		InstRef check = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(check))
			.addParam(new InstParam.Ref(endBranch)));
		
		generateStat(stat.getBody(), procedure);
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		
		// end:
		procedure.addInst(new Inst(Opcode.LABLE, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));
		
		breakBranch = oldBreakBranch;
		continueBranch = oldContinueBranch;
		
		return NONE;
	}
	
	// Expressions
	private InstRef generateBinaryExpr(BinaryExpr expr, Procedure procedure) {
		InstRef left = generateStat(expr.getLeft(), procedure);
		InstRef right = generateStat(expr.getRight(), procedure);
		InstRef holder;
		
		Opcode opcode = getBinaryOpcode(expr.getOperation());
		if (opcode == Opcode.MOV) {
			holder = left;
			procedure.addInst(new Inst(opcode, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(left))
				.addParam(new InstParam.Ref(right)));
		} else {
			holder = createDataReference(".binary");
			procedure.addInst(new Inst(opcode, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(holder))
				.addParam(new InstParam.Ref(left))
				.addParam(new InstParam.Ref(right)));
		}
		
		return holder;
	}
	
	private InstRef generateCallExpr(CallExpr expr, Procedure procedure) {
		InstRef caller = generateStat(expr.getCaller(), procedure);
		InstRef holder = createDataReference(".call");
		
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
	
	private InstRef generateCastExpr(CastExpr expr, Procedure procedure) {
		InstRef holder = createDataReference(".cast");
		InstRef value = generateStat(expr.getValue(), procedure);
		
		procedure.addInst(new Inst(Opcode.CAST, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Type(expr.getCastType()))
			.addParam(new InstParam.Ref(value)));
		
		return holder;
	}
	
	private InstRef generateCommaExpr(CommaExpr expr, Procedure procedure) {
		InstRef holder = NONE;
		
		Iterator<Expr> iter = expr.getValues().iterator();
		if (iter.hasNext()) {
			holder = generateStat(iter.next(), procedure);
		}
		
		while (iter.hasNext()) {
			generateStat(iter.next(), procedure);
		}
		
		return holder;
	}
	
	private InstRef generateNameExpr(NameExpr expr, Procedure procedure) {
		return wrapReference(expr.getReference());
	}
	
	private InstRef generateNullExpr(NullExpr expr, Procedure procedure) {
		// TODO: Create a better null value
		return createDataReference(".null");
	}
	
	private InstRef generateNumExpr(NumExpr expr, Procedure procedure) {
		InstRef holder = createDataReference(".number");
		// TODO: Assign number value to ref
		// TODO: Add type size value to ref
		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Num(expr.toString())));
		
		return holder;
	}
	
	private InstRef generateStrExpr(StrExpr expr, Procedure procedure) {
		InstRef holder = createDataReference(".string");
		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Str(expr.getValue())));
		
		return holder;
	}
	
	private InstRef generateUnaryExpr(UnaryExpr expr, Procedure procedure) {
		InstRef holder = createDataReference(".unary");
		InstRef value = generateStat(expr.getValue(), procedure);
		
		// TODO: Post and Pre modification of the expression
		procedure.addInst(new Inst(getUnaryOpcode(expr.getOperation()), expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(value)));
		
		return holder;
	}
	
	// Reference
	private InstRef createLocalLabel(String name) {
		return new InstRef(name, ++count, Reference.LABEL);
	}
	
	private InstRef createDataReference(String name) {
		return new InstRef(name, ++count, Reference.VARIABLE);
	}
	
	private InstRef wrapReference(Reference reference) {
		if (reference.isImported() || reference.isExported()) {
			reference = exportMap.getReference(reference);
		}
		
		InstRef result = wrappedReferences.get(reference);
		if (result != null) {
			return result;
		}
		
		result = new InstRef(reference.getName(), count++, 0);
		result.setType(reference.getType());
		wrappedReferences.put(reference, result);
		return result;
	}
	
	// Type conversions
	public Opcode getBinaryOpcode(Operation operation) {
		return switch (operation) {
			// ???
			case ASSIGN -> Opcode.MOV;
			
			// Binary
			case ADD -> Opcode.ADD;
			case SUBTRACT -> Opcode.SUB;
			case MULTIPLY -> Opcode.MUL;
			case DIVIDE -> Opcode.DIV;
			case MODULO -> Opcode.MOD;
			case AND -> Opcode.AND;
			case XOR -> Opcode.XOR;
			case OR -> Opcode.OR;
			case SHIFT_RIGHT -> Opcode.SHR;
			case SHIFT_LEFT -> Opcode.SHL;
			case MORE_THAN_EQUALS -> Opcode.GTE;
			case MORE_THAN -> Opcode.GT;
			case LESS_THAN_EQUALS -> Opcode.LTE;
			case LESS_THAN -> Opcode.LT;
			case EQUALS -> Opcode.EQ;
			case NOT_EQUALS -> Opcode.NEQ;
			default -> throw new ParseException("Unknown binary operation '%s'", operation);
		};
	}
	
	public Opcode getUnaryOpcode(Operation operation) {
		return switch (operation) {
			case UNARY_MINUS -> Opcode.NEG;
			case UNARY_PLUS -> Opcode.POS;
			case UNARY_NOT -> Opcode.NOT;
			case UNARY_NOR -> Opcode.NOR;
			default -> throw new ParseException("Unknown unary operation '%s'", operation);
		};
	}
}
