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
 * <p>The default group name is </code>:null</code>
 * 
 * @author HardCoded
 */
public class LexerTokenizer implements Serializable {
	private static final long serialVersionUID = 9103878187629667166L;
	private static final String DEFAULT_GROUP = ":null";
	
	protected final Map<String, LexerGroup> groups;
	
	protected LexerTokenizer() {
		groups = new LinkedHashMap<>();
	}
	
	/**
	 * Add a new token group.
	 * @param	name	the name of the new group
	 * @return	a new {@code LexerGroup}
	 */
	public LexerGroup addGroup(String name) {
		return addGroup(name, false);
	}
	
	/**
	 * Add a new token group.
	 * @param	name	the name of the new group
	 * @param	discard	if {@code true} this token will be removed from the output when seen
	 * @return	a new {@code LexerGroup}
	 * @throws	IllegalArgumentException
	 * 			if the group was already present
	 */
	public LexerGroup addGroup(String name, boolean discard) {
		if(groups.containsKey(name))
			throw new IllegalArgumentException("A group with the name '" + name + "' has already been added to this tokenizer");
		
		LexerGroup group = new LexerGroup(name, discard);
		groups.put(name, group);
		return group;
	}
	
	/**
	 * Returns a immutable version of this tokenizer.
	 * @return a immutable version of this tokenizer
	 */
	public LexerTokenizer getImmutableTokenizer() {
		return new ImmutableTokenzier(this);
	}
	
	/**
	 * Returns {@code true} if this tokenizer is immutable.
	 * @return {@code ture} if this tokenizer is immutable
	 */
	public boolean isImmutable() {
		return false;
	}
	
	/**
	 * Parse a string into a list of tokens using the charset ISO_8859_1.
	 * 
	 * @param	string	the input string
	 * @return	a list of tokens
	 * @throws	NullPointerException
	 * 			if the string was {@code null}
	 */
	public List<Token> parse(String string) {
		return parse(string.getBytes(StandardCharsets.ISO_8859_1));
	}
	
	/**
	 * Parse a string into a list of tokens using the specified charset.
	 * 
	 * @param	string	the input string
	 * @return	a list of tokens
	 * @throws	NullPointerException
	 * 			if the string was {@code null}
	 */
	public List<Token> parse(String string, Charset charset) {
		return parse(string.getBytes(charset));
	}
	
	/**
	 * Parse a byte array into a list of tokens.
	 * 
	 * @param	bytes	a byte array
	 * @return	a list of tokens
	 */
	public List<Token> parse(byte[] bytes) {
		TokenizerString string = new TokenizerString(bytes);
		List<Token> list = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		boolean hasNull = false;
		
		int offset = 0;
		int line = 0;
		int column = 0;
		
		while(string.length() > 0) {
			Token token = nextToken(string);
			
			if(token == null) {
				// If we didn't read a token we should add a new "null token"
				if(!hasNull) {
					offset = string.getOffset();
					column = string.getColumn();
					line = string.getLine();
					hasNull = true;
				}
				
				sb.append(string.charAt(0));
				string.move(1);
				continue;
			}
			
			// If we have a null token and we found a non null token we should add the token.
			if(hasNull) {
				hasNull = false;
				
				list.add(new Token(sb.toString(), DEFAULT_GROUP, offset, line, column));
				sb.delete(0, sb.length());
			}
			
			// We should always discard tokens that are invalid.
			if(!token.isInvalid()) {
				list.add(token);
			}
		}
		
		if(hasNull) {
			list.add(new Token(sb.toString(), DEFAULT_GROUP, offset, line, column));
		}
		
		return list;
	}
	
	private Token nextToken(TokenizerString string) {
		LexerGroup g = null;
		int length = 0;
		
		// FIXME: We should never match zero lengthed patterns!
		for(LexerGroup group : groups.values()) {
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
			if(g.discard) {
				// Return an invalid token
				
				string.move(length);
				return new Token("", "", -1, -1, -1);
			}
			
			Token sym = new Token(
				string.subSequence(0, length).toString(),
				g.name,
				string.getOffset(),
				string.getLine(),
				string.getColumn()
			);
			
			string.move(length);
			return sym;
		}
		
		return null;
	}
	
	public static final class LexerGroup implements Serializable {
		private static final long serialVersionUID = 6252614685302203463L;
		private final List<Rule> rules;
		private final boolean discard;
		private final String name; 
		
		private LexerGroup(String name, boolean discard) {
			this.rules = new ArrayList<>();
			this.discard = discard;
			this.name = name;
		}
		
		public LexerGroup addString(String string) {
			rules.add(new Rule(string));
			return this;
		}
		
		public LexerGroup addStrings(String... strings) {
			for(String string : strings) {
				rules.add(new Rule(string));
			}
			return this;
		}
		
		public LexerGroup addRegex(String regex) {
			rules.add(new Rule(Pattern.compile(regex, Pattern.DOTALL)));
			return this;
		}
		
		public LexerGroup addRegexes(String... patterns) {
			for(String regex : patterns) {
				rules.add(new Rule(Pattern.compile(regex, Pattern.DOTALL)));
			}
			return this;
		}
		
		public LexerGroup addDelimiter(String open, String escape, String close) {
			rules.add(new Rule(open, escape, close));
			return this;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("LexerGroup{")
				.append("name=\"").append(name).append("\", ")
				.append("discard=").append(discard)
			.append("}");
			
			return sb.toString();
		}
	}
	
	private static class Rule implements Serializable {
		private static final long serialVersionUID = 1885877594264837458L;
		protected final Pattern pattern;
		protected final String string;
		
		private Rule(String open, String escape, String close) {
			String s = StringUtils.regexEscape(open);
			String c = StringUtils.regexEscape(close);
			
			String regex;
			if(escape.isEmpty()) {
				regex = s + ".*?" + c;
			} else {
				String e = StringUtils.regexEscape(escape);
				regex = s + "(?:" + e + "(?:" + e + "|" + c + "|(?!" + c + ").)|(?!" + e + "|" + c + ").)*" + c;
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
	}
	
	private static class ImmutableTokenzier extends LexerTokenizer {
		private static final long serialVersionUID = 5232114952149326460L;
		private ImmutableTokenzier(LexerTokenizer lexer) {
			for(String key : lexer.groups.keySet()) {
				LexerGroup old = lexer.groups.get(key);
				LexerGroup group = new LexerGroup(key, old.discard);
				groups.put(key, group);
				
				for(Rule r : old.rules) {
					group.rules.add(r.pattern == null ? new Rule(r.string):new Rule(r.pattern));
				}
			}
		}
		
		public LexerGroup addGroup(String name, boolean discard) { throw new UnsupportedOperationException("This tokenizer is not modifiable"); }
		public LexerGroup addGroup(String name) { throw new UnsupportedOperationException("This tokenizer is not modifiable"); }
		public LexerTokenizer getImmutableTokenizer() { return this; }
		public boolean isImmutable() { return true; }
	}
}