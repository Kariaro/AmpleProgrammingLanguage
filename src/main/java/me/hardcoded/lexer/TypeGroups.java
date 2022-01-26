package me.hardcoded.lexer;

import java.util.Set;

import me.hardcoded.lexer.Token.Type;

public class TypeGroups {
	private static final Set<Type> ASSIGNMENTS = Set.of(
		Type.ASSIGN,
		Type.ADD_ASSIGN,
		Type.SUB_ASSIGN,
		Type.MUL_ASSIGN,
		Type.DIV_ASSIGN,
		Type.MOD_ASSIGN,
		Type.XOR_ASSIGN,
		Type.OR_ASSIGN,
		Type.AND_ASSIGN,
		Type.SHIFT_LEFT_ASSIGN,
		Type.SHIFT_RIGHT_ASSIGN
	);
	
	private static final Set<Type> FUNCTION_MODIFIERS = Set.of(
		Type.EXPORT,
		Type.INLINE
	);
	
	public static boolean isAssignmentOperator(Type type) {
		return ASSIGNMENTS.contains(type);
	}
	
	public static boolean isFunctionModifier(Type type) {
		return FUNCTION_MODIFIERS.contains(type);
	}
}
