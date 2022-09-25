package me.hardcoded.test;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MangleTest {
	public static String createFunctionString(String namespace, String name, List<ValueType> params) {
		Reference result = new Reference(name, new Namespace(namespace), Primitives.LINKED, 0, Reference.FUNCTION);
		List<Reference> parameters = params.stream().map(param -> new Reference("", new Namespace(), param, 0, 0)).toList();
		
		return AmpleMangler.mangleFunction(result.getNamespace(), name, parameters);
	}
	
	public static AmpleMangler.MangledFunction createFunction(String namespace, String name, List<ValueType> params) {
		return AmpleMangler.demangleFunction(createFunctionString(namespace, name, params));
	}
	
	@Test
	public void testMangledNames() {
		AmpleMangler.MangledFunction function = createFunction("test::namespace", "test", List.of(
			Primitives.I8, Primitives.U32, Primitives.LINKED, Primitives.VARARGS
		));
		
		assertTrue("Linked type", function.matches(createFunctionString("test::namespace", "test", List.of(
			Primitives.LINKED,
			Primitives.LINKED,
			Primitives.LINKED
		))));
		
		assertTrue("Linked type", function.matches(createFunctionString("test::namespace", "test", List.of(
			Primitives.I8,
			Primitives.U32,
			Primitives.I64
		))));
		
		assertTrue("Variable parameters", function.matches(createFunctionString("test::namespace", "test", List.of(
			Primitives.LINKED,
			Primitives.LINKED,
			Primitives.LINKED,
			Primitives.U8,
			Primitives.U8,
			Primitives.U8,
			Primitives.U8,
			Primitives.U8
		))));
		
		assertFalse("Wrong parameter", function.matches(createFunctionString("test::namespace", "test", List.of(
			Primitives.I8,
			Primitives.U64,
			Primitives.I64
		))));
		
		assertFalse("Wrong namespace", function.matches(createFunctionString("test::help", "test", List.of(
			Primitives.I8,
			Primitives.U32,
			Primitives.I64
		))));
		
		assertFalse("Wrong name", function.matches(createFunctionString("test::namespace", "tet", List.of(
			Primitives.I8,
			Primitives.U32,
			Primitives.I64
		))));
		
		assertFalse("Not enough params", function.matches(createFunctionString("test::namespace", "test", List.of(
			Primitives.I8,
			Primitives.U32
		))));
	}
}
