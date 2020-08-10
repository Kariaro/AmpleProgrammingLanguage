package hardcoded.lexer;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import hardcoded.utils.StringUtils;

// TODO: Optimize the groups to ensure that if there is two symbols A:'ab' and C:'abcd', C should be tested always before A.
public class Tokenizer {
	private Map<String, SymbolGroup> groups;
	private List<Rule> rules;
	
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
		
		rules = groups.values().stream().flatMap(x -> x.rules.stream()).collect(Collectors.toList());
	}
	
	public List<TSym> parse(byte[] bytes) {
		TokenizerString string = new TokenizerString(bytes);
		
		List<TSym> list = new ArrayList<>();
		
		boolean hn = false;
		while(string.length() > 0) {
			TSym sym = parseSingle(string, 0);
			if(sym != null) {
				if(hn) {
					hn = false;
					System.out.println("'");
				}
				
				// Test if push is correct
				if(!sym.shouldDiscard()) {
					System.out.println("[" + sym.symbolName() + "] '" + sym.value + "'");
				}
			} else {
				if(!hn) {
					System.out.print("[Null] '");
					hn = true;
				}
				
				System.out.print(string.charAt(0));
				string.move(1);
			}
		}
		
		if(hn) {
			hn = false;
			System.out.println("')");
		}
		
		return list;
	}
	
	private TSym parseSingle(TokenizerString string, int index) {
		for(Rule rule : rules) {
			TSym sym = parseSingleRule(rule, string, index);
			if(sym != null) return sym;
		}
		
		return null;
	}
	
	private TSym parseSingleRule(Rule rule, TokenizerString string, int index) {
		if(rule.isDelimiter()) {
			Rule[] delim = rule.delimiter();
			int mark = string.index();
			
			String start = delim[0].string();
			
			int length = string.indexOf(delim[0].string());
			if(length != 0) return null;
			length += start.length();
			string.move(length);
			
			{
				String escape = delim[1].string();
				String close = delim[2].string();
				
				if(escape.isEmpty()) {
					int end = string.indexOf(close);
					
					string.move(mark - string.index());
					if(end < 0) return null;
					end += close.length();
					
					TSym sym = new TSym(rule, string.subSequence(0, length + end).toString());
					string.move(length + end);
					return sym;
				} else {
					int size = 0;
					
					String combined = escape + close;
					// System.out.println("Combined: '" + combined + "'");
					while(true) {
						int si = string.indexOf(size, close);
						// System.out.println("string.indexOf(" + size + ", '" + close + "') == " + si);
						if(si < 0) {
							string.move(mark - string.index());
							return null;
						}
						
						
						// Check for the escape character
						int ei = string.indexOf(size, combined);
						// System.out.println("string.indexOf(" + size + ", '" + combined + "') == " + ei);
						
						if(ei < 0 || ei > si) {
							string.move(mark - string.index());
							TSym sym = new TSym(rule, string.subSequence(0, length + si + close.length()).toString());
							string.move(length + si + close.length());
							return sym;
						}
						
						size = si + close.length();
					}
				}
			}
		} else {
			int length = string.matches(rule);
			
			if(length > 0) {
				TSym sym = new TSym(rule, string.subSequence(0, length).toString());
				string.move(length);
				return sym;
			}
		}
		
		return null;
	}
	
	public class SymbolGroup {
		private final List<Rule> rules;
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
			rules.add(new Rule(this, StringUtils.unescapeString(string)));
			return this;
		}
		
		public SymbolGroup addRegex(String regex) {
			rules.add(new Rule(this, Pattern.compile(regex)));
			return this;
		}
		
		public SymbolGroup addDelimiter(String open, String escape, String close) {
			rules.add(new Rule(this, new Rule[] {
				new Rule(this, StringUtils.unescapeString(open)),
				new Rule(this, StringUtils.unescapeString(escape)),
				new Rule(this, StringUtils.unescapeString(close))
			}));
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean shouldDiscard() {
			return discard;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("SymbolGroup('").append(name).append("'")
			  .append(discard ? " [remove]) { ":") { ");
			
			for(Rule rule : rules)
				sb.append(rule).append(", ");
			
			if(!rules.isEmpty())
				sb.deleteCharAt(sb.length() - 2);
			
			return sb.append("}").toString();
		}
	}
	
	public class TSym {
		public String value;
		public Rule rule;
		
		public int lineIndex;
		public int column;
		
		private TSym(Rule rule, String value) {
			this.rule = rule;
			this.value = value;
		}
		
		public int getLineIndex() {
			return lineIndex;
		}
		
		public int getColumn() {
			return column;
		}
		
		public String symbolName() {
			return rule.group.getName();
		}
		
		public boolean shouldDiscard() {
			return rule.group.shouldDiscard();
		}
		
		public String value() {
			return value;
		}
		
		public String toString() {
			return value;
		}
	}
	
	class Rule {
		private final SymbolGroup group;
		private final Rule[] delimiter;
		private final Pattern pattern;
		private final String string;
		
		private Rule(SymbolGroup group, Rule[] delimiter) { this(group, delimiter.clone(), null, null); }
		private Rule(SymbolGroup group, Pattern pattern) { this(group, null, pattern, null); }
		private Rule(SymbolGroup group, String string) { this(group, null, null, string); }
		private Rule(SymbolGroup group, Rule[] delimiter, Pattern pattern, String string) {
			this.delimiter = delimiter;
			this.pattern = pattern;
			this.string = string;
			this.group = group;
		}
		
		public boolean isDelimiter() { return delimiter != null; }
		public boolean isPattern() { return pattern != null; }
		public boolean isString() { return string != null; }
		
		public Rule[] delimiter() { return delimiter; }
		public Pattern pattern() { return pattern; }
		public String string() { return string; }
		
		@Override
		public String toString() {
			if(delimiter != null) return "%DELIMITER(" + delimiter[0] + ", " + delimiter[1] + ", " + delimiter[2] + ")";
			if(pattern != null) return "['" + pattern + "']";
			if(string != null) return "'" + string + "'";
			return null;
		}
	}
}
