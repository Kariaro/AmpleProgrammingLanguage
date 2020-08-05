package hardcoded.lexer;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	private final Map<String, List<Pattern>> groups;
	
	Tokenizer(Map<String, List<Pattern>> groups) {
		this.groups = groups;
	}

	public void parse(String string) {
		// System.out.println("Parsing string: '" + string + "'");
		
		for(String name : groups.keySet()) {
			List<Pattern> list = groups.get(name);
			System.out.println("Group: " + name);
			System.out.println("  -> " + list);
			
			for(Pattern pattern : list) {
				Matcher mat = pattern.matcher(string);
				System.out.println("TEST: '" + mat.replaceAll("") + "'");
				
			}
		}
	}
}
