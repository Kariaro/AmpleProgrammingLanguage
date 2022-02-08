package me.hardcoded.compiler.intermediate;

import me.hardcoded.compiler.intermediate.AmpleLinker.ExportMap;
import me.hardcoded.compiler.parser.LinkableObject;
import me.hardcoded.compiler.parser.expr.BinaryExpr;
import me.hardcoded.compiler.parser.stat.ProgStat;

import java.util.List;

public class AmpleValidator {
	private final ExportMap exportMap;
	
	public AmpleValidator(ExportMap exportMap) {
		this.exportMap = exportMap;
	}
	
	public void validate(LinkableObject main, List<LinkableObject> list) {
	
	}
	
	private void validateBinaryExpr(BinaryExpr expr) {
		// When we validate a binary expression we must make sure that
		// All types have the correct value
		
		// References should contain the value types
		
		// Function references the returnType
		// Variable references the type
		// Label    references none
		// String   references char pointer
		// Numbers  integer by default unless specified
	}
}
