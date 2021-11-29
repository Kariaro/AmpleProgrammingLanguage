package hardcoded.test;

import static hardcoded.compiler.constants.ExprType.*;
import static org.junit.Assert.*;

import org.junit.Test;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.OpExpr;

public class ExpressionTest {
	@Test
	public void addSubExpression() {
		AtomExpr a = ParseTreeHelper.atom("a", 0, Atom.i32);
		AtomExpr b = ParseTreeHelper.atom("b", 1, Atom.i32);
		AtomExpr c = ParseTreeHelper.atom("c", 2, Atom.i32);
		
		// add(add(a, b), ...)     -> add(a, b, ...)
		// add(sub(a, b), ...)     -> add(a, neg(b), ...)
		// add(..., add(a, b))     -> sub(..., a, b)
		// add(..., sub(a, b))     -> sub(..., a, neg(b))
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(add, new OpExpr(add, a, b), c),
			new OpExpr(add, a, b, c)
		));
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(add, new OpExpr(sub, a, b), c),
			new OpExpr(add, a, new OpExpr(neg, b), c)
		));
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(add, a, new OpExpr(add, b, c)),
			new OpExpr(add, a, b, c)
		));
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(add, a, new OpExpr(sub, b, c)),
			new OpExpr(add, a, b, new OpExpr(neg, c))
		));
		
		// sub(add(a, b), ...)     -> sub(a, neg(b), ...)
		// sub(sub(a, b), ...)     -> sub(a, b, ...)
		// sub(..., add(a, b))     -> sub(..., a, b)
		// sub(..., sub(a, b))     -> sub(..., a, neg(b))
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(sub, new OpExpr(add, a, b), c),
			new OpExpr(sub, a, new OpExpr(neg, b), c)
		));
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(sub, new OpExpr(sub, a, b), c),
			new OpExpr(sub, a, b, c)
		));
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(sub, a, new OpExpr(add, b, c)),
			new OpExpr(sub, a, b, c)
		));
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(sub, a, new OpExpr(sub, b, c)),
			new OpExpr(sub, a, b, new OpExpr(neg, c))
		));
	}
	
	@Test
	public void constantPropagation() {
		AtomExpr zero = ParseTreeHelper.atom(0);
		AtomExpr one = ParseTreeHelper.atom(1);
		AtomExpr two = ParseTreeHelper.atom(2);
		AtomExpr three = ParseTreeHelper.atom(3);
		
		// add(1, 2)    ->  3
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(add, one, two),
			three
		));
		
		// sub(3, 2)    ->  1
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(sub, three, one),
			two
		));
		
		// add(2, neg(2)) -> 0
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(add, two, new OpExpr(neg, two)),
			zero
		));
	}
	
	@Test
	public void negTest() {
		AtomExpr a = ParseTreeHelper.atom("a", 0, Atom.i32);
		
		// neg(neg(a))      -> a
		assertTrue(ParseTreeHelper.checkOptimizedEquality(
			new OpExpr(neg, new OpExpr(neg, a)),
			a
		));
	}
}
