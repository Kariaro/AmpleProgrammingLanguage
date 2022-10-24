package me.hardcoded.utils.types;

import me.hardcoded.compiler.AmpleMangler;
import me.hardcoded.compiler.parser.type.Reference;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A mangled map that contains mangled function references
 * <p>
 * This class is used to resolve vararg functions and check for overloads
 *
 * @author HardCoded
 */
public class MangledFunctionMap {
	/**
	 * First create a map that separates the path of the function
	 *
	 * <code>(Path, Parameters)</code>
	 */
	private final Map<String, ParameterMap> map;
	
	public MangledFunctionMap() {
		this.map = new LinkedHashMap<>();
	}
	
	public void clear() {
		map.clear();
	}
	
	public int size() {
		int result = 0;
		for (ParameterMap item : map.values()) {
			result += item.size();
		}
		
		return result;
	}
	
	public boolean put(Reference reference) {
		AmpleMangler.MangledFunction mangledFunction = AmpleMangler.demangleFunction(reference.getMangledName());
		if (isInvalidSetter(mangledFunction)) {
			// Invalid function. Only fully defined functions are addable
			return false;
		}
		
		return map.computeIfAbsent(reference.getPath(), (name) -> new ParameterMap(null))
			.put(reference, mangledFunction, 0);
	}
	
	public Reference get(Reference reference) {
		return get(reference.getMangledName());
	}
	
	public Reference get(String mangledName) {
		AmpleMangler.MangledFunction mangledFunction = AmpleMangler.demangleFunction(mangledName);
		ParameterMap root = map.get(mangledFunction.getPath());
		return root == null ? null : root.get(mangledFunction, 0);
	}
	
	public Reference getBlocker(Reference reference) {
		return getBlocker(reference.getMangledName());
	}
	
	public Reference getBlocker(String mangledName) {
		AmpleMangler.MangledFunction mangledFunction = AmpleMangler.demangleFunction(mangledName);
		if (isInvalidSetter(mangledFunction)) {
			// Invalid function. Only fully defined functions are addable
			return null;
		}
		
		ParameterMap root = map.get(mangledFunction.getPath());
		return root == null ? null : root.getBlocker(mangledFunction, 0);
	}
	
	/**
	 * Returns if the value is allowed to be put inside this map
	 */
	private boolean isInvalidSetter(AmpleMangler.MangledFunction mangledFunction) {
		// Linked variables are not allowed in the map
		if (mangledFunction.getReturnType().isLinked()) {
			return true;
		}
		
		for (int i = 0; i < mangledFunction.getParameterCount(); i++) {
			if (mangledFunction.getParameter(i).getValueType().isLinked()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns if this map contains the mangled name
	 *
	 * @param mangledName the mangled name
	 * @see AmpleMangler
	 */
	public boolean contains(String mangledName) {
		return get(mangledName) != null;
	}
	
	private static class ParameterMap {
		final Map<String, ParameterMap> map = new LinkedHashMap<>();
		final String name;
		Reference reference;
		boolean varargs;
		
		ParameterMap(String name) {
			this.name = name;
		}
		
		// Setter
		boolean put(Reference reference, AmpleMangler.MangledFunction mangledFunction, int index) {
			// Now we need to check that the list does not contain the reference
			// Check if we are vararg and next argument is varargs
			if ((index == mangledFunction.getParameterCount())
				|| (index == mangledFunction.getParameterCount() - 1 && mangledFunction.isVararg())) {
				// If reference has already been defined we cannot add it again
				if (this.reference != null) {
					return false;
				}
				
				if (mangledFunction.isVararg()) {
					// If we are vararg but we have already had elements added
					if (!map.isEmpty()) {
						return false;
					}
					
					varargs = true;
				}
				
				this.reference = reference;
				return true;
			}
			
			// Check if this map is varargs
			if (varargs) {
				// Because this parameter is varargs no other reference could replace this
				return false;
			}
			
			ValueType type = mangledFunction.getParameter(index).getValueType();
			if (type.isLinked()) {
				// The type that we are adding must be resolvable
				return false;
			}
			
			// Add the type directly
			String rawType = mangledFunction.getRawParameter(index);
			
			// Make sure we only add the map if we were allowed to create it
			ParameterMap next = map.get(rawType);
			if (next != null) {
				return next.put(reference, mangledFunction, index + 1);
			}
			
			next = new ParameterMap(rawType);
			if (next.put(reference, mangledFunction, index + 1)) {
				map.put(rawType, next);
				return true;
			}
			
			return false;
		}
		
		// Getter
		Reference get(AmpleMangler.MangledFunction mangledFunction, int index) {
			// If we went to the end we found a match
			// If we are varargs we return our value
			if (index == mangledFunction.getParameterCount() || varargs) {
				// Return types does not match
				ValueType returnType = mangledFunction.getReturnType();
				if (!returnType.isLinked() && !reference.getValueType().equals(returnType)) {
					return null;
				}
				
				return reference;
			}
			
			// Check value type
			ValueType type = mangledFunction.getParameter(index).getValueType();
			if (type.isLinked()) {
				// Only one result is allowed
				Reference result = null;
				
				for (ParameterMap item : map.values()) {
					Reference itemResult = item.get(mangledFunction, index + 1);
					
					if (result != null && itemResult != null) {
						// If we have more than one result we return null
						return null;
					}
					
					result = result == null ? itemResult : result;
				}
				
				return result;
			} else if (type.isVarargs()) {
				// If only one element in the tree matches the description we return
				return size() == 1
					? findFirstValue()
					: null;
			}
			
			// Get the type directly
			String rawType = mangledFunction.getRawParameter(index);
			ParameterMap next = map.get(rawType);
			return next == null ? null : next.get(mangledFunction, index + 1);
		}
		
		// Getter blocker
		
		/**
		 * Returns {@code null} if nothing will block the put operation
		 */
		Reference getBlocker(AmpleMangler.MangledFunction mangledFunction, int index) {
			// Check if we have gotten to the end
			if ((index == mangledFunction.getParameterCount())
				|| (index == mangledFunction.getParameterCount() - 1 && mangledFunction.isVararg())) {
				if (reference != null) {
					// Our reference blocks the put operation
					return reference; // false;
				}
				
				if (mangledFunction.isVararg() && !map.isEmpty()) {
					// The first value found blocks
					return findFirstValue();
				}
				
				// Nothing blocks the operation
				return null;
			}
			
			// Check if this map is varargs
			if (varargs) {
				// Because this parameter is varargs no other reference could replace this
				return reference;
			}
			
			ValueType type = mangledFunction.getParameter(index).getValueType();
			if (type.isLinked()) {
				// Linked types cannot be used to put
				return null;
			}
			
			// Check the next entry if it blocks
			ParameterMap next = map.get(mangledFunction.getRawParameter(index));
			return next == null ? null : next.getBlocker(mangledFunction, index + 1);
		}
		
		int size() {
			int result = (reference != null) ? 1 : 0;
			
			for (ParameterMap item : map.values()) {
				result += item.size();
			}
			
			return result;
		}
		
		Reference findFirstValue() {
			if (reference != null) {
				return reference;
			}
			
			for (ParameterMap item : map.values()) {
				Reference result = item.findFirstValue();
				
				if (result != null) {
					return result;
				}
			}
			
			return null;
		}
		
		void callback(LinkedList<String> list, BiConsumer<List<String>, Reference> consumer) {
			if (varargs) {
				list.add(".");
			}
			
			if (reference != null) {
				consumer.accept(list, reference);
			}
			
			for (var entry : map.entrySet()) {
				list.add(entry.getKey());
				entry.getValue().callback(list, consumer);
				list.removeLast();
			}
			
			if (varargs) {
				list.removeLast();
			}
		}
	}
	
	@Override
	public String toString() {
		// To accurately print this map we need to step through the different params
		StringBuilder result_sb = new StringBuilder();
		
		for (var entry : map.entrySet()) {
			StringBuilder func_sb = new StringBuilder();
			entry.getValue().callback(new LinkedList<>(), (list, reference) -> {
				if (!func_sb.isEmpty()) {
					func_sb.append("\n");
				}
				
				func_sb.append("(");
				for (int i = 0; i < list.size(); i++) {
					if (i > 0) {
						func_sb.append(", ");
					}
					
					func_sb.append(AmpleMangler.demangleType(list.get(i)));
				}
				func_sb.append(")");
			});
			
			result_sb
				.append(entry.getKey()).append(":\n")
				.append(func_sb.toString().indent(4));
		}
		
		return result_sb.toString();
	}
}
