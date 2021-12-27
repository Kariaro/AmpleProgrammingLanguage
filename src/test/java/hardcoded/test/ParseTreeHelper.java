package hardcoded.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.expression.*;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.utils.StatementUtils;

public class ParseTreeHelper {
	private static final Logger LOGGER = LogManager.getLogger(ParseTreeHelper.class);
	
	public static boolean checkOptimizedEquality(Expression input, Expression expected) {
		// Comma is used here because it will always contain atleast one element
		// and does not change the way input will be optimized by the parser.
		String inputString = input.toString();
		OpExpr expr = new OpExpr(ExprType.comma, input);
		StatementUtils.getAllExpressions(null, expr, new ParseTreeOptimizer()::constantFolding);
		input = expr.get(0);
		
		// TODO: Implement a better comparison.
		// String comparison is the easiest.
		boolean result = input.toString().equals(expected.toString());
		if(!result) {
			LOGGER.info("");
			LOGGER.info("Input   : {}", inputString);
			LOGGER.info("Result  : {}", input);
			LOGGER.info("Expected: {}", expected);
		}
		
		return result;
	}

	public static AtomExpr atom(String name, int index, Atom atom) {
		return new AtomExpr(Identifier.createTempLocalVariable(name, index, LowType.create(atom)));
	}
	
	public static AtomExpr atom(int value) {
		return new AtomExpr(value);
	}
}
