package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.List;

public class IRData {
	public List<String> strings = new ArrayList<>();
	
	public int getStringIndex(String value) {
		return strings.indexOf(value);
	}
	
	public boolean hasString(String value) {
		return strings.contains(value);
	}
	
	public int getStringIndexAddIfAbsent(String value) {
		int index = getStringIndex(value);
		if(index < 0) {
			strings.add(value);
			return strings.size() - 1;
		}
		
		return index;
	}

	public String getString(int index) {
		return strings.get(index);
	}
}
