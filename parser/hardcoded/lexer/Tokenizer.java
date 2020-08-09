package hardcoded.lexer;

import java.util.*;
import java.util.regex.Pattern;

import hardcoded.utils.StringUtils;

public class Tokenizer {
	private Map<String, SymbolGroup> groups;
	
	public Tokenizer() {
		groups = new LinkedHashMap<>();
	}
	
	public SymbolGroup addGroup(String name) {
		if(contains(name)) return null;
		SymbolGroup group = new SymbolGroup(name);
		groups.put(name, group);
		return group;
	}
	
	public SymbolGroup getGroup(String name) {
		return groups.get(name);
	}
	
	public void removeGroup(String name) {
		groups.remove(name);
	}

	public boolean contains(String itemName) {
		return groups.containsKey(itemName);
	}

	void dump() {
		for(String name : groups.keySet()) {
			SymbolGroup group = groups.get(name);
			
			System.out.println(group);
		}
	}
	
	public class SymbolGroup {
		private final List<Object> rules;
		private boolean discard;
		private String name;
		
		private SymbolGroup(String name) {
			this.rules = new ArrayList<>();
			this.name = name;
		}
		
		public SymbolGroup setName(String name) {
			this.name = name;
			return this;
		}
		
		public SymbolGroup setDiscard(boolean discard) {
			this.discard = discard;
			return this;
		}
		
		public SymbolGroup addString(String string) {
			rules.add(StringUtils.unescapeString(string));
			return this;
		}
		
		public SymbolGroup addRegex(String regex) {
			rules.add(Pattern.compile(regex));
			return this;
		}
		
		public SymbolGroup addDelimiter(Object open, Object escape, Object close) {
			// System.out.println("  Delimiter(" + _getString(open) + ", " + _getString(escape) + ", " + _getString(close) + ")");
			rules.add(new Object[] { open, escape, close });
			return this;
		}
		
//		private String _getString(Object obj) {
//			if(obj == null) return null;
//			if(obj instanceof String) return "'" + obj + "'";
//			if(obj instanceof Pattern) return "['" + obj + "']";
//			return obj.toString();
//		}
		
		public String getName() {
			return name;
		}
		
		public boolean shouldDiscard() {
			return discard;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("SymbolGroup('").append(name).append("'");
			
			if(discard) sb.append(" [remove]");
			return sb.append(") { ").append(StringUtils.join(", ", rules)).append(" }").toString();
		}
	}
}
