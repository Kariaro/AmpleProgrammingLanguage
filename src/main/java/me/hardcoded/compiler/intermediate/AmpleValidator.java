package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.intermediate.AmpleLinker.ExportMap;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.*;
import me.hardcoded.compiler.parser.stat.*;
import me.hardcoded.compiler.parser.type.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for creating validating the abstract syntax tree of the arucas programming language.
 * Type checking and reference modifications should be applied here.
 *
 * This parser will resolve all exported and imported objects between multiple {@link LinkableObject} classes.
 * <ul>
 *   <li>Type checking</li>
 *   <li>Object linking</li>
 * </ul>
 *
 * @author HardCoded
 */
public class AmpleValidator {
	private final ExportMap exportMap;
	private final Map<Reference, Reference> wrappedReferences;
	private int count;
	
	public AmpleValidator(ExportMap exportMap) {
		this.exportMap = exportMap;
		this.wrappedReferences = new HashMap<>();
	}
	
	public void validate(List<LinkableObject> list) {
//		ProgStat combined = new ProgStat(ISyntaxPosition.empty());
		
		for (LinkableObject link : list) {
//			for (Stat stat : link.getProgram().getElements()) {
//				combined.addElement(stat);
//			}
			
			validateStat(link.getProgram());
		}
		
//		validateStat(combined);
	}
	
	private void validateStat(Stat stat) {
		switch (stat.getTreeType()) {
			// Statements
			case BREAK -> {}
			case CONTINUE -> {}
			case EMPTY -> {}
			case FOR -> validateForStat((ForStat)stat);
			case FUNC -> validateFuncStat((FuncStat)stat);
			case GOTO -> validateGotoStat((GotoStat)stat);
			case IF -> validateIfStat((IfStat)stat);
			case LABEL -> validateLabelStat((LabelStat)stat);
			case PROG -> validateProgStat((ProgStat)stat);
			case RETURN -> validateReturnStat((ReturnStat)stat);
			case SCOPE -> validateScopeStat((ScopeStat)stat);
			case VAR -> validateVarStat((VarStat)stat);
			case WHILE -> validateWhileStat((WhileStat)stat);
			case NAMESPACE -> validateNamespaceStat((NamespaceStat)stat);
			
			// Expressions
			case BINARY -> validateBinaryExpr((BinaryExpr)stat);
			case CALL -> validateCallExpr((CallExpr)stat);
			case CAST -> validateCastExpr((CastExpr)stat);
			case COMMA -> validateCommaExpr((CommaExpr)stat);
			case NAME -> validateNameExpr((NameExpr)stat);
			case NULL -> {}
			case NUM -> {}
			case STR -> {}
			case UNARY -> validateUnaryExpr((UnaryExpr)stat);
			case CONDITIONAL -> validateConditionalExpr((ConditionalExpr)stat);
		}
	}
	
	// Statements
	private void validateForStat(ForStat stat) {
		validateStat(stat.getStart());
		validateStat(stat.getCondition());
		validateStat(stat.getAction());
		validateStat(stat.getBody());
	}
	
	private void validateFuncStat(FuncStat stat) {
		// Wrap the reference
		stat.setReference(wrapReference(stat.getReference()));
		
		List<Reference> parameters = stat.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			parameters.set(i, wrapReference(parameters.get(i)));
		}
		
		// TODO: Validate parameter types when we allow custom objects
		// TODO: Validate return type when we allow custom objects
		validateStat(stat.getBody());
	}
	
	private void validateGotoStat(GotoStat stat) {
		// Wrap the reference
		stat.setReference(wrapReference(stat.getReference()));
	}
	
	private void validateIfStat(IfStat stat) {
		validateStat(stat.getCondition());
		validateStat(stat.getBody());
		validateStat(stat.getElseBody());
	}
	
	private void validateLabelStat(LabelStat stat) {
		// Wrap the reference
		stat.setReference(wrapReference(stat.getReference()));
	}
	
	private void validateProgStat(ProgStat stat) {
		for (Stat s : stat.getElements()) {
			validateStat(s);
		}
	}
	
	private void validateReturnStat(ReturnStat stat) {
		validateStat(stat.getValue());
	}
	
	private void validateScopeStat(ScopeStat stat) {
		for (Stat s : stat.getElements()) {
			validateStat(s);
		}
	}
	
	private void validateVarStat(VarStat stat) {
		validateStat(stat.getValue());
	}
	
	private void validateWhileStat(WhileStat stat) {
		validateStat(stat.getCondition());
		validateStat(stat.getBody());
	}
	
	private void validateNamespaceStat(NamespaceStat stat) {
		for (Stat s : stat.getElements()) {
			validateStat(s);
		}
	}
	
	// Expressions
	private void validateBinaryExpr(BinaryExpr expr) {
		// When we validate a binary expression we must make sure that
		// All types have the correct value
		
		// References should contain the value types
		
		// Function references the returnType
		// Variable references the type
		// Label    references none
		// String   references char pointer
		// Numbers  integer by default unless specified
		
		validateStat(expr.getLeft());
		validateStat(expr.getRight());
	}
	
	private void validateCallExpr(CallExpr expr) {
		// TODO: Do type checking
		
		validateStat(expr.getCaller());
		
		for (Expr e : expr.getParameters()) {
			validateStat(e);
		}
	}
	
	private void validateCastExpr(CastExpr expr) {
		validateStat(expr.getValue());
	}
	
	private void validateCommaExpr(CommaExpr expr) {
		for (Expr e : expr.getValues()) {
			validateStat(e);
		}
	}
	
	private void validateNameExpr(NameExpr expr) {
		// Wrap the reference
		expr.setReference(wrapReference(expr.getReference()));
	}
	
	private void validateUnaryExpr(UnaryExpr expr) {
		// unsigned unary negation is not allowed
		
		validateStat(expr.getValue());
	}
	
	private void validateConditionalExpr(ConditionalExpr expr) {
		// Type check
		for (Expr e : expr.getValues()) {
			validateStat(e);
		}
	}
	
	// Reference
	private Reference wrapReference(Reference reference) {
		if (reference.isImported() || reference.isExported()) {
			reference = exportMap.getReference(reference);
		}
		
		Reference result = wrappedReferences.get(reference);
		if (result == null) {
			result = new Reference(reference.getName(), reference.getValueType(), count++, 0);
			result.setType(reference.getType());
			wrappedReferences.put(reference, result);
		}
		
		return result;
	}
}
