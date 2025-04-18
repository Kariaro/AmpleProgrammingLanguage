package me.hardcoded.compiler;

import me.hardcoded.compiler.parser.type.Namespace;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Mangle class used by the {@code AmpleProgrammingLanguage}
 * <p/>
 * <pre>
 * Function: (returnType)@(namespace)@(name)@(&lt;Types&gt;)
 * Type:     (namespace)@(name)@(type)(depth)(size)</pre>
 */
public class AmpleMangler {
	private static final String BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final String HEX = "0123456789abcdef";
	
	public static String mangleFunction(ValueType returnType, Namespace namespace, String name, List<Reference> parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append("_AF");
		if (namespace.isRoot()) {
			sb.append(name.length()).append(name);
		} else {
			sb.append("N");
			for (String part : namespace.getParts()) {
				sb.append(part.length()).append(part);
			}
			sb.append(name.length()).append(name);
			sb.append("E");
		}
		if (!Primitives.NONE.equals(returnType)) {
			sb.append("R").append(mangleType(returnType));
		}
		for (Reference param : parameters) {
			sb.append(mangleType(param.getValueType()));
		}
		return sb.toString();
	}
	
	public static String mangleVariable(Namespace namespace, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("_AV");
		if (namespace.isRoot()) {
			sb.append(name.length()).append(name);
		} else {
			sb.append("N");
			for (String part : namespace.getParts()) {
				sb.append(part.length()).append(part);
			}
			sb.append(name.length()).append(name);
			sb.append("E");
		}
		return sb.toString();
	}
	
	public static String mangleType(ValueType type) {
		if (type.isVarargs()) {
			return "V";
		}
		
		if (Primitives.NONE.equals(type)) {
			return "n";
		}
		
		StringBuilder sb = new StringBuilder();
		
		// Depth information
		if (type.getDepth() > 0) {
			sb.append("A").append(type.getDepth());
		}
		
		if (type.isLinked()) {
			// TODO: Type namespace?
			return "?" + type.getName().length() + type.getName();
		}
		
		// Type information
		if (type.isSigned() || type.isUnsigned()) {
			String match = switch (type.getSize()) {
				case 8 -> type.isUnsigned() ? "B" : "b";
				case 16 -> type.isUnsigned() ? "W" : "w";
				case 32 -> type.isUnsigned() ? "D" : "d";
				case 64 -> type.isUnsigned() ? "Q" : "q";
				case 128 -> type.isUnsigned() ? "X" : "x";
				case 256 -> type.isUnsigned() ? "Y" : "y";
				case 512 -> type.isUnsigned() ? "Z" : "z";
				default -> {
					String str = (type.isSigned() ? "i" : "u") + type.getSize();
					yield "?" + str.length() + str;
				}
			};
			sb.append(match);
		} else if (type.isFloating()) {
			String match = switch (type.getSize()) {
				case 32 -> "f";
				case 64 -> "F";
				default -> {
					String str = "f" + type.getSize();
					yield "?" + str.length() + str;
				}
			};
			sb.append(match);
		} else {
			sb.append("S" + type.getName().length() + type.getName());
		}
		return sb.toString();
	}
	
	private static int readInt(CharIter iter) {
		int result = 0;
		char value;
		while (iter.hasNext() && Character.isDigit(value = iter.next())) {
			result = result * 10 + (value - '0');
		}
		iter.prev();
		return result;
	}
	
	private static String readVarString(CharIter iter) {
		int len = readInt(iter);
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < len && iter.hasNext(); i++) {
			result.append(iter.next());
		}
		return result.toString();
	}
	
	private static String readString(CharIter iter, int len) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < len && iter.hasNext(); i++) {
			result.append(iter.current());
			iter.next();
		}
		return result.toString();
	}
	
	public static ValueType demangleType(String mangle) {
		if (mangle == null) {
			return null;
		}
		
		return demangleType(CharIter.of(mangle));
	}
	
	public static ValueType demangleType(CharIter iter) {
		// System.out.println("start " + iter.index + ", " + iter.remString());
		char c = iter.next();
		int depth = 0;
		switch (c) {
			case 'A' -> {
				depth = readInt(iter);
				c = iter.next();
			}
		}
		
		// System.out.println("  arr " + iter.index + ", " + iter.remString() + ", " + c + ", " + depth);
		ValueType result = switch (c) {
			case '?' -> {
				String name = readVarString(iter);
				yield new ValueType(name, 0, depth, ValueType.LINKED);
			}
			case 'S' -> {
				String name = readVarString(iter);
				yield new ValueType(name, 0, depth, ValueType.STRUCT);
			}
			case 'V' -> new ValueType("", 8, 1, ValueType.VARARGS);
			case 'n' -> Primitives.NONE.createArray(depth);
			
			case 'b' -> Primitives.I8.createArray(depth);
			case 'B' -> Primitives.U8.createArray(depth);
			case 'w' -> Primitives.I16.createArray(depth);
			case 'W' -> Primitives.U16.createArray(depth);
			case 'd' -> Primitives.I32.createArray(depth);
			case 'D' -> Primitives.U32.createArray(depth);
			case 'q' -> Primitives.I64.createArray(depth);
			case 'Q' -> Primitives.U64.createArray(depth);
			case 'x' -> Primitives.I128.createArray(depth);
			case 'X' -> Primitives.U128.createArray(depth);
			case 'y' -> Primitives.I256.createArray(depth);
			case 'Y' -> Primitives.U256.createArray(depth);
			case 'z' -> Primitives.I512.createArray(depth);
			case 'Z' -> Primitives.U512.createArray(depth);
			case 'f' -> Primitives.F32.createArray(depth);
			case 'F' -> Primitives.F64.createArray(depth);
			
			default -> {
				throw new RuntimeException("Invalid type '" + (char) c + "'");
			}
		};
		// System.out.println("after " + iter.index + ", " + iter.remString());
		// System.out.println("==== > " + result);
		return result;
	}
	
	public static MangledFunction demangleFunction(String mangle) {
		var iter = CharIter.of(mangle);
		if (iter.remaining() < 4 || !(iter.next() == '_' && iter.next() == 'A' && iter.next() == 'F')) {
			throw new RuntimeException("Invalid mangled name '" + mangle + "'");
		}
		
		try {
			List<String> values = new ArrayList<>();
			if (iter.peek() == 'N') {
				iter.next();
				int max = 256;
				while (--max > 0) {
					if (iter.peek() == 'E') {
						break;
					}
					values.add(readVarString(iter));
				}
				if (max <= 0) {
					throw new RuntimeException("Failed to demangle '" + mangle + "' got stuck reading name");
				}
				iter.next();
			} else {
				values.add(readVarString(iter));
			}
			
			ValueType returnType = Primitives.NONE;
			List<ValueType> params = new ArrayList<>();
			
			if (iter.hasNext()) {
				if (iter.peek() == 'R') {
					iter.next();
					returnType = demangleType(iter);
				}
				
				while (iter.remaining() > 0) {
					params.add(demangleType(iter));
				}
			}
			
			String namespacePath = String.join("::", values.subList(0, values.size() - 1));
			String functionName = values.get(values.size() - 1);
			return new MangledFunction(mangle, namespacePath, functionName, returnType, params);
		} catch (Exception e) {
			throw new RuntimeException("Failed to demangle the string '" + mangle + "'", e);
		}
	}
	
	public static class MangledFunction {
		public final String mangledString;
		public final String namespacePath;
		public final String functionName;
		private final Reference[] parameters;
		private final ValueType returnType;
		
		private MangledFunction(String mangledString, String namespace, String name, ValueType returnType, List<ValueType> params) {
			this.mangledString = mangledString;
			this.namespacePath = namespace;
			this.functionName = name;
			this.returnType = returnType;
			this.parameters = new Reference[params.size()];
			for (int i = 0; i < params.size(); i++) {
				this.parameters[i] = new Reference("", new Namespace(), params.get(i), i, Reference.VARIABLE);
				this.parameters[i].setMangledName(AmpleMangler.mangleType(params.get(i)));
			}
		}
		
		public int getParameterCount() {
			return parameters.length;
		}
		
		public Reference getParameter(int index) {
			return parameters[index];
		}
		
		public ValueType getReturnType() {
			return returnType;
		}
		
		public String getRawParameter(int index) {
			return parameters[index].getMangledName();
		}
		
		public boolean isVararg() {
			return getParameterCount() > 0 && getParameter(getParameterCount() - 1).getValueType().isVarargs();
		}
		
		public String getPath() {
			return namespacePath.isEmpty()
				? functionName
				: (namespacePath + "::" + functionName);
		}
		
		@Deprecated
		public boolean matches(String mangled) {
			if (mangledString.equals(mangled)) {
				return true;
			}
			
			var other = AmpleMangler.demangleFunction(mangled);
			
			// Check that the namespace and name matches
			if (!namespacePath.equals(other.namespacePath)
				|| !functionName.equals(other.functionName)) {
				return false;
			}
			
			// Not enough arguments
			if (!isVararg()) {
				if (parameters.length != other.parameters.length) {
					return false;
				}
				
				// All arguments must match
				for (int i = 0; i < parameters.length; i++) {
					var thisParam = getParameter(i).getValueType();
					var thatParam = other.getParameter(i).getValueType();
					
					// Vararg params
					if (thisParam.isLinked() || thatParam.isLinked()) {
						continue;
					}
					
					if (!thisParam.equals(thatParam)) {
						return false;
					}
				}
			} else {
				// Varargs
				// Other func did not have enough parameters
				if (other.getParameterCount() < getParameterCount() - 1) {
					return false;
				}
				
				// All arguments must match (skip last param because that is vararg)
				for (int i = 0; i < parameters.length - 1; i++) {
					var thisParam = getParameter(i).getValueType();
					var thatParam = other.getParameter(i).getValueType();
					
					// Vararg params
					if (thisParam.isLinked() || thatParam.isLinked()) {
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
			sb.append("fn ");
			if (namespacePath.isEmpty()) {
				sb.append(functionName);
			} else {
				sb.append(namespacePath).append("::").append(functionName);
			}
			sb.append(" (");
			
			for (int i = 0; i < parameters.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				
				sb.append(parameters[i].getValueType());
			}
			sb.append(") : ").append(returnType);
			return sb.toString();
		}
	}
	
	private static class CharIter {
		private final char[] array;
		private int index;
		private char current;
		
		private CharIter(char[] values) {
			this.array = values;
			this.index = 0;
		}
		
		public char next() {
			current = array[index];
			index += 1;
			return current;
		}
		
		public char peek() {
			return array[index];
		}
		
		public char prev() {
			current = array[index - 1];
			index -= 1;
			return current;
		}
		
		public char current() {
			return current;
		}
		
		public boolean hasNext() {
			return index < array.length;
		}
		
		public int remaining() {
			return array.length - index;
		}
		
		public String remString() {
			return new String(array).substring(index);
		}
		
		static CharIter of(String text) {
			return new CharIter(text.toCharArray());
		}
	}
}
