package hardcoded.compiler.instruction;

import java.text.DateFormat;
import java.util.*;

/**
 * Context class for IRPrograms.
 * 
 * This class will contain detailed information
 * about the code that has been compiled.
 * 
 * @author HardCoded
 */
public class IRContext {
	protected final List<String> strings = new ArrayList<>();
	protected String programName;
	public long creationDate;
	
	protected IRContext() {
		creationDate = System.currentTimeMillis();
	}
	
	public List<String> getStrings() {
		return Collections.unmodifiableList(strings);
	}
	
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
	
	@Override
	public String toString() {
		return "IRContext{date=\"%s\"}".formatted(DateFormat.getInstance().format(new Date(creationDate)));
	}
}
