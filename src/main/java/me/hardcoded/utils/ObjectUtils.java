package me.hardcoded.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This class is used to print a java objects content. It will print all private,
 * protected and public fields and return it in a tree type string.
 * 
 * @author HardCoded
 */
public class ObjectUtils {
	public static String deepPrint(Object obj, int depth) throws Exception {
		return deepPrint0("", obj, depth, false);
	}
	
	public static String deepPrint(String name, Object obj, int depth) throws Exception {
		return deepPrint0(name, obj, depth, true);
	}
	
	private static Set<Class<?>> getAllClasses(Class<?> clazz) {
		Set<Class<?>> set = new LinkedHashSet<>();
		List<Class<?>> classes = new ArrayList<>();
		
		do {
			classes.add(clazz);
			clazz = clazz.getSuperclass();
			
			if (clazz == Object.class) break;
		} while (clazz != null);
		
		// Add reverse order
		for (int i = classes.size() - 1; i >= 0; i--) {
			set.add(classes.get(i));
		}
		
		return set;
	}
	
	private static Set<Field> getAllFields(Class<?> clazz) {
		Set<Field> fields = new LinkedHashSet<>();
		for (Class<?> c : getAllClasses(clazz)) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		
		return fields;
	}
	
	private static boolean isCommonClass(Class<?> clazz) {
		return clazz == String.class
			|| clazz == File.class
			|| clazz == Pattern.class
			|| clazz.isEnum()
			|| clazz == Boolean.class
			|| clazz == AtomicInteger.class
			|| Collection.class.isAssignableFrom(clazz)
			|| Number.class.isAssignableFrom(clazz);
	}
	
	private static String getDebugName(String name, Object obj) {
		if (obj == null) {
			return name;
		}
		
		return name + (isCommonClass(obj.getClass())
			? ""
			: " (%s)".formatted(obj.getClass().getSimpleName())
		);
	}
	
	private static String deepPrint0(String name, Object obj, int depth, boolean showName) throws Exception {
		if (showName) {
			name += ": ";
		} else {
			name = "";
		}
		
		if (obj == null || depth < 1) {
			return name + Objects.toString(obj, "null");
		}
		
		Class<?> clazz = obj.getClass();
		String ty = name + clazz.getSimpleName() + " ";
		
		if (clazz == String.class) {
			return "%s(\"%s\")".formatted(ty, StringUtils.escapeString(obj.toString()));
		} else if (clazz == File.class) {
			return "%s( %s )".formatted(ty, obj.toString());
		} else if (clazz == Pattern.class) {
			return "%s(\"%s\")".formatted(ty, ((Pattern)obj).pattern());
		} else if (clazz.isEnum()
		|| clazz == Boolean.class
		|| clazz == AtomicInteger.class
		|| Number.class.isAssignableFrom(clazz)) {
			return "%s(%s)".formatted(ty, obj);
		}
//		else if (clazz == LowType.class) {
//			return "%s(%s, %d)".formatted(ty, ((LowType)obj).type(), ((LowType)obj).depth());
//		}
		
		if (Collection.class.isAssignableFrom(clazz)) {
			Collection<?> list = (Collection<?>)obj;
			StringBuilder sb = new StringBuilder();
			sb.append(clazz.getSimpleName()).append(" ").append(name.trim()).append("\n");
			
			Object[] array = list.toArray();
			for (int i = 0; i < array.length; i++) {
				String string = deepPrint0(getDebugName(Integer.toString(i), array[i]), array[i], depth - 1, true).trim();
				
				if (showName) {
					if (i == array.length - 1) {
						sb.append("\t+ ").append(string.replace("\n", "\n\t  "));
					} else {
						sb.append("\t+ ").append(string.replace("\n", "\n\t| "));
					}
				} else {
					sb.append(string);
				}
				
				sb.append("\n");
			}
			
			return sb.toString();
		}
		
		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder();
			sb.append(name.trim()).append("\n");
			
			int len = Array.getLength(obj);
			for (int i = 0; i < len; i++) {
				Object value = Array.get(obj, i);
				String string = deepPrint0(getDebugName(Integer.toString(i), value), value, depth - 1, true).trim();
				
				if (showName) {
					if (i == len - 1) {
						sb.append("\t+ ").append(string.replace("\n", "\n\t  "));
					} else {
						sb.append("\t+ ").append(string.replace("\n", "\n\t| "));
					}
				} else {
					sb.append(string);
				}
				
				sb.append("\n");
			}
			
			return sb.toString();
		}
		
		{
			List<Field> fields = getAllFields(clazz).stream()
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.toList();
			
			StringBuilder sb = new StringBuilder();
			sb.append(name.trim()).append("\n");
			
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);
				boolean acc = field.canAccess(obj);
				
				Object value = null;
				if (field.trySetAccessible()) {
					value = field.get(obj);
					field.setAccessible(acc);
				}
				
				String string = deepPrint0(
					getDebugName(field.getName(), value),
					value,
					depth - 1,
					true
				).trim();
				
				if (showName) {
					if (i == fields.size() - 1) {
						sb.append("\t+ ").append(string.replace("\n", "\n\t  "));
					} else {
						sb.append("\t+ ").append(string.replace("\n", "\n\t| "));
					}
				} else {
					sb.append(string);
				}
				
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
}
