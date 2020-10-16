package hardcoded.lexer;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hardcoded.utils.StringUtils;

/**
 * This {@code Tokenizer} class reads an input string and outputs a {@code Token}
 * generated from each rule inside this tokenizer.
 * 
 * @author HardCoded
 */
public class Tokenizer implements Serializable {
	private static final long serialVersionUID = -39792822918295242L;
	private transient ImmutableTokenzier immutable;
	
	private final Map<String, SymbolGroup> groups;
	
	/**
	 * If a token didn't match any group it will default to this value.
	 */
	private String defaultGroup = null;
	
	/**
	 * This field is set if groups should automaticly be discarded
	 * from the token list.
	 */
	private boolean autoDiscard = true;
	
	protected Tokenizer() {
		groups = new LinkedHashMap<>();
	}
	
	public void setDefaultGroup(String string) {
		this.defaultGroup = string;
	}
	
	public String getDefaultGroup() {
		return defaultGroup;
	}
	
	/**
	 * Add a new symbol group to this tokenizer.
	 * 
	 * @param	name
	 * @return	a new symbol group
	 */
	public SymbolGroup add(String name) {
		return add(name, false);
	}
	
	/**
	 * Add a new symbol group to this tokenizer.
	 * 
	 * @param	name
	 * @param	discard
	 * @return	a new symbol group
	 */
	public SymbolGroup add(String name, boolean discard) {
		if(contains(name)) return null;
		
		SymbolGroup group = new SymbolGroup(name);
		group.setDiscard(discard);
		groups.put(name, group);
		return group;
	}
	
	/**
	 * Set if the lexer should automaticly remove all tokens that has the discard flag.
	 * @param	enable
	 */
	public void setAutoDiscard(boolean enable) {
		autoDiscard = enable;
	}
	
	public boolean hasAutoDiscard() {
		return autoDiscard;
	}
	
	/**
	 * Get a group from this tokenizer.
	 * @param	name	the group name
	 * @return	the group that matched the name
	 */
	public SymbolGroup get(String name) {
		return groups.get(name);
	}
	
	/**
	 * Remove a group from this tokenizer.
	 * @param	name	the group name
	 * @return	{@code true} if the group was found and removed
	 */
	public boolean remove(String name) {
		return groups.remove(name) != null;
	}
	
	/**
	 * Check if this tokenizer contains this group.
	 * @param	name	the group name
	 * @return	{@code true} if the tokenizer found the item
	 */
	public boolean contains(String name) {
		return groups.containsKey(name);
	}
	
	/**
	 * Get a immutable version of this tokenizer.
	 */
	public Tokenizer getImmutableTokenizer() {
		if(immutable == null) {
			immutable = new ImmutableTokenzier(this);
		}
		
		return immutable;
	}
	
	/**
	 * Get if this tokenizer is immutable.
	 */
	public boolean isImmutable() {
		return this.getClass() != Tokenizer.class;
	}
	
	/**
	 * Parse a string into a list of symbols using the charset ISO_8859_1.
	 * 
	 * @param	string
	 * @return	a list of symbols
	 * @throws	NullPointerException
	 * 			if the string was {@code null}
	 */
	public Token parse(String string) {
		return parse(string.getBytes(StandardCharsets.ISO_8859_1));
	}
	
	/**
	 * Parse a string into a list of symbols using the specified charset.
	 * 
	 * @param	string
	 * @return	a list of symbols.
	 * @throws	NullPointerException
	 * 			if the string was {@code null}
	 */
	public Token parse(String string, Charset charset) {
		return parse(string.getBytes(charset));
	}
	
	/**
	 * Parse a byte array into a list of symbols using
	 * the patterns given to this lexer.
	 * 
	 * @param	bytes
	 * @return	a list of symbols
	 */
	public Token parse(byte[] bytes) {
		TokenizerString string = new TokenizerString(bytes);
		Token start = new Token();
		Token token = start;
		
		StringBuilder sb = new StringBuilder();
		boolean hasNull = false;
		
		int line = 0;
		int column = 0;
		int index = 0;
		
		while(string.length() > 0) {
			Token sym = parseSingle(string, 0);
			if(sym == null) {
				if(!hasNull) {
					line = string.getLine();
					column = string.getColumn();
					index = string.getIndex();
					hasNull = true;
				}
				
				sb.append(string.charAt(0));
				string.move(1);
				continue;
			}
			
			if(hasNull) {
				hasNull = false;
				
				Token nullSym = new Token(defaultGroup, sb.toString(), false, line, column, index);
				sb.delete(0, sb.length());
				
				token.next = nullSym;
				nullSym.prev = token;
				token = nullSym;
			}
			
			if(!autoDiscard || !sym.discard) {
				token.next = sym;
				sym.prev = token;
				token = sym;
			}
		}
		
		if(hasNull) {
			Token nullSym = new Token(defaultGroup, sb.toString(), false, line, column, index);
			token.next = nullSym;
			nullSym.prev = token;
		}
		
		if(start.next != null) {
			start = start.next;
			start.prev = null;
		}
		
		return start;
	}
	
	private Token parseSingle(TokenizerString string, int index) {
		SymbolGroup g = null;
		int length = -1;
		
		for(SymbolGroup group : groups.values()) {
			for(Rule rule : group.rules) {
				int len = -1;
				
				if(rule.string != null) {
					String rule_string = rule.string;
					
					if(rule_string.length() <= string.length()) {
						len = rule_string.length();
						for(int i = 0; i < rule_string.length(); i++) {
							if(rule_string.charAt(i) != string.charAt(i)) {
								len = -1;
								break;
							}
						}
					}
				} else {
					Matcher matcher = rule.pattern.matcher(string);
					if(matcher.lookingAt()) len = matcher.end();
				}
				
				if(len >= length) {
					length = len;
					g = group;
				}
			}
		}
		
		if(g != null && length > 0) {
			Token sym = new Token(g.name, string.subSequence(0, length).toString(), g.discard, string.getLine(), string.getColumn(), string.getIndex());
			string.move(length);
			return sym;
		}
		
		return null;
	}
	
	public class SymbolGroup implements Serializable {
		private static final long serialVersionUID = -8706828513749481057L;
		
		private final List<Rule> rules;
		private final String name; 
		private boolean discard;
		
		private SymbolGroup(String name) {
			this.rules = new ArrayList<>();
			this.name = name;
		}
		
		public SymbolGroup setDiscard(boolean discard) {
			this.discard = discard;
			return this;
		}
		
		public SymbolGroup addString(String string) {
			rules.add(new Rule(string));
			return this;
		}
		
		public SymbolGroup addStrings(String... strings) {
			for(String string : strings) {
				rules.add(new Rule(string));
			}
			return this;
		}
		
		public SymbolGroup addRegex(String regex) {
			rules.add(new Rule(Pattern.compile(regex, Pattern.DOTALL)));
			return this;
		}
		
		public SymbolGroup addRegexes(String... patterns) {
			for(String regex : patterns) {
				rules.add(new Rule(Pattern.compile(regex, Pattern.DOTALL)));
			}
			return this;
		}
		
		public SymbolGroup addDelimiter(String open, String escape, String close) {
			rules.add(new Rule(open, escape, close));
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
	
	private class Rule implements Serializable {
		private static final long serialVersionUID = 1885877594264837458L;
		protected final Pattern pattern;
		protected final String string;
		
		private Rule(String open, String escape, String close) {
			String S = StringUtils.regexEscape(open);
			String C = StringUtils.regexEscape(close);
			
			String regex;
			if(escape.isEmpty()) {
				regex = S + ".*?" + C;
			} else {
				String E = StringUtils.regexEscape(escape);
				regex = S + "(?:" + E + "(?:" + E + "|" + C + "|(?!" + C + ").)|(?!" + E + "|" + C + ").)*" + C;
			}
			
			this.pattern = Pattern.compile(regex, Pattern.DOTALL);
			this.string = null;
		}
		
		private Rule(Pattern pattern) {
			this.pattern = pattern;
			this.string = null;
		}
		
		private Rule(String string) {
			this.pattern = null;
			this.string = string;
		}
		
		@Override
		public String toString() {
			return pattern == null ? ("'" + string + "'"):("['" + pattern + "']");
		}
	}
	
	private class ImmutableTokenzier extends Tokenizer {
		private static final long serialVersionUID = 5232114952149326460L;
		
		private final Tokenizer tokenizer;
		private ImmutableTokenzier(Tokenizer lexer) {
			if(lexer == null) throw new NullPointerException();
			this.tokenizer = lexer;
		}
		
		public SymbolGroup add(String name, boolean discard) { throw new UnsupportedOperationException("Tokenizer is not modifiable."); }
		public void setAutoDiscard(boolean enable) { throw new UnsupportedOperationException("Tokenizer is not modifiable."); }
		public void setDefaultGroup(String string) { throw new UnsupportedOperationException("Tokenizer is not modifiable."); }
		public SymbolGroup get(String name) { throw new UnsupportedOperationException("Tokenizer is not modifiable."); }
		public boolean remove(String name) { throw new UnsupportedOperationException("Tokenizer is not modifiable."); }
		public boolean contains(String itemName) { return tokenizer.contains(itemName); }
		public String getDefaultGroup() { return tokenizer.defaultGroup; }
		public boolean hasAutoDiscard() { return tokenizer.autoDiscard; }
		public Tokenizer getImmutableTokenizer() { return this; }
		public Token parse(String string, Charset charset) { return tokenizer.parse(string, charset); }
		public Token parse(String string) { return tokenizer.parse(string); }
		public Token parse(byte[] bytes) { return tokenizer.parse(bytes); }
		public boolean equals(Object obj) { return Objects.equals(tokenizer, obj); }
		public int hashCode() { return tokenizer.hashCode(); }
		public String toString() { return tokenizer.toString(); }
	}
}
