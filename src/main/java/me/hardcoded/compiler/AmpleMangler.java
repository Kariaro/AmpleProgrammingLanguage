package me.hardcoded.compiler;

import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AmpleMangler {
	private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String HEX = "0123456789abcdef";
	
	public static String mangleFunction(Namespace namespace, String name, List<Reference> parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append(namespace.getPath())
			.append('@')
			.append(name);
		for (Reference param : parameters) {
			sb.append('@').append(mangleType(param.getValueType()));
		}
		return sb.toString();
	}
	
	public static String mangleVariable(Namespace namespace, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(namespace.getPath())
			.append('@')
			.append(name);
		return sb.toString();
	}
	
	public static String mangleType(ValueType type) {
		if (type.isLinked()) {
			return "?";
		}
		
		if (type.isVarargs()) {
			return ".";
		}
		
		StringBuilder sb = new StringBuilder();
		if (type.isSigned())
			sb.append('i');
		else if (type.isUnsigned())
			sb.append('u');
		else if (type.isFloating())
			sb.append('f');
		else
			sb.append('x');
		sb.append(HEX.charAt(type.getDepth()))
			.append(BASE64.charAt(type.getSize() % 64))
			.append(BASE64.charAt(type.getSize() / 64));
		return sb.toString();
	}
	
	public static ValueType demangleType(String value) {
		char type = value.charAt(0);
		if (type == '?') {
			return new ValueType("", 0, 0, ValueType.LINKED);
		}
		
		if (type == '.') {
			return new ValueType("", 0, 1, ValueType.VARARGS);
		}
		
		int depth = Character.digit(value.charAt(1), 16);
		int size = BASE64.indexOf(value.charAt(2)) + (BASE64.indexOf(value.charAt(3)) * 64);
		int flags = switch (type) {
			case 'i' -> ValueType.SIGNED;
			case 'u' -> ValueType.UNSIGNED;
			case 'f' -> ValueType.FLOATING;
			default -> ValueType.GENERIC;
		};
		return new ValueType("", size, depth, flags);
	}
	
	public static boolean isFunctionVarargs(String mangledName) {
		String[] parts = mangledName.split("@", -1);
		return ".".equals(parts[parts.length - 1]);
	}
	
	public static MangledFunction demangleFunction(String name) {
		return new MangledFunction(name);
	}
	
	public static class MangledFunction {
		public final String mangledString;
		public final String namespacePath;
		public final String functionName;
		public final String[] mangledParts;
		public final List<Reference> parameters;
		
		private MangledFunction(String mangledString) {
			this.mangledParts = mangledString.split("@", -1);
			this.mangledString = mangledString;
			this.namespacePath = mangledParts[0];
			this.functionName = mangledParts[1];
			this.parameters = new ArrayList<>();
			
			Namespace empty = new Namespace();
			for (int i = 2; i < mangledParts.length; i++) {
				ValueType type = AmpleMangler.demangleType(mangledParts[i]);
				parameters.add(new Reference("", empty, type, i - 2, Reference.VARIABLE));
			}
		}
		
		public int getParameterCount() {
			return parameters.size();
		}
		
		public boolean isVararg() {
			return !parameters.isEmpty() && parameters.get(parameters.size() - 1).getValueType().isVarargs();
		}
		
		public boolean matches(String mangled) {
			if (mangledString.equals(mangled)) {
				return true;
			}
			
			String[] parts = mangled.split("@", -1);
			
			// Check that the namespace and name matches
			if (!namespacePath.equals(parts[0]) || !functionName.equals(parts[1])) {
				return false;
			}
			
			// Not enough arguments
			if (!isVararg()) {
				if (mangledParts.length != parts.length) {
					return false;
				}
				
				// All arguments must match
				for (int i = 2; i < mangledParts.length; i++) {
					String thisParam = mangledParts[i];
					String thatParam = parts[i];
					
					// Vararg params
					if (thisParam.equals("?") || thatParam.equals("?")) {
						continue;
					}
					
					if (!thisParam.equals(thatParam)) {
						return false;
					}
				}
			} else {
				// Varargs
				// Part did not have enough parameters
				if (parts.length < mangledParts.length - 1) {
					return false;
				}
				
				// All arguments must match (skip last param because that is vararg)
				for (int i = 2; i < mangledParts.length - 1; i++) {
					String thisParam = mangledParts[i];
					String thatParam = parts[i];
					
					// Vararg params
					if (thisParam.equals("?") || thatParam.equals("?")) {
						continue;
					}
					
					if (!thisParam.equals(thatParam)) {
						return false;
					}
				}
			}
			
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("func ");
			if (namespacePath.isEmpty()) {
				sb.append(functionName);
			} else {
				sb.append(namespacePath).append("::").append(functionName);
			}
			sb.append(" (");
			
			Iterator<Reference> iter = parameters.iterator();
			while (iter.hasNext()) {
				Reference reference = iter.next();
				if (reference.getValueType().isLinked()) {
					sb.append("?");
				} else {
					sb.append(reference.getValueType());
				}
				
				if (iter.hasNext()) {
					sb.append(", ");
				}
			}
			
			return sb.append(")").toString();
		}
	}
}
