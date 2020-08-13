package hardcoded.lexer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hardcoded.utils.StringUtils;

/**
 * This class is thread-safe as long as no groups are added while calling the addGroup functions.
 * 
 * @author HardCoded
 */
public class Tokenizer {
	private ImmutableTokenzier immutable;
	
	// TODO: Optimize the groups to ensure that if there is two symbols A:'ab' and C:'abcd', C should be tested always before A.
	private final Map<String, SymbolGroup> groups;
	private String defaultGroup = null;
	
	protected Tokenizer() {
		groups = new LinkedHashMap<>();
	}
	
	public void setDefaultGroup(String string) {
		this.defaultGroup = string;
	}
	
	public String getDefaultGroup() {
		return defaultGroup;
	}
	
	public SymbolGroup addGroup(String groupName) {
		return addGroup(groupName, false);
	}
	
	public SymbolGroup addGroup(String groupName, boolean discard) {
		if(contains(groupName)) return null;
		
		SymbolGroup group = new SymbolGroup(groupName);
		group.setDiscard(discard);
		groups.put(groupName, group);
		return group;
	}
	
	public SymbolGroup getGroup(String groupName) {
		return groups.get(groupName);
	}
	
	public void removeGroup(String groupName) {
		groups.remove(groupName);
	}
	
	/**
	 * Check if this tokenizer contains this item.
	 * @param groupName the group name.
	 * @return true if the tokenizer found the item.
	 */
	public boolean contains(String groupName) {
		return groups.containsKey(groupName);
	}
	
	/**
	 * Get a unmodifiable version of this tokenizer.
	 */
	public Tokenizer unmodifiableTokenizer() {
		if(immutable == null) {
			immutable = new ImmutableTokenzier(this);
		}
		
		return immutable;
	}
	
	public boolean isParseOnly() {
		return this.getClass() != Tokenizer.class;
	}

	void dump() {
		for(String name : groups.keySet()) {
			SymbolGroup group = groups.get(name);
			System.out.println(group);
		}
	}
	
	/**
	 * Parse a string into a list of symbols using the charset ISO_8859_1.
	 * 
	 * @param string
	 * @return a list of symbols.
	 * @throws NullPointerException if the string was null.
	 */
	public List<Symbol> parse(String string) {
		return parse(string.getBytes(StandardCharsets.ISO_8859_1));
	}
	
	/**
	 * Parse a string into a list of symbols using the specified charset.
	 * 
	 * @param string
	 * @return a list of symbols.
	 * @throws NullPointerException if the string was null.
	 */
	public List<Symbol> parse(String string, Charset charset) {
		return parse(string.getBytes(charset));
	}
	
	/**
	 * Parse a byte array into a list of symbols using
	 * the patterns given to this lexer.
	 * 
	 * @param bytes
	 * @return a list of symbols.
	 */
	public List<Symbol> parse(byte[] bytes) {
		TokenizerString string = new TokenizerString(bytes);
		List<Symbol> list = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		boolean hasNull = false;
		
		int line = 0;
		int column = 0;
		
		while(string.length() > 0) {
			Symbol sym = parseSingle(string, 0);
			if(sym != null) {
				if(hasNull) {
					hasNull = false;
					
					list.add(new Symbol(defaultGroup, false, sb.toString(), line, column));
					sb.delete(0, sb.length());
				}
				
				if(!sym.shouldDiscard()) list.add(sym);
			} else {
				if(!hasNull) {
					line = string.getLine();
					column = string.getColumn();
					hasNull = true;
				}
				
				sb.append(string.charAt(0));
				string.move(1);
			}
		}
		
		if(hasNull) {
			list.add(new Symbol(defaultGroup, false, sb.toString(), line, column));
			hasNull = false;
		}
		
		// for(Symbol sym : list) System.out.printf("[%s] %s\n", sym.group(), sym.value());
		
		return list;
	}
	
	private Symbol parseSingle(TokenizerString string, int index) {
		for(SymbolGroup group : groups.values()) {
			for(Rule rule : group.rules) {
				int length = -1;
				
				if(rule.isString()) {
					String rule_string = rule.string();
					
					if(rule_string.length() <= string.length()) {
						length = rule_string.length();
						for(int i = 0; i < rule_string.length(); i++) {
							if(rule_string.charAt(i) != string.charAt(i)) {
								length = -1;
								break;
							}
						}
					}
				} else if(rule.isPattern()) {
					Matcher matcher = rule.pattern().matcher(string);
					if(matcher.lookingAt()) length = matcher.end();
				}
				
				if(length > 0) {
					Symbol sym = new Symbol(rule.group.name, rule.group.discard, string.subSequence(0, length).toString(), string.getLine(), string.getColumn());
					string.move(length);
					return sym;
				}
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
		
		public SymbolGroup addStrings(String... strings) {
			for(String string : strings) {
				rules.add(new Rule(this, StringUtils.unescapeString(string)));
			}
			return this;
		}
		
		public SymbolGroup addRegex(String regex) {
			rules.add(new Rule(this, Pattern.compile(regex, Pattern.DOTALL)));
			return this;
		}
		
		public SymbolGroup addRegexes(String... patterns) {
			for(String regex : patterns) {
				rules.add(new Rule(this, Pattern.compile(regex, Pattern.DOTALL)));
			}
			return this;
		}
		
		public SymbolGroup addDelimiter(String open, String escape, String close) {
			rules.add(new Rule(this, open, escape, close));
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
	
	// TODO: Convert all strings into regex patterns to make this class smaller? (Performance???)
	class Rule {
		private final SymbolGroup group;
		private final Pattern pattern;
		private final String string;
		
		private Rule(SymbolGroup group, String open, String escape, String close) {
			String S = StringUtils.regexEscape(StringUtils.unescapeString(open));
			String C = StringUtils.regexEscape(StringUtils.unescapeString(close));
			
			String regex;
			if(escape.isEmpty()) {
				regex = S + ".*?" + C;
			} else {
				String E = StringUtils.regexEscape(StringUtils.unescapeString(escape));
				regex = S + "(?:" + E + "(?:" + E + "|" + C + "|(?!" + C + ").)|(?!" + E + "|" + C + ").)*" + C;
			}
			
			this.pattern = Pattern.compile(regex, Pattern.DOTALL);
			this.string = null;
			this.group = group;
		}
		
		private Rule(SymbolGroup group, Pattern pattern) {
			this.pattern = pattern;
			this.string = null;
			this.group = group;
		}
		
		private Rule(SymbolGroup group, String string) {
			this.pattern = null;
			this.string = string;
			this.group = group;
		}
		
		public boolean isPattern() { return pattern != null; }
		public boolean isString() { return string != null; }
		
		public Pattern pattern() { return pattern; }
		public String string() { return string; }
		
		@Override
		public String toString() {
			if(pattern != null) return "['" + pattern + "']";
			if(string != null) return "'" + string + "'";
			return null;
		}
	}
	
	private class ImmutableTokenzier extends Tokenizer {
		private final Tokenizer tokenizer;
		private ImmutableTokenzier(Tokenizer lexer) {
			if(lexer == null) throw new NullPointerException();
			this.tokenizer = lexer;
		}
		
		public void setDefaultGroup(String string) { throw new UnsupportedOperationException("You cannot modify this tokenizer."); }
		public String getDefaultGroup() { return tokenizer.defaultGroup; }
		public SymbolGroup addGroup(String name) { throw new UnsupportedOperationException("You cannot modify this tokenizer."); }
		public SymbolGroup addGroup(String name, boolean discard) { throw new UnsupportedOperationException("You cannot modify this tokenizer."); }
		public void removeGroup(String name) { throw new UnsupportedOperationException("You cannot modify this tokenizer."); }
		public SymbolGroup getGroup(String name) { throw new UnsupportedOperationException("You cannot modify this tokenizer."); }
		public boolean contains(String itemName) { return tokenizer.contains(itemName); }
		public Tokenizer unmodifiableTokenizer() { return this; }
		public List<Symbol> parse(String string, Charset charset) { return tokenizer.parse(string, charset); }
		public List<Symbol> parse(String string) { return tokenizer.parse(string); }
		public List<Symbol> parse(byte[] bytes) { return tokenizer.parse(bytes); }
		public boolean equals(Object obj) { return Objects.equals(tokenizer, obj); }
		public int hashCode() { return tokenizer.hashCode(); }
		public String toString() { return tokenizer.toString(); }
	}
}
