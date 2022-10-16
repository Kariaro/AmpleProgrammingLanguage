package me.hardcoded.compiler.intermediate.generator;

import me.hardcoded.compiler.errors.InstException;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.intermediate.ExportMap;
import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.*;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.error.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class IntermediateGenerator {
	private static final Logger LOGGER = LogManager.getLogger(IntermediateGenerator.class);
	
	private static final Namespace NONE_NAMESPACE = new Namespace();
	private static final InstRef NONE = new InstRef("<invalid>", NONE_NAMESPACE, Primitives.NONE, -1, 0);
	
	private final IntermediateFile file;
	private final ExportMap exportMap;
	
	// Used for reference creation
	private final Map<Reference, InstRef> wrappedReferences;
	private int count;
	private int funcCount;
	private InstRef breakBranch;
	private InstRef continueBranch;
	
	public IntermediateGenerator(IntermediateFile file, ExportMap exportMap) {
		this.file = file;
		this.exportMap = exportMap;
		this.wrappedReferences = new HashMap<>();
	}
	
	public void generate(LinkableObject obj) throws InstException {
		generateProgStat(obj.getProgram());
	}
	
	private InstRef generateStat(Stat stat, Procedure procedure) throws InstException {
		return switch (stat.getTreeType()) {
			// Root
			case PROGRAM -> generateProgStat((ProgStat) stat);
			
			// Statements
			case BREAK -> generateBreakStat((BreakStat) stat, procedure);
			case CONTINUE -> generateContinueStat((ContinueStat) stat, procedure);
			case EMPTY -> generateEmptyStat((EmptyStat) stat, procedure);
			case FOR -> generateForStat((ForStat) stat, procedure);
			case FUNC -> generateFuncStat((FuncStat) stat, procedure);
			case IF -> generateIfStat((IfStat) stat, procedure);
			//			case LABEL -> generateLabelStat((LabelStat) stat, procedure);
			case RETURN -> generateReturnStat((ReturnStat) stat, procedure);
			case SCOPE -> generateScopeStat((ScopeStat) stat, procedure);
			case VAR -> generateVarStat((VarStat) stat, procedure);
			case COMPILER -> generateCompilerStat((CompilerStat) stat, procedure);
			case WHILE -> generateWhileStat((WhileStat) stat, procedure);
			
			// Expressions
			case STACK_ALLOC -> generateStackAllocExpr((StackAllocExpr) stat, procedure);
			case BINARY -> generateBinaryExpr((BinaryExpr) stat, procedure);
			case CALL -> generateCallExpr((CallExpr) stat, procedure);
			case CAST -> generateCastExpr((CastExpr) stat, procedure);
			case NAME -> generateNameExpr((NameExpr) stat, procedure);
			case NONE -> generateNoneExpr((NoneExpr) stat, procedure);
			case NUM -> generateNumExpr((NumExpr) stat, procedure);
			case STR -> generateStrExpr((StrExpr) stat, procedure);
			case UNARY -> generateUnaryExpr((UnaryExpr) stat, procedure);
			default -> throw new RuntimeException("Invalid expr %s".formatted(stat.getTreeType()));
		};
	}
	
	@Deprecated
	private InstParam generateParam(Stat stat, Procedure procedure) throws InstException {
		return switch (stat.getTreeType()) {
			case NUM -> {
				NumExpr expr = (NumExpr) stat;
				yield new InstParam.Num(expr.getType(), expr.getValue());
			}
			
			default -> new InstParam.Ref(generateStat(stat, procedure));
		};
	}
	
	private InstRef generateProgStat(ProgStat stat) throws InstException {
		LinkedList<Stat> stats = new LinkedList<>(stat.getElements());
		
		while (!stats.isEmpty()) {
			Stat s = stats.poll();
			
			if (s instanceof NamespaceStat) {
				stats.addAll(0, ((NamespaceStat) s).getElements());
				continue;
			}
			
			// Each statement inside of this program gets its own procedure
			// some procedures are variable procedures and some a function
			// procedures.
			
			Procedure procedure = new Procedure(switch (s.getTreeType()) {
				case FUNC -> Procedure.ProcedureType.FUNCTION;
				case VAR -> Procedure.ProcedureType.VARIABLE;
				default -> throw new RuntimeException("Invalid statement inside procedure");
			});
			
			count = 0;
			generateStat(s, procedure);
			file.addProcedure(procedure);
		}
		
		return NONE;
	}
	
	private InstRef generateBreakStat(BreakStat stat, Procedure procedure) throws InstException {
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(breakBranch)));
		return NONE;
	}
	
	private InstRef generateContinueStat(ContinueStat stat, Procedure procedure) throws InstException {
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(continueBranch)));
		return NONE;
	}
	
	private InstRef generateEmptyStat(EmptyStat stat, Procedure procedure) throws InstException {
		return NONE;
	}
	
	private InstRef generateForStat(ForStat stat, Procedure procedure) throws InstException {
		InstRef nextBranch = createLocalLabel(".for.next");
		InstRef loopBranch = createLocalLabel(".for.loop");
		InstRef endBranch = createLocalLabel(".for.end");
		
		InstRef oldBreakBranch = breakBranch;
		InstRef oldContinueBranch = continueBranch;
		breakBranch = endBranch;
		continueBranch = nextBranch;
		
		generateStat(stat.getInitializer(), procedure);
		
		// start:
		InstRef check_1 = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(check_1))
			.addParam(new InstParam.Ref(endBranch)));
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(loopBranch)));
		
		// next:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		generateStat(stat.getAction(), procedure);
		
		InstRef check_2 = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(check_2))
			.addParam(new InstParam.Ref(endBranch)));
		
		// loop:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(loopBranch)));
		
		generateStat(stat.getBody(), procedure);
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		
		// end:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));
		
		breakBranch = oldBreakBranch;
		continueBranch = oldContinueBranch;
		
		return NONE;
	}
	
	private InstRef generateFuncStat(FuncStat stat, Procedure procedure) throws InstException {
		InstRef reference = wrapReference(stat.getReference(), funcCount++);
		
		Inst functionLabel = new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(reference));
		procedure.addInst(functionLabel);
		
		// Fill data for function procedure type
		if (procedure.getType() == Procedure.ProcedureType.FUNCTION) {
			List<InstRef> parameters = stat.getParameters().stream().map(this::wrapReference).toList();
			procedure.fillData(reference, parameters);
		}
		
		generateStat(stat.getBody(), procedure);
		
		List<Inst> list = procedure.getInstructions();
		if (list.isEmpty() || list.get(list.size() - 1).getOpcode() != Opcode.RET) {
			throw new InstException(ErrorUtil.createFullError(
				ISyntaxPosition.of(stat.getSyntaxPosition().getStartPosition(), stat.getSyntaxPosition().getStartPosition()),
				"Missing return statement '%s'".formatted(
					stat.getReference()
				)
			));
			
			// throw new RuntimeException("Missing return statement : " + stat.getReference());
		}
		
		return NONE;
	}
	
	private InstRef generateIfStat(IfStat stat, Procedure procedure) throws InstException {
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
	
	//	private InstRef generateLabelStat(LabelStat stat, Procedure procedure) throws InstException {
	//		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
	//			.addParam(new InstParam.Ref(wrapReference(stat.getReference()))));
	//		return NONE;
	//	}
	
	private InstRef generateReturnStat(ReturnStat stat, Procedure procedure) throws InstException {
		if (stat.hasValue()) {
			InstRef value = generateStat(stat.getValue(), procedure);
			procedure.addInst(new Inst(Opcode.RET, stat.getSyntaxPosition())
				.addParam(new InstParam.Ref(value)));
		} else {
			procedure.addInst(new Inst(Opcode.RET, stat.getSyntaxPosition()));
		}
		return NONE;
	}
	
	private InstRef generateScopeStat(ScopeStat stat, Procedure procedure) throws InstException {
		for (Stat s : stat.getElements()) {
			generateStat(s, procedure);
		}
		
		return NONE;
	}
	
	private InstRef generateVarStat(VarStat stat, Procedure procedure) throws InstException {
		InstRef holder = wrapReference(stat.getReference());
		InstRef value = generateStat(stat.getValue(), procedure);
		
		procedure.addInst(new Inst(Opcode.MOV, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(value)));
		
		// Fill data for variable procedure type
		if (procedure.getType() == Procedure.ProcedureType.VARIABLE) {
			procedure.fillData(holder, List.of());
		}
		
		if (!holder.getValueType().equals(value.getValueType())) {
			throw new InstException(ErrorUtil.createFullError(stat.getValue().getSyntaxPosition(),
				"Left and Right side does not match (%s != %s)".formatted(
					holder.getValueType().toShortName(),
					value.getValueType().toShortName()
				)
			));
		}
		
		return NONE;
	}
	
	private InstRef generateCompilerStat(CompilerStat stat, Procedure procedure) throws InstException {
		for (CompilerStat.Part part : stat.getParts()) {
			Inst inst = new Inst(Opcode.INLINE_ASM, stat.getSyntaxPosition())
				.addParam(new InstParam.Str(stat.getTargetType()))
				.addParam(new InstParam.Str(part.command()));
			
			for (Reference reference : part.references()) {
				inst.addParam(new InstParam.Ref(wrapReference(reference)));
			}
			
			procedure.addInst(inst);
		}
		
		return NONE;
	}
	
	private InstRef generateWhileStat(WhileStat stat, Procedure procedure) throws InstException {
		InstRef nextBranch = createLocalLabel(".while.next");
		InstRef endBranch = createLocalLabel(".while.end");
		
		InstRef oldBreakBranch = breakBranch;
		InstRef oldContinueBranch = continueBranch;
		breakBranch = endBranch;
		continueBranch = nextBranch;
		
		// next:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		
		InstRef check = generateStat(stat.getCondition(), procedure);
		procedure.addInst(new Inst(Opcode.JZ, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(check))
			.addParam(new InstParam.Ref(endBranch)));
		
		generateStat(stat.getBody(), procedure);
		procedure.addInst(new Inst(Opcode.JMP, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(nextBranch)));
		
		// end:
		procedure.addInst(new Inst(Opcode.LABEL, stat.getSyntaxPosition())
			.addParam(new InstParam.Ref(endBranch)));
		
		breakBranch = oldBreakBranch;
		continueBranch = oldContinueBranch;
		
		return NONE;
	}
	
	// Expressions
	private InstRef generateStackAllocExpr(StackAllocExpr expr, Procedure procedure) throws InstException {
		ValueType stackType = expr.getType().createArray(1);
		InstRef stack = createDataReference(".stack", stackType);
		
		procedure.addInst(new Inst(Opcode.STACK_ALLOC, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(stack))
			.addParam(new InstParam.Num(Primitives.I32, expr.getSize())));
		
		Expr value = expr.getValue();
		if (value instanceof StrExpr s) {
			String text = s.getValue();
			
			int minLength = Math.min(text.length(), expr.getSize());
			for (int i = 0; i < minLength; i++) {
				procedure.addInst(new Inst(Opcode.STORE, expr.getSyntaxPosition())
					.addParam(new InstParam.Ref(stack))
					.addParam(new InstParam.Num(Primitives.I32, i))
					.addParam(new InstParam.Num(Primitives.I8, text.charAt(i) & 0xff)));
			}
			
			return stack;
		} else {
			// Uninitialized
			return stack;
		}
	}
	
	private InstRef generateBinaryExpr(BinaryExpr expr, Procedure procedure) throws InstException {
		if (expr.getOperation() == Operation.C_AND
			|| expr.getOperation() == Operation.C_OR) {
			return generateConditionalExpr(expr, procedure);
		}
		
		if (expr.getOperation() == Operation.ASSIGN) {
			return generateAssignExpr(expr, procedure);
		}
		
		if (expr.getOperation() == Operation.ARRAY) {
			return generateArrayExpr(expr, procedure);
		}
		
		Opcode opcode = getBinaryOpcode(expr.getOperation(), expr.getType().isUnsigned());
		InstRef left = generateStat(expr.getLeft(), procedure);
		InstRef right = generateStat(expr.getRight(), procedure);
		InstRef holder = createDataReference(".binary", expr.getType());
		procedure.addInst(new Inst(opcode, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(left))
			.addParam(new InstParam.Ref(right)));
		
		if (!left.getValueType().equals(right.getValueType())) {
			throw new InstException(ErrorUtil.createFullError(expr.getSyntaxPosition(),
				"Left and Right side does not match (%s != %s)".formatted(
					left.getValueType().toShortName(),
					right.getValueType().toShortName()
				)
			));
		}
		
		return holder;
	}
	
	private InstRef generateCallExpr(CallExpr expr, Procedure procedure) throws InstException {
		// First resolve the parameters
		List<InstParam.Ref> params = new ArrayList<>();
		for (Expr parameter : expr.getParameters()) {
			InstRef paramRef = generateStat(parameter, procedure);
			params.add(new InstParam.Ref(paramRef));
		}
		
		InstRef caller;
		if (expr.getReference().isImported()) {
			List<Reference> parameters = params.stream().map(param -> (Reference) param.getReference()).toList();
			caller = wrapFunctionReference(expr.getReference(), parameters);
		} else {
			caller = wrapReference(expr.getReference());
		}
		
		InstRef holder = createDataReference(".call", caller.getValueType());
		
		Inst callInst = new Inst(Opcode.CALL, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(caller));
		
		for (InstParam param : params) {
			callInst.addParam(param);
		}
		
		procedure.addInst(callInst);
		
		return holder;
	}
	
	private InstRef generateCastExpr(CastExpr expr, Procedure procedure) throws InstException {
		InstRef holder = createDataReference(".cast", expr.getType());
		InstRef value = generateStat(expr.getValue(), procedure);
		
		// casting from unsigned always zero extends
		// casting from signed to signed sign extends
		
		Opcode opcode;
		if (expr.getType().calculateBytes() <= value.getValueType().calculateBytes()) {
			opcode = Opcode.TRUNC;
		} else if (expr.getType().isSigned() && value.getValueType().isSigned()) {
			opcode = Opcode.SEXT;
		} else {
			opcode = Opcode.ZEXT;
		}
		
		procedure.addInst(new Inst(opcode, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(value)));
		
		return holder;
	}
	
	private InstRef generateNameExpr(NameExpr expr, Procedure procedure) throws InstException {
		return wrapReference(expr.getReference());
	}
	
	private InstRef generateNoneExpr(NoneExpr expr, Procedure procedure) throws InstException {
		return createDataReference(".none", Primitives.NONE);
	}
	
	private InstRef generateNumExpr(NumExpr expr, Procedure procedure) throws InstException {
		InstRef holder = createDataReference(".number", expr.getType());
		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Num(expr.getType(), expr.getValue())));
		
		return holder;
	}
	
	private InstRef generateStrExpr(StrExpr expr, Procedure procedure) throws InstException {
		InstRef holder = createDataReference(".string", expr.getType());
		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Str(expr.getValue())));
		
		return holder;
	}
	
	private InstRef generateUnaryExpr(UnaryExpr expr, Procedure procedure) throws InstException {
		InstRef holder = createDataReference(".unary", expr.getType());
		InstRef value = generateStat(expr.getValue(), procedure);
		
		// TODO: Post and Pre modification of the expression
		procedure.addInst(new Inst(getUnaryOpcode(expr.getOperation()), expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(value)));
		
		return holder;
	}
	
	private InstRef generateArrayExpr(BinaryExpr expr, Procedure procedure) throws InstException {
		InstRef holder = createDataReference(".array", expr.getType().createArray(0));
		InstRef left = generateStat(expr.getLeft(), procedure);
		InstRef right = generateStat(expr.getRight(), procedure);
		procedure.addInst(new Inst(Opcode.LOAD, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Ref(left))
			.addParam(new InstParam.Ref(right)));
		return holder;
	}
	
	private InstRef generateAssignExpr(BinaryExpr expr, Procedure procedure) throws InstException {
		InstRef holder;
		if (expr.getLeft() instanceof BinaryExpr left
			&& left.getOperation() == Operation.ARRAY) {
			InstRef arrayObject = generateStat(left.getLeft(), procedure);
			InstRef arrayOffset = generateStat(left.getRight(), procedure);
			InstRef right = generateStat(expr.getRight(), procedure);
			holder = right;
			procedure.addInst(new Inst(Opcode.STORE, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(arrayObject))
				.addParam(new InstParam.Ref(arrayOffset))
				.addParam(new InstParam.Ref(right)));
			
			ValueType childType = arrayObject.getValueType()
				.createArray(arrayObject.getValueType().getDepth() - 1);
			
			if (!childType.equals(right.getValueType())) {
				Position pos = expr.getSyntaxPosition().getStartPosition();
				throw new InstException(
					"(line: %d, column: %d) Left and Right side does not match (%s != %s)",
					pos.line + 1,
					pos.column + 1,
					childType.toShortName(),
					right.getValueType().toShortName()
				);
			}
		} else {
			InstRef left = generateStat(expr.getLeft(), procedure);
			InstRef right = generateStat(expr.getRight(), procedure);
			holder = left;
			procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(left))
				.addParam(new InstParam.Ref(right)));
			
			if (!left.getValueType().equals(right.getValueType())) {
				Position pos = expr.getSyntaxPosition().getStartPosition();
				throw new InstException(
					"(line: %d, column: %d) Left and Right side does not match (%s != %s)",
					pos.line + 1,
					pos.column + 1,
					left.getValueType().toShortName(),
					right.getValueType().toShortName()
				);
			}
		}
		
		return holder;
	}
	
	private InstRef generateConditionalExpr(BinaryExpr expr, Procedure procedure) throws InstException {
		boolean isAnd = expr.getOperation() == Operation.C_AND;
		
		InstRef jmpBranch = createLocalLabel(isAnd ? ".cand.end" : ".cor.value");
		InstRef holder = createDataReference(isAnd ? ".cand" : ".cor", expr.getType());
		
		procedure.addInst(new Inst(Opcode.MOV, expr.getSyntaxPosition())
			.addParam(new InstParam.Ref(holder))
			.addParam(new InstParam.Num(expr.getType(), 0)));
		
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
				.addParam(new InstParam.Num(expr.getType(), 1)));
			
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
				.addParam(new InstParam.Num(expr.getType(), 1)));
			
			// :end
			procedure.addInst(new Inst(Opcode.LABEL, expr.getSyntaxPosition())
				.addParam(new InstParam.Ref(corEndBranch)));
		}
		
		return holder;
	}
	
	// Reference
	private InstRef createLocalLabel(String name) {
		return new InstRef(name, NONE_NAMESPACE, Primitives.NONE, ++count, Reference.LABEL);
	}
	
	private InstRef createDataReference(String name, ValueType type) {
		return new InstRef(name, NONE_NAMESPACE, type, ++count, Reference.VARIABLE);
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
		
		result = new InstRef(reference.getName(), reference.getNamespace(), reference.getValueType(), id, 0);
		result.setType(reference.getType());
		result.setMangledName(reference.getMangledName());
		wrappedReferences.put(reference, result);
		return result;
	}
	
	private InstRef wrapFunctionReference(Reference reference, List<Reference> parameters) {
		return wrapFunctionReference(reference, parameters, count++);
	}
	
	private InstRef wrapFunctionReference(Reference reference, List<Reference> parameters, int id) {
		if (reference.isImported() || reference.isExported()) {
			reference = exportMap.getMangledFunctionReference(reference, parameters);
		}
		
		InstRef result = wrappedReferences.get(reference);
		if (result != null) {
			return result;
		}
		
		result = new InstRef(reference.getName(), reference.getNamespace(), reference.getValueType(), id, 0);
		result.setType(reference.getType());
		result.setMangledName(reference.getMangledName());
		wrappedReferences.put(reference, result);
		return result;
	}
	
	// Type conversions
	public Opcode getBinaryOpcode(Operation operation, boolean unsigned) {
		boolean floating = false;
		
		return switch (operation) {
			// Binary
			case PLUS -> get(Opcode.ADD, Opcode.ADD, Opcode.FADD, unsigned, floating);
			case MINUS -> get(Opcode.SUB, Opcode.SUB, Opcode.FSUB, unsigned, floating);
			case MULTIPLY -> get(Opcode.MUL, Opcode.IMUL, Opcode.FMUL, unsigned, floating);
			case DIVIDE -> get(Opcode.DIV, Opcode.IDIV, Opcode.FDIV, unsigned, floating);
			case MODULO -> get(Opcode.MOD, Opcode.IMOD, Opcode.FMOD, unsigned, floating);
			case AND -> Opcode.AND;
			case XOR -> Opcode.XOR;
			case OR -> Opcode.OR;
			case SHIFT_RIGHT -> Opcode.SHR;
			case SHIFT_LEFT -> Opcode.SHL;
			case MORE_EQUAL -> get(Opcode.GTE, Opcode.IGTE, Opcode.FGTE, unsigned, floating);
			case MORE_THAN -> get(Opcode.GT, Opcode.IGT, Opcode.FGT, unsigned, floating);
			case LESS_EQUAL -> get(Opcode.LTE, Opcode.ILTE, Opcode.FLTE, unsigned, floating);
			case LESS_THAN -> get(Opcode.LT, Opcode.ILT, Opcode.FLT, unsigned, floating);
			case EQUAL -> Opcode.EQ;
			case NOT_EQUAL -> Opcode.NEQ;
			
			default -> throw new RuntimeException("Unknown binary operation '%s'".formatted(operation));
		};
	}
	
	private Opcode get(Opcode uOp, Opcode iOp, Opcode fOp, boolean unsigned, boolean floating) {
		if (unsigned) {
			if (floating) {
				throw new RuntimeException("No unsigned floating type");
			}
			
			return uOp;
		}
		
		return floating ? fOp : iOp;
	}
	
	public Opcode getUnaryOpcode(Operation operation) {
		return switch (operation) {
			case NEGATIVE -> Opcode.NEG;
			case NOT -> Opcode.NOT;
			//			case NOR -> Opcode.NOR;
			
			default -> throw new RuntimeException("Unknown unary operation '%s'".formatted(operation));
		};
	}
}
