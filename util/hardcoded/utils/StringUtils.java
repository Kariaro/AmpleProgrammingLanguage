package hardcoded.utils;

import java.lang.reflect.Array;
import java.util.List;

public final class StringUtils {
	private StringUtils() {
		
	}
	
	public static String listToString(CharSequence separator, List<?> list) {
		if(list == null) return null;
		return arrayToString(separator, list.toArray());
	}
	
	public static String arrayToString(CharSequence separator, Object array) {
		if(array == null) return null;
		
		try {
			int length = Array.getLength(array);
			
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < length; i++)
				sb.append(separator).append(Array.get(array, i));
			
			if(length > 0)
				sb.delete(0, separator.length());
			
			return sb.toString();
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
}
