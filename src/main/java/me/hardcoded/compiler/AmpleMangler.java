package me.hardcoded.compiler;

import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.List;

public class AmpleMangler {
	private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String HEX = "0123456789abcdef";
	
	public static String mangleFunction(String name, List<Reference> parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		for (Reference param : parameters) {
			sb.append('@').append(mangleType(param.getValueType()));
		}
		return sb.toString();
	}
	
	public static String mangleType(ValueType type) {
		StringBuilder sb = new StringBuilder();
		if (type.isSigned())        sb.append('s');
		else if (type.isUnsigned()) sb.append('u');
		else if (type.isFloating()) sb.append('f');
		else                        sb.append('x');
		sb.append(HEX.charAt(type.getDepth()))
		  .append(BASE64.charAt(type.getSize() % 64))
		  .append(BASE64.charAt(type.getSize() / 64));
		return sb.toString();
	}
	
	public ValueType demangleType(String value) {
		char type = value.charAt(0);
		int depth = Character.digit(value.charAt(1), 16);
		int size = BASE64.indexOf(value.charAt(2)) + (BASE64.indexOf(value.charAt(3) * 64));
		int flags = switch (type) {
			case 's' -> ValueType.SIGNED;
			case 'u' -> ValueType.UNSIGNED;
			case 'f' -> ValueType.FLOATING;
			default  -> ValueType.GENERIC;
		};
		return new ValueType("", size, depth, flags);
	}
	
	public static String demangleFunctionName(String name) {
		String[] parts = name.split("@", -1);
		return parts[0];
	}
}
