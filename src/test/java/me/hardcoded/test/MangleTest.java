package me.hardcoded.test;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.utils.types.MangledFunctionMap;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MangleTest {
	public static String createFunctionString(String namespace, String name, List<ValueType> params) {
		Reference result = new Reference(name, new Namespace(namespace), Primitives.NONE, 0, Reference.FUNCTION);
		List<Reference> parameters = params.stream().map(param -> new Reference("", new Namespace(), param, 0, 0)).toList();
		return AmpleMangler.mangleFunction(Primitives.NONE, result.getNamespace(), name, parameters);
	}
	
	
	public static Reference createFunctionReference(String name, List<ValueType> params) {
		return createFunctionReference(Primitives.NONE, null, name, params);
	}
	
	public static Reference createFunctionReference(ValueType returnType, String name, List<ValueType> params) {
		return createFunctionReference(returnType, null, name, params);
	}
	
	public static Reference createFunctionReference(ValueType returnType, String namespace, String name, List<ValueType> params) {
		Reference result = new Reference(name, namespace == null ? new Namespace() : new Namespace(namespace), returnType, 0, Reference.FUNCTION);
		List<Reference> parameters = params.stream().map(param -> new Reference("", new Namespace(), param, 0, 0)).toList();
		result.setMangledName(AmpleMangler.mangleFunction(returnType, result.getNamespace(), name, parameters));
		return result;
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
	
	@Test
	public void testMangledMap() {
		MangledFunctionMap map = new MangledFunctionMap();
		
		// Test put
		assertTrue(map.put(createFunctionReference("test", List.of(Primitives.I8, Primitives.VARARGS))));
		assertTrue(map.put(createFunctionReference("a", List.of(Primitives.I8, Primitives.I32, Primitives.I64))));
		assertTrue(map.put(createFunctionReference("b", List.of(Primitives.I16, Primitives.I32, Primitives.I64))));
		assertFalse(map.put(createFunctionReference("test", List.of(Primitives.I8, Primitives.I32, Primitives.I64))));
		assertFalse(map.put(createFunctionReference("test", List.of(Primitives.I8, Primitives.VARARGS))));
		assertFalse(map.put(createFunctionReference("test", List.of(Primitives.VARARGS))));
		assertTrue(map.put(createFunctionReference("test", List.of())));
		assertFalse(map.put(createFunctionReference("test", List.of())));
		assertFalse(map.put(createFunctionReference("test", List.of(Primitives.VARARGS))));
		assertTrue(map.put(createFunctionReference("abcd", List.of(Primitives.VARARGS))));
		assertFalse(map.put(createFunctionReference("abcd", List.of())));
		assertTrue(map.put(createFunctionReference("test", List.of(Primitives.I64, Primitives.I32, Primitives.I64))));
		assertFalse(map.put(createFunctionReference("test", List.of(Primitives.LINKED))));
		assertTrue(map.put(createFunctionReference("test", List.of(Primitives.I8.createArray(1)))));
		map.clear();
		
		// Test getter
		Reference reference;
		assertTrue(map.put(reference = createFunctionReference("test", List.of(Primitives.I8))));
		assertNull(map.get(createFunctionReference("test", List.of(Primitives.LINKED, Primitives.LINKED))));
		assertEquals(reference, map.get(createFunctionReference("test", List.of(Primitives.LINKED))));
		assertEquals(reference, map.get(createFunctionReference("test", List.of(Primitives.I8))));
		assertEquals(reference, map.get(createFunctionReference("test", List.of(Primitives.VARARGS))));
		assertNotEquals(reference, map.get(createFunctionReference("test", List.of(Primitives.I16))));
		
		// Add one more element
		Reference reference2;
		map.put(reference2 = createFunctionReference("test2", List.of(Primitives.I8, Primitives.I64)));
		map.put(createFunctionReference("test2", List.of(Primitives.I8)));
		map.put(createFunctionReference("test2", List.of(Primitives.I64)));
		assertFalse(map.put(createFunctionReference("test2", List.of(Primitives.I64, Primitives.VARARGS))));
		assertEquals(reference2, map.get(createFunctionReference("test2", List.of(Primitives.I8, Primitives.I64))));
		
		// Test linked
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I64, Primitives.I16, Primitives.I32)));
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I64, Primitives.I32, Primitives.I32)));
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I64, Primitives.I64, Primitives.I32)));
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I64, Primitives.I8, Primitives.I32)));
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I64, Primitives.I16)));
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I32)));
		map.put(createFunctionReference("test3", List.of(Primitives.I8, Primitives.I16)));
		map.put(createFunctionReference("test3", List.of(Primitives.I16, Primitives.I32)));
		map.put(createFunctionReference("test3", List.of(Primitives.I16, Primitives.I64)));
		assertNotNull(map.get(createFunctionReference("test3", List.of(Primitives.LINKED, Primitives.I64, Primitives.I16))));
		assertNull(map.get(createFunctionReference("test3", List.of(Primitives.LINKED, Primitives.LINKED, Primitives.LINKED, Primitives.I32))));
		assertNotNull(map.get(createFunctionReference("test3", List.of(Primitives.LINKED, Primitives.LINKED, Primitives.I16, Primitives.I32))));
		
		// Test returnType
		map.clear();
		assertFalse(map.put(createFunctionReference(Primitives.LINKED, "test", List.of())));
		assertTrue(map.put(reference = createFunctionReference(Primitives.I8, "test", List.of())));
		assertNull(map.get(createFunctionReference("test", List.of(Primitives.LINKED))));
		assertNull(map.get(createFunctionReference(Primitives.I8, "test", List.of(Primitives.LINKED))));
		assertNotNull(map.get(createFunctionReference(Primitives.LINKED, "test", List.of())));
		assertNotNull(map.get(createFunctionReference(Primitives.I8, "test", List.of())));
		assertNull(map.get(createFunctionReference(Primitives.I16, "test", List.of())));
		
		System.out.println(map);
	}
}
